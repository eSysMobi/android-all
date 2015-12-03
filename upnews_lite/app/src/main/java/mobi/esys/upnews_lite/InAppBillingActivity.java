package mobi.esys.upnews_lite;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
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

import mobi.esys.constants.UNLConsts;
import mobi.esys.tasks.GetProductInfoTask;

public class InAppBillingActivity extends Activity {
    private transient static final int BILL_INTENT_CODE = 1001;
    private transient IInAppBillingService billingService;
    private transient ServiceConnection billingServiceConn;
    private transient boolean buyOK = false;
    private transient boolean permOK = false;

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
                                getPackageName(), "upnews_lite_one_month", "subs", "");
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

        checkPermision();

        if (BuildConfig.DEBUG) {
            Log.d("buy", "It's debug version. Not need to buy!");
            buyOK = true;
        } else {
            bindService(new Intent(
                            "com.android.vending.billing.InAppBillingService.BIND").setPackage("com.android.vending"),
                    billingServiceConn, BIND_AUTO_CREATE);
        }
        allOK();
    }


    void allOK() {
        if (buyOK && permOK) {
            startActivity(new Intent(InAppBillingActivity.this, DriveAuthActivity.class));
            finish();
        }
    }

    void checkPermision() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        UNLConsts.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                permOK = true;
            }
        } else {
            permOK = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == UNLConsts.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permOK = true;
                allOK();
            }
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
