package com.socialsite.daffodilvarsity.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.holders.UsersViewHolder;
import com.socialsite.daffodilvarsity.managers.ProfileManager;
import com.socialsite.daffodilvarsity.managers.listeners.OnObjectChangedListener;
import com.socialsite.daffodilvarsity.model.ChatModel;
import com.socialsite.daffodilvarsity.model.ChatUser;
import com.socialsite.daffodilvarsity.model.Conversation;
import com.socialsite.daffodilvarsity.model.Messages;
import com.socialsite.daffodilvarsity.model.Profile;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView chatList;
    private FirebaseRecyclerAdapter<Conversation, ChatListActivity.ChatUserHolder> mPeopleRVAdapter;
    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerOptions<Conversation> personsOptions;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Toolbar toolbar;


    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;


    private ProfileManager profileManager;
    private Profile profile;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);


        progressBar=new ProgressBar(this);

        chatList=findViewById(R.id.chatList);
        toolbar=findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.chat_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference("chatList").child(mAuth.getCurrentUser().getUid());

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mAuth.getCurrentUser().getUid());

        mConvDatabase.keepSynced(true);
        //mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mAuth.getCurrentUser().getUid());
        //mUsersDatabase.keepSynced(true);

        chatList.setHasFixedSize(true);
        chatList.setLayoutManager(new LinearLayoutManager(this));

        Query firebaseSearchQuery = mConvDatabase.orderByChild("timestamp");

        firebaseSearchQuery.keepSynced(true);

        profileManager=ProfileManager.getInstance(this);;
        profileManager.getProfileValue(ChatListActivity.this, mAuth.getCurrentUser().getUid(), createOnProfileChangedListener());

        personsOptions = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(firebaseSearchQuery, Conversation.class).build();

        mPeopleRVAdapter = new FirebaseRecyclerAdapter<Conversation, ChatUserHolder>(personsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatUserHolder holder, int position, @NonNull final Conversation model) {

                Log.e("onBindViewHolder","Yes we are here");

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.e("addChildEventListener","Yes we are here");
                        /*Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        ChatModel msg=(ChatModel) map.get("message")*/;

                        ChatModel chatModel=dataSnapshot.getValue(ChatModel.class);
                        Messages msg=chatModel.getMessage();

                        if (msg != null)
                        holder.setMessage(msg.getMessage(), model.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                Log.e("mUsersDatabase","Yes we are here");

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e("mUsersDatabase","Yes we are here");
                        final String userName = dataSnapshot.child("personName").getValue().toString();
                        String userThumb = dataSnapshot.child("personImage").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);

                        }

                        holder.chatUserName.setText(userName);



                        if (userThumb != null) {
                            Glide.with(getApplicationContext())
                                    .load(userThumb)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .crossFade()
                                    .error(R.drawable.default_avatar)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(holder.chatUserPhoto);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            holder.chatUserPhoto.setImageResource(R.drawable.default_avatar);
                        }




                        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent startChatIntent=new Intent(ChatListActivity.this,ChatActivity.class);
                                startChatIntent.putExtra("user_id",list_user_id);
                                startChatIntent.putExtra("user_name",userName);
                                startChatIntent.putExtra("chatUserImage",imageUrl);
                                startChatIntent.putExtra("currentUserName",profile.getUsername());
                                startChatIntent.putExtra("currentUserPhoto",profile.getPhotoUrl());
                                startChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                                getApplicationContext().startActivity(startChatIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



/*




                imageUrl=model.getPersonImage();
                holder.chatUserName.setText(model.getPersonName());


*/




            }

            @Override
            public ChatUserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_single_user_layout,parent,false);

                return new ChatUserHolder(view);
            }
        };

        chatList.setAdapter(mPeopleRVAdapter);

        if (mPeopleRVAdapter != null){
            mPeopleRVAdapter.startListening();
        }

    }

    private static class ChatUserHolder extends RecyclerView.ViewHolder{

        TextView chatUserName,status;
        ImageView chatUserPhoto;
        RelativeLayout rootLayout;
        ImageView userOnlineView;
        public ChatUserHolder(View itemView) {
            super(itemView);
            userOnlineView =itemView.findViewById(R.id.user_single_online_icon);
            rootLayout=itemView.findViewById(R.id.root_layout);
            chatUserName=itemView.findViewById(R.id.user_single_name);
            chatUserPhoto=itemView.findViewById(R.id.user_single_image);
            status=itemView.findViewById(R.id.user_single_status);

        }

        public void setMessage(String message, boolean isSeen){

            status.setText(message);

            if(!isSeen){
                status.setTypeface(status.getTypeface(), Typeface.BOLD);
            } else {
                status.setTypeface(status.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setUserOnline(String online_status) {



            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mPeopleRVAdapter != null)
        mPeopleRVAdapter.stopListening();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mPeopleRVAdapter != null){
            mPeopleRVAdapter.startListening();
        }

    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile=obj;
            }
        };
    }

}
