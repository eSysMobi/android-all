package mobi.esys.upnews_tube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditYouTubeChannelName extends Activity {

    private EditText etEditYouTubeChannelName;
    private Button bEditYouTubeChannelName;
    private String lastChannelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_you_tube_channel_name);

        etEditYouTubeChannelName = (EditText) findViewById(R.id.etEditYouTubeChannelName);
        bEditYouTubeChannelName = (Button) findViewById(R.id.bEditYouTubeChannelName);

        lastChannelName = getIntent().getStringExtra("lastChannelName");
        if (!lastChannelName.isEmpty()) {
            etEditYouTubeChannelName.setText(lastChannelName);
        }

        bEditYouTubeChannelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        etEditYouTubeChannelName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                back();
                return false;
            }
        });
    }


    private void back() {
        String name = etEditYouTubeChannelName.getText().toString();
        Log.d("unTag_EditYouTubeCnllN", "New youtube channel name is " + name);
        if (name.isEmpty()) {   //   || name.equals(lastChannelName)
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("name", etEditYouTubeChannelName.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
