package com.pedrycz.app3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            ProgressInfo info = bundle.getParcelable("INFO");
            TextView downloadedValue = findViewById(R.id.downloadedValue);
            ProgressBar progressBar = findViewById(R.id.progressBar);
            downloadedValue.setText(String.valueOf(info.downloaded));
            progressBar.setProgress(info.progress);
        }
    };
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 111;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 112;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 113;
    private static final int FOREGROUND_SERVICE_REQUEST_CODE = 114;

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(DownloadFileService.NOTIFICATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button infoButton = findViewById(R.id.infoButton);
        Button downloadButton = findViewById(R.id.downloadButton);
        EditText urlInput = findViewById(R.id.urlInput);

        infoButton.setOnClickListener(view -> {
            GetInfoAsync getInfoAsync = new GetInfoAsync();
            try {
                List<String> effect = getInfoAsync.execute(urlInput.getText().toString()).get();
                TextView sizeValue = findViewById(R.id.sizeValue);
                TextView typeValue = findViewById(R.id.typeValue);
                sizeValue.setText(effect.get(0));
                typeValue.setText(effect.get(1));
            } catch (ExecutionException | InterruptedException e) {
                Toast toast = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        downloadButton.setOnClickListener(view -> downloadFile(urlInput.getText().toString()));

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        EditText urlInput = findViewById(R.id.urlInput);
        TextView sizeValue = findViewById(R.id.sizeValue);
        TextView typeValue = findViewById(R.id.typeValue);
        TextView downloadedValue = findViewById(R.id.downloadedValue);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        outState.putString("URL", urlInput.getText().toString());
        outState.putString("SIZE", sizeValue.getText().toString());
        outState.putString("TYPE", typeValue.getText().toString());
        outState.putString("DOWNLOADED", downloadedValue.getText().toString());
        outState.putInt("PROGRESS", progressBar.getProgress());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText urlInput = findViewById(R.id.urlInput);
        TextView sizeValue = findViewById(R.id.sizeValue);
        TextView typeValue = findViewById(R.id.typeValue);
        TextView downloadedValue = findViewById(R.id.downloadedValue);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        urlInput.setText(savedInstanceState.getString("URL"));
        sizeValue.setText(savedInstanceState.getString("SIZE"));
        typeValue.setText(savedInstanceState.getString("TYPE"));
        downloadedValue.setText(savedInstanceState.getString("DOWNLOADED"));
        progressBar.setProgress(savedInstanceState.getInt("PROGRESS"));
    }

    private void downloadFile(String url) {
        requestPermissions();
        DownloadFileService.runService(MainActivity.this, url);
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_REQUEST_CODE);
            }
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        FOREGROUND_SERVICE_REQUEST_CODE);
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                        MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }
    }
}