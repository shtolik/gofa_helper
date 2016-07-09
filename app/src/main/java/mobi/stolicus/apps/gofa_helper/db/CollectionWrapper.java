package mobi.stolicus.apps.gofa_helper.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * * Wrapper class for keeping group of clips. Not finished.
 * Created by shtolik on 25.08.2015.
 */
public class CollectionWrapper {
    protected static final Logger logger = LoggerFactory.getLogger(CollectionWrapper.class);
    private static CollectionWrapper INSTANCE;
    protected Context mApplicationContext;
    protected CupboardHelper mOpenHelper = null;

    CollectionWrapper(Context context) {
        if (mApplicationContext == null || !mApplicationContext.equals(context)) {
            mApplicationContext = context.getApplicationContext();
        }
        mOpenHelper = new CupboardHelper(mApplicationContext);

    }

    public static CollectionWrapper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new CollectionWrapper(context);
        }
        return INSTANCE;
    }

    public int dbUpdate(ContentValues cv, String selection) {
        int updated = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            updated = cupboard().withDatabase(db).update(Collection.class, cv, selection);
            mApplicationContext.getContentResolver().notifyChange(Collection.COLLECTION_URI, null);
        } catch (Exception e) {
            logger.warn("/dbUpdate/" + e.getMessage() + ", with cv: " + cv.toString() + ", for selection:" + selection, e);
        }
        return updated;
    }

    public long dbInsert(ContentValues cv) {
        long id = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            id = cupboard().withDatabase(db).put(Collection.class, cv);
            mApplicationContext.getContentResolver().notifyChange(Collection.COLLECTION_URI, null);
        } catch (Exception e) {
            logger.warn("/dbInsert/" + e.getMessage() + ", with cv: " + cv.toString(), e);
        }
        return id;
    }

    public List<Collection> dbQueryList(String selection, String[] projection, String orderBy) {
        List<Collection> CollectionList = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            CollectionList = cupboard().withDatabase(db).query(Collection.class).withSelection(selection).
                    withProjection(projection).orderBy(orderBy).list();
        } catch (Exception e) {
            logger.warn("/dbQueryList/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return CollectionList;
    }

    public Cursor dbQueryCursor(String selection, String[] projection, String orderBy) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            cursor = cupboard().withDatabase(db).query(Collection.class).withSelection(selection).
                    withProjection(projection).orderBy(orderBy).getCursor();
        } catch (Exception e) {
            logger.warn("/dbQueryCursor/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return cursor;
    }

    public Collection dbQueryCollection(String selection, String orderBy) {
        Collection Collection = null;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            Collection = cupboard().withDatabase(db).query(Collection.class).withSelection(selection).orderBy(orderBy).get();
        } catch (Exception e) {
            logger.warn("/dbQueryCollection/" + e.getMessage() + ", for selection:" + selection + ", orderBy:" + orderBy, e);
        }
        return Collection;
    }

    public int dbDelete(String selection) {
        int deleted = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            deleted = cupboard().withDatabase(db).delete(Collection.class, selection);
        } catch (Exception e) {
            logger.warn("/dbDelete/" + e.getMessage() + ", for selection:" + selection, e);
        }
        return deleted;
    }

    public boolean dbDelete(Collection selection) {
        boolean deleted = false;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            deleted = cupboard().withDatabase(db).delete(selection);
            mApplicationContext.getContentResolver().notifyChange(Collection.COLLECTION_URI, null);
        } catch (Exception e) {
            logger.warn("/dbDeleteItem/" + e.getMessage() + ", for selection:" + selection, e);
        }
        return deleted;
    }


    public long addCollection(Collection Collection) {
        ContentValues values = cupboard().withEntity(Collection.class).toContentValues(Collection);
        return dbInsert(values);
    }

    public Collection getLastCollection() {
        String orderBy = Collection.TIMESTAMP_OLD_FIRST_SORT_ORDER;
        return dbQueryCollection(null, orderBy);
    }

    public int countCollections() {
        int ret = 0;
        Cursor cursor = null;

        try {
            String selection = parseSelection(-1, null, -1, null);
            cursor = dbQueryCursor(selection, null, null);
            ret = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            logger.error("/countCollections/" + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return ret;
    }

    private String parseSelection(long _id, String text, long timestamp, String searchWord) {
        StringBuilder selection = new StringBuilder();
        if (_id > 0) {
            selection.append("_id='").append(_id).append("'");
        } else {
            selection.append("1=1");
        }

        if (text != null && text.length() > 0) {
            selection.append(" AND ");
            selection.append("name='").append(text).append("'");
        }

        if (timestamp > 0) {
            selection.append(" AND ");
            selection.append("timestamp='").append(timestamp).append("'");
        }

        if (searchWord != null && searchWord.length() > 0) {
            selection.append(" AND ");
            selection.append("name" + " LIKE '%");
            selection.append(searchWord.toLowerCase().trim());
            selection.append("%'");

            selection.append("OR ");

            selection.append("comment" + " LIKE '%");
            selection.append(searchWord.toLowerCase().trim());
            selection.append("%'");

        }


        return selection.toString();
    }

    public long addReplaceCollection(Collection collection, boolean ignoreTimestamp) {
        String orderBy = Collection.TIMESTAMP_OLD_FIRST_SORT_ORDER;
        String selection = parseSelection(-1, collection.getName(), ignoreTimestamp ? -1 : collection.getTimestamp(), null);
        Collection existing = dbQueryCollection(selection, orderBy);
        long ret;
        if (existing != null) {
            collection.set_id(existing.get_id());
            ret = updateCollection(collection);
        } else {
            ret = addCollection(collection);
        }
        logger.info("/addReplaceCollection/Collections in history now:" + countCollections());
        return ret;
    }

    private int updateCollection(Collection Collection) {
        ContentValues values = cupboard().withEntity(Collection.class).toContentValues(Collection);
        String selection = parseSelection(Collection.get_id(), Collection.getName(), 0, null);
        return dbUpdate(values, selection);
    }

    public List<Collection> getCollectionsList() {
        return dbQueryList(null, null, Collection.DEFAULT_SORT_ORDER);
    }

    public Cursor getCollectionsCursor() {
        return dbQueryCursor(null, null, Collection.DEFAULT_SORT_ORDER);
    }

    public Collection getCollectionById(long id) {
        String selection = parseSelection(id, null, -1, null);

        return dbQueryCollection(selection, null);
    }

    public CursorLoader getCollectionsCursorLoader(String searchWord) {
        String selection = parseSelection(-1, null, -1, searchWord);
        return new CursorLoader(mApplicationContext, Collection.COLLECTION_URI,
                null, selection, null,
                Collection.DEFAULT_SORT_ORDER);
    }
}
