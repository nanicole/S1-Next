package cl.monsoon.s1next.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cl.monsoon.s1next.model.list.PostList;

public class ThreadAttachmentInfoListArrayAdapter extends ArrayAdapter<PostList.ThreadAttachment.Info> {

    @LayoutRes
    private final int mResource;

    public ThreadAttachmentInfoListArrayAdapter(Context context, @LayoutRes int resource, List<PostList.ThreadAttachment.Info> objects) {
        super(context, resource, objects);

        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.mLabelView = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.mValueView = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PostList.ThreadAttachment.Info info = getItem(position);

        viewHolder.mLabelView.setText(info.getLabel());
        viewHolder.mValueView.setText(info.getValue());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder {

        private TextView mLabelView;
        private TextView mValueView;
    }
}
