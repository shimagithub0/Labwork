package jp.aoyama.a5813018.arduinobluetooth;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends ActionBarActivity implements View.OnClickListener{

    private SQLiteDatabase db;
    TextView message;
    Button b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11;
    ScrollView scroll;
    ContentValues values;
    Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Time time = new Time("Asia/Tokyo");
        time.setToNow();

        ImageView imageView1 = (ImageView)findViewById(R.id.toothHome);
        TextView t_message = (TextView)findViewById(R.id.t_message);
        message = (TextView)findViewById(R.id.message);
        message.setOnClickListener(this);
        b1 = (Button)findViewById(R.id.button1);
        b2 = (Button)findViewById(R.id.button2);
        b3 = (Button)findViewById(R.id.button3);
        b4 = (Button)findViewById(R.id.button4);
        b5 = (Button)findViewById(R.id.button5);
        b6 = (Button)findViewById(R.id.button6);
        b7 = (Button)findViewById(R.id.button7);
        //b8 = (Button)findViewById(R.id.button8);
        //b9 = (Button)findViewById(R.id.button9);
        //b10 = (Button)findViewById(R.id.button10);
        b11 = (Button)findViewById(R.id.button11);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        b6.setOnClickListener(this);
        b7.setOnClickListener(this);
        //b8.setOnClickListener(this);
        //b9.setOnClickListener(this);
        //b10.setOnClickListener(this);
        b11.setOnClickListener(this);
        scroll = (ScrollView)findViewById(R.id.scroll);


        if(time.hour > 19){
            imageView1.setImageResource(R.drawable.ic_tooth_night);
            t_message.setText("good night!");
        }else if(time.hour > 12){
            imageView1.setImageResource(R.drawable.ic_tooth_daytime);
            t_message.setText("good afternoon!");
        }else if(time.hour > 6){
            imageView1.setImageResource(R.drawable.ic_tooth);
            t_message.setText("good morning!");
        }else{
            imageView1.setImageResource(R.drawable.ic_tooth_night);
            t_message.setText("good night!");
        }

        /*
        //データベースの作成,一回でいい
        // インスタンス作成
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        // 読み書き出来るように開く
        db = helper.getWritableDatabase();
        // レコード1設定
        ContentValues values = new ContentValues();
        values.put("month",(time.month+1));
        values.put("day",time.monthDay);
        values.put("morning",0);
        values.put("daytime",0);
        values.put("night",0);

        db.insert("day_data", null, values);
        */

        //db
        // インスタンス作成
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        // 読み書き出来るように開く
        db = helper.getWritableDatabase();
        values = new ContentValues();
        //values.put("_id",1);
        //values.put("num",200);
        //db.insert("mokuhyo_num", null, values);

        //目標の回数を取得
        Cursor c = db.query("mokuhyo_num", null, null, null, null, null, null);//テーブル内の全てのデータ

        while (c.moveToNext()) {


            if(c.getInt(c.getColumnIndex("_id")) == 1){
                message.setText("目標："+c.getInt(c.getColumnIndex("num"))+"回磨く");
            }
        }

        String num = Integer.toString(1);
        //cursor = db.query("mokuhyo_num", null, null, null, null, null, null);
        //Cursor c = db.rawQuery("select * from mokuhyo_num",null);
        //message.setText("目標："+cursor.getInt(cursor.getColumnIndex("_id")));

    }

    public void onClick(View v){
        if(v.getId() == message.getId()){
            //message.setText("click");
            scroll.setVisibility(View.VISIBLE);
            values.put("num", 100);
            String num = Integer.toString(1);
            db.update("mokuhyo_num", values, "_id=?", new String[]{num});
        }

        if(v.getId() == b2.getId()){
            message.setText("目標150回磨く");
            scroll.setVisibility(View.INVISIBLE);
            values.put("num", 150);
            String num = Integer.toString(1);
            db.update("mokuhyo_num", values, "_id=?", new String[]{num});
        }

        if(v.getId() == b3.getId()){
            message.setText("目標200回磨く");
            scroll.setVisibility(View.INVISIBLE);
            values.put("num", 200);
            String num = Integer.toString(1);
            db.update("mokuhyo_num", values, "_id=?", new String[]{num});
        }

        if(v.getId() == b4.getId()){
            message.setText("目標250回磨く");
            scroll.setVisibility(View.INVISIBLE);
            values.put("num", 250);
            String num = Integer.toString(1);
            db.update("mokuhyo_num", values, "_id=?", new String[]{num});
        }

        if(v.getId() == b5.getId()){
            message.setText("目標300回磨く");
            scroll.setVisibility(View.INVISIBLE);
            values.put("num", 300);
            String num = Integer.toString(1);
            db.update("mokuhyo_num", values, "_id=?", new String[]{num});
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.book:
                //記録アイコンが押されたとき
                Intent intent1 = new Intent(getApplication(), DiaryActivity.class);
                startActivity(intent1);
                break;
            case R.id.tooth:
                //歯磨きボタンが押されたら
                Intent intent = new Intent(getApplication(), Brushing.class);
                startActivity(intent);
                break;
            case R.id.menu_home:
                //Intent intent3 = new Intent(getApplication(), HomeActivity.class);
                //startActivity(intent3);
                break;
        }
        return true;
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}