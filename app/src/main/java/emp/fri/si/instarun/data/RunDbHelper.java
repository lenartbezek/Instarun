package emp.fri.si.instarun.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import emp.fri.si.instarun.model.Run;
import emp.fri.si.instarun.util.IsoDateHelper;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class RunDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Instarun.db";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE Run (" +
            "_id INTEGER PRIMARY KEY, " +
            "globalId TEXT, " +
            "ownerId TEXT, "+
            "title TEXT, "+
            "length REAL, " +
            "steps REAL, " +
            "trackFile TEXT, " +
            "startIsoTime TEXT, " +
            "endIsoTime TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS Run";


    public RunDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Saves new Run or updates an existing one.
     * @param run
     */
    public void save(Run run){
        if (run.isSaved)
            update(run);
        else
            insert(run);

    }

    /**
     * Saves Run to local SQLite database. Returns the same run with it's id field set.
     * @param run model. Run to be saved.
     * @return
     */
    public Run insert(Run run){
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("globalId", run.globalId);
        values.put("ownerId", run.ownerId);
        values.put("title", run.title);
        values.put("length", run.length);
        values.put("steps", run.steps);
        values.put("trackFile", run.trackFile);
        values.put("startIsoTime", IsoDateHelper.dateToIsoString(run.startTime));
        values.put("endIsoTime", IsoDateHelper.dateToIsoString(run.endTime));

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Run", null, values);
        run.id = newRowId;
        run.isSaved = true;
        return run;
    }

    /**
     * Updates existing Run. Will not work if the Run isn't saved locally yet.
     * Check Run.isSaved before calling.
     * @param run Run to be updated.
     * @return
     */
    public boolean update(Run run) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("globalId", run.globalId);
        values.put("ownerId", run.ownerId);
        values.put("title", run.title);
        values.put("length", run.length);
        values.put("steps", run.steps);
        values.put("trackFile", run.trackFile);
        values.put("startIsoTime", IsoDateHelper.dateToIsoString(run.startTime));
        values.put("endIsoTime", IsoDateHelper.dateToIsoString(run.endTime));

        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(run.id) };

        return db.update("Run", values, selection, selectionArgs) > 0;
    }

    /**
     * Deletes existing Run. Returns true if successful.
     * @param run Run to be deleted.
     * @return
     */
    public boolean delete(Run run){
        SQLiteDatabase db = getWritableDatabase();

        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(run.id) };
        boolean success = db.delete("Run", selection, selectionArgs) > 0;
        if (success) run.isSaved = false;
        return success;
    }

    /**
     * Returns a locally stored Run by id.
     * @param id
     * @return
     */
    public Run read(long id){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                "_id",
                "globalId",
                "ownerId",
                "title",
                "length",
                "steps",
                "trackFile",
                "startIsoTime",
                "endIsoTime"
        };

        String selection = "_id = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                "Run",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Run run = new Run();
        if (cursor.moveToNext()){
            try
            {
                run.id = cursor.getLong(cursor.getColumnIndex("_id"));
                run.globalId = cursor.getString(cursor.getColumnIndex("globalId"));
                run.ownerId = cursor.getString(cursor.getColumnIndex("ownerId"));
                run.title = cursor.getString(cursor.getColumnIndex("title"));
                run.length = cursor.getFloat(cursor.getColumnIndex("length"));
                run.steps = cursor.getInt(cursor.getColumnIndex("steps"));
                run.trackFile = cursor.getString(cursor.getColumnIndex("trackFile"));
                run.startTime = IsoDateHelper.isoStringToDate(cursor.getString(cursor.getColumnIndex("startIsoTime")));
                run.endTime = IsoDateHelper.isoStringToDate(cursor.getString(cursor.getColumnIndex("endIsoTime")));
                run.isSaved = true;
            } catch (ParseException e) {
                return null;
            } finally{
                cursor.close();
            }
        }

        return run;
    }

    /**
     * Returns all locally stored Runs.
     * @return
     */
    public List<Run> read(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                "_id",
                "globalId",
                "ownerId",
                "title",
                "length",
                "steps",
                "trackFile",
                "startIsoTime",
                "endIsoTime"
        };

        Cursor cursor = db.query(
                "Run",
                projection,
                null,
                null,
                null,
                null,
                null
        );

        LinkedList<Run> list = new LinkedList<>();
        while (cursor.moveToNext()){
            try
            {
                Run run = new Run();
                run.id = cursor.getLong(cursor.getColumnIndex("_id"));
                run.globalId = cursor.getString(cursor.getColumnIndex("globalId"));
                run.ownerId = cursor.getString(cursor.getColumnIndex("ownerId"));
                run.title = cursor.getString(cursor.getColumnIndex("title"));
                run.length = cursor.getFloat(cursor.getColumnIndex("length"));
                run.steps = cursor.getInt(cursor.getColumnIndex("steps"));
                run.trackFile = cursor.getString(cursor.getColumnIndex("trackFile"));
                run.startTime = IsoDateHelper.isoStringToDate(cursor.getString(cursor.getColumnIndex("startIsoTime")));
                run.endTime = IsoDateHelper.isoStringToDate(cursor.getString(cursor.getColumnIndex("endIsoTime")));
                run.isSaved = true;
                list.add(run);
            } catch (ParseException e) {
                continue;
            }
        }

        cursor.close();
        return list;
    }
}