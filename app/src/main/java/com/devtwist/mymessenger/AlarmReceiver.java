package com.devtwist.mymessenger;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.devtwist.mymessenger.Activities.ChatActivity;
import com.devtwist.mymessenger.Models.ChatListData;
import com.devtwist.mymessenger.Models.MessageData;
import com.devtwist.mymessenger.Models.SendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {

    private String contactNo, smsText, type, mDate, mTime;
    private FirebaseDatabase database;
    private ChatActivity chatActivity;
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    // implement onReceive() method
    public void onReceive(Context context, Intent intent) {

        final PendingResult pendingResult = goAsync();
        backgroundExecutor.execute(() -> {
            try {
                contactNo = smsText = type = "";
                // we will use vibrator first
                Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                vibrator.vibrate(4000);

                database = FirebaseDatabase.getInstance();

                Bundle bundle = new Bundle();
                bundle = intent.getExtras();
                smsText = bundle.getString("sms");
                type = bundle.getString("type");

                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }

                if (type.equals("SMS")) {
                    contactNo = bundle.getString("contactNo");
                    SmsManager smsManager;
                    smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contactNo, null, smsText, null, null);
                    Toast.makeText(context, "successfully Delivered", Toast.LENGTH_LONG).show();
                }

                if (type.equals("online")) {
                    String senderUId = bundle.getString("sender");
                    String receiverUId = bundle.getString("receiver");
                    String senderRoom = bundle.getString("sRoom");
                    String receiverRoom = bundle.getString("rRoom");
                    String senderUsername = bundle.getString("sUname");
                    String userToken = bundle.getString("token");
                    String imgUrl = bundle.getString("url");


                    Date date = new Date();
                    mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String randomKey = database.getReference().push().getKey();
                    //MessageData messageData = new MessageData(randomKey, smsText, senderUId, " ", receiverRoom, mTime, mDate, date.getTime(),false);

                    if (imgUrl.length() > 3) {
                        try {
                            MessageData messageData = new MessageData(randomKey, smsText, senderUId, imgUrl, receiverRoom, mTime, mDate, date.getTime(), false, "img");

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
                                                            SendNotification sendNotification = new SendNotification(senderUsername, "Photo", userToken, senderUId, context);
                                                            sendNotification.sendNotification();

                                                            double timeStamp = date.getTime();
                                                            ChatListData chatListData = new ChatListData(receiverUId, "imageMessage", timeStamp);
                                                            database.getReference().child("ChatList").child(senderUId)
                                                                    .child(receiverUId).setValue(chatListData);

                                                            chatListData = new ChatListData(senderUId, "imageMessage", timeStamp);
                                                            database.getReference().child("ChatList").child(receiverUId)
                                                                    .child(senderUId).setValue(chatListData);
                                                        }
                                                    });
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        MessageData messageData = new MessageData(randomKey, smsText, senderUId, "", receiverRoom, mTime, mDate, date.getTime(), false, "txt");
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

                                                        SendNotification sendNotification = new SendNotification(senderUsername, smsText, userToken, senderUId, context);
                                                        sendNotification.sendNotification();

                                                        //sendMessageNotification(username, messageText, userToken);

                                                        double timeStamp = date.getTime();
                                                        ChatListData chatListData = new ChatListData(receiverUId, smsText, timeStamp);
                                                        database.getReference().child("ChatList").child(senderUId)
                                                                .child(receiverUId).setValue(chatListData);

                                                        chatListData = new ChatListData(senderUId, smsText, timeStamp);
                                                        database.getReference().child("ChatList").child(receiverUId)
                                                                .child(senderUId).setValue(chatListData);


                                                    }
                                                });
                                    }
                                });
                    }
                }
            } finally {
                // Must call finish() so the BroadcastReceiver can be recycled
                pendingResult.finish();
            }
        });
        Toast.makeText(context, "successfully Delivered", Toast.LENGTH_LONG).show();

        /*contactNo = smsText = type = "";
        // we will use vibrator first
        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        vibrator.vibrate(4000);

        database = FirebaseDatabase.getInstance();

        Bundle bundle = new Bundle();
        bundle = intent.getExtras();
        smsText = bundle.getString("sms");
        type = bundle.getString("type");

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        if (type.equals("SMS")) {
            contactNo = bundle.getString("contactNo");
            SmsManager smsManager;
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contactNo, null, smsText, null, null);
            Toast.makeText(context, "successfully Delivered", Toast.LENGTH_LONG).show();
        }

        if (type.equals("online")){
            String senderUId = bundle.getString("sender");
            String receiverUId = bundle.getString("receiver");
            String senderRoom = bundle.getString("sRoom");
            String receiverRoom = bundle.getString("rRoom");
            String senderUsername = bundle.getString("sUname");
            String userToken = bundle.getString("token");
            String imgUrl = bundle.getString("url");



            Date date = new Date();
            mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            mTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            String randomKey = database.getReference().push().getKey();
            //MessageData messageData = new MessageData(randomKey, smsText, senderUId, " ", receiverRoom, mTime, mDate, date.getTime(),false);

            if (imgUrl.length()>3){
                try {
                    MessageData messageData = new MessageData(randomKey, smsText, senderUId, imgUrl, receiverRoom, mTime, mDate, date.getTime(),false, "img");

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
                                                    SendNotification sendNotification = new SendNotification(senderUsername, "Photo", userToken, senderUId, context);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                MessageData messageData = new MessageData(randomKey, smsText, senderUId, "", receiverRoom, mTime, mDate, date.getTime(),false, "txt");
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

                                                SendNotification sendNotification = new SendNotification(senderUsername, smsText, userToken, senderUId, context);
                                                sendNotification.sendNotification();

                                                //sendMessageNotification(username, messageText, userToken);

                                                double timeStamp = date.getTime();
                                                ChatListData chatListData = new ChatListData(receiverUId, smsText, timeStamp);
                                                database.getReference().child("ChatList").child(senderUId)
                                                        .child(receiverUId).setValue(chatListData);

                                                chatListData = new ChatListData(senderUId, smsText, timeStamp);
                                                database.getReference().child("ChatList").child(receiverUId)
                                                        .child(senderUId).setValue(chatListData);


                                            }
                                        });
                            }
                        });
            }
            Toast.makeText(context, "successfully Delivered", Toast.LENGTH_LONG).show();
        }*/





        /*if (type.equals("Whatsapp")) {

            try {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, smsText);
                whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);

            } catch (Exception e) {
                Log.i("Whatsapp Error", e.getMessage().toString());
            }
        }

         */
        //Toast.makeText(context, "successfully Delivered", Toast.LENGTH_LONG).show();
        // setting default ringtone
        //Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);

        // play ringtone
        //ringtone.play();
    }

}
