package com.vb.translator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView textToTranslate;
    Button translate, say, speak;
    Spinner languages;
    TextView result;
    String baseUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    final static String API_KEY = "trnsl.1.1.20160222T052905Z.1aba240c0f86103a.fb87ac03d43c739285c7d1106f81e901eec6d46c";
    final static String key = "key=";
    final static String text = "&text=";
    final static String lang_param = "&lang=";
    String url;
    InputStream is = null;
    String json = "";
    JSONObject jObj = null;
    TextToSpeech sayIt;
    private final int REQ_CODE_SPEECH_INPUT = 100;


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
        textToTranslate = (TextView) findViewById(R.id.textToTranslate);
        translate = (Button) findViewById(R.id.translate);
        languages = (Spinner) findViewById(R.id.spin);

        result = (TextView) findViewById(R.id.result);
        translate.setOnClickListener(this);

        speak = (Button) findViewById(R.id.speechToText);
        speak.setOnClickListener(this);

        say = (Button) findViewById(R.id.say);
        say.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        sayIt = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    int result = sayIt.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");

                    }

                } else {
                    Log.e("TTS", "Initialisation Failed!");
                }
            }
        });
        super.onResume();
    }

    public void onPause() {
        if (sayIt != null) {
            sayIt.stop();
            sayIt.shutdown();
        }
        super.onPause();
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.not_support),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textToTranslate.setText(result.get(0));
                }
                break;
            }

        }
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

        if (v.getId() == R.id.say) {
            sayIt.speak(result.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        } else if (v.getId() == R.id.speechToText) {
            promptSpeechInput();
        } else if (v.getId() == R.id.result) {

            String[] param = new String[2];
            param[0] = textToTranslate.getText().toString();
            param[0] = param[0].replaceAll(" ", "%20");
            param[1] = (String) languages.getItemAtPosition(languages.getSelectedItemPosition());
            new Translate().execute(param);
        }
    }

    class Translate extends AsyncTask<String, Void, JSONObject> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

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
                result.setText(res);
            } catch (Exception e) {
                Log.v("post", e.getMessage());
            }
        }
    }
}
