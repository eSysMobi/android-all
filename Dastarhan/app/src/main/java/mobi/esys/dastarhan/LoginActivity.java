package mobi.esys.dastarhan;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "dtagLoginActivity";
    private TextView mtvLogin;
    private TextView mtvSignUp;
    private TextView mtvRemind;
    private EditText metEmail;
    private EditText metPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mtvLogin = (TextView) findViewById(R.id.tvLogin);
        metEmail = (EditText) findViewById(R.id.etEmail);
        metPass = (EditText) findViewById(R.id.etPass);
        mtvSignUp = (TextView) findViewById(R.id.tvSignUp);
        mtvRemind = (TextView) findViewById(R.id.tvRemind);

        metEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG,"next");
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metPass.setText("");
                    metPass.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG,"done");
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                        Intent intent = new Intent(LoginActivity.this, Restorans.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please input email and password", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        mtvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                    Intent intent = new Intent(LoginActivity.this, Restorans.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please input email and password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mtvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Need start registration activity", Toast.LENGTH_SHORT).show();
            }
        });

        mtvRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Need start activity for remind password",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
