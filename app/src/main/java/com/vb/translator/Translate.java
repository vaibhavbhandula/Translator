package com.vb.translator;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by Vaibhav on 2/23/16.
 */
public class Translate extends AsyncTask<String,Void,JSONObject> {

    AsyncResponse response=null;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String baseUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    final static String API_KEY = "trnsl.1.1.20160222T052905Z.1aba240c0f86103a.fb87ac03d43c739285c7d1106f81e901eec6d46c";
    final static String key = "key=";
    final static String text = "&text=";
    final static String lang_param = "&lang=";
    String url;
    InputStream is = null;
    String json = "";
    JSONObject jObj = null;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            String lang = Locale.getDefault().getLanguage();
            Log.v("lang", lang);
            url = baseUrl + key + API_KEY + text + params[0] + lang_param + lang + "-" + params[1];

            URL ur = new URL(url);

            urlConnection = (HttpURLConnection) ur.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            is = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();
            if (is == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            json = buffer.toString();
        } catch (Exception e) {
            Log.v("reader", e.getMessage());
        }
        try {
            jObj = new JSONObject(json);
        } catch (Exception e) {
            Log.v("tag", e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("error", "Error closing stream", e);
                }
            }
        }
        return jObj;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        try {
            JSONArray array = jsonObject.getJSONArray("text");
            String res = array.getString(0);
            response.processFinished(res);
        } catch (Exception e) {
            Log.v("post", e.getMessage());
        }
    }
}

