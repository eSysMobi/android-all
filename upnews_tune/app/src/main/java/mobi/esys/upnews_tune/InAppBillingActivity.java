package mobi.esys.upnews_tune;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.UNLConsts;
import mobi.esys.tasks.GetProductInfoTask;

public class InAppBillingActivity extends Activity {
    private transient static final int BILL_INTENT_CODE = 1001;
    private transient IInAppBillingService billingService;
    private transient ServiceConnection billingServiceConn;
    private transient boolean buyOK = false;
    private transient boolean permWriteOK = false;
    //    private transient boolean permReadOK = false;
    private transient boolean permAccOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inappbilling);

        billingServiceConn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                billingService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                billingService = IInAppBillingService.Stub.asInterface(service);
                GetProductInfoTask getProductInfoTask = new GetProductInfoTask(
                        InAppBillingActivity.this);
                getProductInfoTask.execute(billingService);
                try {
                    String purchaseData = "";
                    Bundle ownedItems = billingService.getPurchases(3,
                            getPackageName(), "subs", null);
                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> purchaseDataList = ownedItems
                                .getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                        if (purchaseDataList != null) {
                            for (int i = 0; i < purchaseDataList.size(); ++i) {
                                purchaseData = purchaseDataList.get(i);
                                Log.d("info", purchaseData);
                            }
                        }

                    }
                    if (purchaseData.equals("")) {
                        Bundle buyIntentBundle = billingService.getBuyIntent(3,
                                getPackageName(), "upnews_tune_one_month", "subs", "");
                        PendingIntent pendingIntent = buyIntentBundle
                                .getParcelable("BUY_INTENT");

                        if (pendingIntent != null) {
                            startIntentSenderForResult(
                                    pendingIntent.getIntentSender(),
                                    BILL_INTENT_CODE, new Intent(),
                                    0, 0,
                                    0);
                        }
                    } else {
                        buyOK = true;
                        allOK();
                    }

                } catch (RemoteException | IntentSender.SendIntentException ignored) {
                }
            }

        };

        //check version
        Log.w("unTag_InAppBillingAct", "SDK version is " + Build.VERSION.SDK_INT);

        //call checking version
        //checkVersion();

        //call checking permissions
        checkPermision();

        if (BuildConfig.DEBUG) {
            Log.d("buy", "It's debug version. Not need to buy!");
            buyOK = true;
            //allow show toast
            //UNLConsts.ALLOW_TOAST = true;
        } else {
            bindService(new Intent(
                            "com.android.vending.billing.InAppBillingService.BIND").setPackage("com.android.vending"),
                    billingServiceConn, BIND_AUTO_CREATE);
        }
        allOK();
    }

    private void checkVersion() {
        UNLApp mApp = (UNLApp) getApplication();
        int versionCode = BuildConfig.VERSION_CODE;
        SharedPreferences prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);
        if(prefs.getInt("lastAppVersion",0)!=versionCode){
            //clear all saved data
            Log.w("unTag_InAppBillingAct", "Version not matched. Clearing SharedPreferences.");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("lastAppVersion", versionCode);
            editor.putString("accName", "");  //may be not need clear
            editor.putString("lastPlayedFile", "");
            editor.putString("md5sApp", "");
            editor.putString("folderId", "");
            editor.apply();
        }
    }

    void allOK() {
        if (buyOK && permWriteOK && permAccOK) {
            checkVersion();
            startActivity(new Intent(InAppBillingActivity.this, DriveAuthActivity.class));
            finish();
        }
    }

    void checkPermision() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> perm = new ArrayList<>();

            //checking permission WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                perm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                Log.d("unTag_InAppBillingAct ", "WRITE_EXTERNAL_STORAGE permission already granted");
                permWriteOK = true;
            }

            //checking permission GET_ACCOUNTS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS);
                perm.add(Manifest.permission.GET_ACCOUNTS);
            } else {
                Log.d("unTag_InAppBillingAct ", "GET_ACCOUNTS permission already granted");
                permAccOK = true;
            }

            //request permissions
            if (perm.size() > 0) {
                ActivityCompat.requestPermissions(this,
                        perm.toArray(new String[perm.size()]),
                        UNLConsts.PERMISSIONS_REQUEST);
            }

        } else {
            permWriteOK = true;
            permAccOK = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions != null && grantResults != null && requestCode == UNLConsts.PERMISSIONS_REQUEST && grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permWriteOK = true;
                        }
                        break;
                    case Manifest.permission.GET_ACCOUNTS:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permAccOK = true;
                        }
                        break;
                }
            }
            allOK();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingService != null && billingServiceConn != null) {
            unbindService(billingServiceConn);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Log.d("buy", "You have bought the " + sku
                            + ". Excellent choice," + "adventurer!");
                    buyOK = true;
                    allOK();

                } catch (JSONException e) {
                    Log.d("buy", "Failed to parse purchase data.");
                    e.printStackTrace();
                }
            } else {
                finish();
            }
        }
    }
}