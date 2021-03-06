package at.smartshopper.smartshopperapp.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;

import java.sql.SQLException;

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.db.Database;
import at.smartshopper.smartshopperapp.shoppinglist.Member;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "SMASH";
    private static final int RC_SIGN_IN = 1;
    SignInButton button;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private Database db;
    //Für Double Back press to exit
    private boolean doubleBackToExitPressedOnce = false;

    public void getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        String sl_idToGo = "";
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            String invite = deepLink.toString();
                            invite = invite.replaceAll("https://smartshopper.cf/invite/", "");
                            invite = invite.replaceAll(".slid=.*", "");
                            sl_idToGo = deepLink.getQueryParameter("slid");
                            Log.d("SmartShopper", deepLink.toString());
                            if (null != mAuth.getCurrentUser()) {
                                goDash(sl_idToGo, invite);
                            }
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }

                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Wechselt zu der Dash Activity
     */
    private void goDash(String sl_idToGo, String inviteToAdd) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();
                        Member user;
                        String name = null;
                        String picture = null;
                        String email = null;

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        try {
                            if (!db.checkIfUserExists(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                name = userFirebase.getDisplayName();
                                picture = userFirebase.getPhotoUrl().toString();
                                email = userFirebase.getEmail();
                                db.createUser(userFirebase.getUid(), token, name, picture, email);

                            } else {
                                try {
                                    user = db.getUser(userFirebase.getUid());
                                    name = user.getName();
                                    picture = user.getPic();
                                    email = user.getEmail();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                db.updateUser(userFirebase.getUid(), token, name, picture, email);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                });


        Intent intent = new Intent(this, Dash.class);
        if (sl_idToGo != null) {
            intent.putExtra("sl_idToGo", sl_idToGo);
            intent.putExtra("inviteToAdd", inviteToAdd);
        }
        finish();
        startActivity(intent);
    }

    /**
     * Loggt den User per Email ein
     *
     * @param email    Email des Users
     * @param password Passwort des Users
     */
    private void signInEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            goDash(null, null);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    /**
     * Erstellt einen Account mit Email und Passwort
     *
     * @param email    Email des neuen Users
     * @param password Passwort des neuen Users
     */
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
        mAuth = FirebaseAuth.getInstance();
        db = new Database();

        Button register = (Button) findViewById(R.id.registrierenBtn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Register.class);
                finish();
                startActivity(intent);
            }
        });


        final Button loginEmailBtn = (Button) findViewById(R.id.loginEmailBtn);
        loginEmailBtn.setEnabled(false);
        final TextView email = (TextView) findViewById(R.id.email);
        final TextView passwort = (TextView) findViewById(R.id.password);
        button = (SignInButton) findViewById(R.id.loginGoogleBtn);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!email.getText().toString().isEmpty() && !passwort.getText().toString().isEmpty()) {
                    loginEmailBtn.setEnabled(true);
                } else {
                    loginEmailBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passwort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!email.getText().toString().isEmpty() && !passwort.getText().toString().isEmpty()) {
                    loginEmailBtn.setEnabled(true);
                } else {
                    loginEmailBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailstr = email.getText().toString();
                String passwortstr = passwort.getText().toString();
                signInEmail(emailstr, passwortstr);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    /**
     * Loggt den User ein. Bei erfolg wird der Aufruf zur Dash Activity getätigt
     *
     * @param acct Der Google Account, welcher eingelogt werden soll
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            goDash(null, null);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.email), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    /**
     * Prüft ob der User bereits eingelogt ist. Wenn ja, wird er auf die Dash Activity weitergeleitet
     */
    @Override
    public void onStart() {
        super.onStart();

        getDynamicLink();

        if (null != mAuth.getCurrentUser()) {
            goDash(null, null);
        }

    }

    /**
     * 2 Mal Zurück Drücken um die App zu schließen
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
