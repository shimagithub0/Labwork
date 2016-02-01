package jp.aoyama.a5813018.arduinobluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);

        start = (ImageButton)findViewById(R.id.start);
        start.setOnClickListener(this);
    }


    public void onClick(View v) {
        // 押下時の処理
        if(v.getId() == start.getId()){

            Intent intent = new Intent(getApplication(), HomeActivity.class);
            startActivity(intent);
        }

    }
}
