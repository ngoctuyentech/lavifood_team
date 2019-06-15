package a1a4w.onhandsme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mEmailInput, mPasswordInput;
    private Button btnLogin,btnEmployeeLogin;
    private Button btnRegister;
    private ProgressDialog mProgressDialog;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String default_trial, default_saleman, loginType,userToken;
    public AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private DatabaseReference refDatabase;
    private String emailBoss,nameBoss,phoneBoss,passBoss;
    private Switch employeeCreate;
    private EditText edtEmailBoss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        initializeScreen();


        refDatabase = FirebaseDatabase.getInstance().getReference();
        /*

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        default_trial = mFirebaseRemoteConfig.getString("default_trial");
        default_saleman = mFirebaseRemoteConfig.getString("default_saleman");

        fetchValue();

         */


    }

    private void initializeScreen() {
        mEmailInput = (EditText) findViewById(R.id.edt_login_email);
        mPasswordInput = (EditText) findViewById(R.id.edt_login_pass);
        btnLogin = (Button)findViewById(R.id.btn_login_signin);

        //btnEmployeeLogin = (Button)findViewById(R.id.btn_login_employee);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                managerLogin();
            }
        });



        /*
        btnEmployeeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                employeeLogin();
            }
        });
         */


    }

    private void managerLogin() {
        String email = mEmailInput.getText().toString().trim();
        final String password = mPasswordInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Nhập Email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Nhập mật mã!", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();


        //authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                mPasswordInput.setError(getString(R.string.minimum_password));
                                hideProgressDialog();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //String userUid = mAuth.getCurrentUser().getUid();
                           // Constants.refDatabase.child("Login").child(userUid).setValue(userUid);
                            //Constants.refUserUid.child("Setting/ShopMan").push().setValue(userToken);
                            hideProgressDialog();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            //intent.putExtra("ShopMan",true);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();

    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void fetchValue() {

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(MainActivity.this, "Fetch Succeeded",Toast.LENGTH_SHORT).show();
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            //Toast.makeText(MainActivity.this, "Fetch Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END fetch_config_with_callback]

    }



}
