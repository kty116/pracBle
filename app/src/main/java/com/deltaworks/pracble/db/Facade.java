package com.deltaworks.pracble.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2018-02-28.
 */

public class Facade {

    public static final String TAG = Facade.class.getSimpleName();
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public Facade(Context context) {
        this.mDBHelper = new DBHelper(context);
    }

    /**
     * dtg db에 데이터 넣기
     */

    public boolean insertDTGData(ContentValues values) {
        mDB = mDBHelper.getWritableDatabase();
        try {
            mDB.beginTransaction();  //대용량일때 사용
            mDB.insert(DTGContract.DTGDataEntry.DTG_TABLE_NAME, null, values);
            mDB.setTransactionSuccessful();
        } catch (SQLException e) {
            return false;
        } finally {
            mDB.endTransaction();
            return true;
        }
    }

    /**
     * dtg 모든 데이터 가져오기
     *
     * @return
     */
    public Cursor queryDTGAllData() {
        mDB = mDBHelper.getReadableDatabase();

        Cursor cursor = mDB.query(DTGContract.DTGDataEntry.DTG_TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    /**
     * 해당 id 보다 id 작은 data 삭제하기
     */

    public int deleteData(String deleteId) {
        mDB = mDBHelper.getWritableDatabase();
//        mDB.execSQL("VACUUM");
        return mDB.delete(DTGContract.DTGDataEntry.DTG_TABLE_NAME,
                DTGContract.DTGDataEntry._ID + "<=" + deleteId,
                null);

    }


    /**
     * 테이블 삭제
     */
    public void dropTable() {
        mDB = mDBHelper.getWritableDatabase();
        mDB.execSQL("drop table " + DTGContract.DTGDataEntry.DTG_TABLE_NAME);

    }

    /**
     * 테이블 생성
     */
    public void createTable() {
        mDB = mDBHelper.getWritableDatabase();
        mDB.execSQL(DTGContract.DTGDataEntry.DTG_ENTRIES);  //테이블 생성
    }

    /**
     * 테이블 조회
     */
    public boolean hasDTGTable() {
        boolean hasTable = false;
        mDB = mDBHelper.getReadableDatabase();
        Cursor cursor = mDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='" + DTGContract.DTGDataEntry.DTG_TABLE_NAME + "'", null);

        if (cursor.moveToFirst()) {
            for (; ; ) {
                hasTable = true;//한번이라도 쿼리 타면 있다는 뜻
                if (!cursor.moveToNext())
                    break;
            }
        }
        return hasTable;
    }
}
