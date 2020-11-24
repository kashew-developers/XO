package `in`.kashewdevelopers.xo

import `in`.kashewdevelopers.xo.databinding.ActivityAuthenticateUserBinding
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthenticateUser : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticateUserBinding

    private var phoneNumber = ""
    private var verificationId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var auth: FirebaseAuth
    private var credential: PhoneAuthCredential? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser != null) {
            goToGameRoom()
        }

        initialization()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234)
            onBackPressed()
    }


    // initialization
    private fun initialization() {
        progressDialog = ProgressDialog(this)
        auth = FirebaseAuth.getInstance()

        binding.otpSection.visibility = View.GONE
        binding.verifyButton.visibility = View.GONE
    }


    // handle widget clicks
    fun getOtpClicked(view: View) {
        if (binding.phoneNumber.text.isEmpty()) {
            binding.phoneNumber.error = "Cannot be empty"
            return
        } else if (binding.phoneNumber.text.length < 10) {
            binding.phoneNumber.error = "Invalid Phone Number"
            return
        }

        phoneNumber = binding.countryCodePicker.selectedCountryCodeWithPlus + binding.phoneNumber.text

        initiateOtpProcess()
    }

    fun verifyOtpClicked(view: View) {
        if (binding.otpCode.text.isEmpty()) {
            binding.otpCode.error = "Cannot be empty"
            return
        }

        with(progressDialog) {
            setTitle(R.string.verifying_otp)
            setMessage(getText(R.string.please_wait))
            setCancelable(false)
            show()
        }

        verifyCode()
    }


    // functionality
    private fun initiateOtpProcess() {
        with(binding) {
            phoneNumber.isEnabled = false
            otpButton.visibility = View.GONE
            otpSection.visibility = View.VISIBLE
            verifyButton.visibility = View.VISIBLE
            countryCodePicker.setCcpClickable(false)
        }

        with(progressDialog) {
            setTitle(R.string.sending_otp)
            setMessage(getText(R.string.please_wait))
            setCancelable(false)
            show()
        }

        val phoneAuthOptions = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(mCallBack)
                .setActivity(this)
                .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)

    }

    private fun undoOtpProcess() {
        with(binding) {
            phoneNumber.isEnabled = true
            otpButton.visibility = View.VISIBLE
            otpSection.visibility = View.GONE
            verifyButton.visibility = View.GONE
            countryCodePicker.setCcpClickable(true)
        }
        progressDialog.dismiss()
    }

    private fun goToGameRoom() {
        val i = Intent(this, GameCreateJoin::class.java)
        startActivityForResult(i, 1234)
    }


    // otp sending & verification
    private val mCallBack =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(s, forceResendingToken)
                    verificationId = s
                    progressDialog.dismiss()
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    progressDialog.dismiss()
                    credential = phoneAuthCredential
                    if (verificationId.isNotEmpty()) {
                        val code = phoneAuthCredential.smsCode
                        binding.otpCode.setText(code)
                    } else {
                        verifyCode()
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.dismiss()
                    Toast.makeText(this@AuthenticateUser, e.message, Toast.LENGTH_SHORT).show()
                    undoOtpProcess()
                }

                override fun onCodeAutoRetrievalTimeOut(s: String) {
                    super.onCodeAutoRetrievalTimeOut(s)
                    undoOtpProcess()
                    progressDialog.dismiss()
                }
            }

    private fun verifyCode() {
        if (credential == null) {
            val code = binding.otpCode.text.toString()
            credential = PhoneAuthProvider.getCredential(verificationId, code)
        }

        credential?.let { signInWithCredential(it) }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@AuthenticateUser,
                            getText(R.string.verified), Toast.LENGTH_SHORT).show()
                    goToGameRoom()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    binding.otpCode.setText("")
                    verificationId = ""
                    undoOtpProcess()
                }
    }

}