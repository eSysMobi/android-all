package mobi.esys.upnews_tune;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import mobi.esys.constants.UNLConsts;
import mobi.esys.services.UpnewsTunePlay;

public class StartPlayerActivity extends Activity {

    //autostart
    private transient final Handler startHandler = new Handler();
    private transient Runnable loadOldAccName = null;
    private transient SharedPreferences preferences;
    private transient SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_start_player);
        Log.d("unTag_StartPlayer", "Start onCreate StartPlayerActivity");

        Button mSelectAlphabet = (Button) findViewById(R.id.bSelectAlphabet);
        Button mSelectRandom = (Button) findViewById(R.id.bSelectRandom);

        preferences = getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        editor = preferences.edit();

        mSelectAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("RandomPlaylist",false);
                editor.apply();
                startPlayService();
            }
        });

        mSelectRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("RandomPlaylist",true);
                editor.apply();
                startPlayService();
            }
        });

        if(UNLApp.isFirstStart()){loadOldAccName = new Runnable() {
                @Override
                public void run() {
                    startPlayService();
                }
            };
            startHandler.postDelayed(loadOldAccName, UNLConsts.START_OLD_PROFILE_DELAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UNLApp.getIsPlaying()) {
            Intent service = new Intent(StartPlayerActivity.this, UpnewsTunePlay.class);
            service.setAction(UNLConsts.ACTION_STOP);
            startService(service);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        startHandler.removeCallbacks(loadOldAccName);
    }

    private void startPlayService() {
        if (!UNLApp.getIsPlaying()) {
            Intent service = new Intent(StartPlayerActivity.this, UpnewsTunePlay.class);
            service.setAction(UNLConsts.ACTION_PLAY);
            startService(service);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
