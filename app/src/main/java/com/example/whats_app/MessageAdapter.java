package com.example.whats_app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
  
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public ImageView messageSenderPicture ,  messageRecieverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.reciever_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageRecieverPicture= itemView.findViewById(R.id.message_reciever_imageView);
            messageSenderPicture = itemView.findViewById(R.id.messege_sendr_imageView);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messaages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageRecieverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());


               // messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.reciever_message_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());

            }
        }

        if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.messageRecieverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageRecieverPicture);

            }
        }

        if (fromMessageType.equals("pdf") || fromMessageType.equals("docs"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);


            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.messageRecieverPicture.setVisibility(View.VISIBLE);

                messageViewHolder.messageRecieverPicture.setBackgroundResource(R.drawable.file);






            }

        }
        if(fromUserID.equals(messageSenderId))
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (userMessagesList.get(i).getType().equals("pdf") || userMessagesList.get(i).getType().equals("docs"))
                    {
                        final CharSequence options [] = new CharSequence[]{
                                "Delete for me"
                                        ,"Download and view"
                                        ,"Cancel"
                                        ,"Delete for everyone"
                    };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0)
                                {
                                    deleteSentMessages(i , messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                               else if(which == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(userMessagesList.get(i).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                               else if(which == 2)
                                {
                                }
                               else if(which == 3)
                                {
                                    deleteMessaageForEveryOne(i , messageViewHolder);


                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                }

                    else if (userMessagesList.get(i).getType().equals("text"))
                    {
                        CharSequence options [] = new CharSequence[]{
                                "Delete for me"
                                ,"Cancel"
                                ,"Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0)
                                {
                                    deleteSentMessages(i , messageViewHolder);


                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 3)
                                {
                                    deleteMessaageForEveryOne(i , messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }

                    else if (userMessagesList.get(i).getType().equals("image"))
                    {
                        CharSequence options [] = new CharSequence[]{
                                "Delete for me"
                                ,"View this image"
                                ,"Cancel"
                                ,"Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0)
                                {
                                    deleteSentMessages(i , messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(which == 1)
                                {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , ImageViewActivity.class);
                                    intent.putExtra("url" , userMessagesList.get(i).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if(which == 3)
                                {
                                    deleteMessaageForEveryOne(i , messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }

                }
            });
        }

        else

            {
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (userMessagesList.get(i).getType().equals("pdf") || userMessagesList.get(i).getType().equals("docs"))
                        {
                            CharSequence options [] = new CharSequence[]{
                                    "Delete for me"
                                    ,"Download and view"
                                    ,"Cancel"
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete message ?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if(which == 0)
                                    {
                                        deleteRecievedMessages(i , messageViewHolder);

                                        Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(which == 1)
                                    {
                                        Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(userMessagesList.get(i).getMessage()));
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(which == 2)
                                    {
                                    }
                                    else if(which == 3)
                                    {
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessagesList.get(i).getType().equals("text"))
                        {
                            CharSequence options [] = new CharSequence[]{
                                    "Delete for me"
                                    ,"Cancel"
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete message ?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if(which == 0)
                                    {
                                        deleteRecievedMessages(i , messageViewHolder);


                                        Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(which == 3)
                                    {
                                    }
                                }
                            });

                            builder.show();
                        }

                        else if (userMessagesList.get(i).getType().equals("image"))
                        {
                            CharSequence options [] = new CharSequence[]{
                                    "Delete for me"
                                    ,"View this image"
                                    ,"Cancel"
                                                                };
                            AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builder.setTitle("Delete message ?");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if(which == 0)
                                    {
                                        deleteRecievedMessages(i , messageViewHolder);

                                        Intent intent = new Intent(messageViewHolder.itemView.getContext() , MainActivity.class);
                                        messageViewHolder.itemView.getContext().startActivity(intent);
                                    }
                                    else if(which == 1)
                                    {
                                        Intent intent = new Intent(messageViewHolder.itemView.getContext() , ImageViewActivity.class);

                                        intent.putExtra("url" , userMessagesList.get(i).getMessage());
                                        messageViewHolder.itemView.getContext().startActivity(intent);


                                    }

                                }
                            });

                            builder.show();
                        }

                    }
                });
            }
    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deleteSentMessages(final  int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue();
    }

    private void deleteRecievedMessages(final  int position , final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rootRef.child("Messages")
                        .child(userMessagesList.get(position).getFrom())
                        .child(userMessagesList.get(position).getTo())
                        .child(userMessagesList.get(position).getMessageId())
                        .removeValue();
            }
        });
    }

    private void deleteMessaageForEveryOne(final  int position , final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue();
    }
}
