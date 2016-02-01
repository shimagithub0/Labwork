package jp.aoyama.a5813018.arduinobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Brushing extends ActionBarActivity implements Runnable, View.OnClickListener {
    /* tag */
    private static final String TAG = "BluetoothSample";

    /* Bluetooth Adapter */
    private BluetoothAdapter mAdapter;

    /* Bluetoothデバイス */
    private BluetoothDevice mDevice;

    /* Bluetooth UUID */
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /* デバイス名 */
    private final String DEVICE_NAME = "YAMAZAKI";

    /* Soket */
    private BluetoothSocket mSocket;

    /* Thread */
    private Thread mThread;

    /* Threadの状態を表す */
    private boolean isRunning;



    /** ステータス. */
    private TextView mStatusTextView;

    /** Bluetoothから受信した値. */
    private TextView mInputTextView;

    /** Action(ステータス表示). */
    private static final int VIEW_STATUS = 0;

    /** Action(取得文字列). */
    private static final int VIEW_INPUT = 1;

    /** Connect確認用フラグ */
    private boolean connectFlg = false;

    /** BluetoothのOutputStream. */
    OutputStream mmOutputStream = null;

    //SensorView
    SensorView sx,sy,sz;

    //信号処理関係
    int framesize = 20;
    int[] s_x = new int[framesize];
    int[] s_y = new int[framesize];
    int[] s_z = new int[framesize];
    int s_num = 0;
    float xmoving, xbmoving;
    float ymoving, ybmoving;
    float zmoving, zbmoving;
    int moving_frameshift = 1;

    TextView position, position2;
    Button b_end;
    Time time;

    //データベース
    private SQLiteDatabase db;
    TextView dbtest;
    int flag=0,flag2=0;
    int id_num=0;//その日の何回目か
    ContentValues values2;
    int time_hour=0, time_second=0;

    //何回磨いたか
    int numtimes = 0;
    int pm_flag = 0;
    int max=0,min=300;
    TextView numtime;
    int pm=2;
    int gosa_count=0;
    int rl= 2;
    int gosa_state=2;
    long start = System.currentTimeMillis();
    long end;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brushing);

        mInputTextView = (TextView)findViewById(R.id.inputValue);
        mStatusTextView = (TextView)findViewById(R.id.statusValue);
        position = (TextView)findViewById(R.id.position);
        position2 = (TextView)findViewById(R.id.position2);
        b_end = (Button)findViewById(R.id.b_end);
        b_end.setOnClickListener(this);
        dbtest = (TextView)findViewById(R.id.dbtest);
        numtime = (TextView)findViewById(R.id.numtimes);

        //時刻の取得
        time = new Time("Asia/Tokyo");
        time.setToNow();
        time_hour = time.hour;
        time_second = time.second;



        //XML レイアウトからレイアウトオブジェクトを取得
        RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.main_layout);
        //プロット専用のView オブジェクトのインスタンス生成
        sx = new SensorView(this);
        mLayout.addView(sx);
        sy = new SensorView(this);
        mLayout.addView(sy);
        sz = new SensorView(this);
        mLayout.addView(sz);


        // Bluetoothのデバイス名を取得
        // デバイス名は、RNBT-XXXXになるため、
        // DVICE_NAMEでデバイス名を定義
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mStatusTextView.setText("SearchDevice");
        Set< BluetoothDevice > devices = mAdapter.getBondedDevices();
        for ( BluetoothDevice device : devices){

            if(device.getName().equals(DEVICE_NAME)){
                mStatusTextView.setText("find: " + device.getName());
                mDevice = device;
            }
        }

        // 接続されていない場合のみ
        if (!connectFlg) {
            mStatusTextView.setText("try connect");

            mThread = new Thread(this);
            // Threadを起動し、Bluetooth接続
            isRunning = true;
            mThread.start();
        }

    }

    public void onClick(View v){
        end = System.currentTimeMillis();

        if(v.getId() == b_end.getId()){
            dbtest.setText("");
            // インスタンス作成
            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
            // 読み書き出来るように開く
            db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values2 = new ContentValues();

            Cursor cursor = db.query("day_data", null, null, null, null, null, null);//その日の朝昼夜カウントテーブル内の全てのデータ
            Cursor cursor2 = db.query("day_data2",null, null, null, null, null, "month,day ASC , _id DESC");

            //朝昼夜カウントが既にされているかどうか調べる
            while (cursor.moveToNext()){
                if((time.month+1) == cursor.getInt(cursor.getColumnIndex("month")) &&
                        time.monthDay == cursor.getInt(cursor.getColumnIndex("day")) ){
                    //その日の行が作成されているとき
                    flag = 1;//その日のテーブルはもう作成された
                    break;
                }
            }

            if(flag == 0){
                //その日の行が作成されていないとき
                values.put("month",(time.month+1));
                values.put("day",time.monthDay);
                values.put("morning",0);
                values.put("daytime",0);
                values.put("night", 0);
                values.put("m_num",0);
                values.put("d_num",0);
                values.put("n_num", 0);
                db.insert("day_data", null, values);
                flag= 1;
            }

            //その日の活動記録カウントが既にされているかどうか調べる
            while (cursor2.moveToNext()){
                if((time.month+1) == cursor2.getInt(cursor2.getColumnIndex("month")) &&
                        time.monthDay == cursor2.getInt(cursor2.getColumnIndex("day")) ){
                    //その日の行が作成されているとき
                    flag2 = 1;//その日のテーブルはもう作成された
                    id_num = cursor2.getInt(cursor2.getColumnIndex("_id"));//ひとつ前のデータが何回目か
                    values2.put("month",(time.month+1));
                    values2.put("day",time.monthDay);
                    values2.put("_id",id_num+1);
                    values2.put("num",0);
                    values2.put("time", 0);

                    db.insert("day_data2", null, values2);
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

            //データベースの更新
            if(time.hour >= 19){
                values.put("night",1);
                values.put("n_num",numtimes);
            }else if(time.hour >= 12){
                values.put("daytime",1);
                values.put("d_num",numtimes);
            }else if(time.hour >= 6){
                values.put("morning",1);
                values.put("m_num",numtimes);
            }else{
                values.put("night",1);
                values.put("n_num",numtimes);
            }

            //更新
            String month = Integer.toString((time.month+1));
            String day = Integer.toString(time.monthDay);
            db.update("day_data", values, "day=?", new String[]{day});

            //データベースの更新Ⅱ
            //values2.put("month",(time.month+1));
            //values2.put("day",time.monthDay);
            //values2.put("_id",id_num+1);
            values2.put("num",numtimes);
            values2.put("time", (end-start)/1000);
            values2.put("start_time", time_hour);
            values2.put("start_second", time_second);

            String id_nums = Integer.toString(id_num+1);
            db.update("day_data2", values2, "day=? AND _id = ?", new String[]{day, id_nums});//その日の+1回目にデータ更新


            //削除
            //db.delete("day_data", "day=? AND daytime=?", new String[]{"4", "1"});
            //db.delete("day_data", "day=? ", new String[]{"8"});
            //表示
            cursor = db.query("day_data", null, null, null, null, null, null);
            List<String> record = new ArrayList<String>();
            while (cursor.moveToNext()) {
                String str = cursor.getString(cursor.getColumnIndex("month")) + "\t"
                        + cursor.getString(cursor.getColumnIndex("day")) + "\t"
                        + cursor.getString(cursor.getColumnIndex("morning")) + "\t"
                        + cursor.getString(cursor.getColumnIndex("daytime")) + "\t"
                        + cursor.getString(cursor.getColumnIndex("night"))+ "\t\t";
                record.add(str);
            }
            for (int i = 0; i < record.size(); i++) {
                //TextView tv = new TextView(this);
                //tv.setText(record.get(i));

                dbtest.append(record.get(i));
            }
            // カーソルクローズ
            cursor.close();
            cursor2.close();
            // DBクローズ
            db.close();
            // MySQLiteOpenHelperクローズ
            helper.close();


            numtime.setText("time"+(end-start)/1000 + "秒");
            //Intent intent = new Intent(getApplication(), DiaryActivity.class);
            //startActivity(intent);
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
                //Intent intent = new Intent(getApplication(), Brushing.class);
                //startActivity(intent);
                break;
            case R.id.menu_home:
                Intent intent3 = new Intent(getApplication(), HomeActivity.class);
                startActivity(intent3);
                break;
        }
        return true;
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause(){
        super.onPause();

        isRunning = false;
        try{
            mSocket.close();
        }
        catch(Exception e){}
    }

    @Override
    public void run() {
        InputStream mmInStream = null;

        Message valueMsg = new Message();
        valueMsg.what = VIEW_STATUS;
        valueMsg.obj = "connecting...";
        mHandler.sendMessage(valueMsg);

        try{

            // 取得したデバイス名を使ってBluetoothでSocket接続
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mSocket.connect();
            mmInStream = mSocket.getInputStream();
            mmOutputStream = mSocket.getOutputStream();

            // InputStreamのバッファを格納
            byte[] buffer = new byte[16*100];

            // 取得したバッファのサイズを格納
            int bytes;
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "connected.";
            mHandler.sendMessage(valueMsg);

            connectFlg = true;

            while(isRunning){
                ///////////////////////////
                // InputStreamの読み込み///
                ///////////////////////////
                bytes = mmInStream.read(buffer);
                Log.i(TAG,"bytes="+String.valueOf(buffer));
                // String型に変換
                String readMsg = new String(buffer, 0, bytes);

                // null以外なら表示
                if(readMsg.trim() != null && !readMsg.trim().equals("")){
                    Log.i(TAG,"value="+readMsg.trim());

                    valueMsg = new Message();
                    valueMsg.what = VIEW_INPUT;
                    valueMsg.obj = readMsg;
                    mHandler.sendMessage(valueMsg);

                    //Thread.sleep(100);

                }
                else{
                    // Log.i(TAG,"value=nodata");
                }

            }
        }catch(Exception e){
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "Error1:" + e;
            mHandler.sendMessage(valueMsg);
            try{
                mSocket.close();
            }catch(Exception ee){}
            isRunning = false;
            connectFlg = false;
        }
    }


    ////////////////////////////
    //配列m_normの平均値を出す//
    ////////////////////////////
    float mean(int[] m_norm) {
        int total = 0;
        for (int i = 0; i < m_norm.length; i++) {
            total += m_norm[i];
        }
        return (float)total/m_norm.length;
    }

    /**
     * 描画処理はHandlerでおこなう
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String)msg.obj;
            if(action == VIEW_INPUT){

                String[] sensors = msgStr.split(",", -1);
                for(int i = 0; i < (sensors.length-5);i++){
                    if((sensors[i].equals("H")) &&(sensors[i+4].equals("L"))){
                        //ヘッダとフッダの間にある値を描画する
                        ////////////////
                        //グラフの描画//
                        ////////////////

                        float sensorx[] = {Integer.parseInt(sensors[i+1])-300};
                        float sensory[] = {Integer.parseInt(sensors[i+2])-300};
                        float sensorz[] = {Integer.parseInt(sensors[i+3])-300};

                        sx.setColor(1);
                        sx.plotSensor(sensorx);
                        sy.setColor(2);
                        sy.plotSensor(sensory);
                        sz.setColor(3);
                        sz.plotSensor(sensorz);



                        ///////////////////////////////
                        //フレーム分だけの配列を作る///
                        ///////////////////////////////
                        s_x[s_num] = Integer.parseInt(sensors[i+1]);
                        s_y[s_num] = Integer.parseInt(sensors[i+2]);
                        s_z[s_num] = Integer.parseInt(sensors[i+3]);
                        s_num++;
                        ///////////////////////////////////////
                        //配列sに20フレーム分値が入ったら//////
                        ///////////////////////////////////////
                        if (s_num > (framesize - 1)) {
                            /////////////////////////////////////////
                            //移動平均の算出/////////////////////////
                            /////////////////////////////////////////
                            ymoving = mean(s_y);
                            xmoving = mean(s_x);
                            zmoving = mean(s_z);
                            ///////////////////////////////////////////////
                            //xyzのaverageからど位置を磨いているのか算出///
                            ///////////////////////////////////////////////
                            if (xmoving > 260) {
                                //右側
                                position.setText("Right");
                            } else if (xmoving < 240) {
                                //左側
                                position.setText("Left");
                            } else {}

                            if (zmoving > 260) {
                                //左の歯
                                position2.setText("上の歯");
                            } else if (zmoving < 225) {
                                //右の歯
                                position2.setText("下の歯");
                            } else {}
                            ///////////////////
                            //配列をシフト/////
                            ///////////////////
                            //x
                            for (int j = 0; j < (framesize-moving_frameshift); j++) {
                                s_x[j] = s_x[j + moving_frameshift];
                            }

                            //y
                            for (int j = 0; j < (framesize-moving_frameshift); j++) {
                                s_y[j] = s_y[j + moving_frameshift];
                            }

                            //z
                            for (int j = 0; j < (framesize-moving_frameshift); j++) {
                                s_z[j] = s_z[j + moving_frameshift];
                            }

                            ////////////////////////////////////////
                            //シフトした分の次の値からになるように//
                            ////////////////////////////////////////
                            s_num = framesize - moving_frameshift;//15
                        }

                        //////////////////////
                        //磨いているかどうか//
                        //////////////////////

                        if((ymoving - (float)Integer.parseInt(sensors[i+2])) < -5|| 5 <(ymoving - (float)Integer.parseInt(sensors[i+2]))){//誤差揺れでないとき
                            gosa_count=0;
                            if((ymoving - (float)Integer.parseInt(sensors[i+2])) >= 0){//下
                                if(pm==2){
                                    rl=0;
                                }
                                pm = 0;
                            }else{
                                if(pm==2){
                                    rl=1;
                                }
                                pm = 1;
                            }
                        }else{//誤差揺れ
                            gosa_count++;
                            if(gosa_count > 20){
                                pm = 2;
                                gosa_state=1;
                            }
                        }

                        ////////////////
                        //何回磨いたか//
                        ////////////////
                        if((ymoving - (float)Integer.parseInt(sensors[i+2])) >= 0){
                            //下

                            if(min > Integer.parseInt(sensors[i+2])){
                                min = Integer.parseInt(sensors[i+2]);
                            }

                            pm_flag = 0;
                        }else{
                            //上
                            if(pm_flag == 0){//直前が上だったら
                                if((max - min) > 25){
                                    if(rl==0 && gosa_state == 1){
                                        numtimes -= 1;
                                        gosa_state=0;
                                    }

                                    numtimes++;//カウント
                                    //println("num:"+numtimes);
                                    numtime.setText("numtime:"+numtimes);
                                }
                                //println("min"+min);
                                //println("max"+max);
                                min = (int)ymoving;//minリセット
                                max = (int)ymoving;//maxリセット
                            }

                            if(max < Integer.parseInt(sensors[i+2])){
                                max = Integer.parseInt(sensors[i+2]);
                            }

                            pm_flag = 1;

                        }

                        mInputTextView.setText("x:"+sensors[i+1] +" y:"+sensors[i+2]+" z:"+sensors[i+3]);
                    }
                }


                //mInputTextView.setText(msgStr);
                mStatusTextView.setVisibility(View.INVISIBLE);
            }
            else if(action == VIEW_STATUS){
                mStatusTextView.setText(msgStr);
            }
        }
    };
}