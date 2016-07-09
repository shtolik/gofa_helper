package mobi.stolicus.apps.gofa_helper.db;

import android.net.Uri;

import mobi.stolicus.app.gofa_helper.R;

/**
 * Class for keeping single clip with gofa link
 * Created by shtolik on 25.08.2015.
 */
public class Clip {
    public static final String TAG = Clip.class.getSimpleName();
    public static final String SORT_ORDER_TIMESTAMP_OLD_FIRST = "timestamp DESC";
    public static final String SORT_ORDER_COLLECTION_ID_LOW_FIRST = "collection_id DESC";
    public static final String DEFAULT_SORT_ORDER = SORT_ORDER_TIMESTAMP_OLD_FIRST;
    public static final String CLIP = "clip";
    public static final Uri CLIP_URI = Uri.parse("content://" + CupboardProvider.AUTHORITY + "/" + CLIP);

    //class fields are used as sqlite column names by cupboard. better don't change
    private String text;
    private long timestamp;
    private Long _id;
    private String comment = "";
    private long collection_id = -1;
//    boolean favourite = false;

    public Clip() {

    }

    public Clip(String text, long time) {
        this.text = text;
        this.timestamp = time;
        this.comment = "";
    }

    public static int getResIconFromClipText(String text) {
        if (text.contains("planets")) {
            return R.drawable.geography6_;
        } else if (text.contains("alliances")) {
            return R.drawable.users30_;
        } else if (text.contains("players")) {
            return R.drawable.science28_;
        } else {
            return R.drawable.paperclip1_;
        }
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCollection_id() {
        return collection_id;
    }

    public void setCollection_id(long collection_id) {
        this.collection_id = collection_id;
    }

}
