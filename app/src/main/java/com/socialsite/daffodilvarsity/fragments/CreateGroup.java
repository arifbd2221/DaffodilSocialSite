package com.socialsite.daffodilvarsity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.holders.AddUserToGroupHolder;
import com.socialsite.daffodilvarsity.adapters.holders.UsersViewHolder;
import com.socialsite.daffodilvarsity.model.ChatModel;
import com.socialsite.daffodilvarsity.model.ChatUser;
import com.socialsite.daffodilvarsity.model.GroupChatModel;
import com.socialsite.daffodilvarsity.model.GroupInfo;
import com.socialsite.daffodilvarsity.model.Messages;
import com.socialsite.daffodilvarsity.model.Profile;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by User on 3/20/2018.
 */

public class CreateGroup extends Fragment {

    private EditText group_name;
    private AppCompatImageButton create_group;
    private RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<ChatUser, AddUserToGroupHolder> mPeopleRVAdapter;
    private DatabaseReference mUserDatabase;
    private DatabaseReference memberListDatabase;
    private DatabaseReference groupMessageList;
    private DatabaseReference mCurrentUserProfileDatabase;
    private FirebaseRecyclerOptions<ChatUser> personsOptions;
    FirebaseAuth mAuth;

    private Context context;
    private String mCurrentUser;
    private String mCurrentUserName;
    private String mCurrentUserImage;
    private ProgressBar progressBar;

    private ArrayList<String> groupMemberList;
    private String gName;

    public CreateGroup() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_group_fragment_layout, container, false);


        groupMemberList=new ArrayList<>();
        context = getActivity();
        mAuth=FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser().getUid();
        memberListDatabase=FirebaseDatabase.getInstance().getReference("GroupMember");
        groupMessageList=FirebaseDatabase.getInstance().getReference("GroupMessages");
        mCurrentUserProfileDatabase=FirebaseDatabase.getInstance().getReference("profiles");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("chatList").child(mCurrentUser);
        group_name = view.findViewById(R.id.group_name);
        create_group = view.findViewById(R.id.create_group);
        recyclerView = view.findViewById(R.id.result_list);
        progressBar=view.findViewById(R.id.progressBar);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        loadChatListPersons();

        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("setOnClickListener", "is clicked "+groupMemberList.size());
                String key=null;
                gName=group_name.getText().toString();
                Log.w("setOnClickListener", "Gname "+gName);
                if (gName.isEmpty())
                    group_name.setError("Group Name Needed");
                else{

                    groupMemberList.add(mCurrentUser);
                    key =  memberListDatabase.child(mCurrentUser).push().getKey();
                    for (String s : groupMemberList){
                        Log.w("InsideLoop", "Key "+key);
                        GroupInfo groupInfo=new GroupInfo(key,gName,groupMemberList);
                        memberListDatabase.child(s).child(key).setValue(groupInfo);
                    }

                    memberListDatabase.child(mCurrentUser).child(key).setValue(new GroupInfo(key,gName,groupMemberList));
                }
                Log.w("setOnClickListener", "Key "+key);
                setMessageToGroup(key);
            }
        });
    }

    private void setMessageToGroup(final String key) {
        Log.w("setMessageToGroup", "Key "+key);

        mCurrentUserProfileDatabase.child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mCurrentUserName=dataSnapshot.child("username").getValue().toString();
                mCurrentUserImage=dataSnapshot.child("photoUrl").getValue().toString();
                Log.w("setMessageToGroup", "Name "+mCurrentUserName+" img "+mCurrentUserImage);
                Messages message = new Messages("Hello Everyone","text",false);

                GroupChatModel model=new GroupChatModel(message, Calendar.getInstance().getTime().getTime()+"",mCurrentUser,mCurrentUserName,mCurrentUserImage);

                groupMessageList.child(key).push().setValue(model);
                Toast.makeText(context,"Group Has Been Created Successfully",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadChatListPersons() {

        Query firebaseSearchQuery = mUserDatabase.orderByChild("online");

        firebaseSearchQuery.keepSynced(true);

        personsOptions = new FirebaseRecyclerOptions.Builder<ChatUser>().setQuery(firebaseSearchQuery, ChatUser.class).build();

        mPeopleRVAdapter = new FirebaseRecyclerAdapter<ChatUser, AddUserToGroupHolder>(personsOptions) {

            @Override
            public AddUserToGroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chatlist_single_person_layout_group, parent, false);
                Log.w("UsersViewHolder", "working here");
                return new AddUserToGroupHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull AddUserToGroupHolder holder, final int position, @NonNull final ChatUser model) {

                holder.setDetails(context,model.getPersonName(),model.getPersonImage(),"",0,"","");


                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            Log.w("CheckedListenerSelected", "Name: "+model.getPersonName());
                            groupMemberList.add(model.getPersodId());
                        }else {
                            Log.w("CheckedUnselected", "Id : "+groupMemberList.get(position));
                            groupMemberList.remove(position);
                        }
                    }
                });

                  progressBar.setVisibility(View.GONE);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

        if (mPeopleRVAdapter == null) {
            Log.w("mPeopleRVAdapter", "is null");
        } else {
            Log.w("mPeopleRVAdapter", "is not null");
        }
        recyclerView.setAdapter(mPeopleRVAdapter);

        if (mPeopleRVAdapter != null)
            mPeopleRVAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        if (mPeopleRVAdapter != null)
            mPeopleRVAdapter.startListening();
    }


}


