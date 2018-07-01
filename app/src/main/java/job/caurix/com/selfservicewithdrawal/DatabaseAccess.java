package job.caurix.com.selfservicewithdrawal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

//The database class
public class DatabaseAccess {
    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    //First constructor
    public DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    //Second constructor
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    //This populates the mainActivity
    public List<Response> getResponses() {
        List<Response> responseList = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM offlineRecords", null);
        cursor.moveToFirst();

        while (!(cursor.isAfterLast())) {

            responseList.add(new Response(cursor.getString(cursor.getColumnIndex("phone_number")),
                    cursor.getString(cursor.getColumnIndex("amount")),
                    cursor.getString(cursor.getColumnIndex("confirmation_id"))));

            cursor.moveToNext();
        }
        cursor.close();

        return responseList;
    }


    //This is for adding a new value to the favorites activity
    public boolean addOfflineRecord(String phone_number, String amount, String confirmation_id) {
        try {
            //Adding a record to the offline table
            ContentValues cv = new ContentValues();
            cv.put("phone_number", phone_number);
            cv.put("amount", amount);
            cv.put("confirmation_id", confirmation_id);

            database.insert("offlineRecords", null, cv);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean storeAmount(String amount) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("amount", amount);
            database.update("selfWithdrawal", cv, null, null);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean storePhoneNumber(String phoneNumber){
        try {
            ContentValues cv = new ContentValues();
            cv.put("phone_number", phoneNumber);
            database.update("selfWithdrawal", cv, null, null);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean storeConfirmationId(String confirmation_id){
        try {
            ContentValues cv = new ContentValues();
            cv.put("confirmation_id", confirmation_id);
            database.update("selfWithdrawal", cv, null, null);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAmount() {
        Cursor cursor = database.rawQuery("SELECT amount FROM selfWithdrawal", null);
        cursor.moveToFirst();
        String currentAmount = cursor.getString(cursor.getColumnIndex("amount"));
        cursor.moveToNext();
        cursor.close();
        return currentAmount;
    }


    public String getPhoneNumber() {
        Cursor cursor = database.rawQuery("SELECT amount FROM selfWithdrawal", null);
        cursor.moveToFirst();
        String currentAmount = cursor.getString(cursor.getColumnIndex("phone_number"));
        cursor.moveToNext();
        cursor.close();
        return currentAmount;

    }

    public String getConfirmationId() {
        Cursor cursor = database.rawQuery("SELECT amount FROM selfWithdrawal", null);
        cursor.moveToFirst();
        String currentAmount = cursor.getString(cursor.getColumnIndex("confirmation_id"));
        cursor.moveToNext();
        cursor.close();
        return currentAmount;
    }

}
