package in.kashewdevelopers.xo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import in.kashewdevelopers.xo.databinding.ActivityAuthenticateUserBinding;

public class AuthenticateUser extends AppCompatActivity {

    ActivityAuthenticateUserBinding binding;

    String phoneNumber;
    String verificationId;

    ProgressDialog progressDialog;

    private FirebaseAuth auth;
    PhoneAuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticateUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        binding.otpSection.setVisibility(View.GONE);
        binding.verifyButton.setVisibility(View.GONE);
    }


    // handle widget clicks
    public void getOtpClicked(View v) {
        if (binding.phoneNumber.getText().toString().isEmpty()) {
            binding.phoneNumber.setError("Cannot be empty");
            return;
        } else if (binding.phoneNumber.getText().toString().length() < 10) {
            binding.phoneNumber.setError("Invalid Phone Number");
            return;
        }

        phoneNumber = binding.countryCodePicker.getSelectedCountryCodeWithPlus() +
                binding.phoneNumber.getText().toString();

        initiateOtpProcess();
    }

    public void verifyOtpClicked(View v) {
        if (binding.otpCode.getText().toString().isEmpty()) {
            binding.otpCode.setError("Cannot be empty");
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
        binding.phoneNumber.setEnabled(false);
        binding.otpButton.setVisibility(View.GONE);
        binding.otpSection.setVisibility(View.VISIBLE);
        binding.verifyButton.setVisibility(View.VISIBLE);
        binding.countryCodePicker.setCcpClickable(false);

        progressDialog.setTitle(R.string.sending_otp);
        progressDialog.setMessage(getText(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,
                TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBack);
    }

    public void undoOtpProcess() {
        binding.phoneNumber.setEnabled(true);
        binding.otpButton.setVisibility(View.VISIBLE);
        binding.otpSection.setVisibility(View.GONE);
        binding.verifyButton.setVisibility(View.GONE);
        binding.countryCodePicker.setCcpClickable(true);

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
                binding.otpCode.setText(code);
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
            String code = binding.otpCode.getText().toString();
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
                        binding.otpCode.setText("");
                        verificationId = null;
                        undoOtpProcess();
                    }
                });
    }

}