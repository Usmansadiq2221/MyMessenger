package com.devtwist.mymessenger.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devtwist.mymessenger.Adapters.PostAdapter;
import com.devtwist.mymessenger.Models.CountryCodeData;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.PostData;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TimelineActivity extends AppCompatActivity {

    private TextView _tTimerSmsText, _tMessengerText;
    private ImageView _postProfile, _postImage;
    private Button _postSubmit;
    private LinearLayout _addPostPhoto;
    private ShimmerRecyclerView _prePostView;
    private EditText _postDescription;
    private Intent intent;
    private String postDesc, pDate,pTime;
    private Uri imageFilePath;
    private Bitmap bitmap;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private ArrayList<PostData> postList;
    private ArrayList<String> friendList;
    private PostAdapter postAdapter;
    private ProgressBar _postProgressBar, _pLoadingProgress;
    private PostData postData;
    private String postId, myId;
    private CountDownTimer timer;
    private InterstitialAd mInterstitialAd;
    private AdView _timelineAdView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        setUpAds();

        _tTimerSmsText = findViewById(R.id._tTimerSmsText);
        _tMessengerText = findViewById(R.id._tMessengerText);
        _postProfile = findViewById(R.id._postProfile);
        _postImage = findViewById(R.id._postImage);
        _postSubmit = findViewById(R.id._postSubmit);
        _addPostPhoto = findViewById(R.id._addPostPhoto);
        _prePostView = findViewById(R.id._prePostView);
        _postDescription = findViewById(R.id._postDescription);
        _postProgressBar = findViewById(R.id._postProgressBar);
        _pLoadingProgress = findViewById(R.id._pLoadingProgress);
        _timelineAdView = findViewById(R.id._timeLineAdView);

        AdRequest adRequest = new AdRequest.Builder().build();
        _timelineAdView.loadAd(adRequest);

        postDesc = "";
        imageFilePath = null;
        myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        storage = FirebaseStorage.getInstance();

        timer = new CountDownTimer(300000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                runAds();

            }
        };
        timer.start();

        FirebaseDatabase.getInstance().getReference().child("Users").child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Userdata userdata = snapshot.getValue(Userdata.class);
                if (userdata.getProfileUrl().length()>1) {
                    Glide.with(TimelineActivity.this).load(userdata.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(_postProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TimelineActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });

        _postProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimelineActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        _prePostView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, myId);
        _prePostView.setAdapter(postAdapter);
        _prePostView.showShimmerAdapter();

        friendList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        FriendsData friendsData = dataSnapshot.getValue(FriendsData.class);
                        friendList.add(friendsData.getUserId());
                    }
                }
                readPostList(friendList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //readPostList();

        _tTimerSmsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(TimelineActivity.this, SendSMSActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _tMessengerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(TimelineActivity.this, MessengerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _addPostPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(TimelineActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                                Intent select = new Intent(Intent.ACTION_PICK);
                                select.setType("image/*");
                                startActivityForResult(Intent.createChooser(select,"Select an Image"), 1);

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(TimelineActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        _postSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _postSubmit.setVisibility(View.GONE);
                _postProgressBar.setVisibility(View.VISIBLE);
                if (_postDescription.getText().length()>0 || imageFilePath != null){
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    submitPost();
                }
            }
        });

    }

    private void readPostList(ArrayList<String> friendList) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        postData = dataSnapshot.getValue(PostData.class);
                        if (postData.getUserId().equals(myId)){
                            postList.add(postData);
                        }
                        else {
                            for (String friend : friendList) {
                                if (postData.getUserId().equals(friend)) {
                                    postList.add(postData);
                                }
                            }
                        }

                    }
                    _prePostView.hideShimmerAdapter();
                    postAdapter.notifyDataSetChanged();
                    //_pLoadingProgress.setVisibility(View.GONE);
                    //_prePostView.setVisibility(View.VISIBLE);
                }



                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TimelineActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.i("PostListError", e.getMessage().toString());
        }
    }

    private void submitPost() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postId = databaseReference.push().getKey();
        postDesc = _postDescription.getText().toString().trim();
        pDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        pTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if (imageFilePath==null){
            PostData postData = new PostData(postId, pTime, pDate, postDesc, "",myId);
            databaseReference.child(postId).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    _postDescription.setText("");
                    _postImage.setImageResource(R.drawable.placeholder);
                    _postProgressBar.setVisibility(View.GONE);
                    _postSubmit.setVisibility(View.VISIBLE);
                    timer.cancel();
                    runAds();
                }
            });
        }
        else{
            StorageReference uploadfile = storage.getReference().child("Posts").child(myId).child(postId);
            uploadfile.putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            PostData postData = new PostData(postId, pTime, pDate, postDesc, uri.toString(),myId);
                            databaseReference.child(postId).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    _postDescription.setText("");
                                    _postImage.setVisibility(View.GONE);
                                    _addPostPhoto.setVisibility(View.VISIBLE);
                                    _postImage.setImageResource(R.drawable.placeholder);
                                    _postProgressBar.setVisibility(View.GONE);
                                    _postSubmit.setVisibility(View.VISIBLE);
                                    timer.cancel();
                                    runAds();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode == RESULT_OK ) {
            imageFilePath = data.getData();
            try {
                CropImage.activity(imageFilePath)
                        .start(TimelineActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageFilePath = result.getUri();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageFilePath);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    _postImage.setImageBitmap(bitmap);
                    _postImage.setVisibility(View.VISIBLE);
                    _addPostPhoto.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
                    mInterstitialAd.show(TimelineActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            timer.cancel();
                            setUpAds();
                            timer.start();

                        }

                    });
                } else {
                    timer.cancel();
                    setUpAds();
                    timer.start();
                }
            }
        }.start();
    }

}