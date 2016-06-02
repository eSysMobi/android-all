package mobi.esys.dastarhan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "dtagLoginActivity";
    private transient SharedPreferences prefs;

    private TextView mtvLogin;
    private TextView mtvSignUp;
    private TextView mtvRemind;
    private EditText metEmail;
    private EditText metPass;

    private FrameLayout mflLoginRememberYes;
    private FrameLayout mflLoginRememberNo;

    private boolean hasText1 = false;
    private boolean hasText2 = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

        mtvLogin = (TextView) findViewById(R.id.tvLogin);
        metEmail = (EditText) findViewById(R.id.etEmail);
        metPass = (EditText) findViewById(R.id.etPass);
        mtvSignUp = (TextView) findViewById(R.id.tvSignUp);
        mtvRemind = (TextView) findViewById(R.id.tvRemind);

        mflLoginRememberYes = (FrameLayout) findViewById(R.id.flLoginRememberYes);
        mflLoginRememberNo = (FrameLayout) findViewById(R.id.flLoginRememberNo);


        metEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hasText1 = s.toString().length() > 0;
                if (hasText1 || hasText2) {
                    mtvLogin.setText(getResources().getString(R.string.login));
                } else {
                    mtvLogin.setText(getResources().getString(R.string.skip));
                }
            }
        });

        metEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "next");
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    metPass.setText("");
                    metPass.requestFocus();
                    return true;
                }
                return false;
            }
        });

        metEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hasText2 = s.toString().length() > 0;
                if (hasText1 || hasText2) {
                    mtvLogin.setText(getResources().getString(R.string.login));
                } else {
                    mtvLogin.setText(getResources().getString(R.string.skip));
                }
            }
        });

        metPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "done");
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                        startAnimSave();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please input email and password", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
                return false;
            }
        });

        mtvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!metEmail.getText().toString().isEmpty() || !metPass.getText().toString().isEmpty()) {
                    if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                        startAnimSave();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.enter_or_skip, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    startMainActivity();
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
                Toast.makeText(getApplicationContext(), "Need start activity for remind password", Toast.LENGTH_SHORT).show();
            }
        });

        mflLoginRememberYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        mflLoginRememberNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    private void startAnimSave() {
        FrameLayout mflEmail = (FrameLayout) findViewById(R.id.flEmail);
        Animation fade_1 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_1.setFillAfter(true);
        fade_1.setAnimationListener(new animList(mflEmail, metEmail, true));
        mflEmail.startAnimation(fade_1);

        FrameLayout mflPassword = (FrameLayout) findViewById(R.id.flPassword);
        Animation fade_2 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_2.setFillAfter(true);
        fade_2.setStartOffset(300);
        fade_2.setAnimationListener(new animList(mflPassword, metPass, true));
        mflPassword.startAnimation(fade_2);

        FrameLayout mflLoginButton = (FrameLayout) findViewById(R.id.flLoginButton);
        Animation fade_3 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_3.setFillAfter(true);
        fade_3.setStartOffset(600);
        fade_3.setAnimationListener(new animList(mflLoginButton, mtvLogin, true));
        mflLoginButton.startAnimation(fade_3);

        Animation fade_4 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_4.setFillAfter(true);
        fade_4.setStartOffset(900);
        fade_4.setAnimationListener(new animList(mtvSignUp, mtvSignUp, true));
        mtvSignUp.startAnimation(fade_4);

        Animation fade_5 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_5.setFillAfter(true);
        fade_5.setStartOffset(900);
        fade_5.setAnimationListener(new animList(mtvRemind, mtvRemind, true));
        mtvRemind.startAnimation(fade_5);

        TextView mtvRemember = (TextView) findViewById(R.id.tvRemember);
        Animation fade_6_0 = new AlphaAnimation(1,0);
        fade_6_0.setDuration(1);
        fade_6_0.setFillAfter(true);
        fade_6_0.setStartOffset(1800);
        mtvRemember.startAnimation(fade_6_0);
        Animation fade_6_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_6_1.setFillAfter(true);
        fade_6_1.setStartOffset(1802);
        fade_6_1.setAnimationListener(new animList(mtvRemember, mtvRemember, false));
        mtvRemember.startAnimation(fade_6_1);

        Animation fade_7_0 = new AlphaAnimation(1,0);
        fade_7_0.setDuration(1);
        fade_7_0.setFillAfter(true);
        fade_7_0.setStartOffset(2100);
        mflLoginRememberYes.startAnimation(fade_7_0);
        Animation fade_7_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_7_1.setFillAfter(true);
        fade_7_1.setStartOffset(2102);
        fade_7_1.setAnimationListener(new animList(mflLoginRememberYes, mflLoginRememberYes, false));
        mflLoginRememberYes.startAnimation(fade_7_1);

        Animation fade_8_0 = new AlphaAnimation(1,0);
        fade_8_0.setDuration(1);
        fade_8_0.setFillAfter(true);
        fade_8_0.setStartOffset(2100);
        mflLoginRememberNo.startAnimation(fade_8_0);
        Animation fade_8_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_8_1.setFillAfter(true);
        fade_8_1.setStartOffset(2102);
        fade_8_1.setAnimationListener(new animList(mflLoginRememberNo, mflLoginRememberNo, false));
        mflLoginRememberNo.startAnimation(fade_8_1);

        //startMainActivity();
    }

    private class animList implements Animation.AnimationListener {

        View v;
        View v2;
        boolean hide;

        public animList(View v, View v2, boolean hide) {
            this.v = v;
            this.v2 = v2;
            this.hide = hide;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.d(TAG,"end anim");
            if (hide) {
                v.setVisibility(View.INVISIBLE);
                v2.setEnabled(false);
            } else {
                v.setVisibility(View.VISIBLE);
                v2.setEnabled(true);
            }

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
