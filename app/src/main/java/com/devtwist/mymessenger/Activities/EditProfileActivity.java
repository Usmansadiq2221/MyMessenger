package com.devtwist.mymessenger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView _editProfileImage;
    private EditText _editProfileName;
    private Button _editSaveButton;
    private ProgressBar _eProgressBar;
    private String myId;
    private Uri imageFilePath;
    private Bitmap bitmap;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        _editProfileImage = findViewById(R.id._editProfileImage);
        _editProfileName = findViewById(R.id._editProfileName);
        _editSaveButton = findViewById(R.id._editSaveButton);
        _eProgressBar = findViewById(R.id._eProgressBar);
        imageFilePath = null;
        myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        FirebaseDatabase.getInstance().getReference().child("Users").child(myId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Userdata userdata = snapshot.getValue(Userdata.class);
                        if (userdata.getProfileUrl().length()>1) {
                            Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(_editProfileImage);
                        }
                        _editProfileName.setText(userdata.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        _editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(EditProfileActivity.this)
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
                                Toast.makeText(EditProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        _editSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _editSaveButton.setVisibility(View.GONE);
                _eProgressBar.setVisibility(View.VISIBLE);
                saveChanges();
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
                        .start(EditProfileActivity.this);
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
                    _editProfileImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    private void saveChanges() {
        String name = _editProfileName.getText().toString().trim();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference uploadfile = storage.getReference().child("Profile Pictures").child(myId);
        if (name.length()<1){
            Toast.makeText(EditProfileActivity.this, "Enter name", Toast.LENGTH_SHORT).show();
        } else if (imageFilePath == null) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(myId)
                    .child("username").setValue(_editProfileName.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            _eProgressBar.setVisibility(View.GONE);
                            _editSaveButton.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            _eProgressBar.setVisibility(View.GONE);
                            _editSaveButton.setVisibility(View.VISIBLE);
                            Toast.makeText(EditProfileActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            uploadfile.putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(myId)
                                    .child("profileUrl").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(myId)
                                                    .child("username").setValue(_editProfileName.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            _eProgressBar.setVisibility(View.GONE);
                                                            _editSaveButton.setVisibility(View.VISIBLE);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            _eProgressBar.setVisibility(View.GONE);
                                                            _editSaveButton.setVisibility(View.VISIBLE);
                                                            Toast.makeText(EditProfileActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            _eProgressBar.setVisibility(View.GONE);
                                            _editSaveButton.setVisibility(View.VISIBLE);
                                            Toast.makeText(EditProfileActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
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