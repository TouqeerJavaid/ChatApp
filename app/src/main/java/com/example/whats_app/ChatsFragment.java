package com.example.whats_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment
{

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference chatsRef , usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = (RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        if (currentUserId != null) {
            chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        }

        return privateChatsView;



    }


   @Override
    public void onStart()
    {
        super.onStart();

        if (currentUserId != null) {
            FirebaseRecyclerOptions<Contacts> options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(chatsRef, Contacts.class)
                            .build();

            FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, int i, @NonNull Contacts contacts) {
                            final String userId = getRef(i).getKey().toString();
                            final String[] retImage = {"default"};

                            usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild("image")) {
                                            retImage[0] = dataSnapshot.child("image").getValue().toString();
                                            Log.d("image", "" + retImage[0]);
                                            Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(chatsViewHolder.userImage);
                                        }

                                        final String retName = dataSnapshot.child("name").getValue().toString();
                                        final String retStatus = dataSnapshot.child("status").getValue().toString();


                                        chatsViewHolder.userName.setText(retName);
                                        chatsViewHolder.userStatus.setText("Last seen: " + "Date: " + "Time: ");

                                        if (dataSnapshot.child("userState").hasChild("state")) {
                                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                            if (state.equals("online")) {
                                                chatsViewHolder.userStatus.setText("online");
                                            } else if (state.equals("offline")) {
                                                chatsViewHolder.userStatus.setText("Last seen" + date + " " + time);
                                            }
                                        } else {
                                            chatsViewHolder.userStatus.setText("offline");
                                        }


                                        chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent chatInten = new Intent(getContext(), ChatsActivity.class);
                                                chatInten.putExtra("visit_user_id", userId);
                                                chatInten.putExtra("visit_user_name", retName);
                                                chatInten.putExtra("visit_user_image", retImage[0]);
                                                startActivity(chatInten);

                                            }
                                        });
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @NonNull
                        @Override
                        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                            ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                            return viewHolder;
                        }
                    };
            chatsList.setAdapter(adapter);
            adapter.startListening();
        }

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus;
        CircleImageView userImage;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}
