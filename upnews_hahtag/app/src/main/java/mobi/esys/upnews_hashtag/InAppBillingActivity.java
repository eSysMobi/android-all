package mobi.esys.upnews_hashtag;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
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

import mobi.esys.consts.ISConsts;
import mobi.esys.tasks.GetProductInfoTask;


public class InAppBillingActivity extends Activity {
    private transient static final int BILL_INTENT_CODE = 1001;
    private transient IInAppBillingService billingService;
    private transient ServiceConnection billingServiceConn;
    //variables for checking
    private transient boolean buyOK = false;
    private transient boolean permWriteOK = false;
    private transient boolean permAccOK = false;

    /*
    * The activity for checking permits and purchase applications.
    * If you are not given permission or the user has not purchased the application,
    * then the following activity will not start.
    */

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
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            purchaseData = purchaseDataList.get(i);
                            Log.d("info", purchaseData);
                        }
                    }
                    if (purchaseData.equals("")) {
                        Bundle buyIntentBundle = billingService.getBuyIntent(3,
                                getPackageName(), "upnews_hashtag_one_month", "subs", "");
                        PendingIntent pendingIntent = buyIntentBundle
                                .getParcelable("BUY_INTENT");

                        if (pendingIntent != null) {
                            startIntentSenderForResult(
                                    pendingIntent.getIntentSender(),
                                    BILL_INTENT_CODE, new Intent(),
                                    0, 0, 0);
                        }
                    } else {
                        buyOK = true;
                        allOK();
                    }
                } catch (RemoteException | SendIntentException ignored) {
                }
            }
        };
        checkPermission();

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

    /*
    * If all is well, then launch the application and close this activity.
    */
    void allOK() {
        if (buyOK && permWriteOK && permAccOK) {
            startActivity(new Intent(InAppBillingActivity.this, InstaLoginActivity.class));
            finish();
        }
    }

    /*
    * Check permissions to read and write from an external storage.
    */
    void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> perm = new ArrayList<>();
            //checking permission WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                perm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                Log.d("unTag_InAppBillingAct ", "PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE already granted");
                permWriteOK = true;
            }
            //checking permission GET_ACCOUNTS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS);
                perm.add(Manifest.permission.GET_ACCOUNTS);
            } else {
                Log.d("unTag_InAppBillingAct ", "GET_ACCOUNTS already granted");
                permAccOK = true;
            }
            //request permissions
            if (perm.size() > 0) {
                ActivityCompat.requestPermissions(this,
                        perm.toArray(new String[perm.size()]),
                        ISConsts.globals.PERMISSION_REQUEST_CODE);
            }

        } else {
            permWriteOK = true;
            permAccOK = true;
        }
    }

    /*
    * Processing the query results permissions.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ISConsts.globals.PERMISSION_REQUEST_CODE && grantResults.length > 0) {
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

    /*
    * Processing the results of the purchase request/
     */
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