package com.game.ks2mathgame.account;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.game.ks2mathgame.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
public class CreateAccount extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText name_input, email_input, psd_input, cpsd_input;
    private Button signup_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //initialize views
        name_input = findViewById(R.id.name_input);
        email_input = findViewById(R.id.email_input);
        psd_input = findViewById(R.id.psd_input);
        cpsd_input = findViewById(R.id.cpsd_input);

        signup_btn = findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = name_input.getText().toString();
                String email = email_input.getText().toString();
                String psd = psd_input.getText().toString();
                String cpsd = cpsd_input.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(psd) || TextUtils.isEmpty(cpsd))
                { //check if any field is empty while registering
                    Toast.makeText(getApplicationContext(), "All fields must be filled!", Toast.LENGTH_SHORT).show();
                } else if(!email.contains("@")){
                    //validate email
                    Toast.makeText(getApplicationContext(), "Invalid Email!", Toast.LENGTH_SHORT).show();
                }else if(!psd.equals(cpsd)){
                    //confirm password
                    Toast.makeText(getApplicationContext(), "Passwords Don't Match!", Toast.LENGTH_SHORT).show();
                } else{
                    registerUser(email_input.getText().toString(), psd_input.getText().toString());
                }
            }
        });



        auth = FirebaseAuth.getInstance();

        TextView signin_link = findViewById(R.id.signin_link);
        signin_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();  //sign in and sign up links can create a loop, so finish the previous activity
                startActivity(new Intent(getApplicationContext(), Signin.class));
            }
        });
    }

    private  void registerUser(String email, String pass){
        signup_btn.setEnabled(false);
        signup_btn.setText("Registering...");
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Registered Successfully!", Toast.LENGTH_SHORT).show();
                    //firebase can save email and password at registration time so save user after registration time
                    if (task.getResult().getUser() != null){
                        setName(task.getResult().getUser());
                        FirebaseAuth.getInstance().signOut(); //registering signs in the user, so signout so that user can sign in again
                    }

                    //start the activity
                    startActivity(new Intent(getApplicationContext(), Signin.class));
                }else{
                    Toast.makeText(getApplicationContext(), "Registration Failed!", Toast.LENGTH_SHORT).show();
                    signup_btn.setEnabled(true);
                    signup_btn.setText("Create Account");
                }
            }
        });
    }

    private void setName(FirebaseUser user){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name_input.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });

    }

}