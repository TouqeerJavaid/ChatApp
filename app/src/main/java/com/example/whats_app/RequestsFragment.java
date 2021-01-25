package com.example.whats_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment
{
    private View RequestFregmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference chatRequestRef , userRef , contatctsRef;
    private FirebaseAuth mAuth;
    private String currentUserId = "";

    public RequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        RequestFregmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestsList = (RecyclerView) RequestFregmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        chatRequestRef = FirebaseDatabase.getInstance().getReference("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        contatctsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");



        return RequestFregmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (currentUserId!=null) {
            FirebaseRecyclerOptions<Contacts> options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(chatRequestRef.child(currentUserId), Contacts.class)
                            .build();
            FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {
                            holder.itemView.findViewById(R.id.request_Accept_btn).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.request_Cancel_btn).setVisibility(View.VISIBLE);

                            final String listUserId = getRef(position).getKey();

                            DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                            getTypeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String type = dataSnapshot.getValue().toString();
                                        if (type.equals("recieved")) {
                                            userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild("image")) {
                                                        final String requestUserImage = dataSnapshot.child("image").toString().toString();

                                                        Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                                    }

                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("Wants to connect with you");

                                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            CharSequence options[] = new CharSequence[]
                                                                    {
                                                                            "Accept",
                                                                            "Cancel"
                                                                    };
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                            builder.setTitle(requestUserName + "Chat Request");

                                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int i) {
                                                                    if (i == 0) {
                                                                        contatctsRef.child(currentUserId).child(listUserId).child("Contacts")
                                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    contatctsRef.child(listUserId).child(currentUserId).child("Contacts")
                                                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                chatRequestRef.child(currentUserId).child(listUserId)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    chatRequestRef.child(listUserId).child(currentUserId)
                                                                                                                            .removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if (task.isSuccessful()) {
                                                                                                                                        Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
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

                                                                            }
                                                                        });

                                                                    }
                                                                    if (i == 1) {
                                                                        chatRequestRef.child(currentUserId).child(listUserId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            chatRequestRef.child(listUserId).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                Toast.makeText(getContext(), "Request Deletd", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                            builder.show();
                                                        }

                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        } else if (type.equals("request_sent")) {
                                            Button request_sent_button = holder.itemView.findViewById(R.id.request_Accept_btn);
                                            request_sent_button.setText("Request Sent");
                                            holder.itemView.findViewById(R.id.request_Cancel_btn).setVisibility(View.INVISIBLE);

                                            userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild("image")) {
                                                        final String requestUserImage = dataSnapshot.child("image").toString().toString();

                                                        Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userImage);
                                                    }

                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText("You sent request to : " + requestUserName);

                                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            CharSequence options[] = new CharSequence[]
                                                                    {
                                                                            "Cancel Chat Request"
                                                                    };
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                            builder.setTitle("Already sent request");

                                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int i) {
                                                                    if (i == 0) {
                                                                        chatRequestRef.child(currentUserId).child(listUserId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            chatRequestRef.child(listUserId).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                Toast.makeText(getContext(), "You have cancel the chat request", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                            builder.show();
                                                        }

                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @NonNull
                        @Override
                        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                            RequestViewHolder viewHolder = new RequestViewHolder(view);
                            return viewHolder;

                        }
                    };
            myRequestsList.setAdapter(adapter);
            adapter.startListening();
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName , userStatus;
        CircleImageView userImage;
        Button acceptButton , cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_Accept_btn);
            cancelButton = itemView.findViewById(R.id.request_Cancel_btn);

        }
    }



}
