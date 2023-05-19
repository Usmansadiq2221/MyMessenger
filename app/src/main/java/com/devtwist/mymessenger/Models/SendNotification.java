package com.devtwist.mymessenger.Models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {

    private String title, messageText, token, senderUId;
    private Context context;



    public SendNotification() {
    }

    public SendNotification(String title, String messageText, String token, String senderUId, Context context) {
        this.title = title;
        this.messageText = messageText;
        this.token = token;
        this.senderUId = senderUId;
        this.context = context;
    }

    public void sendNotification(){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject objectData = new JSONObject();
            if (title.equals("Incoming Audio Call" )|| title.equals("Incoming Video Call")){
                FirebaseDatabase.getInstance().getReference().child("Users").child(messageText).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageText = snapshot.child("username").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                objectData.put("title", messageText);
                objectData.put("body", title);
            }else {
                objectData.put("title", title);
                objectData.put("body", messageText);
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
                    String serverKey = "Key=AAAAok5RD84:APA91bEUL3W8HTO4uboN-qrKuEavM75sIoIL-xdd8Z7CM9YmLmEAjyoTmj1ewVH4JNW71_oZ9iQBKC44cjpLtPPdWYrE8rCA9ni8KgC7ctqsPvIxcOWNnGprwKzQp0oSSB3PdbQvDcc9";
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
}
