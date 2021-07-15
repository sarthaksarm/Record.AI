package com.sarm.recordai;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sarm.recordai.predictcall.PredictActivity;
import com.sarm.recordai.recordcall.RecordActivity;
import com.sarm.recordai.transcriptcall.TranscribeHomeActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.sarm.recordai.ViewFile.filepath;

public class MainActivity extends AppCompatActivity {
TextView recentUpdatetext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recentUpdatetext = findViewById(R.id.recentUpdatetxt);

        String filename = "recent_predict_string.txt";
        String res = readFromFile(filename);

        if(!res.isEmpty())
            recentUpdatetext.setText(res);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        String filename = "recent_predict_string.txt";
        String res = readFromFile(filename);

        if(!res.isEmpty())
            recentUpdatetext.setText(res);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String filename = "recent_predict_string.txt";
        String res = readFromFile(filename);

        if(!res.isEmpty())
            recentUpdatetext.setText(res);
    }

    public String readFromFile(String nameFile) {
        String aBuffer = "";
        try {
            String path = getExternalFilesDir("/").getAbsolutePath() +"/Recent_Call/"+nameFile;

            File myFile = new File(path);
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

    public void recordingscard(View view)
    {
        Intent i = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(i);
    }

    public void notescard(View view)
    {
        Intent i = new Intent(MainActivity.this, NotesActivity.class);
        startActivity(i);
    }

    public void transcribepredictcard(View view)
    {
        Intent i = new Intent(MainActivity.this, TranscribeHomeActivity.class);
        startActivity(i);
    }

    public void aboutcard(View view)
    {
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
    }
}