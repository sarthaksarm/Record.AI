package com.sarm.recordai.transcriptcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.sarm.recordai.R;
import com.sarm.recordai.recordcall.RecordActivity;

import java.util.ArrayList;
import java.util.List;

public class TranscribeHomeActivity extends AppCompatActivity{
static String number_speaker = "+918095030481";
static String number_caller = "+918318940391";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribehome);
        mAuth = FirebaseAuth.getInstance();

//        Bundle bundle=getIntent().getExtras();
//        if (bundle != null) {
//            if (bundle.containsKey("number")) {
//                number = bundle.get("number").toString();
//                if (number != null) {
//                    //Toast.makeText(getApplicationContext(), number, Toast.LENGTH_LONG).show();
//                }
//            }
//        }

        Button button=(Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(TranscribeHomeActivity.this, TranscribeMainActivity.class);
                startActivity(intent);
            }
        });
    }

public void showHistory(View view){
    Intent i=new Intent(TranscribeHomeActivity.this, RecordActivity.class);
    startActivity(i);
    finish();
}

}
