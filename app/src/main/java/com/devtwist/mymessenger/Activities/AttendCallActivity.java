package com.devtwist.mymessenger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devtwist.mymessenger.Models.CallData;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.InterfaceJava;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AttendCallActivity extends AppCompatActivity {

    private WebView _webView;
    private ImageView _aEndCall, _aMicBtn, _aVideoBtn, _aCallProfile;
    private TextView _aCallName, _aCallTime;
    private Group _aCallControls, _aCallLoadingGroup;
    private String uniqueId = "", myId;
    private FirebaseAuth auth;
    private DatabaseReference firebaseRef;
    private String username = "";
    private String friendsUsername = "";

    private boolean isPeerConnected = false;

    private boolean isAudio = true;
    private boolean isVideo = true;
    private String createdBy, incoming;

    private boolean pageExit = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_call);

        _webView = findViewById(R.id._webView);
        _aEndCall = findViewById(R.id._aEndCall);
        _aMicBtn = findViewById(R.id._aMicBtn);
        _aVideoBtn = findViewById(R.id._aVideoBtn);
        _aCallProfile = findViewById(R.id._aCallProfile);
        _aCallName = findViewById(R.id._aCallName);
        _aCallTime = findViewById(R.id._aCallTime);
        _aCallControls = findViewById(R.id._aCallControls);
        _aCallLoadingGroup = findViewById(R.id._aCallLoadingGroup);


        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("Call");

        username = getIntent().getStringExtra("username");
        incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");
        myId = auth.getCurrentUser().getPhoneNumber();
        friendsUsername = incoming;

        //cheking call is ended by the user or not...
        FirebaseDatabase.getInstance().getReference().child("Call").child(createdBy+incoming).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CallData callData = snapshot.getValue(CallData.class);
                    if (callData.getCallStatus().equals("ended")){
                        createdBy = "";
                        incoming = "";
                        friendsUsername = "";
                        pageExit = true;
                        _webView.destroy();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        setupWebView();

        _aMicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if(isAudio){
                    _aMicBtn.setImageResource(R.drawable.btn_unmute_normal);
                } else {
                    _aMicBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        _aVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if(isVideo){
                    _aVideoBtn.setImageResource(R.drawable.btn_video_normal);
                } else {
                    _aVideoBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        _aEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });




    }

    private void setupWebView() {
        _webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        _webView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        _webView.loadUrl(filePath);

        _webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    private void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        //if my id store in username then I will set the connid else  I'll listen the Id...
        if(createdBy.equalsIgnoreCase(username)) {
            if(pageExit) {
                return;
            }
            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            _aCallLoadingGroup.setVisibility(View.GONE);
            _aCallControls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            Userdata user = snapshot.getValue(Userdata.class);

                            Glide.with(AttendCallActivity.this).load(user.getProfileUrl())
                                    .into(_aCallProfile);
                            FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).child(friendsUsername)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                FriendsData friendsData = snapshot.getValue(FriendsData.class);
                                                _aCallName.setText(friendsData.getUsername());
                                            }
                                            else{
                                                _aCallName.setText(friendsUsername);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(friendsUsername)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                    Userdata user = snapshot.getValue(Userdata.class);

                                    Glide.with(AttendCallActivity.this).load(user.getProfileUrl())
                                            .placeholder(R.drawable.profile_placeholder).into(_aCallProfile);
                                    FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).child(friendsUsername)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()){
                                                        FriendsData friendsData = snapshot.getValue(FriendsData.class);
                                                        _aCallName.setText(friendsData.getUsername());
                                                    }
                                                    else{
                                                        _aCallName.setText(friendsUsername);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                }

                                @Override
                                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                }
                            });
                    FirebaseDatabase.getInstance().getReference()
                            .child("Call")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                }
                            });
                }
            }, 3000);
        }

    }

    public void onPeerConnected(){
        isPeerConnected = true;
    }

    private void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please check your internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    private void listenConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

                _aCallLoadingGroup.setVisibility(View.GONE);
                _aCallControls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void callJavaScriptFunction(String function){
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.evaluateJavascript(function, null);
            }
        });
    }

    private String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseRef.child(createdBy+incoming).child("callStatus").setValue("ended");
        pageExit = true;

        //firebaseRef.child(createdBy).setValue(null);
        finish();
    }



}