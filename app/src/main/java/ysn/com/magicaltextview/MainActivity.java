package ysn.com.magicaltextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String text="宋茜：1987年2月2日出生于山东省青岛市，中国内地女演员、歌手，亚洲多栖发展女艺人。2008年，宋茜作为演员、广告模特在亚洲地区正式开始演艺活动。2009年，担任女子流行演唱组合f(x)队长，以歌手身份正式出道。";
//        String text = "宋茜：1987年2月2日出生于山东省青岛市，中国内地女演员";
//        String text="宋茜：1987年2月2日出生于山东省青岛市";

        MagicalTextView autoWrapTextView = findViewById(R.id.main_activity_text_view);
        autoWrapTextView.setText(text);
        autoWrapTextView.setOnDetailsClickListener(new MagicalTextView.OnDetailsClickListener() {
            @Override
            public void onDetailsClick() {
                Toast.makeText(MainActivity.this, "点击了详情", Toast.LENGTH_SHORT).show();
            }
        });

        autoWrapTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this, "点击了文本", Toast.LENGTH_SHORT).show();
    }
}
