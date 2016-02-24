package com.vb.translator;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {
    TextView textToTranslate;
    Button translate, say, speak;
    Spinner languages;
    TextView result;
    TextToSpeech sayIt;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String[] param;
    String textToSpeak = "";

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
        textToTranslate.setVisibility(View.GONE);
        speak.setVisibility(View.GONE);
        say.setVisibility(View.GONE);
        param = new String[2];

    }


    void setUpTTS() {
        sayIt = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e("tts", "Inside INIT");
                if (status == TextToSpeech.SUCCESS) {

                    int result = sayIt.setLanguage(Locale.getDefault());

                    Log.e("tts result", textToSpeak);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");

                    }

                    speakUp();
                } else {
                    Log.e("TTS", "Initialisation Failed!");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        setUpTTS();
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
                    param[0] = result.get(0);
                    param[0] = param[0].replaceAll(" ", "%20");
                    param[1] = (String) languages.getItemAtPosition(languages.getSelectedItemPosition());
                    Translate translation = new Translate();
                    translation.response = this;
                    translation.execute(param);
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
        promptSpeechInput();
    }


    @Override
    public void processFinished(String result) {
        this.result.setText(result);
        textToSpeak = result;
        this.result.setVisibility(View.GONE);
        speakUp();
    }

    void speakUp() {
        sayIt.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
}
