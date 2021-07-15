package com.sarm.recordai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import android.os.Bundle;

public class ViewFile extends AppCompatActivity {
    public static String filepath = "";
    public static String filename = "";
    TextView mTextVIew;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);
        filepath = getIntent().getStringExtra("filepath");
        filename = getIntent().getStringExtra("filename");
        mTextVIew = findViewById(R.id.textView1);
        String content = readFromFile(getApplicationContext(),filename);
        Toast.makeText(this, ""+content, Toast.LENGTH_SHORT).show();
        mTextVIew.setText(content);

    }

    public static String readFromFile(Context context, String nameFile) {
        String aBuffer = "";
        try {
            File myFile = new File(filepath);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow+"\n";
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
}