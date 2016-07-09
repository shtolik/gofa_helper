package mobi.stolicus.apps.gofa_helper.db;

import android.net.Uri;

/**
 * Class for keeping group of clips. Not finished.
 * Created by shtolik on 25.08.2015.
 */
public class Collection {
    public static final String TIMESTAMP_OLD_FIRST_SORT_ORDER = "timestamp DESC";
    public static final String DEFAULT_SORT_ORDER = TIMESTAMP_OLD_FIRST_SORT_ORDER;
    public static final String COLLECTION = "collection";
    public static final Uri COLLECTION_URI = Uri.parse("content://" + CupboardProvider.AUTHORITY + "/" + COLLECTION);

    String name;
    long timestamp;
    Long _id;
    String comment = "";
    int priority = 0;

    public Collection() {
    }

    public Collection(String text, long time) {
        this.name = text;
        this.timestamp = time;
        this.comment = "";
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
