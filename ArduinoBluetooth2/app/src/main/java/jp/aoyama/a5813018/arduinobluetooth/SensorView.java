package jp.aoyama.a5813018.arduinobluetooth;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.LinkedList;

public class SensorView extends View {
    public final static int MAX_VALUES = 600; //プロットする最大計測点数
    public final static int SCALING = 5; //計測値（加速度）とPixel 数の変換倍率
    private final LinkedList<float[]> fifo; //センサ値収納用キュー
    private final LinkedList<float[]> fifo2; //センサ値収納用キュー
    private final LinkedList<float[]> fifo3; //センサ値収納用キュー
    private float xVal = 0; //画面の左右軸でプロットする位置
    private float yVal = 0; //一個前の計測点を保存する変数
    private int xYLine; //センサ値のベースライン（0 値）を画面内のどこに表示する（縦の位置）
    private float len; //二つの計測点間の画面上の間隔
    private Paint p = new Paint(); //表示の属性を管理するPaint オブジェクトを取得する

    public SensorView(Context context ) {
        super(context);
        fifo = new LinkedList<float[]>();
        fifo2 = new LinkedList<float[]>();
        fifo3 = new LinkedList<float[]>();
    }
    public void plotSensor(float[] values){
        if (fifo.size() > MAX_VALUES){
            fifo.poll(); //キューが一杯と、キューの最後のデータを削除する
        }
        if (fifo2.size() > MAX_VALUES){
            fifo2.poll(); //キューが一杯と、キューの最後のデータを削除する
        }
        if (fifo3.size() > MAX_VALUES){
            fifo3.poll(); //キューが一杯と、キューの最後のデータを削除する
        }

        if(values.length == 1){
            fifo.add(new float[]{values[0]});
        }

        if(values.length == 3){
            fifo.add(new float[]{values[0]});
            fifo2.add(new float[]{values[1]});
            fifo3.add(new float[]{values[2]});
        }

        invalidate(); //強制的に再表示させる
    }

    public void setColor(int i){
        //どの色でプロットするかを設定
        switch (i){
            case 3:
                p.setColor(Color.BLUE);
                break;
            case 1:
                p.setColor(Color.RED);
                break;
            case 2:
                p.setColor(Color.GREEN);
                break;
            default:
        }



    }

    @Override
    public void onDraw(Canvas canvas) {
        //プロットを実際に描くコード……
        xYLine = canvas.getHeight()/6;
        len = canvas.getWidth()/MAX_VALUES;
        xVal = 0;
        //キュー内にデータがある限り続ける
        for (float[] f:fifo){


            //一個前の計測点と最新計側点間の線を描く
            canvas.drawLine(xVal, xYLine-yVal*SCALING, xVal + len, xYLine-f[0]*SCALING,p);
            xVal+=len; //画面の左右軸でプロットする位置をずらす
            yVal = f[0]; //“一個前”計測値を更新
        }


    }
}



