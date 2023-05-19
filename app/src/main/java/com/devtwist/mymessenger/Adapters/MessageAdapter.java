package com.devtwist.mymessenger.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.mymessenger.Activities.ViewImage;
import com.devtwist.mymessenger.Models.MessageData;
import com.devtwist.mymessenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<MessageData> messages;
    private final int item_sent = 1;
    private final int item_receive = 2;
    private String myId,userId;

    public MessageAdapter(Context context, ArrayList<MessageData> messages, String _myId,String userId) {
        this.context = context;
        this.messages = messages;
        this.myId = _myId;
        this.userId = userId;
    }

    public MessageAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

         if(viewType == item_sent){
           View view = LayoutInflater.from(context).inflate(R.layout.sent_message_item, parent, false);
            return new SentViewHolder(view);
        }else {
           View view = LayoutInflater.from(context).inflate(R.layout.receive_message_items, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageData messageData = messages.get(position);
        if (myId.equals(messageData.getSenderId())) {
            return item_sent;
        } else {
            return item_receive;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageData messageData = messages.get(position);
        String  message = messageData.getMessageText();

        if (holder.getClass() == SentViewHolder.class){

            SentViewHolder viewHolder = (SentViewHolder) holder;
            viewHolder._sentViewItem.setVisibility(View.GONE);
            viewHolder._sentMessageTime.setText(messageData.getTime());
            if (messageData.getImageUrl().length()>1){
                if (messageData.getImageUrl().length()>1) {
                    Picasso.get().load(messageData.getImageUrl()).into(viewHolder._sendImage);
                }
                viewHolder._sendImage.setVisibility(ImageView.VISIBLE);
                if (messageData.getMessageText().trim().length()<1) {
                    viewHolder._sentMessage.setVisibility(TextView.GONE);
                }
                else {
                    viewHolder._sentMessage.setText(messageData.getMessageText().toString().trim());
                }
                viewHolder._sendImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        viewHolder._sentViewItem.setVisibility(View.VISIBLE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete message...");

                        String[] options = {"Delete for me", "Undo Message"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0){
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                            .child("messages").child(messageData.getMessageId()).removeValue();
                                    /*if (getItemCount()-1!=0){
                                        MessageData messageData1 = messages.get(getItemCount()-1);
                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId).child(userId).child("lastMessage").setValue(messageData1.getMessageText());
                                        FirebaseDatabase.getInstance().getReference().child("ChatList").child(myId).child(userId).child("timeStamp").setValue(messageData1.getTimestamp());
                                    }*/
                                }
                                else if (position == 1){
                                    if (messageData.getImageUrl().length()>1) {
                                        viewHolder._sImgDellProgress.setVisibility(View.VISIBLE);
                                        StorageReference deleteImg = FirebaseStorage.getInstance().getReferenceFromUrl(messageData.getImageUrl());
                                        deleteImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                                        .child("messages").child(messageData.getMessageId()).removeValue();
                                                FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                                        .child("messages").child(messageData.getMessageId()).removeValue();
                                                viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                            }
                                        });
                                    }else{
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                                .child("messages").child(messageData.getMessageId()).removeValue()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                                .child("messages").child(messageData.getMessageId()).removeValue().addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                viewHolder._sentViewItem.setVisibility(View.GONE);
                            }
                        });
                        // create and show the alert dialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                });
                viewHolder._sendImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ViewImage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("tag", "i");
                        bundle.putString("imgUrl", messageData.getImageUrl());
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                });
            }
            else {
                viewHolder._sentMessage.setText(message);
            }
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewHolder._sentViewItem.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete message...");

                    String[] options = {"Delete for me", "Undo Message"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0){
                                FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                        .child("messages").child(messageData.getMessageId()).removeValue();
                            }
                            else if (position == 1){
                                if (messageData.getImageUrl().length()>1) {
                                    viewHolder._sImgDellProgress.setVisibility(View.VISIBLE);
                                    StorageReference deleteImg = FirebaseStorage.getInstance().getReferenceFromUrl(messageData.getImageUrl());
                                    deleteImg.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue();
                                            FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                                    .child("messages").child(messageData.getMessageId()).removeValue();
                                            viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                            viewHolder._sImgDellProgress.setVisibility(View.GONE);
                                        }
                                    });
                                }else{
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                            .child("messages").child(messageData.getMessageId()).removeValue()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(userId+myId)
                                            .child("messages").child(messageData.getMessageId()).removeValue().addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            viewHolder._sentViewItem.setVisibility(View.GONE);
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });

            if (position == messages.size()-1){
                if (messageData.isSeen()){
                    viewHolder._textSeen.setText("Seen");
                }
                else{
                    viewHolder._textSeen.setText("Delivered");
                }
                viewHolder._textSeen.setVisibility(View.VISIBLE);
            }else{
                viewHolder._textSeen.setVisibility(View.GONE);
            }

        }else {

            ReceiverViewHolder viewHolder = (ReceiverViewHolder)  holder;
            viewHolder._receiveViewItem.setVisibility(View.GONE);
            viewHolder._receiveMessageTime.setText(messageData.getTime());
            if (messageData.getImageUrl().length()>1){
                if (messageData.getImageUrl().length()>1) {
                    Picasso.get().load(messageData.getImageUrl()).into(viewHolder._receiveImage);
                }
                viewHolder._receiveImage.setVisibility(ImageView.VISIBLE);
                if (messageData.getMessageText().trim().length()<1) {
                    viewHolder._receiveMessage.setVisibility(TextView.GONE);
                }
                else {
                    viewHolder._receiveMessage.setText(messageData.getMessageText().toString().trim());
                }
                viewHolder._receiveImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        viewHolder._receiveViewItem.setVisibility(View.VISIBLE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete message...");

                        String[] options = {"Delete for me"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0){
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId+userId)
                                            .child("messages").child(messageData.getMessageId()).removeValue();
                                }
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                viewHolder._receiveViewItem.setVisibility(View.GONE);
                            }
                        });
                        // create and show the alert dialog
                        AlertDialog dialog = builder.create();
                        dialog.setCancelable(true);
                        dialog.setIcon(R.drawable.app_icon);
                        dialog.show();
                        return false;
                    }
                });
                viewHolder._receiveImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ViewImage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("tag", "i");
                        bundle.putString("imgUrl", messageData.getImageUrl());
                        intent.putExtras(bundle);
                        v.getContext().startActivity(intent);
                    }
                });

            }
            else {
                viewHolder._receiveMessage.setText(message);
            }
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewHolder._receiveViewItem.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete message...");

                    String[] options = {"Delete for me"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0){
                                    FirebaseDatabase.getInstance().getReference().child("Chat").child(myId + userId)
                                            .child("messages").child(messageData.getMessageId()).removeValue();
                            }
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            viewHolder._receiveViewItem.setVisibility(View.GONE);
                        }
                    });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(true);
                    dialog.setIcon(R.drawable.app_icon);
                    dialog.show();
                    return false;
                }
            });



            if (position == messages.size()-1){
                if (messageData.isSeen()){
                    viewHolder._isSeen.setText("Seen");
                }
                else{
                    viewHolder._isSeen.setText("Delivered");
                }
                viewHolder._isSeen.setVisibility(View.VISIBLE);
            }else{
                viewHolder._isSeen.setVisibility(View.GONE);
            }

        }
        Log.i("Chat Adapter Error", myId);
        holder.setIsRecyclable(false);

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        TextView _sentMessage, _sentMessageTime, _textSeen;
        ImageView _sendImage;
        View _sentViewItem;
        ProgressBar _sImgDellProgress;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            _sentMessage = (TextView) itemView.findViewById(R.id._sentMessage);
            _sentMessageTime = (TextView) itemView.findViewById(R.id._sentMessageTime);
            _sendImage = (ImageView) itemView.findViewById(R.id._sendImage);
            _textSeen = (TextView) itemView.findViewById(R.id._textSeen);
            _sentViewItem = (View) itemView.findViewById(R.id._sentViewItem);
            _sImgDellProgress = (ProgressBar) itemView.findViewById(R.id._sImgDellProgress);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        private TextView _receiveMessage, _receiveMessageTime, _isSeen;
        private ImageView _receiveImage;
        private View _receiveViewItem;
        private ProgressBar _rImgDellProgress;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            _receiveMessage = (TextView) itemView.findViewById(R.id._receiveMessage);
            _receiveMessageTime = (TextView) itemView.findViewById(R.id._receiveMessageTime);
            _isSeen = (TextView) itemView.findViewById(R.id._isSeen);
            _receiveImage = (ImageView) itemView.findViewById(R.id._receiveImage);
            _receiveViewItem = (View) itemView.findViewById(R.id._receiveViewItem);
            _rImgDellProgress = (ProgressBar) itemView.findViewById(R.id._rImgDellProgress);
        }
    }

}
