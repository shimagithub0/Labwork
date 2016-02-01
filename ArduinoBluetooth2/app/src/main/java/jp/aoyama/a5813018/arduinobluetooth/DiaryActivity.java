package jp.aoyama.a5813018.arduinobluetooth;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class DiaryActivity extends ActionBarActivity {
    private SQLiteDatabase db;
    int flag = 0, flag2 = 0;
    int id_num=0;//その日の何回目か
    Time time;
    Cursor cursor, cursor2;
    ImageView check1,check2,check3;
    TextView m_num, d_num, n_num;
    ContentValues values2;
    TextView day_data_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView1);
        check1 = (ImageView)findViewById(R.id.check1);
        check2 = (ImageView)findViewById(R.id.check2);
        check3 = (ImageView)findViewById(R.id.check3);
        m_num = (TextView)findViewById(R.id.m_num);
        d_num = (TextView)findViewById(R.id.d_num);
        n_num = (TextView)findViewById(R.id.n_num);

        day_data_list = (TextView)findViewById(R.id.day_date_list);

        time = new Time("Asia/Tokyo");
        time.setToNow();

        //本日分の歯磨きログの表示
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        // 読み書き出来るように開く
        db = helper.getWritableDatabase();
        String month = Integer.toString((time.month+1));
        String day = Integer.toString(time.monthDay);
        cursor = db.query("day_data",  new String[] { "morning", "daytime",
                "night", "m_num", "d_num","n_num"}, "month = ? AND day = ?", new String[] { month,day }, null, null, null);//テーブル内の全てのデータ
        while (cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex("morning")) == 1){
                check1.setImageResource(R.drawable.check);
                m_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("m_num")));
            }else{
                check1.setImageResource(R.drawable.nocheck);
                m_num.setText("num:");
            }
            if(cursor.getInt(cursor.getColumnIndex("daytime")) == 1){
                check2.setImageResource(R.drawable.check);
                d_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("d_num")));
            }else{
                check2.setImageResource(R.drawable.nocheck);
                d_num.setText("num:");
            }
            if(cursor.getInt(cursor.getColumnIndex("night")) == 1){
                check3.setImageResource(R.drawable.check);
                n_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("n_num")));
            }else{
                check3.setImageResource(R.drawable.nocheck);
                n_num.setText("num:");
            }
        }

        cursor2 = db.query("day_data2", null, "Month = ? AND day = ?", new String[]{month, day}, null, null, null);
        while (cursor2.moveToNext()) {
            //同じ月、日の複数回のデータを一行ずつ表示
            day_data_list.append(cursor2.getString(cursor2.getColumnIndex("start_time"))+":"+cursor2.getString(cursor2.getColumnIndex("start_second"))
                    +"～ "+cursor2.getString(cursor2.getColumnIndex("num"))+"回 "+cursor2.getString(cursor2.getColumnIndex("time"))+"秒\n");
        }

        // カーソルクローズ
        cursor.close();
        // DBクローズ
        db.close();
        //MySQLiteOpenHelperクローズ

        //カレンダーの日にちが押されたとき
        calendarView.setOnDateChangeListener(new OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                //Toast.makeText(getApplicationContext(), (month + 1) + "/" + dayOfMonth, Toast.LENGTH_SHORT).show();

                //データベースから、その日の朝昼夜の3部門磨いたかを取ってきてイメージに表示
                showDb(month, dayOfMonth);
            }
        });

    }

    public void showDb(int month, int dayOfMonth){
        day_data_list.setText("");
        ContentValues values = new ContentValues();
        // インスタンス作成
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        // 読み書き出来るように開く
        db = helper.getWritableDatabase();
        cursor = db.query("day_data", null, null, null, null, null, null);//テーブル内の全てのデータ
        cursor2 = db.query("day_data2", null, null, null, null, null, null);//テーブル内の全てのデータ


        while (cursor.moveToNext()) {
            if ((month + 1) == cursor.getInt(cursor.getColumnIndex("month")) &&
                    dayOfMonth == cursor.getInt(cursor.getColumnIndex("day"))) {
                flag = 1;//その日のテーブルはもう作成された
                //その日の行が作成されているとき
                break;
            }
        }

        if (flag == 0) {
            //その日の行が作成されていないとき
            values.put("month", (month + 1));
            values.put("day", dayOfMonth);
            values.put("morning", 0);
            values.put("daytime", 0);
            values.put("night", 0);
            values.put("m_num",0);
            values.put("d_num",0);
            values.put("n_num",0);
            db.insert("day_data", null, values);
            flag = 1;
        }
        //その日の活動記録カウントが既にされているかどうか調べる
        while (cursor2.moveToNext()){
            if((time.month+1) == cursor2.getInt(cursor2.getColumnIndex("month")) &&
                    time.monthDay == cursor2.getInt(cursor2.getColumnIndex("day")) ){
                //その日の行が作成されているとき
                flag2 = 1;//その日のテーブルはもう作成された
                id_num = cursor2.getInt(cursor2.getColumnIndex("_id"));//ひとつ前のデータが何回目か
                break;
            }
        }

        if(flag2 == 0){
            //その日の行が作成されていないとき
            id_num = 0;//その日の一回目
            values2.put("month",(time.month+1));
            values2.put("day",time.monthDay);
            values2.put("_id",1);
            values2.put("num",0);
            values2.put("time", 0);

            db.insert("day_data2", null, values2);
            flag2= 1;
        }

        //削除
        //db.delete("day_data", null, null);

        //表示
        String day = Integer.toString(dayOfMonth);
        String month1 = Integer.toString(month+1);
        cursor = db.query("day_data", null, "Month = ? AND day = ?", new String[]{month1, day}, null, null, null);
        List<String> record = new ArrayList<String>();
        while (cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex("morning")) == 1){
                check1.setImageResource(R.drawable.check);
                m_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("m_num")));
            }else{
                check1.setImageResource(R.drawable.nocheck);
                m_num.setText("num:" );
            }
            if(cursor.getInt(cursor.getColumnIndex("daytime")) == 1){
                check2.setImageResource(R.drawable.check);
                d_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("d_num")));
            }else{
                check2.setImageResource(R.drawable.nocheck);
                d_num.setText("num:" );
            }
            if(cursor.getInt(cursor.getColumnIndex("night")) == 1){
                check3.setImageResource(R.drawable.check);
                n_num.setText("num:" + cursor.getInt(cursor.getColumnIndex("n_num")));
            }else{
                check3.setImageResource(R.drawable.nocheck);
                n_num.setText("num:" );
            }
        }

        //表示2
        cursor2 = db.query("day_data2", null, "Month = ? AND day = ?", new String[]{month1, day}, null, null, null);
        record = new ArrayList<String>();
        while (cursor2.moveToNext()) {
            //同じ月、日の複数回のデータを一行ずつ表示
            day_data_list.append(cursor2.getString(cursor2.getColumnIndex("start_time"))+":"+cursor2.getString(cursor2.getColumnIndex("start_second"))
                                    +"～ "+cursor2.getString(cursor2.getColumnIndex("num"))+"回 "+cursor2.getString(cursor2.getColumnIndex("time"))+"秒\n");
        }

        // カーソルクローズ
        cursor.close();
        cursor2.close();
        // DBクローズ
        db.close();
        //MySQLiteOpenHelperクローズ
        helper.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.book:
                //記録アイコンが押されたとき
                //Intent intent1 = new Intent(getApplication(), DiaryActivity.class);
                //startActivity(intent1);
                break;
            case R.id.tooth:
                //歯磨きボタンが押されたら
                Intent intent = new Intent(getApplication(), Brushing.class);
                startActivity(intent);
                break;
            case R.id.menu_home:
                Intent intent3 = new Intent(getApplication(), HomeActivity.class);
                startActivity(intent3);
                break;
        }
        return true;
    }
}