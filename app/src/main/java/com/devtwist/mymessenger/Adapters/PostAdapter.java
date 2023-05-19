package com.devtwist.mymessenger.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devtwist.mymessenger.Activities.ViewImage;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.PostData;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private String postUserIdString, myId, postUsername, profileUri, token;
    private DatabaseReference postRefrence;
    private Context context;
    private ArrayList<PostData> postList;

    public PostAdapter(Context context, ArrayList<PostData> postList, String myId) {
        this.context = context;
        this.postList = postList;
        this.myId = myId;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_items, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        try {
            holder._postLayoutItem.setAlpha(1f);
            Log.i("PostAdapter","successfull");
            PostData model = postList.get(position);
            postUserIdString = model.getUserId();
            holder._postTimeItem.setText(model.getPostDate() + "  " + model.getPostTime());
            if (model.getPostText().length()>0){
                holder._postTextItem.setText(model.getPostText());
                holder._postTextItem.setVisibility(View.VISIBLE);
            }
            if (model.getPostImageUrl().length()>3){
                holder._pImageLayout.setVisibility(View.VISIBLE);
                if (model.getPostImageUrl().length()>1) {
                    Picasso.get().load(model.getPostImageUrl()).into(holder._postImageItem);
                }
                holder._postImageItem.setVisibility(View.VISIBLE);
                holder._postImageItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ViewImage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("tag", "i");
                        bundle.putString("imgUrl", model.getPostImageUrl());
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                });
                holder._pImageLoadingItem.setVisibility(View.GONE);
            }
            else{
                holder._pImageLayout.setVisibility(View.GONE);
                holder._postImageItem.setVisibility(View.GONE);
            }

            postRefrence = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUserId());
            postRefrence.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Userdata userdata = snapshot.getValue(Userdata.class);
                    token = userdata.getToken();
                    if (userdata.getProfileUrl().length()>1) {
                        //Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(holder._postUserProfile);
                        Glide.with(context).load(userdata.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(holder._postUserProfile);
                    }
                    if (model.getUserId().equals(myId)) {
                        holder._postUsernameItem.setText(userdata.getUsername());
                    }
                    else{
                        FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).child(model.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            FriendsData friendsData = snapshot.getValue(FriendsData.class);
                                            holder._postUsernameItem.setText(friendsData.getUsername());
                                        }
                                        else{
                                            holder._postUsernameItem.setText(model.getUserId());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if (model.getUserId().equals(myId)){
                holder._deletePostItem.setVisibility(View.VISIBLE);
            }

            holder._deletePostItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete this post")
                            .setIcon(R.drawable.app_icon)
                            .setMessage("Are you sure you want to delete this chat?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton("Delete Post", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    holder._deletePostItem.setVisibility(View.GONE);
                                    holder._pImgDellProgress.setVisibility(View.VISIBLE);
                                    if (model.getPostImageUrl().length()>1){
                                        StorageReference deleteImg = FirebaseStorage.getInstance().getReferenceFromUrl(model.getPostImageUrl());
                                        deleteImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                new CountDownTimer(500, 500) {
                                                    @Override
                                                    public void onTick(long millisUntilFinished) {
                                                        holder._postLayoutItem.animate().translationXBy(1000).setDuration(500);
                                                    }

                                                    @Override
                                                    public void onFinish() {
                                                        FirebaseDatabase.getInstance().getReference().child("Posts")
                                                                .child(model.getPostId()).removeValue();
                                                    }
                                                }.start();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                holder._deletePostItem.setVisibility(View.VISIBLE);
                                                holder._pImgDellProgress.setVisibility(View.GONE);
                                                Toast.makeText(context, "Network Error!", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    else {
                                        holder._deletePostItem.setVisibility(View.GONE);
                                        holder._pImgDellProgress.setVisibility(View.VISIBLE);
                                        new CountDownTimer(500, 500) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                                holder._postLayoutItem.animate().translationXBy(1000).setDuration(500);
                                            }

                                            @Override
                                            public void onFinish() {
                                                FirebaseDatabase.getInstance().getReference().child("Posts")
                                                        .child(model.getPostId()).removeValue().addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                holder._pImgDellProgress.setVisibility(View.GONE);
                                                                holder._deletePostItem.setVisibility(View.VISIBLE);
                                                                Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        }.start();
                                    }

                                }
                            }).setNegativeButton(android.R.string.cancel, null)
                            .setCancelable(true)
                            .show();
                }
            });



        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder{
        private ImageView _postUserProfile, _postImageItem, _deletePostItem;
        private TextView _postUsernameItem, _postTimeItem, _postTextItem;
        private ConstraintLayout _pImageLayout;
        private ProgressBar _pImageLoadingItem, _pImgDellProgress;
        private ConstraintLayout _postLayoutItem;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                _postImageItem = (ImageView) itemView.findViewById(R.id._postImageItem);
                _postUserProfile = (ImageView) itemView.findViewById(R.id._postUserProfile);
                _postUsernameItem = (TextView) itemView.findViewById(R.id._postUsernameItem);
                _postTimeItem = (TextView) itemView.findViewById(R.id._postTimeItem);
                _postTextItem = (TextView) itemView.findViewById(R.id._postTextItem);
                _pImageLayout = (ConstraintLayout) itemView.findViewById(R.id._pImageLayout);
                _pImageLoadingItem = (ProgressBar) itemView.findViewById(R.id._pImageLoadingItem);
                _deletePostItem = (ImageView) itemView.findViewById(R.id._deletePostItem);
                _postLayoutItem = (ConstraintLayout) itemView.findViewById(R.id._postLayoutItem);
                _pImgDellProgress = (ProgressBar) itemView.findViewById(R.id._pImgDellProgress);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("PostViewHolder Error", e.getMessage().toString());
            }

        }
    }
}
