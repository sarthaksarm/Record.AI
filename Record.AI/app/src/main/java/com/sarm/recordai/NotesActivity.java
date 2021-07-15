package com.sarm.recordai;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {
    ListView listFiles;
    ArrayList<String> myList;
    ArrayAdapter adapter;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        listFiles = (ListView) findViewById(R.id.list);
        myList = new ArrayList<>();
        listFileContent(getApplicationContext());

    }

    // Uses Global listview variable
    void listFileContent(Context context) {
        String path = getExternalFilesDir("/").getAbsolutePath() + "/Call_Notes/";
        Toast.makeText(context, "" + path, Toast.LENGTH_LONG).show();
        if (checkPermission()) {
            File dir = new File(path);
            if (dir.exists()) {
                Log.d("path", dir.toString());
                final File list[] = dir.listFiles();
                for (int i = 0; i < list.length; i++) {
                    myList.add(list[i].getName());
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(NotesActivity.this, android.R.layout.simple_list_item_1, myList);
                listFiles.setAdapter(arrayAdapter);
                listFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent appInfo = new Intent(NotesActivity.this, ViewFile.class);
                        appInfo.putExtra("filepath",""+list[position]);
                        appInfo.putExtra("filename",list[position].getName());
                        startActivity(appInfo);
                    }
                }) ;
            }
        } else {
            requestPermission(); // Code for permission
        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(NotesActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(NotesActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(NotesActivity.this, "Write External Storage permission allows us to read  files. " +
                    "Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(NotesActivity.this, new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
}


