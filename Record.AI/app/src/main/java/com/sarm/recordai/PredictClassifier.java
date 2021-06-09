package com.sarm.recordai;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PredictClassifier{
Context context;
String filename;
int maxlen;

    public PredictClassifier(Context context1, String jsonFilename, int inputMaxLen) {
        context = context1;

        // Filename for the exported vocab ( .json )
        filename = jsonFilename;

        // Max length of the input sequence for the given model.
        maxlen = inputMaxLen;
    }

        HashMap<String, Integer> vocabData = null;

        // Load the contents of the vocab ( see assets/word_dict.json )

        String loadJSONFromAsset (String filename)
        {
            BufferedReader reader = null;
            String data = "";
            try {
                reader = new BufferedReader(new InputStreamReader(context.getAssets().open("android_word_dict.json")));

                // do reading, usually loop until end of file reading
                String mLine;
                while ((mLine = reader.readLine()) != null) {
                    //process line
                    data += mLine;
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }
            return data;
        }

        // Tokenize the given sentence
        int[] tokenize (String message){
            List<String> parts = Arrays.asList(message.split(" "));
            int[] tokenizedMessage = new int[parts.size()];
            int i = 0;

            for (i = 0; i < parts.size(); i++) {
                String key = parts.get(i);
                if (vocabData.get(key) == null) {
                    tokenizedMessage[i] = 0;
                } else {
                    tokenizedMessage[i] = vocabData.get(key);
                }
            }
            return tokenizedMessage;
        }

        // Pad the given sequence to maxlen with zeros.
        int[] padSequence ( int[] sequence){
            int maxlen = this.maxlen;
            int[] myarr = new int[maxlen];

            if (sequence.length < maxlen) {
                int i;
                int temp = maxlen - sequence.length;
                for (i = 0; i < temp; i++) {
                    myarr[i] = 0;
                }
                for (i = temp; i < maxlen; i++) {
                    myarr[i] = sequence[i-temp];
                }
            }

            return myarr;
        }

        void loadVocab(String json) throws JSONException {

            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iterator = jsonObject.keys();

            HashMap<String, Integer> data = new HashMap<String, Integer>();

            while (iterator.hasNext()) {
                String key = iterator.next();
                data.put(key, Integer.parseInt(jsonObject.get(key).toString()));   //data[key]=val;
            }
            vocabData = data;
        }
    }