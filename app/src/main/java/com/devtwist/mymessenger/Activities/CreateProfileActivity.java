package com.devtwist.mymessenger.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CreateProfileActivity extends AppCompatActivity {

    private ImageView _createProfileImage;
    private EditText _enterName;
    private Button _createProfileButton;
    private Bitmap bitmap;
    private Uri imageFilePath;
    private Userdata userData;
    private SharedPreferences preferences;
    private ProgressBar _cProgressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        _createProfileImage = findViewById(R.id._createProfileImage);
        _enterName = findViewById(R.id._enterName);
        _createProfileButton = findViewById(R.id._createProfileButton);
        _cProgressBar = findViewById(R.id._cProgressBar);

        _createProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(CreateProfileActivity.this)
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
                                Toast.makeText(CreateProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        _createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _createProfileButton.setVisibility(View.GONE);
                _cProgressBar.setVisibility(View.VISIBLE);
                uploadToFirebase();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode == RESULT_OK ) {
            imageFilePath = data.getData();
            try {
                CropImage.activity(imageFilePath)
                        .setAspectRatio(1,1)
                        .start(CreateProfileActivity.this);
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
                    _createProfileImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void uploadToFirebase() {
        String name = _enterName.getText().toString().trim();
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference uploadfile = storage.getReference().child("Profile Pictures").child(phoneNumber);
        if (name.length()<1){
            Toast.makeText(CreateProfileActivity.this, "Enter name", Toast.LENGTH_SHORT).show();
        } else if (imageFilePath == null) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    userData = new Userdata(phoneNumber, name, "", s);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            preferences = getSharedPreferences("MyMessengerData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isProfileCreated", true);
                            editor.commit();
                            _cProgressBar.setVisibility(View.GONE);
                            _createProfileButton.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(CreateProfileActivity.this, SendSMSActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            _cProgressBar.setVisibility(View.GONE);
                            _createProfileButton.setVisibility(View.VISIBLE);
                            Toast.makeText(CreateProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            uploadfile.putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    userData = new Userdata(phoneNumber, name, uri.toString(), s);
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            preferences = getSharedPreferences("MyMessengerData", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putBoolean("isProfileCreated", true);
                                            editor.commit();
                                            Intent intent = new Intent(CreateProfileActivity.this, TimelineActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateProfileActivity.this, "Check your internet\nconnection", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    });

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    //dialog.setMessage("Upload :" + (int)percent + "%");
                }
            });
        }
    }
}