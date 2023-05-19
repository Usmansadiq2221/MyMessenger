package com.devtwist.mymessenger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView _userProfilePic;
    private TextView _userName, _userPhoneNo, _userActualName;
    private Intent intent;
    private Bundle bundle;
    private String imgUrl;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        _userProfilePic = findViewById(R.id._userProfilePic);
        _userActualName = findViewById(R.id._userActualName);
        _userName = findViewById(R.id._userName);
        _userPhoneNo = findViewById(R.id._userPhoneNo);
        intent = getIntent();
        bundle = intent.getExtras();
        imgUrl = bundle.getString("pic");

        FirebaseDatabase.getInstance().getReference().child("Users").child(bundle.getString("phone"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Userdata userdata = snapshot.getValue(Userdata.class);
                        _userActualName.setText(userdata.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        try {
            if (bundle.getString("name").equals(bundle.getString("phone"))){
                _userName.setText("");
            }else {
                _userName.setText(bundle.getString("name"));
            }
            _userPhoneNo.setText(bundle.getString("phone"));
            Picasso.get().load(imgUrl).placeholder(R.drawable.profile_placeholder).into(_userProfilePic);
        } catch (Exception e) {
            e.printStackTrace();
        }

        _userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(UserProfileActivity.this, ViewImage.class);
                bundle = new Bundle();
                bundle.putString("tag","i");
                bundle.putString("imgUrl", imgUrl);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }
}