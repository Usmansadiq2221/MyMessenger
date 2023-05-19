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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.mymessenger.Activities.ChatActivity;
import com.devtwist.mymessenger.Models.ChatListData;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.MessageData;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>{

    private Context context;
    private Intent intent;
    private Bundle bundle;
    private ArrayList<ChatListData> chatListUser;
    private String myId;
    private boolean isOpened;
    private CountDownTimer timer;
    private float tVal;

    public ChatListAdapter(Context context, ArrayList<ChatListData> chatListUser, String myId) {
        this.context = context;
        this.chatListUser = chatListUser;
        this.myId = myId;
        isOpened = false;
        tVal = 100;

    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_items, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        ChatListData model = chatListUser.get(position);
        String userId = model.getUserId();
        Log.i("UserId", userId);
        holder._userChatLayout.setTranslationX(0);
        holder._chatDeleteItem.setVisibility(View.GONE);
        if (model.getLastMessage().equals("imageMessage")){
            holder._lMImageItem.setVisibility(View.VISIBLE);
            holder._lastMessageItem.setText("Photo");
        }else{
            holder._lMImageItem.setVisibility(View.GONE);
            holder._lastMessageItem.setText(model.getLastMessage());
        }
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Userdata userdata = snapshot.getValue(Userdata.class);
                if (userdata.getProfileUrl().length()>1) {
                    Picasso.get().load(userdata.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(holder._chatListProfileItem);
                }
                FirebaseDatabase.getInstance().getReference("Chat")
                        .child(model.getUserId()+myId).child("messages")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            MessageData messageData = dataSnapshot.getValue(MessageData.class);
                            if(messageData.getSenderId().equals(model.getUserId())){
                                holder._viewed.setVisibility(View.VISIBLE);
                                if (messageData.isSeen()){
                                    holder._viewed.setVisibility(View.GONE);
                                }else{
                                    holder._viewed.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String username = "";
                            if (snapshot.exists()){
                                FriendsData friendsData = snapshot.getValue(FriendsData.class);
                                username = friendsData.getUsername();
                            }
                            else{
                                username = userId;
                            }
                            holder._chatListUsernameItem.setText(username);
                            String finalUsername = username;
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    intent = new Intent(v.getContext(), ChatActivity.class);
                                    bundle = new Bundle();
                                    bundle.putString("myId", myId);
                                    bundle.putString("userId", userdata.getUserId());
                                    bundle.putString("username", finalUsername);
                                    bundle.putString("profileUri", userdata.getProfileUrl());
                                    bundle.putString("token", userdata.getToken());
                                    intent.putExtras(bundle);
                                    v.getContext().startActivity(intent);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                isOpened = false;
                tVal = 100;
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!isOpened) {
                            isOpened = true;
                            holder._userChatLayout.animate().translationXBy(-100).setDuration(500);
                            new CountDownTimer(500, 500) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    holder._chatDeleteItem.setVisibility(View.VISIBLE);
                                }
                            }.start();

                            timer = new CountDownTimer(4000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    holder._chatDeleteItem.setVisibility(View.GONE);
                                    tVal = 0;
                                    holder._userChatLayout.animate().translationXBy(100).setDuration(500);
                                    isOpened = false;
                                }
                            };
                            timer.start();
                            holder._chatDeleteItem.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Delete this chat")
                                            .setIcon(R.drawable.app_icon)
                                            .setMessage("Are you sure you want to delete this chat?")

                                            // Specifying a listener allows you to take an action before dismissing the dialog.
                                            // The dialog is automatically dismissed when a dialog button is clicked.
                                            .setPositiveButton("Delete Chat", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // Continue with delete operation
                                                    new CountDownTimer(500, 500) {
                                                        @Override
                                                        public void onTick(long millisUntilFinished) {
                                                            holder._chatDeleteItem.setVisibility(View.GONE);
                                                            holder._userChatLayout.animate().translationXBy(1000).setDuration(500);
                                                        }

                                                        @Override
                                                        public void onFinish() {
                                                            FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId)
                                                                    .child(userId).removeValue();
                                                            FirebaseDatabase.getInstance().getReference().child("Chat")
                                                                    .child(myId + userId).removeValue();
                                                        }
                                                    }.start();


                                                    isOpened = false;

                                                }
                                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialog) {
                                                    isOpened = false;
                                                }
                                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    isOpened = false;
                                                }
                                            }).setCancelable(true)
                                            .show();
                                }
                            });
                        }
                        return true;
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatListUser.size();
    }


    public class ChatListViewHolder extends RecyclerView.ViewHolder{

        private ImageView _chatListProfileItem,_viewed, _chatDeleteItem, _lMImageItem;
        private TextView _chatListUsernameItem, _lastMessageItem;
        private LinearLayout _userChatLayout;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            _chatListProfileItem = (ImageView) itemView.findViewById(R.id._listProfileItem);
            _viewed = (ImageView) itemView.findViewById(R.id._viewed);
            _chatListUsernameItem = (TextView) itemView.findViewById(R.id._listUsernameItem);
            _chatDeleteItem = (ImageView) itemView.findViewById(R.id._chatDeleteItem);
            _userChatLayout = (LinearLayout) itemView.findViewById(R.id._userChatLayout);
            _lMImageItem = (ImageView) itemView.findViewById(R.id._lMImageItem);
            _lastMessageItem = (TextView) itemView.findViewById(R.id._lastMessageItem);

        }
    }
}
