package com.pedrycz.app3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class DownloadFileService extends IntentService {
    private static final String DOWNLOAD_ACTION = "com.pedrycz.app3.action.DOWNLOAD_ACTION";
    private static final String DOWNLOAD_LINK = "com.pedrycz.app3.extra.DOWNLOAD_LINK";
    private static final int NOTIFICATION_ID = 1;
    private final ProgressInfo progressInfo = new ProgressInfo();
    private NotificationManager notificationManager;
    public static final String NOTIFICATE = "com.pedrycz.app3.MainActivity";

    public static void runService(Context context, String link){
        Intent intent = new Intent(context, DownloadFileService.class);
        intent.setAction(DOWNLOAD_ACTION);
        intent.putExtra(DOWNLOAD_LINK, link);
        context.startService(intent);
    }

    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotifications();
        startForeground(NOTIFICATION_ID, createNotification());

        if(intent != null) {
            final String action = intent.getAction();

            if(DOWNLOAD_ACTION.equals(action)){
                final String downloadLink = intent.getStringExtra(DOWNLOAD_LINK);
                downloadFile(downloadLink);

                notificationManager.notify(NOTIFICATION_ID, createNotification());
            } else {
                Log.e("DownloadFileService", "Unknown acction");
            }
        }
        Log.d("DownloadFileService", "Done");
    }

    private void downloadFile(String link){
        GetInfoAsync getInfoAsync = new GetInfoAsync();
        try {
            List<String> effect = getInfoAsync.execute(link).get();
            progressInfo.size = Integer.parseInt(effect.get(0));
        } catch (ExecutionException | InterruptedException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
            toast.show();
            progressInfo.status = "Error";
        }
        Log.d("Downloading" , link);
        FileOutputStream fileOutputStream = null;
        InputStream webStream = null;

        try{
            URL url = new URL(link);
            File workingFile = new File(url.getFile());
            File outputFile = new File(Environment.getExternalStorageDirectory() + File.separator + workingFile.getName());
            if(!outputFile.exists()) outputFile.createNewFile();

            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert connection != null;
            DataInputStream reader = new DataInputStream(connection.getInputStream());
            fileOutputStream = new FileOutputStream(outputFile.getPath());
            byte[] buffer = new byte[progressInfo.size];
            int downloaded = reader.read(buffer, 0, progressInfo.size);
            progressInfo.downloaded = downloaded;
            progressInfo.status = "Downloading file";
            while(downloaded != -1){
                fileOutputStream.write(buffer, 0, downloaded);
                downloaded = reader.read(buffer, 0, progressInfo.size);
                progressInfo.downloaded += downloaded;
                if(downloaded != -1){
                    progressInfo.progress = (int)(((float)progressInfo.downloaded / (float) progressInfo.size) * 100);
                    Log.d("Downloading progress", progressInfo.progress + "%");
                    Intent intent = new Intent(NOTIFICATE);
                    intent.putExtra("INFO", progressInfo);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    notificationManager.notify(NOTIFICATION_ID, createNotification());
                }
            }
            progressInfo.status = "Finished";

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(webStream != null){
                try {
                    webStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void prepareNotifications() {
        notificationManager = getSystemService(NotificationManager.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Downloading file";
            String description = "Notification channel";
            NotificationChannel channel = new NotificationChannel("666", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notifyIntent = new Intent(this, MainActivity.class);
//        notifyIntent.putExtra();
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notifyIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "666");
        notificationBuilder.setContentTitle("Downloading file")
                .setProgress(100, progressInfo.progress, false)
                .setContentText("Downloading requested file")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH);

        notificationBuilder.setOngoing(progressInfo.progress < 100);

        return notificationBuilder.build();
    }
}

