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
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.util.*;

public class PredictActivity extends AppCompatActivity {
    Button predbtn;
    TextView restxt;
    Interpreter tftliteInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        int INPUT_MAXLEN = 1000;
        PredictClassifier classifier = new PredictClassifier(this , "android_word_dict.json" , INPUT_MAXLEN);

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
                String testtext = "hello bye";
                testtext = testtext.toLowerCase().trim();

                int[] tokenizedmsg = classifier.tokenize(testtext);
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

        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("recordaimodel.tflite");
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
        return outputs;
    }

}