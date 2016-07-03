package com.example.van.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity {
    TextView displayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = this.getIntent().getExtras();
        ArrayList<String> array = bundle.getStringArrayList("key");

        TextView textView = new TextView(this);
        textView.setTextSize(40);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
        layout.addView(textView);

        displayText = (TextView) findViewById(R.id.printedText);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.get(i) + "\n" + "\n");
        }
        displayText.setText(sb.toString());
    }
}
