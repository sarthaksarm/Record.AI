package com.sarm.recordai.transcriptcall;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sarm.recordai.MainActivity;
import com.sarm.recordai.R;
import com.sarm.recordai.predictcall.PredictActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class TranscribeMainActivity extends AppCompatActivity {
    static int mic = 0;
    static RecyclerView messages;
    private RecyclerView.Adapter adapter;
    static ArrayList<Message> mess;
    static ArrayList<String> mess_caller;
    static String number_speaker = "+918095030481";
    static String number_caller = "+918318940391";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribemain);
        checkPermission();

        mess = initMessages();
        mess_caller = new ArrayList<>();

        messages = (RecyclerView) findViewById(R.id.messages);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        messages.setLayoutManager(mLayoutManager);

        adapter = new MessageAdapter(mess);
        messages.setAdapter(adapter);
        //messages.scrollToPosition(mess.size() - 1);

        //final EditText editText = findViewById(R.id.editText);
        //final EditText editText2 = findViewById(R.id.editText2);
        String languagePref = "en";
        final Bundle bundle=getIntent().getExtras();
        String data = "";
        if (bundle != null) {
            data=bundle.get("data").toString();
            if(bundle.containsKey("number"))
                number_caller = bundle.get("number").toString();
        }

        //Toast.makeText(getApplicationContext(),data,Toast.LENGTH_LONG).show();
        if(data.equals("Hindi")){
            languagePref="hi";
        }
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
//                Locale.getDefault());

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePref);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePref);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePref);
       // mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);


        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                //Toast.makeText(TranscribeMainActivity.this, "Ready", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
               // Toast.makeText(TranscribeMainActivity.this, "Begin speech", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRmsChanged(float v) {
                //Toast.makeText(TranscribeMainActivity.this, "Rms changed", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onBufferReceived(byte[] bytes) {
               // Toast.makeText(TranscribeMainActivity.this, "speech received", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onEndOfSpeech() {
                //Toast.makeText(TranscribeMainActivity.this, "End speech", Toast.LENGTH_SHORT).show();

               //mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            }

            @Override
            public void onError(int i) {
               // Toast.makeText(TranscribeMainActivity.this, "Some error is occurring, i = "+i, Toast.LENGTH_SHORT).show();
                onResults(bundle);
            }

            @Override
            public void onResults(Bundle bundle) {
                //mSpeechRecognizer.stopListening();
                //getting all the matches

                try{
                    ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    //displaying the first match
                    if (matches != null){
                        if(mic  == 0){
                            //editText2.setText(matches.get(0));
                            if(!mess.get(mess.size()-1).equals(new Message(matches.get(0),1)))
                                mess.add(new Message(matches.get(0),1));

                            //1-Calling side (we)
                            messages.scrollToPosition(mess.size() - 1);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(TranscribeMainActivity.this, "Here-1", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            //editText.setText(matches.get(0));
                            if(!mess.get(mess.size()-1).equals(new Message(matches.get(0),2)))
                            {
                                mess.add(new Message(matches.get(0),2));
                                mess_caller.add(matches.get(0));    //added other person's saying.
                            }

                            //2-caller side (other person)
                            messages.scrollToPosition(mess.size() - 1);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(TranscribeMainActivity.this, "Here-2", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
                catch (Exception e)
                {
                        if(mic  == 0){
                            //editText2.setText(matches.get(0));
                            if(!mess.get(mess.size()-1).equals(new Message(".",1)))
                                mess.add(new Message(".",1));

                            //1-Calling side (we)
                            messages.scrollToPosition(mess.size() - 1);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(TranscribeMainActivity.this, "Here-1", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            //editText.setText(matches.get(0));
                            if(!mess.get(mess.size()-1).equals(new Message(".",2)))
                            {
                                mess.add(new Message(".",2));
                                mess_caller.add(".");    //added other person's saying.
                            }

                            //2-caller side (other person)
                            messages.scrollToPosition(mess.size() - 1);
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(TranscribeMainActivity.this, "Here-2", Toast.LENGTH_SHORT).show();

                        }

                    }
                }

            @Override
            public void onPartialResults(Bundle bundle) {
//                //getting all the matches
//                ArrayList<String> matches = bundle
//                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//
//                //displaying the first match
//                if (matches != null){
//                    if(speaker == 1)
//                        editText2.setText(matches.get(0));
//                    else if(mic == 1)
//                        editText.setText(matches.get(0));
//                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        findViewById(R.id.micbtn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mic = 0;
                        //Toast.makeText(TranscribeMainActivity.this, "Action Up Recog ="+mSpeechRecognizer, Toast.LENGTH_LONG).show();

                        mSpeechRecognizer.stopListening();

                        //editText.setHint("You will see input here");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mic = 1;
                        //Toast.makeText(TranscribeMainActivity.this, "Action Down Recog ="+mSpeechRecognizer, Toast.LENGTH_LONG).show();

                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

                        //editText.setText("");
                        //editText.setHint("Listening...");
                        break;
                }
                return false;
            }
        });
        findViewById(R.id.speakerbtn).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:

                        mic = 1;
                        mSpeechRecognizer.stopListening();

                        //editText2.setHint("You will see input here");
                        break;

                    case MotionEvent.ACTION_DOWN:

                        mic = 0;
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

                        //editText2.setText("");
                        //editText2.setHint("Listening...");
                        break;
                }
                return false;
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }
    public void storeChats(View view) throws IOException {
        String email = "xyz@gmail.com";
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user!=null)
//            email = user.getEmail();
        //Toast.makeText(getApplicationContext(),email,Toast.LENGTH_LONG).show();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //getReference(email.substring(0,email.indexOf('@')));

        String caller_data="";

        for(int i=0;i<mess_caller.size();i++)
        {
            caller_data+=mess_caller.get(i);
        }
        String callerId = System.currentTimeMillis()+"";
        String c = "";
        String t = "";
        for (int i = 0; i < mess.size(); i++){
            c+=mess.get(i).getName()+";";
            t+=mess.get(i).getType()+";";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss");
        String format = simpleDateFormat.format(new Date());

        Chat instance = new Chat(format, c, t,number_caller);
        mDatabase.child(number_speaker).child(number_caller).child("Conversation Details").setValue(instance);
        mDatabase.child(number_speaker).child(number_caller).child("Caller_data").setValue(caller_data);

        //save to file

        //file

        String caller_number = number_caller;
        StringTokenizer str = new StringTokenizer(c,";");
        StringTokenizer str_type = new StringTokenizer(t,";");
        ArrayList<String> sen_list = new ArrayList<String>(5);

        String trimmed = str.nextToken(); // Rejects the "Recordings begin here"
        trimmed = str_type.nextToken(); // Rejects "3"
        String number_type="";
        String test_string="";
        String word="";
        while(str.hasMoreTokens())
        {
            number_type = str_type.nextToken();
            if(number_type.equals("1"))
                sen_list.add("You: "+str.nextToken());
            else if(number_type.equals("2")){
                word = str.nextToken();
                test_string+= word+ " ";
                sen_list.add(caller_number+": "+word);
            }
        }
        String data="";
        for (int i = 0; i < sen_list.size(); i++)
            data += sen_list.get(i) + "\n";

        writeToFile(data, getApplicationContext(), caller_number);

        Intent i=new Intent(TranscribeMainActivity.this, PredictActivity.class);
        i.putExtra("Filename",caller_number+".txt");
        i.putExtra("Content",caller_data);
        i.putExtra("Conversations",data);
        Toast.makeText(this, "Conversations recorded successfully..", Toast.LENGTH_SHORT).show();
        startActivity(i);
        finish();
    }

    private void writeToFile(String data, Context context, String callerNumber) {
        try {

            String path = getExternalFilesDir("/").getAbsolutePath() +"/Call_Notes";
            Toast.makeText(context, "File saved at: "+path, Toast.LENGTH_SHORT).show();
            // Create the folder.
            File folder = new File(path);
            folder.mkdirs();

            // Create the file.
            File file = new File(folder, callerNumber+".txt");

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

    }
    private ArrayList<Message> initMessages() {
        ArrayList<Message> list = new ArrayList<>();
        list.add(new Message("Begin the conversation!",3));

        return list;
    }
}

