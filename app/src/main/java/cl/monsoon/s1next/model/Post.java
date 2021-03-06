package cl.monsoon.s1next.model;

import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedDeclaration")
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Post {

    @JsonProperty("pid")
    private String id;

    @JsonProperty("author")
    private String username;

    @JsonProperty("authorid")
    private String userId;

    @JsonProperty("message")
    private String reply;

    @JsonProperty("number")
    private String count;

    @JsonProperty("dbdateline")
    private long time;

    @JsonProperty("attachments")
    private Map<String, Attachment> attachmentMap;

    public Post() {

    }

    private Post(String id, String count) {
        this.id = id;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        // Map colors (see mapColors(String))
        // and replace "imgwidth" with "img width",
        // because some img tags in S1 aren't correct.
        // This may be the best way to deal with it though
        // we may replace something wrong by accident.
        this.reply = mapColors(reply).replaceAll("<imgwidth=\"", "<img width=\"");
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public long getTime() {
        return time;
    }

    /**
     * Convert seconds to milliseconds.
     */
    public void setTime(long time) {
        this.time = TimeUnit.SECONDS.toMillis(time);
    }

    public Map<String, Attachment> getAttachmentMap() {
        return attachmentMap;
    }

    public Post getPartForQuote() {
        return new Post(id, count);
    }

    /**
     * {@link Color} doesn't support all HTML color names.
     * So {@link android.text.Html#fromHtml(String)} won't
     * map some color names for replies in S1.
     * We need to map these color name to its hex value by ourselves.
     */
    private static String mapColors(CharSequence reply) {
        // sample: color="sienna"
        // matcher.group(0): color="sienna"
        // matcher.group(1): sienna
        Pattern pattern = Pattern.compile("color=\"([a-zA-Z]+)\"");
        Matcher matcher = pattern.matcher(reply);

        StringBuffer stringBuffer = new StringBuffer();
        String color;
        while (matcher.find()) {
            // get color hex value for its color name
            color = COLOR_NAME_MAP.get(matcher.group(1).toLowerCase());
            if (color == null) {
                // throw new IllegalStateException("COLOR_NAME_MAP" + "must contain " + matcher.group(0));
                continue;
            }
            // append part of the string and plus its color hex value
            matcher.appendReplacement(stringBuffer, "color=\"" + color + "\"");
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    private static final Map<String, String> COLOR_NAME_MAP;

    static {
        COLOR_NAME_MAP = new HashMap<>();

        COLOR_NAME_MAP.put("sienna", "#A0522D");
        COLOR_NAME_MAP.put("darkolivegreen", "#556B2F");
        COLOR_NAME_MAP.put("darkgreen", "#006400");
        COLOR_NAME_MAP.put("darkslateblue", "#483D8B");
        COLOR_NAME_MAP.put("indigo", "#4B0082");
        COLOR_NAME_MAP.put("darkslategray", "#2F4F4F");
        COLOR_NAME_MAP.put("darkred", "#8B0000");
        COLOR_NAME_MAP.put("darkorange", "#FF8C00");
        COLOR_NAME_MAP.put("slategray", "#708090");
        COLOR_NAME_MAP.put("dimgray", "#696969");
        COLOR_NAME_MAP.put("sandybrown", "#F4A460");
        COLOR_NAME_MAP.put("yellowgreen", "#9ACD32");
        COLOR_NAME_MAP.put("seagreen", "#2E8B57");
        COLOR_NAME_MAP.put("mediumturquoise", "#48D1CC");
        COLOR_NAME_MAP.put("royalblue", "#4169E1");
        COLOR_NAME_MAP.put("orange", "#FFA500");
        COLOR_NAME_MAP.put("deepskyblue", "#00BFFF");
        COLOR_NAME_MAP.put("darkorchid", "#9932CC");
        COLOR_NAME_MAP.put("pink", "#FFC0CB");
        COLOR_NAME_MAP.put("wheat", "#F5DEB3");
        COLOR_NAME_MAP.put("lemonchiffon", "#FFFACD");
        COLOR_NAME_MAP.put("palegreen", "#98FB98");
        COLOR_NAME_MAP.put("paleturquoise", "#AFEEEE");
        COLOR_NAME_MAP.put("lightblue", "#ADD8E6");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostListInfo {

        @JsonProperty("replies")
        private int replies;

        public int getReplies() {
            return replies;
        }

        public void setReplies(int replies) {
            this.replies = replies;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {

        @JsonIgnore
        private final String url;

        @JsonCreator
        public Attachment(
                @JsonProperty("url") String urlPrefix,
                @JsonProperty("attachment") String urlSuffix) {
            this.url = urlPrefix + urlSuffix;
        }

        public String getUrl() {
            return url;
        }
    }
}
