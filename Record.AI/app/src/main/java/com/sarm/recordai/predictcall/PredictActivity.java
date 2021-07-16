package com.sarm.recordai.predictcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sarm.recordai.MainActivity;
import com.sarm.recordai.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.lang.Float;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.tensorflow.lite.Interpreter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.util.Scanner;

public class PredictActivity extends AppCompatActivity {
    Button predbtn;
    TextView restxt;
    Interpreter tftliteInterpreter;
    TextView calltxt;
    DatabaseReference ref;
    String call_data="";
    String conversation_data ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        String filename = getIntent().getStringExtra("Filename");
        call_data = getIntent().getStringExtra("Content");
        conversation_data = getIntent().getStringExtra("Conversations");

        calltxt = findViewById(R.id.calldata_txt);
        load();

        int INPUT_MAXLEN = 1000;
        final PredictClassifier classifier = new PredictClassifier(this , "android_word_dict.json" , INPUT_MAXLEN);

        try {
            tftliteInterpreter = new Interpreter(loadmodelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            classifier.loadVocab(classifier.loadJSONFromAsset("android_word_dict.json"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        predbtn = (Button)findViewById(R.id.prebtn);
        restxt = findViewById(R.id.restxt);

        predbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[][] output = new float[1][5];
                String testtext = "hyperlink URL has teamed up with hyperlink foundmoney to help you locate and claim your lost cash the amount on that check or more literally be yours for the claiming this is not a contest or a promotion hyperlink foundmoney is a " +
                        "search service dedicated to putting unclaimed money together with its rightful owners hyperlink there are NUMBER million north american people eligible right now to claim unknown cash windfalls the search is fast easy and gauranteed over billion is sitting in our database alone which contains bank and government accounts wills and estates insurance settlements etc hyperlink since NUMBER " +
                        "our web site has reunited millions upon millions of dollars with thousands of rightful owners who didn t even know they had money waiting for them hyperlink click here now or on the link below to find out in seconds if there is money waiting to be claimed in your " +
                        "family name or that of somebody you know the initial search is free you have nothing to lose try foundmoney today hyperlink click here now sincerely URL you received this email because you signed up at one of URL s websites or you signed up with a party that has contracted with URL to unsubscribe from our email newsletter please visit hyperlink URL ";

                String testtext2 = "good morning";

                //String call_txt = restxt.getText().toString();

                if(call_data.equals(""))
                {
                    Toast.makeText(PredictActivity.this, "There is no data to test. Try again.", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(PredictActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

                call_data = call_data.toLowerCase().trim();

                int[] tokenizedmsg = classifier.tokenize(call_data);
                int[] paddedmsg = classifier.padSequence(tokenizedmsg);

                try {
                    output = classifySequence(paddedmsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int i;
                float max=0;
                int index=0;
                for(i=0;i<5;i++)
                {
                    //if(output[0][i]>max)
                    if(Float.compare(output[0][i],max)>0)   //output[0][i]>max
                    {
                        max=output[0][i];
                        index=i;
                    }
                }
                String result="";
                switch (index)
                {
                    case 0: result = "Threat Call";
                            break;

                    case 1: result = "Blackmail Call";
                            break;

                    case 2: result = "Fraud Call";
                        break;

                    case 3: result = "Spam Call";
                        break;

                    case 4: result = "Legitimate Call";
                        break;
                }

                String content_data = "Identified Call Type: " + result + "\n\n" +
                        "Threat Call = "+output[0][0] +"\n"+
                        "Blackmail Call = "+output[0][1] +"\n"+
                        "Fraud Call = " + output[0][2] + "\n" +
                        "Spam Call = " + output[0][3] + "\n"+
                        "Legitimate Call =" + output[0][4];

                restxt.setText(content_data);
                //append to file
                writeToFile(conversation_data + content_data, filename, result);
            }
        });
    }

    public void load() {
        String mynum = "+918095030481";
        final String[] caller_text = new String[1];
        ref = FirebaseDatabase.getInstance().getReference().child(mynum);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> calldata = snapshot.getChildren();
                for(DataSnapshot call: calldata)
                {
                    caller_text[0] = call.child("Caller_data").getValue().toString();
                }
                restxt.setText(caller_text[0]);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

//        FileInputStream fis = openFileInput("caller.txt");
//        Scanner scanner = new Scanner(fis);
//        scanner.useDelimiter("\\Z");
//        String content = scanner.next();
//        scanner.close();

    }

    public MappedByteBuffer loadmodelFile() throws IOException {

        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("recordaikermodel_new.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Perform inference, given the input sequence.
    private float[][] classifySequence (int[] sequence ) throws IOException {
        // Input shape -> ( 1 , INPUT_MAXLEN )

        int i;
        float[][] inputs = new float[1][sequence.length];
        for(i=0;i<sequence.length;i++)
        {
            inputs[0][i]=sequence[i];
            //Toast.makeText(this, ""+inputs[0][i], Toast.LENGTH_SHORT).show();
        }
       // float[] inputs = arrayOf( sequence.map { it.toFloat() }.toFloatArray() );
        // Output shape -> ( 1 , 2 ) ( as numClasses = 2 )
        float[][] outputs = new float[1][5];
//        Toast.makeText(this, "Output feature = "+ outputs[0], Toast.LENGTH_SHORT).show();

        tftliteInterpreter.run( inputs , outputs );
        Toast.makeText(this, "Model is running...", Toast.LENGTH_SHORT).show();
        return outputs;
    }

    private void writeToFile(String data, String filename, String result) {
        try {
            String path = getExternalFilesDir("/").getAbsolutePath() +"/Call_Notes"+"/"+filename;

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            writer.append(data);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        //add result to recentfile.txt
        updateRecentFile("Last Call Identified as: "+result,getApplicationContext());
        //Last Call Identified as: SPAM Call
    }

    //Overwrites all the data into recent.txt
    private void updateRecentFile(String test_string, Context context)
    {
        try {
            String path = getExternalFilesDir("/").getAbsolutePath() +"/Recent_Call";

            // Create the folder.
            File folder = new File(path);
            folder.mkdirs();

            // Create the file.
            File file = new File(folder, "recent_predict_string.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(test_string);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}