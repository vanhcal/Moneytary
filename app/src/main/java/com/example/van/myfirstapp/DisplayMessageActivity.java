package com.example.van.myfirstapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

public class DisplayMessageActivity extends AppCompatActivity {
    TextView displayText, bubbleText;
    private static Button restart;

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

        bubbleText = (TextView) findViewById(R.id.bubbleSpeech);
        final String[] sayings = {"Aiiiii you cannot spend this much!", "This one time....this one time, I'll allow it.",
                "Do you think you are made of money?!", "Sigh...what happened to your goals in life?", "Whatever happened to financial responsibility??"};
        Random random = new Random();
        int index = random.nextInt(sayings.length);
        System.out.println();
        bubbleText.setText("\"" + sayings[index] + "\"");

        displayText = (TextView) findViewById(R.id.printedText);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.get(i) + "\n" + "\n");
        }
        displayText.setText(sb.toString());

        onClickButtonListener();
    }

    public void onClickButtonListener() {
        restart = (Button) findViewById(R.id.newCharge);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), MyActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
        });
    }
}
