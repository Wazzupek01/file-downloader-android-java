package com.pedrycz.app3;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class GetInfoAsync extends AsyncTask<String, Integer, List<String>> {
    public GetInfoAsync() {
        super();
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        String size = null;
        String type = null;
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(strings[0]);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            size = String.valueOf(connection.getContentLength());
            type = connection.getContentType();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) connection.disconnect();
        }
        if(size != null && type != null)
            return List.of(size, type);
        return List.of("0", "0");
    }
}
