package com.devtwist.mymessenger.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devtwist.mymessenger.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.squareup.picasso.Picasso;

public class ViewImage extends AppCompatActivity {

    private ImageView _viewChatImage;
    private Intent intent;
    private Bundle bundle;
    private String imageUrl;
    private TextView _textView;
    private InterstitialAd mInterstitialAd;
    private ScrollView _textScrollView;
    private WebView _viewWebView;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        setUpAds();
        runAds();

        _textScrollView = findViewById(R.id._textScrollView);
        _viewChatImage = findViewById(R.id._viewChatImage);

        intent = getIntent();
        bundle = intent.getExtras();
        _textView = findViewById(R.id._textView);
        _viewWebView = findViewById(R.id._viewWebView);

        _viewChatImage.setVisibility(View.GONE);
        _textScrollView.setVisibility(View.GONE);

        try {
            if (bundle.getString("tag").equals("i")) {
                imageUrl = bundle.getString("imgUrl");
                if (imageUrl.length() > 1) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile_placeholder).into(_viewChatImage);
                }
                _viewChatImage.setVisibility(ImageView.VISIBLE);
            } else if (bundle.getString("tag").equals("t")) {
                _viewWebView.getSettings().setJavaScriptEnabled(true);
                _viewWebView.loadUrl("https://pages.flycricket.io/my-messenger-0/terms.html");
                _viewWebView.setVisibility(View.VISIBLE);
            } else if (bundle.getString("tag").equals("p")) {
                _viewWebView.getSettings().setJavaScriptEnabled(true);
                _viewWebView.loadUrl("https://pages.flycricket.io/my-messenger-0/privacy.html");
                _viewWebView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setUpAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-8385601672345207/4719356511", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("AdError", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void runAds() {
        new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(ViewImage.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();

                        }

                    });
                } else {

                }
            }
        }.start();
    }
}