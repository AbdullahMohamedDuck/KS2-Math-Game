package com.game.ks2mathgame.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;


import androidx.annotation.NonNull;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.game.ks2mathgame.MainActivity;
import com.game.ks2mathgame.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class Signin extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private EditText email_input, psd_input;
    private Button signin_btn;

    FirebaseDatabase db;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //initialize views
        email_input = findViewById(R.id.email_input);
        psd_input = findViewById(R.id.psd_input);
        signin_btn = findViewById(R.id.signin_btn);

        db = FirebaseDatabase.getInstance();
        ref = db.getReference("users");

        SignInButton btn = findViewById(R.id.google_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1027045263173-q4d5dh70qanrg5fv1kr2o9eqtkn99862.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //create button listeners
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first validate if input fields are ! empty
                String email = email_input.getText().toString();
                String pass = psd_input.getText().toString();
                if(TextUtils.isEmpty(email) || !email.contains("@")){
                    //validate the email
                    Toast.makeText(getApplicationContext(), "Invalid Email!", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(pass)){
                    //validate password
                    Toast.makeText(getApplicationContext(), "Enter you password!", Toast.LENGTH_SHORT).show();
                }else{
                    //if validated sign in
                    signin_btn.setEnabled(false); //set button disabled till sign in
                    signin_btn.setText("Logging in...");
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                String name = "";
                                try {
                                    name  = user.getDisplayName();
                                }catch (NullPointerException e){
                                    Toast.makeText(getApplicationContext(), "Name not set!", Toast.LENGTH_SHORT).show();
                                }

                                String email = user.getEmail();
                                //save the logged in user to shared preferences, to be used again
                                saveUser(new User(name, email));
                                //Toast.makeText(getApplicationContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Wrong Email or Password!", Toast.LENGTH_SHORT).show();
                                //show the button so that users can login with correct credentials
                                signin_btn.setText("Login");
                                signin_btn.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.signup_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateAccount.class));
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String name = account.getDisplayName();
                String email = account.getEmail();

                //save the logged in user to shared preferences, to be used again
                saveUser(new User(name, email));

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){
//        String x = "Sign in OK";
//        if(user == null)
//            x = "Not Signed In!";
        if (user == null)
            return;

        setContentView(R.layout.profile);
        EditText profile_name = findViewById(R.id.profile_name);
        EditText profile_email = findViewById(R.id.profile_email);
        EditText profile_playerID = findViewById(R.id.profile_playerID);

        profile_name.setEnabled(false);
        profile_email.setEnabled(false);
        profile_playerID.setEnabled(false);

        profile_name.setText(user.getDisplayName());
        profile_email.setText(user.getEmail());
        profile_playerID.setText(getUserID(user.getEmail()));
        //Toast.makeText(getApplicationContext(), x, Toast.LENGTH_SHORT).show();

        //set signout click listner
        TextView signout_link = findViewById(R.id.signout_link);
        signout_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                //clear the offline app data for this user with signout
                getSharedPreferences("userInfo", MODE_PRIVATE).edit().clear().commit();
                startActivity(new Intent(getApplicationContext(), Signin.class));
                finishAffinity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public static String getUserID(String email){
        //retrive the id from email
        //this function gets the email part before '@' and removes all special characters form it and replace them with '-', then returns the result
        return email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "-");
    }

    private void saveUser(User user){

        //update the progress in firebase database, so that if user login again, it starts from the previously saved state
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference("users");
        DatabaseReference idRef = userRef.child(getUserID(user.email));

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        spEditor.putString("email", user.email);

        //now get all the saved levels from database for this user
        for (int i = 0; i < MainActivity.topics.length; i++){
            int finalI = i;
            //read from database using listeners
            DatabaseReference topicRef = idRef.child("topic"+finalI);
            topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer lvl = snapshot.getValue(Integer.class);
                    if (lvl != null){ //if user is login for the first time, then it may return null so check it
                        spEditor.putInt("topic"+ finalI, lvl);
                        spEditor.apply();  //apply update in background
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        spEditor.apply();
        //user email saved to shared preferences

        idRef.child("email").setValue(user.email);
        idRef.child("name").setValue(user.name);

        //schedule the timer to start activity after some seconds so that app can fetch the progress
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finishAffinity();
            }
        }, 3000);
        Toast.makeText(getApplicationContext(), "Sign In Successful! Wait while we fetch your progress", Toast.LENGTH_LONG).show();
    }

}