package com.devtwist.mymessenger.Models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.devtwist.mymessenger.Activities.IncomingCall;
import com.devtwist.mymessenger.Activities.MessengerActivity;
import com.devtwist.mymessenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseService extends FirebaseMessagingService {
    private String messageText,userId;
    private Intent intent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        sendNotification(notification.getTitle(), notification.getBody());
        Log.i("My Notification", notification.getTitle());

        if (notification.getTitle().equals("VC")){
            Map<String, String> data = remoteMessage.getData();
            Log.e("nbg", notification.getBody());

            Intent intent = new Intent(MyFirebaseService.this, IncomingCall.class);
            Bundle bundle = new Bundle();
            bundle.putString("nType", "VC");
            bundle.putString("userId",notification.getBody());
            intent.putExtras(bundle);
            //intent.putExtra("userId", notification.getBody());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    private void sendNotification(String title, String messageBody) {
        NotificationCompat.Builder notificationBuilder;
        String channelId = "1";
        userId = messageBody;
        if (title.equals("AC")|| title.equals("VC")){
            intent = new Intent(this, IncomingCall.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", messageBody);
            bundle.putString("nType", "VC");
            //intent.putExtra("userId", messageBody);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                        PendingIntent.FLAG_MUTABLE);
            }
            else
            {
                pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            FirebaseDatabase.getInstance().getReference().child("Users").child(messageBody).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Userdata userdata = snapshot.getValue(Userdata.class);
                    messageText = userdata.getUsername();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            notificationBuilder = new NotificationCompat.Builder(this, channelId);
            notificationBuilder.setSmallIcon(R.drawable.app_logo);
            notificationBuilder.setContentTitle("Incoming Call");
            notificationBuilder.setContentText(userId);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setSound(defaultSoundUri);
            notificationBuilder.setContentIntent(pendingIntent);
        }
        else {
            Intent intent = new Intent(this, MessengerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("nType","MSG");
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                        PendingIntent.FLAG_MUTABLE);
            }
            else
            {
                pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                        PendingIntent.FLAG_ONE_SHOT);
            }

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.app_logo)
                            .setContentTitle(title)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}
