package com.devtwist.mymessenger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.devtwist.mymessenger.Adapters.CallListAdapter;
import com.devtwist.mymessenger.Adapters.CountryAdapter;
import com.devtwist.mymessenger.Models.CountryCodeData;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText _otpPhoneInput, _otpCodeInput1, _otpCodeInput2, _otpCodeInput3, _otpCodeInput4, _otpCodeInput5, _otpCodeInput6, _searchCountry;
    private Button _otpPhoneSubmit, _otpCodeSubmit, _cCodeCancel;
    private LinearLayout _otpPhoneInputLayout, _otpCodeInputLayout, _otpLayout, _cCodeLayout;
    private FirebaseAuth mAuth;
    private TextView _resendOtpText,_otpCountryCode;
    private String firebaseOtp, phoneNo;
    private ProgressBar _otpProgressbar;
    private ArrayList<String> _countryNameList, _countryCodeList;
    private Resources res;
    private ArrayList<CountryCodeData> cCodeList;
    private CountryCodeData countryCodeData;
    private RecyclerView _countryList;
    private CountryAdapter countryAdapter;
    private boolean isFound;
    private SharedPreferences preferences;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        _otpPhoneInput = findViewById(R.id._otpPhoneInput);
        _otpCountryCode = findViewById(R.id._otpCountryCode);
        _otpCodeInput1 = findViewById(R.id._otpCodeInput1);
        _otpCodeInput2 = findViewById(R.id._otpCodeInput2);
        _otpCodeInput3 = findViewById(R.id._otpCodeInput3);
        _otpCodeInput4 = findViewById(R.id._otpCodeInput4);
        _otpCodeInput5 = findViewById(R.id._otpCodeInput5);
        _otpCodeInput6 = findViewById(R.id._otpCodeInput6);
        _resendOtpText = findViewById(R.id._resendOtpText);
        _otpPhoneSubmit = findViewById(R.id._otpPhoneSubmit);
        _otpCodeSubmit = findViewById(R.id._otpCodeSubmit);
        _otpProgressbar = findViewById(R.id._otpProgressBar);
        _otpPhoneInputLayout = findViewById(R.id._otpPhoneInputLayout);
        _otpCodeInputLayout = findViewById(R.id._otpCodeInputLayout);

        _searchCountry = findViewById(R.id._searchCountry);
        _cCodeCancel = findViewById(R.id._cCodeCancel);
        _otpLayout = findViewById(R.id._otpLayout);
        _cCodeLayout = findViewById(R.id._cCodeLayout);
        _countryList = findViewById(R.id._countryList);

        isFound = false;

        mAuth = FirebaseAuth.getInstance();

        _countryNameList = new ArrayList<String>();
        _countryCodeList = new ArrayList<String>();

        res = getResources();
        Collections.addAll(_countryNameList, res.getStringArray(R.array.country_name_array));
        Collections.addAll(_countryCodeList, res.getStringArray(R.array.country_code_array));

        LinearLayoutManager countryLayoutManager = new LinearLayoutManager(this);
        countryLayoutManager.setReverseLayout(true);
        countryLayoutManager.setStackFromEnd(true);
        _countryList.setLayoutManager(countryLayoutManager);

        cCodeList = new ArrayList<>();
        countryAdapter = new CountryAdapter(this, cCodeList, _otpLayout, _cCodeLayout, _otpCountryCode);

        _countryList.setHasFixedSize(true);
        _countryList.setAdapter(countryAdapter);

        readCountryList();

        _searchCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                readCountryList();
            }
        });


        _otpCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _otpLayout.setVisibility(View.GONE);
                _cCodeLayout.setVisibility(View.VISIBLE);
            }
        });

        _cCodeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _cCodeLayout.setVisibility(View.GONE);
                _otpLayout.setVisibility(View.VISIBLE);
            }
        });

        _otpCodeInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                _otpCodeInput2.requestFocus();
            }
        });

        _otpCodeInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                _otpCodeInput3.requestFocus();
            }
        });

        _otpCodeInput3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                _otpCodeInput4.requestFocus();
            }
        });

        _otpCodeInput4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                _otpCodeInput5.requestFocus();
            }
        });

        _otpCodeInput5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                _otpCodeInput6.requestFocus();
            }
        });

        _otpPhoneSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(OtpVerificationActivity.this)
                        .withPermission(Manifest.permission.READ_CONTACTS)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                if (_otpPhoneInput.getText().length()>0) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                                    _otpPhoneSubmit.setVisibility(View.GONE);
                                    _otpProgressbar.setVisibility(View.VISIBLE);
                                    sendOtp();
                                }
                                else{
                                    Toast.makeText(OtpVerificationActivity.this, "Please enter phone number!", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(OtpVerificationActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        _resendOtpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOtp();
            }
        });

        _otpCodeSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = _otpCodeInput1.getText().toString() +
                        _otpCodeInput2.getText().toString() +
                        _otpCodeInput3.getText().toString() +
                        _otpCodeInput4.getText().toString() +
                        _otpCodeInput5.getText().toString() +
                        _otpCodeInput6.getText().toString() ;

                if (code.length()==6){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    verifyCode(code);
                    _otpCodeSubmit.setVisibility(View.GONE);
                    _otpProgressbar.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(OtpVerificationActivity.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void readCountryList() {
        cCodeList.clear();
        if (_searchCountry.getText().length()<1) {
            for (int i = 0; i < _countryCodeList.size(); i++) {
                countryCodeData = new CountryCodeData(_countryNameList.get(i), _countryCodeList.get(i));
                cCodeList.add(countryCodeData);
            }
            countryAdapter.notifyDataSetChanged();
        }
        else{
            for (int i = 0; i < _countryCodeList.size(); i++) {
                if (_countryCodeList.get(i).toLowerCase().contains(_searchCountry.getText().toString().toLowerCase())
                        || _countryNameList.get(i).toLowerCase().contains(_searchCountry.getText().toString().toLowerCase()))
                {
                    countryCodeData = new CountryCodeData(_countryNameList.get(i), _countryCodeList.get(i));
                    cCodeList.add(countryCodeData);
                }
            }
            countryAdapter.notifyDataSetChanged();
        }

    }


    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            /*SharedPreferences preferences = getSharedPreferences("MyData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLogedIn", true);
                            editor.commit();
                            Intent i = new Intent(OtpVerificationActivity.this, CreateProfileActivity.class);
                            startActivity(i);
                            finish();*/

                            try {

                                String uID = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                FirebaseDatabase.getInstance().getReference().child("Users").child(uID).get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                isFound = false;
                                                if (task.isSuccessful()) {
                                                    DataSnapshot snapshot = task.getResult();
                                                    if (snapshot.exists()) {
                                                        isFound = true;
                                                    }
                                                }
                                                if (isFound) {
                                                    preferences = getSharedPreferences("MyMessengerData", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putBoolean("isLogedIn", true);
                                                    editor.putBoolean("isProfileCreated", true);
                                                    editor.commit();
                                                    //Toast.makeText(MainActivity.this, "found", Toast.LENGTH_SHORT).show();
                                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                        @Override
                                                        public void onSuccess(String s) {
                                                            String token = s;
                                                            String uId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                            hashMap.put("token", token);
                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child("Users").child(uId).updateChildren(hashMap);

                                                        }
                                                    });
                                                    Intent intent = new Intent(OtpVerificationActivity.this, MessengerActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    preferences = getSharedPreferences("MyMessengerData", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putBoolean("isLogedIn", true);
                                                    editor.commit();
                                                    //Toast.makeText(MainActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(OtpVerificationActivity.this, CreateProfileActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }

                                            }
                                        });


                            } catch (Exception e) {
                                Log.i("Open Activity Error",e.getMessage().toString());
                            }
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(OtpVerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendOtp() {
        try {
            phoneNo = _otpCountryCode.getText().toString().trim() + _otpPhoneInput.getText().toString().trim();
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNo)		 // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)				 // Activity (for callback binding)
                            .setCallbacks(mCallBack)		 // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Send Otp Error", e.getMessage().toString());
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            _otpPhoneInputLayout.setVisibility(LinearLayout.GONE);
            _otpProgressbar.setVisibility(View.GONE);
            _otpCodeInputLayout.setVisibility(LinearLayout.VISIBLE);
            firebaseOtp = s;

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code.length()==6) {
                verifyCode(code);
            }else{
                Toast.makeText(OtpVerificationActivity.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Log.i("Send Otp Error", e.getMessage().toString());
        }
    };

    private void verifyCode(String code) {
        // below line is used for getting getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(firebaseOtp, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }

}
