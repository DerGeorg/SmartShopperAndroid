package at.smartshopper.smartshopperapp.activitys;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import at.smartshopper.smartshopperapp.R;

public class Register extends Activity {

    private FirebaseAuth mAuth;


    private void register(String email, String password, final String name) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Register", "Erfolgreich registriert");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse("http://techfrage.de/upfiles/13547334191038217.png"))
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Register", "Name und bild gesetzt");
                                        Intent intent = new Intent(Register.this, LoginActivity.class);
                                        finish();
                                        startActivity(intent);
                                    }
                                }
                            });

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Register", "Ein fehler ist aufgetreten", task.getException());
                    Toast.makeText(Register.this, "Ein Fehler ist aufgetreten",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ...
// Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        final TextView email = findViewById(R.id.emailregister);
        final TextView passwort = findViewById(R.id.passwordregister);
        final TextView name = findViewById(R.id.nameregister);
        Button back = findViewById(R.id.backLogin);
        final Button register = findViewById(R.id.register);
        register.setEnabled(false);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(email.getText().toString(), passwort.getText().toString(), name.getText().toString());
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!name.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !passwort.getText().toString().isEmpty()) {
                    register.setEnabled(true);
                } else {
                    register.setEnabled(false);
                }
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!name.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !passwort.getText().toString().isEmpty()) {
                    register.setEnabled(true);
                } else {
                    register.setEnabled(false);
                }
            }
        });
        passwort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!name.getText().toString().isEmpty() && !email.getText().toString().isEmpty() && !passwort.getText().toString().isEmpty()) {
                    register.setEnabled(true);
                } else {
                    register.setEnabled(false);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }

}
