package commun.cherestal.communweb;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import commun.cherestal.communweb.R.string;
import commun.cherestal.communweb.app.App;
import commun.cherestal.communweb.constants.Constants;
import commun.cherestal.communweb.util.CustomRequest;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.internal.WebDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */


/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class GoogleSignInActivity extends AppCompatActivity implements Constants{

    private static final String TAG = "GoogleSignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;

    private String username, password, language, fullname, referrerId;
    private String googleId = "", googleName = "", googleEmail = "";

    private TextView mStatusTextView;
    // private TextView mDetailTextView;
    private FirebaseAuth mAuth;
    // private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;

  /*  private FirebaseAuth firebaseAuth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private GoogleApiClient mGoogleApiClient;


*/




    @Override
    protected void onCreate(Bundle savedInstanceState) {


      //  setContentView(R.layout.fragment_login);
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mAuth = FirebaseAuth.getInstance();

        isUserSignedIn();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //signIn();
    }

    @Override
    public void onStart() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        super.onStart();

    }

    @Override
    public void onResume(){

        super.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();

        // this.finish();
    }

    private void isUserSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            signIn();
        } else {
          /*  username = account.getDisplayName();
            googleEmail = account.getEmail();
            fullname= account.getGivenName() + " " + account.getGivenName();
            googleId = account.getId();*/
            firebaseAuthWithGoogle(account);

        }

    }

  /*  @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("methodName").equals("signUpWithGoogle")){
           signUpWithGoogle();
        }
    }
*/
    public void signUpWithGoogle(){

            if (App.getInstance().isConnected()) {

              //  loading = true;

               // showpDialog();

                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINGOOGLE, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {



                                Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                if (App.getInstance().authorize(response)) {

                                  //  welcomeUser();

                                   // Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);


                                    App.getInstance().updateGeoLocation();

                                }
                                else{
                                    Log.d(TAG, "Response was not true");
                                    welcomeUser();
                                }






                                /*else {

                                    switch (App.getInstance().getErrorCode()) {

                                        case 300 : {

                                            signupUsername.setError(getString(R.string.error_login_taken));
                                            break;
                                        }

                                        case 301 : {

                                            signupEmail.setError(getString(R.string.error_email_taken));
                                            break;
                                        }

                                        case 500 : {

                                            Toast.makeText(getActivity(), getText(R.string.label_multi_account_msg), Toast.LENGTH_SHORT).show();
                                            break;
                                        }

                                        default: {

                                            Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                            break;
                                        }
                                    }
                                }*/

                                //loading = false;

                              //  hidepDialog();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                       // Log.e("Error", "Malformed JSON: \"" + error.toString() + "\"");
                       // Log.e("Error", "Malformed JSON: \"" + error.getMessage() + "\"");

                        Toast.makeText(getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();


                        //loading = false;

                       // hidepDialog();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("googleId", googleId);
                        params.put("clientId", CLIENT_ID);
                        params.put("googleEmail", googleEmail);
                        params.put("gcm_regId", App.getInstance().getGcmToken());

                        return params;
                    }
                };

                App.getInstance().addToRequestQueue(jsonReq);

            } else {

                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        public void welcomeUser(){
            Intent intent = new Intent(this, SignupFragment.class);
            intent.putExtra("googleEmail", googleEmail);
            intent.putExtra("googleName", googleName);
            intent.putExtra("googleId", googleId);
            intent.putExtra("googleUsername", username);
            setResult(2651, intent);
            finish();


        }



    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            if (result.isSuccess()){
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                username = account.getDisplayName();
                googleEmail = account.getEmail();
                googleName= account.getGivenName() + " " + account.getFamilyName();
                googleId = account.getId();

                firebaseAuthWithGoogle(account);
            }

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GoogleSignInActivity", "firebaseAuthWithGoogle:" + acct.getId());

        username = acct.getDisplayName();
        googleEmail = acct.getEmail();
        googleName= acct.getGivenName() + " " + acct.getFamilyName();
        googleId = acct.getId();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("GoogleSignInActivity", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                           /* Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();*/
                            signUpWithGoogle();
                        }



                    }
                });
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG,"handleSignInResult:"+ result.isSuccess());
        if (result.isSuccess()){
            GoogleSignInAccount account =result.getSignInAccount();
            //textview.setText("Welcome"+ acc.getDisplayName()+"\n"+"\n"+":email"+ acc.getEmail());
            try {
                //String PohtoUlr=acc.getPhotoUrl().toString();
                //wuName();


            } catch (Exception e){

            }
            //startActivity(new Intent(this, WuActivity.class));
        }
    }

    // [START signIn]
    private void signIn() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [END signIn]

    // [START signOut]
    protected void signOut() {

        // Firebase sign out
        //FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        FirebaseAuth.getInstance().signOut();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]

                        //END_CLUDE]
                    }
                });
    }
    // [END revokeAccess]

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        this.finish();
    }
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

}


