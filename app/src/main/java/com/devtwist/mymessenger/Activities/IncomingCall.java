package com.devtwist.mymessenger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;



import java.net.MalformedURLException;
import java.net.URL;

public class IncomingCall extends AppCompatActivity {

    private Intent intent;
    private String username, profileUri, userId, myId, meetingRoom;
    private ImageView _icProfilePic, _icRejectCallButton, _icAttendCallButton;
    private TextView _icUsername, _icCallStatus;
    private MediaPlayer player;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        _icProfilePic = findViewById(R.id._icCallProfilePic);
        _icUsername = findViewById(R.id._icCallUsername);
        _icCallStatus = findViewById(R.id._icCallStatus);
        _icAttendCallButton = findViewById(R.id._icAttendCallButton);
        _icRejectCallButton = findViewById(R.id._icRejectCallButton);

        intent = getIntent();
        Bundle bundle = intent.getExtras();
        //userId = intent.getStringExtra("userId");
        userId = bundle.getString("userId");
        myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        meetingRoom = userId+myId;

        player = MediaPlayer.create(IncomingCall.this,R.raw.call_ringtoon);
        player.start();

        FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Userdata userdata = snapshot.getValue(Userdata.class);
                        profileUri = userdata.getProfileUrl();
                        username = userdata.getUsername();
                        if (userdata.getProfileUrl().length()>1) {
                            Picasso.get().load(profileUri).placeholder(R.drawable.placeholder).into(_icProfilePic);
                        }
                        _icUsername.setText(username);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        _icAttendCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(IncomingCall.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Dexter.withActivity(IncomingCall.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                                        .child("callStatus").setValue("attended");
                                player.stop();
                                joinVideoCall(meetingRoom);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(IncomingCall.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(IncomingCall.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
            }
        });

        _icRejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom)
                        .child("callStatus").setValue("rejected");
                player.stop();
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Call").child(meetingRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.child("callStatus").getValue(String.class);
                if (status.equals("ended")){
                    Toast.makeText(IncomingCall.this, "Call ended by\n" + username, Toast.LENGTH_LONG).show();
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void joinVideoCall(String meetingRoom) {
        Intent intent = new Intent(IncomingCall.this, AttendCallActivity.class);
        //String incoming = childSnap.child("incoming").getValue(String.class);
        //String createdBy = childSnap.child("createdBy").getValue(String.class);
        //boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
        intent.putExtra("username", myId);
        intent.putExtra("incoming", myId);
        intent.putExtra("createdBy", userId);
        intent.putExtra("isAvailable", true);
        startActivity(intent);
        finish();
    }

}