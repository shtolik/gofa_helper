package mobi.stolicus.apps.gofa_helper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * For initiation of cupboard sqlite ORM
 * Created by shtolik on 25.08.2015.
 */
public class CupboardHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clip.db";
    private static final int DATABASE_VERSION = 3;

    static {
        // register our models
        cupboard().register(Clip.class);
        cupboard().register(Collection.class);
    }

    public CupboardHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
        // add indexes and other database tweaks
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}
