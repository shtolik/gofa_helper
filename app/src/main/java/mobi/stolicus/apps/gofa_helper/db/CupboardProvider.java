package mobi.stolicus.apps.gofa_helper.db;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * ContentProvider getting data from cupboard database
 * Created by shtolik on 31.08.2015.
 */

public class CupboardProvider extends ContentProvider {
    private static final String TAG = CupboardProvider.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final int CLIP = 0;
    private static final int CLIPS = 1;
    public static final String AUTHORITY = "mobi.stolicus.apps.gofa_helper.cupboard_provider";
    private static UriMatcher mMatcher = null;
    private static String mContentProviderAuth;
    private CupboardHelper mDatabaseHelper;

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        if (mMatcher == null) {
            mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            mContentProviderAuth = AUTHORITY;
            mMatcher.addURI(mContentProviderAuth, Clip.CLIP + "/#", CLIP);
            mMatcher.addURI(mContentProviderAuth, Clip.CLIP, CLIPS);
        }

        mDatabaseHelper = new CupboardHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        synchronized (LOCK) {
            SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
            Cursor cursor = null;
            switch (mMatcher.match(uri)) {
                case CLIP:
                case CLIPS:
                    // this is the full query syntax, most of the time you can leave out projection etc
                    // if the content provider returns a fixed set of data
                    cursor = cupboard().withDatabase(db).query(Clip.class).
                            withProjection(projection).
                            withSelection(selection, selectionArgs).
                            orderBy(sortOrder).
                            getCursor();
                    if (getContext()!=null)
                        cursor.setNotificationUri(getContext().getContentResolver(), uri);
                    break;
            }
            return cursor;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        synchronized (LOCK) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            Class clz;
            int count;
            switch (mMatcher.match(uri)) {
                case CLIP:
                case CLIPS:
                    clz = Account.class;
                    count = cupboard().withDatabase(db).update(clz, values, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            if (count > 0 && getContext()!=null)
                getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        synchronized (LOCK) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            Class clz;
            long id = Long.getLong(uri.getLastPathSegment(), 0);
            Uri uriRet;
            switch (mMatcher.match(uri)) {
                case CLIP:
                case CLIPS:
                    clz = Account.class;
                    if (id == 0) {
                        id = cupboard().withDatabase(db).put(clz, values);
                    } else {
                        id = cupboard().withDatabase(db).update(clz, values);
                    }
                    uriRet = Uri.parse(mContentProviderAuth + "/" + Clip.CLIP + "/" + id);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            if (getContext()!=null)
                getContext().getContentResolver().notifyChange(uri, null);
            return uriRet;
        }
    }


    public int bulkInsert(String table, String rowIdColumn, Uri uri, ContentValues values[]) {

        int count = 0;

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        try {

            db.beginTransaction();

            for (ContentValues value : values) {

                long rowId = db.insert(table, rowIdColumn, value);

                if (rowId >= 0) {
                    count++;
                } else {
                    throw new SQLException(TAG + "/bulkInsert/Failed to insert row into table" + table + "; value = " + value + ", some column uninitialized? ");
                }
            }

            db.setTransactionSuccessful();
            if (getContext()!=null)
                getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            count = -1;
        } finally {
            db.endTransaction();
        }

        return count;

    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        synchronized (LOCK) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            int count;
            switch (mMatcher.match(uri)) {
                case CLIP:
                case CLIPS:
                    count = cupboard().withDatabase(db).delete(Clip.class, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
            if (getContext()!=null)
                getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
    }
}