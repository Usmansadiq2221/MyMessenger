package com.devtwist.mymessenger.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devtwist.mymessenger.Adapters.CallListAdapter;
import com.devtwist.mymessenger.Adapters.ChatListAdapter;
import com.devtwist.mymessenger.Adapters.UsersAdapter;
import com.devtwist.mymessenger.Models.CallListData;
import com.devtwist.mymessenger.Models.ChatListData;
import com.devtwist.mymessenger.Models.FriendsData;
import com.devtwist.mymessenger.Models.Userdata;
import com.devtwist.mymessenger.Models.Users;
import com.devtwist.mymessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessengerActivity extends AppCompatActivity {

    private float listPosition;
    private TextView _mTimerSmsText, _mTimelineText,  _mCallText, _chatText, _callText, _mEditProfile, _mContactText, _mPrivacyText, _mTermsText;
    private ImageView _mSearch, _messageIcon, _mBackIcon, _mMenuIcon;
    private LinearLayout _chatListLayout, _callListLayout, _searchFriendLayout, _mMenuLayout;
    private ShimmerRecyclerView _preChatListView, _preCallsListView, _searchFriend;
    private Intent intent;
    private View _chatView, _callView;
    private ArrayList<ChatListData> chatArrayList;
    private ArrayList<CallListData> callArrayList;
    private ChatListAdapter chatListAdapter;
    private CallListAdapter callListAdapter;
    private ConstraintLayout _preChatsLayout;

    private UsersAdapter usersAdapter;
    private List<String> contactsList, nameList;
    private DatabaseReference reference;

    private String myId,tempNumber;
    private boolean isMenuVisible, isFriendFound;
    private char type;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        _mTimerSmsText = findViewById(R.id._mTimerSmsText);
        _mTimelineText = findViewById(R.id._mTimelineText);
        _mCallText = findViewById(R.id._mCallText);
        _chatText = findViewById(R.id._chatText);
        _callText = findViewById(R.id._callText);
        _mSearch = findViewById(R.id._mSearch);
        _messageIcon = findViewById(R.id._messageIcon);
        _mBackIcon = findViewById(R.id._mBackIcon);
        _preChatsLayout = findViewById(R.id._preChatsLayout);
        _searchFriendLayout = findViewById(R.id._searchFriendLayout);
        _chatListLayout = findViewById(R.id._chatListLayout);
        _callListLayout = findViewById(R.id._callListLayout);
        _preChatListView = findViewById(R.id._preChatListView);
        _preCallsListView = findViewById(R.id._preCallsListView);
        _searchFriend = findViewById(R.id._searchFriend);
        _chatView = findViewById(R.id._chatView);
        _callView = findViewById(R.id._callView);
        _mMenuIcon =findViewById(R.id._mMenuIcon);
        _mEditProfile = findViewById(R.id._mEditProfile);
        _mContactText = findViewById(R.id._mContactText);
        _mMenuLayout = findViewById(R.id._mMenuLayout);
        _mPrivacyText = findViewById(R.id._mPrivacyText);
        _mTermsText = findViewById(R.id._mTermsText);

        listPosition = 1800f;
        _callListLayout.setTranslationX(listPosition);
        isMenuVisible = false;

        _searchFriend.setHasFixedSize(true);
        _searchFriend.setLayoutManager(new LinearLayoutManager(MessengerActivity.this));
        _searchFriend.setAdapter(usersAdapter);
        _searchFriend.showShimmerAdapter();
        contactsList = new ArrayList<>();
        nameList = new ArrayList<>();

        getContactList();

        myId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        _preChatsLayout.setVisibility(View.VISIBLE);
        _searchFriendLayout.setVisibility(View.GONE);

        _mMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuVisible){
                    _mMenuLayout.setVisibility(View.VISIBLE);
                    _mMenuLayout.animate().alpha(1f).setDuration(500);
                    new CountDownTimer(3000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            _mMenuLayout.animate().alpha(0f).setDuration(500);
                            _mMenuLayout.setVisibility(View.GONE);
                            isMenuVisible = false;
                        }
                    }.start();
                }

            }
        });

        _mContactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("Users").child("+923053073543").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Userdata userdata = snapshot.getValue(Userdata.class);
                        intent = new Intent(MessengerActivity.this, ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("myId", myId);
                        bundle.putString("userId", userdata.getUserId());
                        bundle.putString("username", userdata.getUsername());
                        bundle.putString("profileUri", userdata.getProfileUrl());
                        bundle.putString("token", userdata.getToken());
                        intent.putExtras(bundle);
                        isMenuVisible = false;
                        startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        _mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MessengerActivity.this, EditProfileActivity.class);
                startActivity(intent);
                isMenuVisible = false;
            }
        });

        _messageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _preChatsLayout.setVisibility(View.GONE);
                _searchFriendLayout.setVisibility(View.VISIBLE);
            }
        });

        _mBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _preChatsLayout.setVisibility(View.VISIBLE);
                _searchFriendLayout.setVisibility(View.GONE);
            }
        });

        _mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _preChatsLayout.setVisibility(View.GONE);
                _searchFriendLayout.setVisibility(View.VISIBLE);
            }
        });

        _mTimerSmsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MessengerActivity.this, SendSMSActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _mTimelineText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MessengerActivity.this, TimelineActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _chatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listPosition != 1800f){
                    listPosition = 1800f;
                    _callView.setAlpha(0f);
                    _callText.setAlpha(0.6f);
                    _chatText.setAlpha(1f);
                    _chatView.setAlpha(1f);
                    _chatListLayout.animate().translationXBy(listPosition).setDuration(400).start();
                    _callListLayout.animate().translationXBy(listPosition).setDuration(400).start();

                }
            }
        });

        _callText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listPosition == 1800f){
                    listPosition = -1800f;
                    _chatView.setAlpha(0f);
                    _chatText.setAlpha(0.6f);
                    _callText.setAlpha(1f);
                    _callView.setAlpha(1f);
                    _chatListLayout.animate().translationXBy(listPosition).setDuration(400).start();
                    _callListLayout.animate().translationXBy(listPosition).setDuration(400).start();

                }
            }
        });

        _mTermsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(MessengerActivity.this, ViewImage.class);
                Bundle mbundle = new Bundle();
                mbundle.putString("tag", "t");
                viewIntent.putExtras(mbundle);
                startActivity(viewIntent);
            }
        });

        _mPrivacyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(MessengerActivity.this, ViewImage.class);
                Bundle mbundle = new Bundle();
                mbundle.putString("tag", "p");
                viewIntent.putExtras(mbundle);
                startActivity(viewIntent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        _preChatListView.setLayoutManager(linearLayoutManager);

        chatArrayList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatArrayList, myId);
        _preChatListView.setAdapter(chatListAdapter);
        _preChatListView.showShimmerAdapter();
        readChatList();


        LinearLayoutManager callLayoutmanager = new LinearLayoutManager(this);
        callLayoutmanager.setReverseLayout(true);
        callLayoutmanager.setStackFromEnd(true);
        _preCallsListView.setLayoutManager(callLayoutmanager);

        callArrayList = new ArrayList<>();
        callListAdapter = new CallListAdapter(MessengerActivity.this, callArrayList);
        _preCallsListView.setAdapter(callListAdapter);
        _preCallsListView.showShimmerAdapter();
        readCallList();

    }


    private void getContactList() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            if (contactsList.contains(phone)) {
                cursor.moveToNext();
            } else {
                contactsList.add(phone);
                nameList.add(name);
            }
        }

        getActualUser();
        Log.d("contact: ", String.valueOf(contactsList.size()));

    }

    private void getActualUser() {
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Userdata> actualuserList = new ArrayList<>();
                actualuserList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Userdata userdata = dataSnapshot.getValue(Userdata.class);
                        for (int i=1; i<contactsList.size(); i++){
                            String number = contactsList.get(i);
                            String username = nameList.get(i);
                            if(number.length()==11) {
                                StringBuffer buffer = new StringBuffer(number);
                                buffer.deleteCharAt(0);
                                number = "+92" + buffer;

                            }
                            if (userdata.getUserId().equals(number) && !myId.equals(number)){
                                actualuserList.add(userdata);
                                reference.child("Friends").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        isFriendFound = false;
                                        if (snapshot.exists()){
                                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                                FriendsData friendsData = dataSnapshot1.getValue(FriendsData.class);
                                                if (userdata.getUserId().equals(friendsData.getUserId())){
                                                    isFriendFound = true;
                                                }
                                            }
                                        }
                                        //if (!isFriendFound){
                                        FriendsData friendsData = new FriendsData(userdata.getUserId(), false,username);
                                        reference.child("Friends").child(myId).child(userdata.getUserId()).setValue(friendsData);
                                        //}


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }
                    Log.d("actual: ", String.valueOf(actualuserList.size()));
                    usersAdapter = new UsersAdapter(MessengerActivity.this, actualuserList, _searchFriendLayout, _preChatsLayout);
                    _searchFriend.hideShimmerAdapter();
                    usersAdapter.notifyDataSetChanged();
                    _searchFriend.setAdapter(usersAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readCallList() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("CallList").child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    callArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CallListData callListData = dataSnapshot.getValue(CallListData.class);
                        callArrayList.add(callListData);
                    }
                    callArrayList.sort(Comparator.comparing(CallListData::getTimeStamp));
                    _preCallsListView.hideShimmerAdapter();
                    callListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Log.i("Call Error", e.getMessage().toString());
        }
    }

    private void readChatList(){
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("ChatList").child(myId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChatListData chatListData = dataSnapshot.getValue(ChatListData.class);
                        chatArrayList.add(chatListData);
                    }
                    chatArrayList.sort(Comparator.comparing(ChatListData::getTimeStamp));
                    _preChatListView.hideShimmerAdapter();
                    chatListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("ChatLstError", e.getMessage().toString());
        }
    }

}