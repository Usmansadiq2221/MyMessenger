package com.devtwist.mymessenger.Activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devtwist.mymessenger.Adapters.MessageAdapter;
import com.devtwist.mymessenger.AlarmReceiver;
import com.devtwist.mymessenger.Models.CallData;
import com.devtwist.mymessenger.Models.CallListData;
import com.devtwist.mymessenger.Models.ChatListData;
import com.devtwist.mymessenger.Models.MessageData;
import com.devtwist.mymessenger.Models.SendNotification;
import com.devtwist.mymessenger.R;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String profilePicString, senderUsername, username, receiverUId, senderUId, userToken;
    private TextView _chatUsername, _typingStatus, _setOnlineTimerText;
    private EditText _messageInput;
    private ImageView _chatbackbutton, _chatProfilePic, _chatVCallIcon, _sendMessage,
            _sendAlMessage, _imageMessage,_cancelOTimer, _cancelChatImage, _chatImage;
    private TimePicker _timePickerOnline;
    private ShimmerRecyclerView _preMessagesView;
    private MessageAdapter messageAdapter;
    private ArrayList <MessageData> messages;
    private String senderRoom, receiverRoom, mDate, mTime;

    private Uri imageFilePath;
    private Intent intent;
    private PendingIntent pendingIntent;
    private Bundle bundle;
    private FirebaseDatabase database;
    private DatabaseReference reference,reference2;
    private FirebaseStorage storage;
    private ProgressDialog dialog;
    private AlarmManager alarmManager;
    private LinearLayoutManager linearLayoutManager;


    private ValueEventListener msgSeenListener1, msgSeenListener2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            _chatUsername = findViewById(R.id._chatUsername);
            _chatbackbutton = findViewById(R.id._chatBackButton);
            _chatProfilePic = findViewById(R.id._chatProfilePic);
            _chatVCallIcon = findViewById(R.id._chatVCallIcon);
            _imageMessage = findViewById(R.id._imageMessage);
            _sendMessage = findViewById(R.id._sendMessage);
            _sendAlMessage = findViewById(R.id._sendAlMessage);
            _messageInput = findViewById(R.id._messageInput);
            _preMessagesView = findViewById(R.id._preMessagesView);
            _timePickerOnline = findViewById(R.id._timePickerOnline);
            _setOnlineTimerText = findViewById(R.id._setOnlineTimerText);
            _cancelOTimer = findViewById(R.id._cancelOTimer);
            _chatImage = findViewById(R.id._chatImage);
            _cancelChatImage = findViewById(R.id._cancelChatImage);

            //geting intent value from previous activity...
            intent = getIntent();
            bundle = intent.getExtras();
            receiverUId = bundle.getString("userId");
            senderUId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            profilePicString = bundle.getString("profileUri");
            username = bundle.getString("username");
            userToken = bundle.getString("token");
            imageFilePath = null;

            //initializing progress dialoge for sending image type message...
            dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading Image...");
            dialog.setCancelable(false);

            _chatUsername.setText(username);
            //assigning values to the views...
            if (profilePicString.length()>1) {
                Picasso.get().load(profilePicString).placeholder(R.drawable.profile_placeholder).into(_chatProfilePic);
            }

            _chatUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                    bundle.putString("name", username);
                    bundle.putString("phone", receiverUId);
                    bundle.putString("pic", profilePicString);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            //initializing chat room for sender and receiver to store messages...
            senderRoom = senderUId + receiverUId;
            receiverRoom = receiverUId + senderUId;

            Log.i("SenderId",senderUId);
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(senderUId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            senderUsername = snapshot.child("username").getValue(String.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            messages = new ArrayList<>();
            messageAdapter = new MessageAdapter(this, messages, senderUId,receiverUId);
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();

            try {
                database.getReference().child("Chat")
                        .child(senderRoom)
                        .child("messages")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    messages.clear();
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        MessageData messageData = dataSnapshot.getValue(MessageData.class);
                                        messages.add(messageData);
                                    }
                                    messages.sort(Comparator.comparing(MessageData::getTimestamp));
                                } catch (Exception e) {
                                    Log.i("ChatError", e.getMessage().toString());
                                }
                                _preMessagesView.hideShimmerAdapter();
                                messageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } catch (Exception e) {
                Log.i("Error Chat", e.getMessage().toString());
            }

            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);
            //linearLayoutManager.setReverseLayout(true);
            _preMessagesView.setLayoutManager(linearLayoutManager);
            _preMessagesView.setAdapter(messageAdapter);
            _preMessagesView.showShimmerAdapter();

            _preMessagesView.scrollToPosition(messages.size()-1);


            _imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dexter.withActivity(ChatActivity.this)
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
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();


                    /*intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);

                     */
                }
            });

            _sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessage();
                }
            });

            _chatVCallIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dexter.withActivity(ChatActivity.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            Dexter.withActivity(ChatActivity.this).withPermission(Manifest.permission.RECORD_AUDIO).withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    String messageBody = senderUId;
                                    CallData callData = new CallData("calling", "VC", senderUId, receiverUId, "");
                                    String randomKey = database.getReference().push().getKey();
                                    database.getReference().child("Call")
                                            .child(senderRoom)
                                            .setValue(callData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    String callRandomkey = database.getReference().push().getKey();
                                                    Date date = new Date();
                                                    mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                                    mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                                                    double timeStamp = date.getTime();
                                                    CallListData callListData = new CallListData(callRandomkey, receiverUId,"VC", mDate, mTime,timeStamp);
                                                    database.getReference().child("CallList").child(senderUId)
                                                            .child(callRandomkey).setValue(callListData);

                                                    callListData = new CallListData(callRandomkey, senderUId,"VC", mDate, mTime, timeStamp);
                                                    database.getReference().child("CallList").child(receiverUId)
                                                            .child(callRandomkey).setValue(callListData);

                                                    SendNotification sendNotification = new SendNotification("VC", messageBody, userToken, senderUId, ChatActivity.this);
                                                    sendNotification.sendNotification();
                                                }
                                            });
                                    nextActivity();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(ChatActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

                }
            });

            _sendAlMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(_messageInput.getText().length()>0 || imageFilePath!=null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                        _timePickerOnline.setVisibility(View.VISIBLE);
                        _setOnlineTimerText.setVisibility(View.VISIBLE);
                        _cancelOTimer.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(ChatActivity.this, "Enter SMS", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            _cancelOTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _cancelOTimer.setVisibility(View.GONE);
                    _setOnlineTimerText.setVisibility(View.GONE);
                    _timePickerOnline.setVisibility(View.GONE);
                }
            });

            _setOnlineTimerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTime();
                    //sendWhatsappMessage();
                    _cancelOTimer.setVisibility(View.GONE);
                    _setOnlineTimerText.setVisibility(View.GONE);
                    _timePickerOnline.setVisibility(View.GONE);
                    _sendAlMessage.setVisibility(View.VISIBLE);
                    _messageInput.setText("");
                    //_optionLayout.setVisibility(View.VISIBLE);
                }
            });

            _cancelChatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _cancelChatImage.setVisibility(View.GONE);
                    _chatImage.setVisibility(View.GONE);
                    _chatImage.setImageResource(R.drawable.placeholder);
                    imageFilePath = null;
                }
            });

            _chatImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewIntent = new Intent(ChatActivity.this, ViewImage.class);
                    Bundle viewBundle = new Bundle();
                    viewBundle.putString("tag","i");
                    viewBundle.putString("imgUrl", imageFilePath.toString());
                    viewIntent.putExtras(viewBundle);
                    startActivity(viewIntent);
                }
            });

        } catch (Exception e) {
            Log.i("Chat Error", e.getMessage().toString());
        }

        _chatbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        seenMessage(receiverUId);

    }

    private void sendMessage(){
        Date date = new Date();
        mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String messageText = _messageInput.getText().toString().trim();
        String randomKey = database.getReference().push().getKey();
        if (messageText.length()<1 && imageFilePath==null){
            Toast.makeText(ChatActivity.this, "Enter Message!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (imageFilePath!=null){
                try {
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(imageFilePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        dialog.dismiss();

                                        MessageData messageData = new MessageData(randomKey, messageText, senderUId, filePath, receiverRoom, mTime, mDate, date.getTime(),false, "img");
                                        database.getReference().child("Chat")
                                                .child(senderRoom)
                                                .child("messages").child(randomKey)
                                                .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        database.getReference().child("Chat")
                                                                .child(receiverRoom)
                                                                .child("messages").child(randomKey)
                                                                .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        SendNotification sendNotification = new SendNotification(senderUsername, "Photo", userToken, senderUId, ChatActivity.this);
                                                                        sendNotification.sendNotification();

                                                                        double timeStamp = date.getTime();
                                                                        ChatListData chatListData = new ChatListData(receiverUId, "imageMessage",timeStamp);
                                                                        database.getReference().child("ChatList").child(senderUId)
                                                                                .child(receiverUId).setValue(chatListData);

                                                                        chatListData = new ChatListData(senderUId, "imageMessage",timeStamp);
                                                                        database.getReference().child("ChatList").child(receiverUId)
                                                                                .child(senderUId).setValue(chatListData);
                                                                    }
                                                                });
                                                    }
                                                });
                                        _messageInput.setText("");
                                    }
                                });
                            }
                        }
                    });
                    _messageInput.setText("");
                    _cancelChatImage.setVisibility(View.GONE);
                    _chatImage.setVisibility(View.GONE);
                    _chatImage.setImageResource(R.drawable.placeholder);
                    imageFilePath = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                MessageData messageData = new MessageData(randomKey, messageText, senderUId, "", receiverRoom, mTime, mDate, date.getTime(),false, "txt");
                database.getReference().child("Chat")
                        .child(senderRoom)
                        .child("messages").child(randomKey)
                        .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("Chat")
                                        .child(receiverRoom)
                                        .child("messages").child(randomKey)
                                        .setValue(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String notificationId = FirebaseDatabase.getInstance().getReference().push().getKey();
                                                //NotificationData notificationData = new NotificationData(notificationId, senderUId, "message", senderUsername, messageText, false);
                                                //FirebaseDatabase.getInstance().getReference().child("Notifications").child(receiverUId).child(notificationId).setValue(notificationData);

                                                SendNotification sendNotification = new SendNotification(senderUsername, messageText, userToken, senderUId, ChatActivity.this);
                                                sendNotification.sendNotification();

                                                //sendMessageNotification(username, messageText, userToken);

                                                double timeStamp = date.getTime();
                                                ChatListData chatListData = new ChatListData(receiverUId, messageText, timeStamp);
                                                database.getReference().child("ChatList").child(senderUId)
                                                        .child(receiverUId).setValue(chatListData);

                                                chatListData = new ChatListData(senderUId, messageText, timeStamp);
                                                database.getReference().child("ChatList").child(receiverUId)
                                                        .child(senderUId).setValue(chatListData);

                                                _preMessagesView.scrollToPosition(messages.size() - 1);

                                            }
                                        });
                            }
                        });
                _messageInput.setText("");
            }
        }
    }

    private void setTime(){
        final long[] time = new long[1];
        Calendar calendar = Calendar.getInstance();
        String type = "online", enterSMS = "";
        String messageText = _messageInput.getText().toString().trim();

        enterSMS = _messageInput.getText().toString().trim();
        // calendar is called to get current time in hour and minute
        calendar.set(Calendar.HOUR_OF_DAY, _timePickerOnline.getCurrentHour());
        calendar.set(Calendar.MINUTE, _timePickerOnline.getCurrentMinute());


        // using intent i have class AlarmReceiver class which inherits
        // BroadcastReceiver
        if (messageText.length()<1 && imageFilePath==null){
            Toast.makeText(ChatActivity.this, "Enter Message!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (imageFilePath!=null){
                try {
                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    String finalEnterSMS = enterSMS;
                    reference.putFile(imageFilePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        dialog.dismiss();
                                        Intent intent = new Intent(ChatActivity.this, AlarmReceiver.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("sms", finalEnterSMS);
                                        bundle.putString("type", type);
                                        bundle.putString("sRoom", senderRoom);
                                        bundle.putString("rRoom", receiverRoom);
                                        bundle.putString("sender", senderUId);
                                        bundle.putString("receiver",receiverUId);
                                        bundle.putString("sUname",senderUsername);
                                        bundle.putString("token", userToken);
                                        bundle.putString("url", filePath);
                                        intent.putExtras(bundle);
                                        intent.setAction("BackgroundProcess");
                                        // we call broadcast using pendingIntent
                                        final int id = (int) System.currentTimeMillis();
                                        pendingIntent = PendingIntent.getBroadcast(ChatActivity.this, id, intent, PendingIntent.FLAG_ONE_SHOT);

                                        time[0] = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

                                        if (System.currentTimeMillis() > time[0]) {
                                            // setting time as AM and PM
                                            if (calendar.AM_PM == 0)
                                                time[0] = time[0] + (1000 * 60 * 60 * 12);
                                            else
                                                time[0] = time[0] + (1000 * 60 * 60 * 24);
                                        }
                                        // Alarm rings continuously until toggle button is turned off
                                        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                        alarmManager.set(AlarmManager.RTC_WAKEUP, time[0],pendingIntent);

                                    }
                                });
                            }
                        }
                    });
                    _messageInput.setText("");
                    _cancelChatImage.setVisibility(View.GONE);
                    _chatImage.setVisibility(View.GONE);
                    _chatImage.setImageResource(R.drawable.placeholder);
                    imageFilePath = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Intent intent = new Intent(ChatActivity.this, AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putString("sms", enterSMS);
                bundle.putString("type", type);
                bundle.putString("sRoom", senderRoom);
                bundle.putString("rRoom", receiverRoom);
                bundle.putString("sender", senderUId);
                bundle.putString("receiver",receiverUId);
                bundle.putString("sUname",senderUsername);
                bundle.putString("token", userToken);
                bundle.putString("url", "");
                intent.putExtras(bundle);
                intent.setAction("BackgroundProcess");
                // we call broadcast using pendingIntent
                final int id = (int) System.currentTimeMillis();
                pendingIntent = PendingIntent.getBroadcast(ChatActivity.this, id, intent, PendingIntent.FLAG_ONE_SHOT);

                time[0] = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

                if (System.currentTimeMillis() > time[0]) {
                    // setting time as AM and PM
                    if (calendar.AM_PM == 0)
                        time[0] = time[0] + (1000 * 60 * 60 * 12);
                    else
                        time[0] = time[0] + (1000 * 60 * 60 * 24);
                }
                // Alarm rings continuously until toggle button is turned off
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 10000, pendingIntent);
                //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (time * 1000), pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, time[0],pendingIntent);
            }
            Toast.makeText(this, "Message successfully scheduled!", Toast.LENGTH_SHORT).show();
        }

    }

    private void  sendMessageNotification(String name, String message, String token){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject objectData = new JSONObject();
            if (name.length()<3) {
                final String[] messageBody = new String[1];
                FirebaseDatabase.getInstance().getReference().child("Users").child(message)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messageBody[0] = snapshot.child("username").getValue(String.class);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                objectData.put("title", "Incoming Video Call");
                objectData.put("body", messageBody[0]);
            }
            else{
                objectData.put("title", name);
                objectData.put("body", message);
            }
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", objectData);
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("NtfyError", error.toString());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    String serverKey = "Key=AAAAhEUOPTI:APA91bFvwZaqNvixtSQyORHLHx-UaMsOp4yf7IUQehKxSETaacCBizqS87fgjZyEs7_ZVrCcDnHtp8Pml1kY4J1GbxvIlWzqDxRob1Gr_iZGJxPQh0BCb6ggagRdsWYcDYZTgfCK93KL";
                    map.put("Authorization", serverKey);
                    map.put("Content-Type", "application/json");
                    return map;
                }
            };
            requestQueue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void seenMessage(String userId){
        String myId= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        reference = FirebaseDatabase.getInstance().getReference("Chat").child(myId+userId).child("messages");
        reference2 = FirebaseDatabase.getInstance().getReference("Chat").child(userId+myId).child("messages");
        msgSeenListener1 = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    MessageData messageData = dataSnapshot.getValue(MessageData.class);
                    if(messageData.getSenderId().equals(myId)){

                    }else{
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        msgSeenListener2 = reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    MessageData messageData = dataSnapshot.getValue(MessageData.class);
                    if(messageData.getSenderId().equals(myId)){

                    }else{
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void nextActivity(){
        intent = new Intent(ChatActivity.this, OutGoingCall.class);
        bundle = new Bundle();
        bundle.putString("userId", receiverUId);
        bundle.putString("myId", senderUId);
        bundle.putString("profileUri", profilePicString);
        bundle.putString("username", username);
        bundle.putString("token", userToken);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode == RESULT_OK ) {
            imageFilePath = data.getData();
            try {
                CropImage.activity(imageFilePath)
                        .start(ChatActivity.this);
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
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    _chatImage.setImageBitmap(bitmap);
                    _chatImage.setVisibility(View.VISIBLE);
                    _cancelChatImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(msgSeenListener1);
        reference2.removeEventListener(msgSeenListener2);
    }
}