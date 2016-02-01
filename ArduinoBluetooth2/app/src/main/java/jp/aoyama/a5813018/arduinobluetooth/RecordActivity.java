package jp.aoyama.a5813018.arduinobluetooth;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class RecordActivity extends ActionBarActivity implements Runnable {
    /* tag */
    private static final String TAG = "BluetoothSample";

    /* Bluetooth Adapter */
    private BluetoothAdapter mAdapter;

    /* Bluetoothデバイス */
    private BluetoothDevice mDevice;

    /* Bluetooth UUID */
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mInputTextView = (TextView)findViewById(R.id.inputValue);
        mStatusTextView = (TextView)findViewById(R.id.statusValue);


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

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.book:

                break;
            case R.id.tooth:
                //歯磨きボタンが押されたら
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

                /*
                String[] sensors = readMsg.split(",", -1);
                for(int i = 0; i < (sensors.length-5);i++){
                    if((sensors[i].equals("H")) &&(sensors[i+4].equals("L"))){
                        //ヘッダとフッダの間にある値を描画する
                        float s[] = {Integer.parseInt(sensors[i+1]), Integer.parseInt(sensors[i+2]), Integer.parseInt(sensors[i+3])};

                        //sx.plotSensor(s);

                    }
                }*/


                // null以外なら表示
                if(readMsg.trim() != null && !readMsg.trim().equals("")){
                    Log.i(TAG,"value="+readMsg.trim());
                    //int num = Integer.parseInt(readMsg.trim());
                    //int num = (int) Long.parseLong(readMsg.trim());

                    valueMsg = new Message();
                    valueMsg.what = VIEW_INPUT;
                    valueMsg.obj = readMsg;
                    mHandler.sendMessage(valueMsg);


                    //mHandler.obtainMessage(VIEW_INPUT, bytes, -1, buffer).sendToTarget();
                    Thread.sleep(100);
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
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


    /**
     * 描画処理はHandlerでおこなう
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String)msg.obj;
            if(action == VIEW_INPUT){
                //int readVal = Integer.parseInt(msgStr);
                //int num = Integer.parseInt(msgStr);
                String[] sensors = msgStr.split(",", -1);
                for(int i = 0; i < (sensors.length-5);i++){
                    if((sensors[i].equals("H")) &&(sensors[i+4].equals("L"))){
                        //ヘッダとフッダの間にある値を描画する
                        float sensorx[] = {Integer.parseInt(sensors[i+1])-300};
                        float sensory[] = {Integer.parseInt(sensors[i+2])-300};
                        float sensorz[] = {Integer.parseInt(sensors[i+3])-300};

                        sx.setColor(1);
                        sx.plotSensor(sensorx);
                        sy.setColor(2);
                        sy.plotSensor(sensory);
                        sz.setColor(3);
                        sz.plotSensor(sensorz);
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