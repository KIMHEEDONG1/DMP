package kr.ac.cu.moai.dcumusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listViewMP3;
    ArrayList<String> mp3files;
    String selectedMP3;
    ListViewMP3Adapter adapter;

    String mp3path = Environment.getExternalStorageDirectory().getPath() + "/Download";

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            loadMP3Files();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMP3Files();
            } else {
                Log.e("DCU_MP", "Permission denied");
            }
        }
    }

    private void loadMP3Files() {
        mp3files = new ArrayList<>();
        File mp3Dir = new File(mp3path);
        if (!mp3Dir.exists()) {
            Log.e("DCU_MP", "Directory does not exist: " + mp3path);
            return;
        }

        File[] files = mp3Dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                Log.i("DCU_MP", filename);
                if (filename.endsWith(".mp3")) {
                    mp3files.add(mp3Dir.getPath() + "/" + filename);
                }
            }
        } else {
            Log.e("DCU_MP", "No files found in directory: " + mp3path);
        }

        Log.i("DCU_MP", mp3files.toString());

        listViewMP3 = findViewById(R.id.listViewMP3);
        adapter = new ListViewMP3Adapter(this, mp3files);
        listViewMP3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewMP3.setAdapter(adapter);
        listViewMP3.setOnItemClickListener((parent, view, position, id) -> {
            selectedMP3 = mp3files.get(position);
            Log.i("DCU_MP", selectedMP3);

            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
            intent.putExtra("mp3", selectedMP3);
            startActivity(intent);
        });
    }
}