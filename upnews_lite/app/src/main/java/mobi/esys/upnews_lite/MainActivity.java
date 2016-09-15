package mobi.esys.upnews_lite;

import android.os.Bundle;
import android.app.Activity;

import org.greenrobot.eventbus.EventBus;

import mobi.esys.events.EventSyncStart;

public class MainActivity extends Activity {

    private final EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.post(new EventSyncStart());
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }
}
