package com.vb.translator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText textToTranslate;
    Button translate,say;
    Spinner languages;
    TextView result;
    String baseUrl="https://translate.yandex.net/api/v1.5/tr.json/translate?";
    final static String API_KEY="trnsl.1.1.20160222T052905Z.1aba240c0f86103a.fb87ac03d43c739285c7d1106f81e901eec6d46c";
    String url;
    InputStream is=null;
    String json="";
    JSONObject jObj=null;
    TextToSpeech sayit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        textToTranslate=(EditText)findViewById(R.id.textToTranslate);
        translate=(Button)findViewById(R.id.translate);
        languages=(Spinner)findViewById(R.id.spin);
        result=(TextView)findViewById(R.id.result);
        translate.setOnClickListener(this);

        sayit=new TextToSpeech(getBaseContext(),new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = sayit.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");

                    }
                }else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
        say=(Button)findViewById(R.id.say);
        say.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sayit.speak(result.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }
    public void onPause(){
        if(sayit !=null){
            sayit.stop();
            sayit.shutdown();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        String [] param = new String[2];
        param[0]=textToTranslate.getText().toString();
        param[1]=(String)languages.getItemAtPosition(languages.getSelectedItemPosition());
        new Translate().execute(param);
    }

    class Translate extends AsyncTask<String,Void,JSONObject>{
        HttpURLConnection urlConnection=null;
        BufferedReader reader=null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                url = baseUrl + "key=" + API_KEY + "&text=" + params[0] + "&lang=" + params[1];

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
                json=buffer.toString();
            }catch (Exception e) {
                Log.v("reader",e.getMessage());
            }
            try {
                jObj = new JSONObject(json);
            }catch (Exception e){
                Log.v("tag",e.getMessage());
            }
            finally {
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
                JSONArray array=jsonObject.getJSONArray("text");
                String res=array.getString(0);
                result.setText(res);
            }catch (Exception e){
                Log.v("post",e.getMessage());
            }
        }
    }
}
