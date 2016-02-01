package jp.aoyama.a5813018.arduinobluetooth;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SQLiteSample extends ActionBarActivity {
    /** データベース */
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);

        Time time = new Time("Asia/Tokyo");
        time.setToNow();

        LinearLayout llayout = (LinearLayout) findViewById(R.id.llayout);
        // インスタンス作成
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        // 読み書き出来るように開く
        db = helper.getWritableDatabase();
        // レコード1設定
        ContentValues values = new ContentValues();
        values.put("month",(time.month+1));
        values.put("day",time.monthDay);
        values.put("morning",1);
        values.put("daytime",1);
        values.put("night",1);
        //values.put("name", "田中一郎");
        //values.put("age", 50);
        //values.put("delete_flg", 0);
        // レコード1追加
        //db.insert("day_data", null, values);
        // レコード1追加
       //db.insert("personal_data", null, values);

        //////削除
        db.delete("day_data", null, null);
        //db.delete("personal_data", "delete_flg = 1", null);
        //////

        //更新
        //db.update("day_data", values, "_id=?", new String[]{"2"});

        // レコードを検索してカーソルを作成
        Cursor cursor = db.query("day_data", null, null, null, null, null, null);
        //Cursor cursor = db.query("personal_data", new String[] { "_id", "name","age" }, "delete_flg = ?", new String[] { "1" }, null, null, "age ASC");

        List<String> record = new ArrayList<String>();
        // カーソルから値を取り出す
        while (cursor.moveToNext()) {
            String str = cursor.getString(cursor.getColumnIndex("_id")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("day")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("morning")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("daytime")) + "\t"
                    + cursor.getString(cursor.getColumnIndex("night"));
            record.add(str);
        }

        // テキストビューで表示
        for (int i = 0; i < record.size(); i++) {
            TextView tv = new TextView(this);
            tv.setText(record.get(i));
            llayout.addView(tv);
        }
        // カーソルクローズ
        cursor.close();
        // DBクローズ
        db.close();
        // MySQLiteOpenHelperクローズ
        helper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}