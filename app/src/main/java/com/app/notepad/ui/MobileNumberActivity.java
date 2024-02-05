package com.app.notepad.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.app.notepad.utils.AppController;
import com.app.notepad.utils.NetworkReceiver;
import com.app.notepad.R;
import com.app.notepad.database.NoteData;
import com.app.notepad.databinding.ActivityMobileNumberBinding;
import com.app.notepad.model.ContentItem;
import com.app.notepad.model.DataNoteResponse;
import com.app.notepad.utils.SMSReceiver;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

public class MobileNumberActivity extends BaseActivity implements TextWatcher, View.OnClickListener {

    private String TAG = "MobileNumberActivity";
    private ActivityMobileNumberBinding binding;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private SMSReceiver smsReceiver;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mobile_number);
        binding.mobileNumber.addTextChangedListener(this);
        binding.generateOtp.setOnClickListener(this);
        binding.submit.setOnClickListener(this);
        binding.resentCode.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ");
                binding.mobileNumber.setText("");
                binding.generateOtp.setEnabled(false);
                binding.generateOtp.setBackgroundColor(getColor(R.color.disabled_btn));
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                dismissLoading();
                Toast.makeText(MobileNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.generateOtp.setEnabled(true);
                binding.generateOtp.setBackgroundColor(getColor(R.color.colorPrimary));
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d(TAG, "onCodeSent:" + verificationId);
                dismissLoading();
                Toast.makeText(MobileNumberActivity.this, "Code Sent!!", Toast.LENGTH_SHORT).show();
                binding.progressLay.setVisibility(View.VISIBLE);
                startTimer();
                // Save verification ID and resending token so we can use them later
                mResendToken = forceResendingToken;
                binding.generateOtpLayout.setVisibility(View.GONE);
                binding.otpVerification.setVisibility(View.VISIBLE);
            }
        };

        binding.otp1.addTextChangedListener(this);
        binding.otp2.addTextChangedListener(this);
        binding.otp3.addTextChangedListener(this);
        binding.otp4.addTextChangedListener(this);
        binding.otp5.addTextChangedListener(this);
        binding.otp6.addTextChangedListener(this);

        startSmsUserContent();

    }

    private void startTimer() {
        binding.mobileNumberVerificationCountDown.setMax(120);
        binding.resentCode.setEnabled(false);
        binding.resentCode.setTextColor(getColor(R.color.disabled_btn));
        CountDownTimer countDownTimer = new CountDownTimer(120 * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (long) (millisUntilFinished / 1000);
                binding.mobileNumberVerificationCountDown.setProgress((int) seconds, true);
                binding.progressText.setText("" + seconds);
            }

            @Override
            public void onFinish() {
                binding.resentCode.setEnabled(true);
                binding.resentCode.setTextColor(getColor(R.color.colorPrimary));
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsReceiver);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (binding.generateOtpLayout.getVisibility() == View.VISIBLE) {
            if (binding.mobileNumber.getText().toString().length() == 10) {
                binding.generateOtp.setEnabled(true);
                binding.generateOtp.setBackgroundColor(getColor(R.color.colorPrimary));
            } else {
                binding.generateOtp.setEnabled(false);
                binding.generateOtp.setBackgroundColor(getColor(R.color.disabled_btn));
            }
        } else if (binding.otpVerification.getVisibility() == View.VISIBLE) {
            if (binding.otp6.getText().toString().length() == 1) {
                hideKeyboard();
                binding.otp6.clearFocus();
            } else if (binding.otp5.getText().toString().length() == 1) {
                binding.otp6.requestFocus();
            } else if (binding.otp4.getText().toString().length() == 1) {
                binding.otp5.requestFocus();
            } else if (binding.otp3.getText().toString().length() == 1) {
                binding.otp4.requestFocus();
            } else if (binding.otp2.getText().toString().length() == 1) {
                binding.otp3.requestFocus();
            } else if (binding.otp1.getText().toString().length() == 1) {
                binding.otp2.requestFocus();
            }
            checkButton();
        }
    }

    private void checkButton() {
        if (binding.otp1.getText().toString().length() == 1 && binding.otp2.getText().toString().length() == 1 &&
                binding.otp3.getText().toString().length() == 1 && binding.otp4.getText().toString().length() == 1 &&
                binding.otp5.getText().toString().length() == 1 && binding.otp6.getText().toString().length() == 1) {
            binding.submit.setEnabled(true);
            binding.submit.setBackgroundColor(getColor(R.color.colorPrimary));
            hideKeyboard();
        } else {
            binding.submit.setEnabled(false);
            binding.submit.setBackgroundColor(getColor(R.color.disabled_btn));
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        String mobileNumber = "+91" + binding.mobileNumber.getText().toString();
        switch (v.getId()) {
            case R.id.generateOtp:
                binding.generateOtp.setEnabled(false);
                binding.generateOtp.setBackgroundColor(getColor(R.color.disabled_btn));
                showLoading();
                startPhoneNumberVerification(mobileNumber);
                break;
            case R.id.submit:
                getPhoneLogin(mobileNumber);
                break;

            case R.id.resentCode:
                resentCode(mobileNumber);
                break;
        }
    }

    public void startSmsUserContent() {
        SmsRetrieverClient smsRetrieverClient = SmsRetriever.getClient(this);
        smsRetrieverClient
                .startSmsUserConsent(null)
                .addOnSuccessListener(vvoid -> Toast.makeText(MobileNumberActivity.this, "on Success", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(exception -> Toast.makeText(MobileNumberActivity.this, "on Failure", Toast.LENGTH_SHORT).show());
    }

    private ActivityResultLauncher<Intent> activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode()==Activity.RESULT_OK && result.getData()!=null){
            String message = result.getData().getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            Toast.makeText(MobileNumberActivity.this, message, Toast.LENGTH_SHORT).show();
            System.out.println("//message : "+message);
        }
    });

    private void registerReceiver(){
        smsReceiver = new SMSReceiver();
        smsReceiver.smsListener=new SMSReceiver.SmsListener() {
            @Override
            public void onSuccess(Intent intent) {
                activityResultLauncher.launch(intent);
            }

            @Override
            public void onFailure() {

            }
        };
        IntentFilter intentFilter= new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsReceiver,intentFilter);
    }



    private void getPhoneLogin(String mobileNo) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            String URL = AppController.GET_NOTE + "?mobileno=" + mobileNo.replace("+", "%2B");
            Log.d(TAG, "getPhoneLogin: " + URL);
            StringRequest request = new StringRequest(Request.Method.GET, URL, response -> {
                Log.d(TAG, "onResponse: " + response);
                DataNoteResponse noteResponse = new Gson().fromJson(response, DataNoteResponse.class);
                if (noteResponse.getStatusCode().equals("200")) {

                    appController.setValues("logged", true);
                    appController.setValues(AppController.USER_MOBILE, mobileNo);
                    Log.d(TAG, "onResponse: " + noteResponse.getContent().size());
                    for (ContentItem content : noteResponse.getContent()) {
                        NoteData noteData = new NoteData();
                        noteData.setStatus("live");
                        noteData.setNotes(content.getNotes());
                        noteData.setTextChanged("olddata");
                        noteData.setServerSync(AppController.SERVER_SYNC);
                        noteData.setId("Notes-" + content.hashCode());
                        noteData.setKeyValues(content.getKey());
                        noteData.setCreatedTime(content.getCreatedtime());
                        noteDataDao.insertOrUpdate(noteData);
                    }

                    dismissLoading();
                    startActivity(new Intent(MobileNumberActivity.this, MainActivity.class));
                    finish();
                }
            }, error -> {
                dismissLoading();
                Toast.makeText(MobileNumberActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            });
            appController.addToRequestQueue(request);
        } else
            Toast.makeText(MobileNumberActivity.this, getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resentCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(mResendToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    @Override
    public void onBackPressed() {
        if (binding.generateOtpLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        } else {
            binding.generateOtpLayout.setVisibility(View.VISIBLE);
            binding.otpVerification.setVisibility(View.GONE);
        }
    }
}
