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
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import mobi.esys.dastarhan.tasks.Authorize;
import mobi.esys.dastarhan.tasks.CallbackAuth;
import mobi.esys.dastarhan.tasks.SignUp;

public class LoginActivity extends AppCompatActivity implements CallbackAuth {

    private static final String TAG = "dtagLoginActivity";
    private SharedPreferences prefs;

    private Button mbLogin;
    private TextView mtvSignUp;
    private TextView mtvRemind;
    private EditText metEmail;
    private EditText metPass;

    private Button mbLoginRememberYes;
    private Button mbLoginRememberNo;

    private boolean hasText1 = false;
    private boolean hasText2 = false;
    private boolean regMode = false;

    private int result = RESULT_CANCELED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        prefs = getApplicationContext().getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);

        mbLogin = (Button) findViewById(R.id.bLoginButton);
        metEmail = (EditText) findViewById(R.id.etEmail);
        metPass = (EditText) findViewById(R.id.etPass);
        mtvSignUp = (TextView) findViewById(R.id.tvSignUp);
        mtvRemind = (TextView) findViewById(R.id.tvRemind);

        mbLoginRememberYes = (Button) findViewById(R.id.bLoginRememberYes);
        mbLoginRememberNo = (Button) findViewById(R.id.bLoginRememberNo);


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
                    if (regMode) {
                        mbLogin.setText(getResources().getString(R.string.registration));
                    } else {
                        mbLogin.setText(getResources().getString(R.string.login));
                    }
                } else {
                    mbLogin.setText(getResources().getString(R.string.skip));
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
                //nothing here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing here
            }

            @Override
            public void afterTextChanged(Editable s) {
                hasText2 = s.toString().length() > 0;
                if (hasText1 || hasText2) {
                    if (regMode) {
                        mbLogin.setText(getResources().getString(R.string.registration));
                    } else {
                        mbLogin.setText(getResources().getString(R.string.login));
                    }
                } else {
                    mbLogin.setText(getResources().getString(R.string.skip));
                }
            }
        });

        metPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "done");
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                        if (regMode) {
                            registration(metEmail.getText().toString(), metPass.getText().toString());
                        } else {
                            authorize(metEmail.getText().toString(), metPass.getText().toString());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please input email and password", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
                return false;
            }
        });

        mbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regMode) {
                    if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                        registration(metEmail.getText().toString(), metPass.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.enter_or_skip, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!metEmail.getText().toString().isEmpty() || !metPass.getText().toString().isEmpty()) {
                        if (!metEmail.getText().toString().isEmpty() && !metPass.getText().toString().isEmpty()) {
                            authorize(metEmail.getText().toString(), metPass.getText().toString());
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.enter_or_skip, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        result = RESULT_OK;
                        returnResult();
                    }
                }
            }
        });

        mtvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegMode();
            }
        });

        mtvRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Coming soon...", Toast.LENGTH_SHORT).show();
            }
        });

        mbLoginRememberYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.PREF_SAVED_AUTH_IS_PERSIST, true);
                editor.apply();
                result = RESULT_OK;
                returnResult();
            }
        });

        mbLoginRememberNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.PREF_SAVED_AUTH_IS_PERSIST, false);
                editor.apply();
                result = RESULT_OK;
                returnResult();
            }
        });
    }

    private void goToRegMode() {
        regMode = true;
        metEmail.setText("");
        metPass.setText("");
        mbLogin.setText(R.string.registration);
        mtvSignUp.setVisibility(View.GONE);
        mtvRemind.setVisibility(View.GONE);
    }

    private void goToLoginMode() {
        regMode = false;
        metEmail.setText("");
        metPass.setText("");
        mbLogin.setText(R.string.skip);
        mtvSignUp.setVisibility(View.VISIBLE);
        mtvRemind.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (regMode) {
            goToLoginMode();
        } else {
            returnResult();
        }
    }

    private void authorize(String email, String pass) {
        Authorize authorizeTask = new Authorize(this);
        authorizeTask.execute(email, pass);
    }

    private void registration(String email, String pass) {
        SignUp signUpTask = new SignUp(this);
        signUpTask.execute(email, pass);
    }

    @Override
    public void onPrepared() {
        lockUI();
    }

    @Override
    public void onSuccessAuth(String authToken, int userID) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_SAVED_LOGIN, metEmail.getText().toString());
        editor.putString(Constants.PREF_SAVED_PASS, metPass.getText().toString());
        editor.putString(Constants.PREF_SAVED_AUTH_TOKEN, authToken);
        editor.putInt(Constants.PREF_SAVED_USER_ID, userID);
        editor.apply();

        result = RESULT_OK;
        startAnimSave();
        unLockUI();
    }

    @Override
    public void onSuccessSighUp(String email, String pass) {
        authorize(email, pass);
        regMode = false;
    }

    @Override
    public void onFail(int errorCode) {
        result = errorCode;
        switch (errorCode) {
            case Constants.RESULT_CODE_NO_INET:
                Toast.makeText(this, R.string.no_inet, Toast.LENGTH_SHORT).show();
                break;
            case Constants.RESULT_CODE_NO_USER_EXISTS:
                goToLoginMode();
                Toast.makeText(this, R.string.user_not_exists, Toast.LENGTH_SHORT).show();
                break;
            case Constants.RESULT_CODE_USER_ALREADY_EXISTS:
                goToRegMode();
                Toast.makeText(this, R.string.user_already_exists, Toast.LENGTH_SHORT).show();
                break;
            case Constants.RESULT_CODE_AUTH_ERROR:
                goToLoginMode();
                Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
                break;
            case Constants.RESULT_CODE_SIGNUP_ERROR:
                Toast.makeText(this, R.string.signup_error, Toast.LENGTH_SHORT).show();
                goToRegMode();
                break;
        }
        unLockUI();
    }

    private void lockUI() {
        metEmail.setEnabled(false);
        metPass.setEnabled(false);
        mbLogin.setEnabled(false);
        mtvSignUp.setEnabled(false);
        mtvRemind.setEnabled(false);
    }

    private void unLockUI() {
        metEmail.setEnabled(true);
        metPass.setEnabled(true);
        mbLogin.setEnabled(true);
        mtvSignUp.setEnabled(true);
        mtvRemind.setEnabled(true);
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
        fade_2.setStartOffset(200);
        fade_2.setAnimationListener(new animList(mflPassword, metPass, true));
        mflPassword.startAnimation(fade_2);

        Animation fade_3 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_3.setFillAfter(true);
        fade_3.setStartOffset(400);
        //fade_3.setAnimationListener(new animList(mbLogin, mbLogin, true));
        fade_3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mbLogin.setVisibility(View.INVISIBLE);
                mbLogin.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mbLogin.startAnimation(fade_3);

        Animation fade_4 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_4.setFillAfter(true);
        fade_4.setStartOffset(600);
        fade_4.setAnimationListener(new animList(mtvSignUp, mtvSignUp, true));
        mtvSignUp.startAnimation(fade_4);

        Animation fade_5 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_5.setFillAfter(true);
        fade_5.setStartOffset(600);
        fade_5.setAnimationListener(new animList(mtvRemind, mtvRemind, true));
        mtvRemind.startAnimation(fade_5);

        TextView mtvRemember = (TextView) findViewById(R.id.tvRemember);
        Animation fade_6_0 = new AlphaAnimation(1, 0);
        fade_6_0.setDuration(1);
        fade_6_0.setFillAfter(true);
        fade_6_0.setStartOffset(1200);
        mtvRemember.startAnimation(fade_6_0);
        Animation fade_6_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_6_1.setFillAfter(true);
        fade_6_1.setStartOffset(1202);
        fade_6_1.setAnimationListener(new animList(mtvRemember, mtvRemember, false));
        mtvRemember.startAnimation(fade_6_1);

        Animation fade_7_0 = new AlphaAnimation(1, 0);
        fade_7_0.setDuration(1);
        fade_7_0.setFillAfter(true);
        fade_7_0.setStartOffset(1400);
        mbLoginRememberYes.startAnimation(fade_7_0);
        Animation fade_7_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_7_1.setFillAfter(true);
        fade_7_1.setStartOffset(1402);
        fade_7_1.setAnimationListener(new animList(mbLoginRememberYes, mbLoginRememberYes, false));
        mbLoginRememberYes.startAnimation(fade_7_1);

        Animation fade_8_0 = new AlphaAnimation(1, 0);
        fade_8_0.setDuration(1);
        fade_8_0.setFillAfter(true);
        fade_8_0.setStartOffset(1400);
        mbLoginRememberNo.startAnimation(fade_8_0);
        Animation fade_8_1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_8_1.setFillAfter(true);
        fade_8_1.setStartOffset(1402);
        fade_8_1.setAnimationListener(new animList(mbLoginRememberNo, mbLoginRememberNo, false));
        mbLoginRememberNo.startAnimation(fade_8_1);
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
            if (hide) {
                v2.setEnabled(false);
            } else {
                v2.setEnabled(true);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (hide) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //nothing here
        }
    }

    private void returnResult() {
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

}
