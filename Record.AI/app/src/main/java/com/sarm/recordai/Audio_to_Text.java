package com.sarm.recordai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Audio_to_Text extends AppCompatActivity {
TextView restxt;
Button b1;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    boolean mIslistening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_to__text);

        restxt = findViewById(R.id.audiototexttxt);
        b1=findViewById(R.id.voicebtn);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(Audio_to_Text.this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.sarm.recordai");


        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mIslistening)
                {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
        super.onDestroy();
    }
    protected class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            //Log.d(TAG, "error = " + error);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
//            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            // matches are the return values of speech recognition engine
            // Use these values for whatever you wish to do
            restxt.setText(""+matches.get(0));
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }
}