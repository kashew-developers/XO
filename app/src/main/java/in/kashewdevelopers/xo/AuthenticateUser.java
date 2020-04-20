package in.kashewdevelopers.xo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class AuthenticateUser extends AppCompatActivity {

    CountryCodePicker countryCodePicker;
    EditText phoneNumberEt, otpEt;
    LinearLayout otpSection;
    Button getOtpButton, verifyButton;

    String phoneNumber;
    String verificationId;

    ProgressDialog progressDialog;

    private FirebaseAuth auth;
    PhoneAuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_user);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent i = new Intent(this, GameCreateJoin.class);
            startActivityForResult(i, 1234);
        }


        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();

        // initialize widgets
        countryCodePicker = findViewById(R.id.countryCodeHolder);
        phoneNumberEt = findViewById(R.id.phoneNumber);
        otpEt = findViewById(R.id.otpCode);
        getOtpButton = findViewById(R.id.otpButton);
        verifyButton = findViewById(R.id.verifyButton);
        otpSection = findViewById(R.id.otpSection);


    }


    public void getOtpClicked(View v) {

        if (phoneNumberEt.getText().toString().isEmpty()) {
            phoneNumberEt.setError("Cannot be empty");
            return;
        } else if (phoneNumberEt.getText().toString().length() < 10) {
            phoneNumberEt.setError("Invalid Phone Number");
            return;
        }

        phoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() +
                phoneNumberEt.getText().toString();

        phoneNumberEt.setEnabled(false);
        getOtpButton.setVisibility(View.GONE);
        otpSection.setVisibility(View.VISIBLE);
        verifyButton.setVisibility(View.VISIBLE);

        progressDialog.setTitle("Sending OTP");
        progressDialog.setMessage("please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,
                TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBack);

    }


    public void verifyOtpClicked(View v){

        if (otpEt.getText().toString().isEmpty()) {
            otpEt.setError("Cannot be empty");
            return;
        }

        progressDialog.setTitle("Verifying OTP");
        progressDialog.setMessage("please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        verifyCode();

    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            progressDialog.dismiss();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            progressDialog.dismiss();
            credential = phoneAuthCredential;
            if (verificationId != null) {
                String code = phoneAuthCredential.getSmsCode();
                otpEt.setText(code);
            } else {
                verifyCode();
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressDialog.dismiss();
            Toast.makeText(AuthenticateUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    private void verifyCode() {

        if (credential == null) {
            String code = otpEt.getText().toString();
            credential = PhoneAuthProvider.getCredential(verificationId, code);
        }
        signInWithCredential(credential);

    }


    private void signInWithCredential(PhoneAuthCredential credential) {

        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(AuthenticateUser.this,
                                    "Verified", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(AuthenticateUser.this,
                                    GameCreateJoin.class);
                            startActivityForResult(i, 1234);

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(AuthenticateUser.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == 1234)
            onBackPressed();
    }
}
