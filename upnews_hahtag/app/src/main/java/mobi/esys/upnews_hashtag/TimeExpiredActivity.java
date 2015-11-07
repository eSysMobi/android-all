package mobi.esys.upnews_hashtag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;

import mobi.esys.consts.ISConsts;


public class TimeExpiredActivity extends Activity {
    private transient long installDateMillis;
    private transient Date installDate;
    private transient SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inappbilling);
        prefs = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);
        installDateMillis = prefs.getLong("installedTime", 0);
        Calendar today = Calendar.getInstance();


        if (installDateMillis == 0) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("installedTime", today.getTimeInMillis());
            editor.commit();
            installDate = today.getTime();
        } else {
            installDate = new Date(installDateMillis);
        }

        DateTime initDT = new DateTime(installDate);
        DateTime now = new DateTime();
        Period sinceInstall = new Period(initDT, now, PeriodType.dayTime()).normalizedStandard(PeriodType.dayTime());

        Log.d("time", sinceInstall.toString());

        int daysBetween = Math.abs(sinceInstall.getDays());

        if (daysBetween <= 34) {
            startActivity(new Intent(TimeExpiredActivity.this, InstaLoginActivity.class));
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(TimeExpiredActivity.this);
            builder.setTitle("Attention!")
                    .setMessage("Test using of the app is over. \n" +
                            "Download new App from Google.Play or connect with the developer.")
                    .setIcon(R.drawable.ic_launcher)
                    .setCancelable(false)
                    .setNegativeButton("Exit",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            }).setPositiveButton("Go to Play Market", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://play.google.com/store/apps/details?id=mobi.esys.upnewshashtag"));
                    startActivity(i);
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}
