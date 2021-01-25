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
import android.widget.ImageView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;


    }

    //Firebase Recycler Adapter to retrieve all contacts
    // 1) RecyclerviewOptions
    // 2) FirebaseRecyclerAdapter
    // 3) ViewHolderClass

    @Override
    public void onStart() {
        super.onStart();
    FirebaseRecyclerOptions<Contacts> options =
            new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(contactsRef, Contacts.class)
                    .build();
    FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
            new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                @Override
                // set field in our layout i...e
                protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                    String userIds = getRef(position).getKey();
                    //Log.d("id" , userIds);
                    //Toast.makeText(getContext(), "id " +userIds, Toast.LENGTH_SHORT).show();
                    userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.exists())
                            {
                                if (dataSnapshot.child("userState").hasChild("state"))
                                {
                                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                    String date= dataSnapshot.child("userState").child("date").getValue().toString();
                                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                    if(state.equals("online"))
                                    {
                                        holder.onlineIcon.setVisibility(View.VISIBLE);
                                    }

                                    else if(state.equals("offline"))
                                    {
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }
                                }

                                else
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }


                                if (dataSnapshot.hasChild("image"))
                                {
                                    String usereImage = dataSnapshot.child("image").getValue().toString();
                                    String profileName = dataSnapshot.child("name").getValue().toString();
                                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);
                                    Picasso.get().load(usereImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }
                                else
                                {
                                    String profileName = dataSnapshot.child("name").getValue().toString();
                                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);
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

                //We call our layout
                public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                    ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                    return viewHolder;

                }
            };
    myContactsList.setAdapter(adapter);
    adapter.startListening();

}
    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
                TextView userName, userStatus;
                CircleImageView profileImage;
                ImageView onlineIcon;

                public ContactsViewHolder(@NonNull View itemView)
                {
                    super(itemView);
                    userName = itemView.findViewById(R.id.user_profile_name);
                    userStatus = itemView.findViewById(R.id.user_status);
                    profileImage = itemView.findViewById(R.id.users_profile_image);
                    onlineIcon = itemView.findViewById(R.id.user_online_status);
                }
    }

}
