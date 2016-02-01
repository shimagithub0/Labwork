package jp.aoyama.a5813018.arduinobluetooth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    /** アクセスするデータベース名 */
    private static final String DB_NAME = "example7_1.db";
    /** DBのバージョン */
    private static final int DB_VERSION = 1;
    /** create table文 */
    private static final String createTableString = "create table day_data(_id integer , " +
            "month integer, day integer ,morning integer,daytime integer, night integer, m_num integer," +
            " d_num integer, n_num integer, primary key(month, day))";

    private static final String createTableString2 = "create table mokuhyo_num(_id integer primary key autoincrement , num integer)";
    private static final String createTableString3 = "create table day_data2(_id integer , month integer, day integer, num integer, " +
            "time integer, start_time integer, start_second integer, primary key(_id, month, day))";
    //name text, old int, address text, primary key(name, old)
    //(_id integer primary key autoincrement, name text,age integer, delete_flg integer)

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableString);
        db.execSQL(createTableString2);
        db.execSQL(createTableString3);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 自動生成されたメソッド・スタブ

    }

}