package vn.com.goldtek.facenotify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Nikunj on 27-08-2015.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notification.db";
    public static final String TABLE_NAME = "NOTIFICATION";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_TIME = "TIME";
    public static final String COLUMN_GROUP = "PERSON_GROUP";
    public static final String COLUMN_IMAGE_URL = "IMAGE_URL";

    private static final int DATABASE_VERSION = 1;
    public static File directory;
    private SQLiteDatabase database;

    public SQLiteHelper(Context context1) {
        super(context1, DATABASE_NAME, null, DATABASE_VERSION);
        final File dbfile = new File(context1.getFilesDir().getParent() + "/databases/" + DATABASE_NAME);
        Log.d("fejij", " " + dbfile.getAbsolutePath());
        copyDatabase(context1, DATABASE_NAME);
    }

    public static void copyDatabase(Context c, String DATABASE_NAME) {
        String databasePath = c.getDatabasePath(DATABASE_NAME).getPath();
        File f = new File(databasePath);
        Log.d("vk", "" + databasePath);
        OutputStream myOutput = null;
        InputStream myInput = null;
        Log.d("testing", " testing db path " + databasePath);
        Log.d("testing", " testing db exist " + f.exists());

        if (f.exists()) {
            try {

                directory = new File("/mnt/sdcard/DB_DEBUG");
                if (!directory.exists())
                    directory.mkdir();

                myOutput = new FileOutputStream(directory.getAbsolutePath()
                        + "/" + DATABASE_NAME);
                myInput = new FileInputStream(databasePath);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }

                myOutput.flush();
            } catch (Exception e) {
            } finally {
                try {
                    if (myOutput != null) {
                        myOutput.close();
                        myOutput = null;
                    }
                    if (myInput != null) {
                        myInput.close();
                        myInput = null;
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_NOTIFICATION_TABLE = " CREATE TABLE " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " VARCHAR, " + COLUMN_TIME + " VARCHAR," + COLUMN_GROUP + " VARCHAR," + COLUMN_IMAGE_URL + " VARCHAR );";
        db.execSQL(CREATE_NOTIFICATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertRecord(NotificationModel notification) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, notification.getName());
        contentValues.put(COLUMN_TIME, notification.getTime());
        contentValues.put(COLUMN_GROUP, notification.getGroup());
        contentValues.put(COLUMN_IMAGE_URL, notification.getImageUrl());
        database.insert(TABLE_NAME, null, contentValues);
        database.close();
    }

    public ArrayList<NotificationModel> getAllRecords() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<NotificationModel> notification = new ArrayList<NotificationModel>();
        NotificationModel notificationModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                notificationModel = new NotificationModel();
                notificationModel.setID(cursor.getString(0));
                notificationModel.setName(cursor.getString(1));
                notificationModel.setTime(cursor.getString(2));
                notificationModel.setGroup(cursor.getString(3));
                notificationModel.setImageUrl(cursor.getString(4));

                notification.add(notificationModel);
            }
        }
        Log.d("tag5", "d" + notification);
        cursor.close();
        database.close();

        return notification;
    }

    public void deleteAllRecords() {
        database = this.getReadableDatabase();
        database.delete(TABLE_NAME, null, null);
        database.close();
    }
}
