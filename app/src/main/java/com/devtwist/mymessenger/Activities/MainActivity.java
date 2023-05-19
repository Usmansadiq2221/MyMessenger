package com.devtwist.mymessenger.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.devtwist.mymessenger.R;

public class MainActivity extends AppCompatActivity {
    private CountDownTimer countDownTimer;
    private boolean isLogedin,isProfileCreated;
    private ImageView _appLogo, _logoOutline, _logoUnhide;
    private Bundle bundle;
    private Intent intent;
    private SharedPreferences preferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bundle = getIntent().getExtras();
        if (bundle!=null){
            startActivity(new Intent(MainActivity.this, MessengerActivity.class));
        }
        else {
            isLogedin = isProfileCreated = false;
            _appLogo = findViewById(R.id._appLogo);
            _logoOutline = findViewById(R.id._logoOutline);
            _logoUnhide = findViewById(R.id._logoUnhide);
            _appLogo.setScaleX(0f);
            _appLogo.setScaleY(0f);
            _logoUnhide.animate().translationYBy(1000f).setDuration(1500);

            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    _appLogo.animate().scaleX(1f).scaleY(1f).setDuration(1000);
                }
            }.start();

            countDownTimer = new CountDownTimer(2500, 1000) {
                @Override
                public void onTick(long l) {

                }

                @SuppressLint("RestrictedApi")
                @Override
                public void onFinish() {
                    bundle = getIntent().getExtras();
                    if (bundle!=null){
                        startActivity(new Intent(MainActivity.this, MessengerActivity.class));
                    }
                    else {
                        preferences = getSharedPreferences("MyMessengerData", MODE_PRIVATE);
                        if (preferences.contains("isLogedIn") && preferences.contains("isProfileCreated")) {
                            isLogedin = preferences.getBoolean("isLogedIn", false);
                            isProfileCreated = preferences.getBoolean("isProfileCreated", false);
                        }
                        if (isLogedin && isProfileCreated) {
                            intent = new Intent(MainActivity.this, TimelineActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (!isLogedin) {
                                intent = new Intent(MainActivity.this, OtpVerificationActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }


                }
            };

            countDownTimer.start();
        }

    }
}