package com.devtwist.mymessenger.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.mymessenger.Activities.ChatActivity;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context mContext;
    private List<Userdata> mList;
    private Intent intent;
    private Bundle sendValues;
    private LinearLayout _searchFriendLayout;
    private ConstraintLayout _preChatsLayout;
    private String username,myId;

    public UsersAdapter(Context mContext, List<Userdata> mList, LinearLayout _searchFriendLayout, ConstraintLayout _preChatsLayout) {
        this.mContext = mContext;
        this.mList = mList;
        this._searchFriendLayout = _searchFriendLayout;
        this._preChatsLayout = _preChatsLayout;
        username = "";
        myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.display_contacts, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Userdata users = mList.get(position);
        holder._friendNameItem.setText(users.getUsername());
        if (users.getProfileUrl().length()>1) {
            Picasso.get().load(users.getProfileUrl()).placeholder(R.drawable.profile_placeholder).into(holder._friendProfileItem);
        }
        FirebaseDatabase.getInstance().getReference().child("Friends").child(myId).child(users.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FriendsData friendsData = snapshot.getValue(FriendsData.class);
                holder._friendNameItem.setText(friendsData.getUsername());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                        intent = new Intent(mContext, ChatActivity.class);
                        sendValues = new Bundle();
                        sendValues.putString("userId",users.getUserId());
                        sendValues.putString("username", friendsData.getUsername());
                        sendValues.putString("profileUri", users.getProfileUrl());
                        sendValues.putString("myId", myId);
                        sendValues.putString("token", users.getToken());
                        intent.putExtras(sendValues);
                        _searchFriendLayout.setVisibility(View.GONE);
                        _preChatsLayout.setVisibility(View.VISIBLE);
                        mContext.startActivity(intent);

                /*Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("name", users.getUsername());
                intent.putExtra("userid", users.getPhoneNumber());
                intent.putExtra("id", users.getUserId());
                mContext.startActivity(intent);*/
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
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView _friendNameItem;
        private ImageView _friendProfileItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            _friendNameItem = itemView.findViewById(R.id._friendNameItem);
            _friendProfileItem = itemView.findViewById(R.id._friendProfileItem);
        }
    }
}
