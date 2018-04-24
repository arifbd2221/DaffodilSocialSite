package com.socialsite.daffodilvarsity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.GroupChatAdapter;
import com.socialsite.daffodilvarsity.fragments.Groups;
import com.socialsite.daffodilvarsity.listeners.ClickListenerChatFirebase;
import com.socialsite.daffodilvarsity.model.GroupChatModel;
import com.socialsite.daffodilvarsity.model.Messages;
import com.socialsite.daffodilvarsity.model.Profile;
import com.socialsite.daffodilvarsity.views.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

/**
 * Created by User on 3/21/2018.
 */

public class GroupChatActivity extends BaseActivity implements ClickListenerChatFirebase {


    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private CircularImageView mProfileImage;


    private EmojiconEditText emojiconEditText;
    private ImageView emojiImageView;
    private ImageView submitButton;
    private EmojIconActions emojIcon;
    private LinearLayout root;


    private TextView mTitleView,typing;
    private TextView mLastSeenView;
    private LinearLayoutManager mLinearLayoutManager;


    private boolean isScrolling=false;
    private  int currentItems,totalItems,scrolloutItems;


    private GroupChatModel groupChatModel;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();

    //private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    private File filePathImageCamera;
    private GroupChatAdapter firebaseAdapter;
    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";


    Intent mCurrentUserIntent;
    private String groupKey,myName;
    private Profile profile;

    String mCurrentUserId;

    private SwipeRefreshLayout refreshLayout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_chat_activity);

        mAuth=FirebaseAuth.getInstance();
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mRootRef= FirebaseDatabase.getInstance().getReference();


        mCurrentUserId = mAuth.getCurrentUser().getUid();
        getCurrentUserName(mCurrentUserId);


        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        refreshLayout = findViewById(R.id.message_swipe_layout);
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircularImageView) findViewById(R.id.custom_bar_image);
        typing=findViewById(R.id.typing);

        emojiImageView =  findViewById(R.id.emoji_btn);
        submitButton =  findViewById(R.id.submit_btn);
        emojiconEditText =  findViewById(R.id.emojicon_edit_text);
        root=  findViewById(R.id.newCommentContainer);

        emojIcon = new EmojIconActions(this, root, emojiconEditText, emojiImageView);


        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("PostDetailsActivity","keyboard opened");
                //mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("onKeyboardOpen").setValue("true");
            }
            @Override
            public void onKeyboardClose() {
                Log.e("PostDetailsActivity","Keyboard closed");
                //mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("onKeyboardOpen").setValue("false");
            }
        });


        //groupKey = mCurrentUserIntent.getStringExtra("key");
        groupKey= Groups.key;


        mLinearLayoutManager=new LinearLayoutManager(this);

        mMessagesList =  findViewById(R.id.messages_list);
        mRefreshLayout =  findViewById(R.id.message_swipe_layout);
        //mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);

        //mMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //loadMoreMessages();


            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        setupMessageListWithFirebaseRecyclerView();


    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {

    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {

    }



    private void setupMessageListWithFirebaseRecyclerView(){

        // This Method is getting invoked by the onCreate() method

        DatabaseReference messageRef = mRootRef.child("GroupMessages").child(groupKey);

        FirebaseRecyclerOptions<GroupChatModel> personsOptions=new FirebaseRecyclerOptions.Builder<GroupChatModel>().setQuery(messageRef, GroupChatModel.class).build();

        firebaseAdapter = new GroupChatAdapter(personsOptions,mCurrentUserId,this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessagesList.scrollToPosition(positionStart);
                }
            }
        });
        mMessagesList.setLayoutManager(mLinearLayoutManager);
        mMessagesList.setAdapter(firebaseAdapter);




        if (firebaseAdapter != null)
            firebaseAdapter.startListening();

    }


    private void sendMessage() {


        String message = emojiconEditText.getText().toString();

        if(!TextUtils.isEmpty(message)){

            sendOnlyMessage(message);

        }

    }



    private void sendOnlyMessage(String message){

        DatabaseReference reference = mRootRef.child("GroupMessages").child(groupKey).push();

        GroupChatModel groupChatModel =new GroupChatModel(new Messages(message,"text",false),Calendar.getInstance().getTime().getTime()+"",profile.getId(),profile.getUsername(),profile.getPhotoUrl());

        reference.setValue(groupChatModel);

    }



    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAdapter != null)
            firebaseAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAdapter != null)
            firebaseAdapter.startListening();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseAdapter != null)
            firebaseAdapter.startListening();
    }



    private void getCurrentUserName(String id){
        Log.e("UserName","inside it");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("profiles").child(id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                profile = dataSnapshot.getValue(Profile.class);
                Log.e("onDataChange",profile.getUsername());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("GroupMessages").child(groupKey);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                GroupChatModel message = dataSnapshot.getValue(GroupChatModel.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    //messagesList.add(itemPos++,message);


                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                firebaseAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayoutManager.scrollToPositionWithOffset(10, 0);

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

    }


}
