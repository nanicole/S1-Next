package cl.monsoon.s1next.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.signature.StringSignature;

import java.util.Map;

import cl.monsoon.s1next.Api;
import cl.monsoon.s1next.R;
import cl.monsoon.s1next.activity.PostListActivity;
import cl.monsoon.s1next.activity.ReplyActivity;
import cl.monsoon.s1next.model.Post;
import cl.monsoon.s1next.singleton.Config;
import cl.monsoon.s1next.util.DateUtil;
import cl.monsoon.s1next.util.ObjectUtil;
import cl.monsoon.s1next.util.TextViewHelper;
import cl.monsoon.s1next.widget.GlideImageGetter;
import cl.monsoon.s1next.widget.MyMovementMethod;
import cl.monsoon.s1next.widget.MyTagHandler;

public final class PostListRecyclerAdapter extends RecyclerAdapter<Post, RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER_PROGRESS = Integer.MIN_VALUE;

    private boolean mHasFooterProgress;

    private final Context mContext;

    private final DrawableRequestBuilder<String> mAvatarRequestBuilder;
    private final DrawableRequestBuilder<String> mImageGetterRequestBuilder;

    public PostListRecyclerAdapter(Context context) {
        this.mContext = context;

        setHasStableIds(true);

        // Lading avatars is prior to images in replies
        mAvatarRequestBuilder =
                Glide.with(mContext)
                        .from(String.class)
                        .signature(new StringSignature(DateUtil.getWeekWithYear()))
                        .error(R.drawable.ic_avatar_placeholder)
                        .priority(Priority.HIGH)
                        .transform(new CenterCrop(Glide.get(context).getBitmapPool()));

        // used in GlideImageGetter
        mImageGetterRequestBuilder =
                Glide.with(mContext)
                        .from(String.class);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER_PROGRESS) {
            View view =
                    LayoutInflater.from(
                            parent.getContext())
                            .inflate(R.layout.fragment_post_list_footer_progress, parent, false);

            return new FooterProgressViewHolder(view);
        }

        View view =
                LayoutInflater.from(
                        parent.getContext())
                        .inflate(R.layout.fragment_post_list, parent, false);

        if(Config.isS1Theme()){
            CardView cardView = ((CardView) view.findViewById(R.id.cardview));
            cardView .setCardElevation(0);
            cardView.setRadius(0);
            cardView.setMaxCardElevation(0);

        }

        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterProgress(position)) {
            return TYPE_FOOTER_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isFooterProgress(position)) {
            return;
        }

        Post post = mList.get(position);

        ItemViewHolder itemViewHolder = ObjectUtil.cast(holder, ItemViewHolder.class);
        ImageView avatarView = itemViewHolder.mAvatar;

        // whether need download avatars depends on settings and Wi-Fi status
        final boolean avatarsDownload = Config.isAvatarsDownload();
        if (avatarsDownload) {
            avatarView.setVisibility(View.VISIBLE);

            // show user's avatar
            mAvatarRequestBuilder
                    .load(Api.getUrlAvatarSmall(post.getUserId()))
                    .into(avatarView);
        } else {
            avatarView.setVisibility(View.GONE);
        }

        itemViewHolder.mUsername.setText(post.getUsername());
        itemViewHolder.mTime.setText(
                DateUtils.getRelativeDateTimeString(
                        mContext,
                        post.getTime(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.DAY_IN_MILLIS,
                        0));

        TextView countView = itemViewHolder.mCount;
        // there is no need to quote #1
        if ("1".equals(post.getCount())) {
            countView.setText("#1");
            countView.setOnTouchListener(null);
        } else {
            Spannable spannable = new SpannableString("#" + post.getCount());
            spannable.setSpan(
                    MY_CLICKABLE_SPAN, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            countView.setText(spannable);
            countView.setTag(post.getPartForQuote());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                countView.setOnTouchListener(MY_TOUCH_LISTENER);
            }
        }

        String reply = post.getReply();
        TextView replayView = itemViewHolder.mPost;
        // some replies are empty
        // like http://bbs.saraba1st.com/2b/thread-1008413-1-1.html#authorposton24836448
        if (TextUtils.isEmpty(reply)) {
            replayView.setText(null);

            return;
        }

        String url;
        Map<String, Post.Attachment> attachmentMap = post.getAttachmentMap();
        if (attachmentMap != null) {
            for (Map.Entry<String, Post.Attachment> entry : attachmentMap.entrySet()) {
                Post.Attachment attachment = entry.getValue();
                url = attachment.getUrl();

                // Replace attach tag with HTML img tag
                // in order to display attachment images in TextView.
                reply = reply.replace(
                        "[attach]" + entry.getKey() + "[/attach]", "<img src=\"" + url + "\" />");
            }
        }

        // use GlideImageGetter to show images
        replayView.setText(
                Html.fromHtml(
                        reply,
                        new GlideImageGetter(replayView, mImageGetterRequestBuilder),
                        new MyTagHandler(mContext)));
    }

    @Override
    public long getItemId(int position) {
        if (isFooterProgress(position)) {
            return Integer.MIN_VALUE;
        }

        return Long.parseLong(mList.get(position).getCount());
    }

    private boolean isFooterProgress(int position) {
        return mHasFooterProgress && position == getItemCount() - 1;
    }

    public void showFooterProgress() {
        if (mHasFooterProgress) {
            return;
        }

        mHasFooterProgress = true;
        int position = getItemCount() - 1;
        // mList.get(position) = null
        // when configuration changes (like orientation changes)
        if (mList.get(position) != null) {
            mList.add(null);
            notifyItemInserted(position + 1);
        }
    }

    public void hideFooterProgress() {
        if (!mHasFooterProgress) {
            return;
        }

        int position = getItemCount() - 1;
        mList.remove(position);
        mHasFooterProgress = false;
        notifyItemRemoved(position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mAvatar;
        private final TextView mUsername;
        private final TextView mTime;
        private final TextView mCount;
        private final TextView mPost;

        public ItemViewHolder(View itemView) {
            super(itemView);

            mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mTime = (TextView) itemView.findViewById(R.id.time);
            mCount = (TextView) itemView.findViewById(R.id.count);
            mPost = (TextView) itemView.findViewById(R.id.post);

            TextViewHelper.updateTextSize(new TextView[]{mUsername, mTime, mCount, mPost});
            TextViewHelper.updateTextColorWhenS1Theme(new TextView[]{mUsername, mPost});

            mCount.setMovementMethod(LinkMovementMethod.getInstance());
            // use custom movement method to provides selection and click
            mPost.setMovementMethod(MyMovementMethod.getInstance());

            // use TouchDelegate to increase mCount's click area
            mCount.post(() -> {
                int halfMinimumTouchTargetSize =
                        mCount.getContext()
                                .getResources()
                                .getDimensionPixelSize(
                                        R.dimen.minimum_touch_target_size) / 2;
                Rect rect = new Rect();
                mCount.getHitRect(rect);
                rect.top -= halfMinimumTouchTargetSize;
                rect.right += halfMinimumTouchTargetSize;
                rect.bottom += halfMinimumTouchTargetSize;
                rect.left -= halfMinimumTouchTargetSize;
                itemView.setTouchDelegate(new TouchDelegate(rect, mCount));
            });
        }
    }

    private static final ClickableSpan MY_CLICKABLE_SPAN = new ClickableSpan() {

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(PostListActivity.ACTION_QUOTE);

            Post post = ObjectUtil.cast(widget.getTag(), Post.class);
            intent.putExtra(ReplyActivity.ARG_QUOTE_POST_ID, post.getId());
            intent.putExtra(ReplyActivity.ARG_QUOTE_POST_COUNT, post.getCount());

            widget.getContext().sendBroadcast(intent);
        }
    };

    @SuppressLint("NewApi")
    private static final View.OnTouchListener MY_TOUCH_LISTENER = (v, event) -> {

        TextView textView = (TextView) v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                textView.setBackgroundColor(textView.getHighlightColor());

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                textView.setBackgroundColor(Color.TRANSPARENT);

                break;
        }

        return false;
    };

    public static class FooterProgressViewHolder extends RecyclerView.ViewHolder {

        public FooterProgressViewHolder(View itemView) {
            super(itemView);
        }
    }
}
