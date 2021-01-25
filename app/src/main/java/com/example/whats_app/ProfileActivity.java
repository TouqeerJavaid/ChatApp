package com.example.whats_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieveUserId , Current_State , senderUserId;
    private CircleImageView userProfileImage;
    private TextView userPrfileName , userProfileStatus;
    private Button sendMessageRequestButton , decliendRequestButton;

    private DatabaseReference userRef , chatRequestRef , contactsRef , notificationRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recieveUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("notification");

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid().toString();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userPrfileName = findViewById(R.id.visist_user_name);
        userProfileStatus = findViewById(R.id.visist_user_status);
        sendMessageRequestButton = findViewById(R.id . send_message_request_button);
        decliendRequestButton = findViewById(R.id . decliend_message_request_button);

        Current_State = "new";



        RetrieveUserIfo();
    }

    private void RetrieveUserIfo() {
        userRef.child(recieveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.hasChild("image") )
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userPrfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    
                    ManagaeChatRequests();

                }

                else
                {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        userPrfileName.setText(userName);
                        userPrfileName.setText(userStatus);

                        ManagaeChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManagaeChatRequests()
    {

         chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(recieveUserId))
                {
                    String requestType = dataSnapshot.child(recieveUserId).child("request_type").getValue().toString();
                    if(requestType.equals("request_sent"))
                    {
                        Current_State = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    //cancell Request
                    else if(requestType.equals("recieved"))
                    {
                        Current_State = "request_recieved";


                        sendMessageRequestButton.setText("Accept Chat Request");
                        decliendRequestButton.setVisibility(View.VISIBLE);
                        decliendRequestButton.setEnabled(true);

                        decliendRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                CancelChatRequet();
                            }
                        });
                    }
                }

                else
                {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(recieveUserId))
                            {
                                Current_State = "friends";
                                sendMessageRequestButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(!senderUserId.equals(recieveUserId))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                sendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new"))
                        {
                            sendChatRequest();
                        }
                    if (Current_State.equals("request_sent"))
                        {
                            CancelChatRequet();

                        }
                    if (Current_State.equals("request_recieved"))
                        {
                            AcceptChatRequest();
                        }
                    if (Current_State.equals("friends"))
                        {
                            RemoveSpecificContacts();
                        }
                }
            });


        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }



    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(recieveUserId)
                .child("request_type").setValue("request_sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(recieveUserId).child(senderUserId)
                                    .child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {

                                                HashMap<String , String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("frome" , senderUserId);
                                                chatNotificationMap.put("type" , "request"); // frnd request and chat request

                                                notificationRef.child(recieveUserId).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void CancelChatRequet()
    {
        chatRequestRef.child(senderUserId)
                .child(recieveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(recieveUserId)
                                    .child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                decliendRequestButton.setVisibility(View.INVISIBLE);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void AcceptChatRequest()
    {
        contactsRef.child(senderUserId).child(recieveUserId)
                .child("Cotacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(recieveUserId).child(senderUserId)
                                    .child("Cotacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "friends";
                                                sendMessageRequestButton.setText("Remove this contact");
                                                decliendRequestButton.setVisibility(View.INVISIBLE);
                                                decliendRequestButton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    public void RemoveSpecificContacts()
    {
        contactsRef.child(senderUserId)
                .child(recieveUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(recieveUserId)
                                    .child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                decliendRequestButton.setVisibility(View.INVISIBLE);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }
}
