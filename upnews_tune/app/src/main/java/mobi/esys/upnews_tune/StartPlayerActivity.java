package mobi.esys.upnews_tune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import mobi.esys.constants.UNLConsts;
import mobi.esys.services.UpnewsTunePlay;

public class StartPlayerActivity extends Activity {


    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_start_player);
        Log.d("unTag_StartPlayer", "Start onCreate StartPlayerActivity");

        Button mSelectAlphabet = (Button) findViewById(R.id.bSelectAlphabet);
        Button mSelectRandom = (Button) findViewById(R.id.bSelectRandom);

        mSelectAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UNLApp.setRandomPlaylist(false);
                startPlayService();
            }
        });

        mSelectRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UNLApp.setRandomPlaylist(true);
                startPlayService();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(StartPlayerActivity.this, UpnewsTunePlay.class);
        service.setAction(UNLConsts.ACTION_STOP);
        UpnewsTunePlay.IS_SERVICE_RUNNING = false;
        startService(service);
    }

    private void startPlayService() {
        Intent service = new Intent(StartPlayerActivity.this, UpnewsTunePlay.class);
        service.setAction(UNLConsts.ACTION_PLAY);
        UpnewsTunePlay.IS_SERVICE_RUNNING = true;
        startService(service);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
