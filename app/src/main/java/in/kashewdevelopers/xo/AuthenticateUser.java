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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
            goToGameRoom();
        }

        initialization();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234)
            onBackPressed();
    }


    // initialization
    public void initialization() {
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();

        countryCodePicker = findViewById(R.id.countryCodeHolder);
        phoneNumberEt = findViewById(R.id.phoneNumber);

        otpSection = findViewById(R.id.otpSection);
        otpEt = findViewById(R.id.otpCode);
        otpSection.setVisibility(View.GONE);

        getOtpButton = findViewById(R.id.otpButton);
        verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setVisibility(View.GONE);
    }


    // handle widget clicks
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

        initiateOtpProcess();
    }

    public void verifyOtpClicked(View v) {
        if (otpEt.getText().toString().isEmpty()) {
            otpEt.setError("Cannot be empty");
            return;
        }

        progressDialog.setTitle(R.string.verifying_otp);
        progressDialog.setMessage(getText(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        verifyCode();
    }


    // functionality
    public void initiateOtpProcess() {
        phoneNumberEt.setEnabled(false);
        getOtpButton.setVisibility(View.GONE);
        otpSection.setVisibility(View.VISIBLE);
        verifyButton.setVisibility(View.VISIBLE);
        countryCodePicker.setCcpClickable(false);

        progressDialog.setTitle(R.string.sending_otp);
        progressDialog.setMessage(getText(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,
                TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBack);
    }

    public void undoOtpProcess() {
        phoneNumberEt.setEnabled(true);
        getOtpButton.setVisibility(View.VISIBLE);
        otpSection.setVisibility(View.GONE);
        verifyButton.setVisibility(View.GONE);
        countryCodePicker.setCcpClickable(true);

        progressDialog.dismiss();
    }

    public void goToGameRoom() {
        Intent i = new Intent(this, GameCreateJoin.class);
        startActivityForResult(i, 1234);
    }


    // otp sending & verification
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
            undoOtpProcess();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            undoOtpProcess();
            progressDialog.dismiss();
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
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        Toast.makeText(AuthenticateUser.this,
                                getText(R.string.verified), Toast.LENGTH_SHORT).show();
                        goToGameRoom();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        otpEt.setText("");
                        verificationId = null;
                        undoOtpProcess();
                    }
                });
    }

}