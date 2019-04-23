package ysn.com.magicaltextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MagicalTextView.OnDetailsClickListener {

    private MagicalTextView magicalTextView1;
    private MagicalTextView magicalTextView2;
    private MagicalTextView magicalTextView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String text1 = getResources().getString(R.string.text1);
        String text2 = getResources().getString(R.string.text2);
        String text3 = getResources().getString(R.string.text3);

        magicalTextView1 = findViewById(R.id.main_activity_text_view1);
        magicalTextView1.setText(text1, 2)
                .setOnDetailsClickListener(this)
                .setOnClickListener(this);

        magicalTextView2 = findViewById(R.id.main_activity_text_view2);
        magicalTextView2.setText(text2, 2)
                .setOnDetailsClickListener(this)
                .setOnClickListener(this);

        magicalTextView3 = findViewById(R.id.main_activity_text_view3);
        magicalTextView3.setText(text3, 2)
                .setOnDetailsClickListener(this)
                .setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(MainActivity.this, "点击了文本", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetailsClick(MagicalTextView magicalTextView) {
        switch (magicalTextView.getId()) {
            case R.id.main_activity_text_view2:
                magicalTextView2.setDetailsImage(getResources().getDrawable(R.drawable.ic_arrow_blue));
                break;
            default:
                Toast.makeText(MainActivity.this, "点击了详情", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
