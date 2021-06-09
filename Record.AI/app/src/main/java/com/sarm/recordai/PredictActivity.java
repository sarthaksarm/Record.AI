package com.sarm.recordai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Float;
import org.json.JSONException;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;

public class PredictActivity extends AppCompatActivity {
    Button predbtn;
    TextView restxt;
    Interpreter tftliteInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

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
                testtext2 = testtext2.toLowerCase().trim();

                int[] tokenizedmsg = classifier.tokenize(testtext2);
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

                restxt.setText("Identified Call Type: " + result + "\n\n" +
                        "Threat Call = "+output[0][0] +"\n"+
                        "Blackmail Call = "+output[0][1] +"\n"+
                        "Fraud Call = " + output[0][2] + "\n" +
                        "Spam Call = " + output[0][3] + "\n"+
                        "Legitimate Call =" + output[0][4]);

                // model.close();
            }
        });

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

}