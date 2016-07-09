package mobi.stolicus.apps.gofa_helper.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Class for handling getting and putting clips into database
 * Created by shtolik on 25.08.2015.
 */
public class ClipWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ClipWrapper.class);
    private static ClipWrapper instance;
    private Context mApplicationContext;
    private CupboardHelper mOpenHelper = null;

    private ClipWrapper(Context context) {
        if (mApplicationContext == null || !mApplicationContext.equals(context)) {
            mApplicationContext = context.getApplicationContext();
        }
        mOpenHelper = new CupboardHelper(mApplicationContext);

    }

    public static ClipWrapper getInstance(Context context) {
        if (instance == null) {
            instance = new ClipWrapper(context);
        }
        return instance;
    }

    private int dbUpdate(ContentValues cv, String selection) {
        int updated = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            updated = cupboard().withDatabase(db).update(Clip.class, cv, selection);
            mApplicationContext.getContentResolver().notifyChange(Clip.CLIP_URI, null);
        } catch (Exception e) {
            logger.warn("/dbUpdate/" + e.getMessage() + ", with cv: " + cv.toString() + ", for selection:" + selection, e);
        }
        return updated;
    }

    private long dbInsert(ContentValues cv) {
        long id = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            id = cupboard().withDatabase(db).put(Clip.class, cv);
            mApplicationContext.getContentResolver().notifyChange(Clip.CLIP_URI, null);
        } catch (Exception e) {
            logger.warn("/dbInsert/" + e.getMessage() + ", with cv: " + cv.toString(), e);
        }
        return id;
    }

    public List<Clip> dbQueryList(String selection, String[] projection, String orderBy) {
        List<Clip> clipList = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            clipList = cupboard().withDatabase(db).query(Clip.class).withSelection(selection).
                    withProjection(projection).orderBy(orderBy).list();
        } catch (Exception e) {
            logger.warn("/dbQueryList/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return clipList;
    }

    private Cursor dbQueryCursor(String selection, String[] projection, String orderBy) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            cursor = cupboard().withDatabase(db).query(Clip.class).withSelection(selection).
                    withProjection(projection).orderBy(orderBy).getCursor();
            cursor.setNotificationUri(mApplicationContext.getContentResolver(), Clip.CLIP_URI);
        } catch (Exception e) {
            logger.warn("/dbQueryCursor/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return cursor;
    }

    private Clip dbQueryClip(String selection, String orderBy) {
        Clip clip = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            clip = cupboard().withDatabase(db).query(Clip.class).withSelection(selection).orderBy(orderBy).get();
        } catch (Exception e) {
            logger.warn("/dbQueryClip/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return clip;
    }

    private int dbDeleteItem(String selection) {
        int deleted = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            deleted = cupboard().withDatabase(db).delete(Clip.class, selection);
            mApplicationContext.getContentResolver().notifyChange(Clip.CLIP_URI, null);
        } catch (Exception e) {
            logger.warn("/dbDelete/" + e.getMessage() + ", for selection:" + selection, e);
        }
        return deleted;
    }

    public boolean dbDeleteItem(Clip clip) {
        boolean deleted = false;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            deleted = cupboard().withDatabase(db).delete(clip);
            mApplicationContext.getContentResolver().notifyChange(Clip.CLIP_URI, null);
        } catch (Exception e) {
            logger.warn("/dbDeleteItem/" + e.getMessage() + ", for selection:" + clip, e);
        }
        return deleted;
    }


    public long addClip(Clip clip) {
        ContentValues values = cupboard().withEntity(Clip.class).toContentValues(clip);
        return dbInsert(values);
    }

    public Clip getLastClip() {
        String orderBy = Clip.SORT_ORDER_TIMESTAMP_OLD_FIRST;
        return dbQueryClip(null, orderBy);
    }

    public int countClips() {
        int ret = 0;
        Cursor cursor = null;

        try {
            String selection = parseSelection(-1, null, -1, null, null);
            cursor = dbQueryCursor(selection, null, null);
            ret = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            logger.error("/countClips/" + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return ret;
    }

    public String parseSelection(long _id, String text, long timestamp, String searchWord, long[] checkedClips) {
        StringBuilder selection = new StringBuilder();
        if (_id > 0) {
            selection.append("_id='").append(_id).append("'");
        } else {
            selection.append("1=1");
        }

        if (text != null && text.length() > 0) {
            selection.append(" AND ");
            selection.append("text='").append(text).append("'");
        }

        if (timestamp > 0) {
            selection.append(" AND ");
            selection.append("timestamp='").append(timestamp).append("'");
        }

        if (searchWord != null && searchWord.length() > 0) {
            String stripped = DatabaseUtils.sqlEscapeString("%" + searchWord.toLowerCase().trim() + "%");
            selection.append(" AND ");
            selection.append("text" + " LIKE ");
            selection.append(stripped);

            selection.append(" OR ");

            selection.append("comment" + " LIKE ");
            selection.append(stripped);

        }

        if (checkedClips!=null && checkedClips.length>0){
            selection.append(" AND (");
            for (int i = 0; i<checkedClips.length; i++) {
                if (i != 0)
                    selection.append(" OR ");
                selection.append("_id=");
                selection.append(checkedClips[i]);
            }
            selection.append(" )");
        }

        return selection.toString();
    }

    public long addReplaceClip(Clip clip, boolean ignoreTimestamp) {
        String orderBy = Clip.SORT_ORDER_TIMESTAMP_OLD_FIRST;
        String selection = parseSelection(-1, clip.getText(), ignoreTimestamp ? -1 : clip.getTimestamp(), null, null);
        Clip existing = dbQueryClip(selection, orderBy);
        long ret;
        if (existing != null) {
            clip.set_id(existing.get_id());
            ret = updateClip(clip);
        } else {
            ret = addClip(clip);
        }
        logger.info("/addReplaceClip/clips in history now:" + countClips());
        return ret;
    }

    private int updateClip(Clip clip) {
        ContentValues values = cupboard().withEntity(Clip.class).toContentValues(clip);
        String selection = parseSelection(clip.get_id(), clip.getText(), 0, null, null);
        return dbUpdate(values, selection);
    }

    public List<Clip> getClipsList() {
        return dbQueryList(null, null, Clip.DEFAULT_SORT_ORDER);
    }

    public Clip getClipById(long id) {
        String selection = parseSelection(id, null, -1, null, null);

        return dbQueryClip(selection, null);
    }

    public CursorLoader getClipsCursorLoader(String searchWord) {
        String selection = parseSelection(-1, null, -1, searchWord, null);
        return new CursorLoader(mApplicationContext, Clip.CLIP_URI,
                null, selection, null,
                Clip.DEFAULT_SORT_ORDER);
    }

    public void dbDeleteItems(long[] checkedClips) {
        String selection = parseSelection(-1, null, -1, null, checkedClips);
        int del = dbDeleteItem(selection);
        logger.info("/dbDeleteItems/deleted " + del + " clips");
    }
}
