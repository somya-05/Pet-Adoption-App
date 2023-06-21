package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Login extends AppCompatActivity {
    private boolean rememberMeChecked = false;
    private SharedPreferences sharedPreferences;
    String type = "";

    private EditText email, password;
    TextView forgot_password, register;
    Button login;
    private FirebaseAuth auth;
    private static final String TAG = "Login";

    private GoogleSignInClient client;
    ImageView google1, facebook;

    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        FacebookSdk.sdkInitialize(getApplicationContext());
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        auth = FirebaseAuth.getInstance();


        //------------------------------------------------------------------------------------------
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        CheckBox rememberMeCheckbox = findViewById(R.id.cb1);
        rememberMeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rememberMeChecked = isChecked;
                sharedPreferences.edit().putBoolean("rememberMeChecked", isChecked).apply();
            }
        });



        //------------------------------------------------------------------------------------------

        //  Login Activity Redirect to MainPage
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_txt = email.getText().toString();
                String password_txt = password.getText().toString();
                type = "email";

                if (TextUtils.isEmpty(email_txt)) {
                    email.setText("Enter Email");
                    email.requestFocus();
                } else if (TextUtils.isEmpty(password_txt)) {
                    email.setText("Enter Password");
                    password.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email_txt).matches()) {
                    email.setError("Enter valid Email id");
                    email.requestFocus();
                } else {
                    loginuser(email_txt, password_txt);
                }

            }
        });


        // Login Activity to Register Activity
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        //Google SIGN IN

        google1 = findViewById(R.id.google1);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options);
        google1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = client.getSignInIntent();
                startActivityForResult(intent, 1234);
            }
        });

        // Facebook Signin
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        Profile profile = Profile.getCurrentProfile();

                        // Fetch the name and email using GraphRequest
                        GraphRequest request = GraphRequest.newMeRequest(
                                accessToken,
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
//                                        try {
//                                            String name = object.getString("name");
//                                            String email = object.getString("email");
//
//                                            // Pass the name and email to the next activity
//                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                            Bundle bundle = new Bundle();
//                                            bundle.putString("type", "facebook");
//                                            bundle.putString("name", name);
//                                            bundle.putString("email", email);
//                                            intent.putExtras(bundle);
//                                            startActivity(intent);
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });
        facebook = findViewById(R.id.facebook);

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("public_profile"));

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        //Google
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credentials = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credentials)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String name = user.getDisplayName();
                                        String email = user.getEmail();

                                        // Use the name and email as needed

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("type", "google");
                                        bundle.putString("name", name);  // Pass the name to the next activity
                                        bundle.putString("email", email);  // Pass the email to the next activity
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    } else {
                                        // Handle case where user is null
                                    }
                                } else {
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    // Method for Sign in Using FireBase
    private void loginuser(String email_txt, String password_txt) {
        auth.signInWithEmailAndPassword(email_txt, password_txt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "User Exists", Toast.LENGTH_SHORT).show();
                    //Get Instance of the current User
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Check if Email is verified or not
                    if (firebaseUser.isEmailVerified()) {

                        Toast.makeText(Login.this, "You are logged in now", Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("type","email");
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    } else {
                        firebaseUser.sendEmailVerification();
                        auth.signOut(); // sign out user
                        showAlertDialog();
                    }

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        email.setError("Invalid credentials");
                        email.requestFocus();
                    } catch (FirebaseAuthInvalidUserException e) {
                        email.setError("User Doesnt exist or is no longer valid. Please Register again");
                        email.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                    Toast.makeText(Login.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please Verify your Email to Login");

        //Open Email app if user clicks on continue
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To open in a new app and not in our app
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean rememberMeChecked = sharedPreferences.getBoolean("rememberMeChecked", false);
        if (rememberMeChecked && auth.getCurrentUser() != null) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        }
    }
}