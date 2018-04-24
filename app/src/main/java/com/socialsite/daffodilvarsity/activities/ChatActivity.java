package com.socialsite.daffodilvarsity.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialsite.daffodilvarsity.BuildConfig;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.ChatFirebaseAdapter;
import com.socialsite.daffodilvarsity.adapters.MessageAdapter;
import com.socialsite.daffodilvarsity.listeners.ClickListenerChatFirebase;
import com.socialsite.daffodilvarsity.model.ChatModel;
import com.socialsite.daffodilvarsity.model.ChatUser;
import com.socialsite.daffodilvarsity.model.FileModel;
import com.socialsite.daffodilvarsity.model.MapModel;
import com.socialsite.daffodilvarsity.model.Messages;
import com.socialsite.daffodilvarsity.utils.GetTimeAgo;
import com.socialsite.daffodilvarsity.utils.Utils;
import com.socialsite.daffodilvarsity.views.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends BaseActivity implements ClickListenerChatFirebase {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView,typing;
    private TextView mLastSeenView;
    private LinearLayoutManager mLinearLayoutManager;

    private CircularImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private String mCurrentUserName;
    private String mCurrentUserPhoto;
    private String chatUserImage;


    private EmojiconEditText emojiconEditText;
    private ImageView emojiImageView;
    private ImageView submitButton;
    private EmojIconActions emojIcon;
    private LinearLayout root;


    private ChatUser chatUser;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    //private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;
    private DatabaseReference mUserRef;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    private File filePathImageCamera;
    private ChatFirebaseAdapter firebaseAdapter;
    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";


    Intent mCurrentUserIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        if (mAuth.getCurrentUser() !=null ){
            mUserRef= FirebaseDatabase.getInstance().getReference().child("profiles").child(mAuth.getCurrentUser().getUid());
        }


        mCurrentUserIntent=getIntent();

        chatUser=new ChatUser();
        chatUser.setPersodId(mCurrentUserId);
        //chatUser.setPersonName(mAuth.getCurrentUser().getDisplayName());
        try{
            chatUser.setPersonImage(mAuth.getCurrentUser().getPhotoUrl().toString());
        }catch (NullPointerException np){
            chatUser.setPersonImage(mCurrentUserIntent.getStringExtra("currentUserPhoto"));
            np.printStackTrace();
        }

        mCurrentUserPhoto=mCurrentUserIntent.getStringExtra("currentUserPhoto");
        mChatUser = mCurrentUserIntent.getStringExtra("user_id");
        String userName = mCurrentUserIntent.getStringExtra("user_name");
        mCurrentUserName= mCurrentUserIntent.getStringExtra("currentUserName");
        chatUserImage=mCurrentUserIntent.getStringExtra("chatUserImage");

        chatUser.setPersonName(mCurrentUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----

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
                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("onKeyboardOpen").setValue("true");
            }
            @Override
            public void onKeyboardClose() {
                Log.e("PostDetailsActivity","Keyboard closed");
                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("onKeyboardOpen").setValue("false");
            }
        });


        mLinearLayoutManager=new LinearLayoutManager(this);

        //mAdapter = new MessageAdapter(messagesList);

        mMessagesList =  findViewById(R.id.messages_list);
        mRefreshLayout =  findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        //mMessagesList.setAdapter(mAdapter);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);

        setupMessageListWithFirebaseRecyclerView();


        mTitleView.setText(userName);




        mRootRef.child("profiles").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                //String image = dataSnapshot.child("photoUrl").getValue().toString();



                if(online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String check= dataSnapshot.child("onKeyboardOpen").getValue().toString();
                if (check.equals("true")){
                    typing.setText("Typing...");
                }
                else {
                    typing.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
                hideKeyboard();

            }
        });



        /*mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image*//*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });*/



        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                //loadMoreMessages1();


            }
        });


    }



    private void setupMessageListWithFirebaseRecyclerView(){
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Log.e("FirebaseRecyclerView",mCurrentUserName);
        FirebaseRecyclerOptions<ChatModel> personsOptions=new FirebaseRecyclerOptions.Builder<ChatModel>().setQuery(messageRef, ChatModel.class).build();

        firebaseAdapter = new ChatFirebaseAdapter(personsOptions,mCurrentUserId,this);
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



    private void loadMoreMessages1() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                //mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

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

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                //mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);

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

    private void sendMessage() {


        String message = emojiconEditText.getText().toString();

        if(!TextUtils.isEmpty(message)){

            sendOnlyMessage(message);

        }

    }



    private void sendOnlyMessage(String message){

        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

        DatabaseReference user_message_push = mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push();

        String push_id = user_message_push.getKey();

        //Map messageMap = new HashMap();


        /*mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).child(push_id).child("timeStamp").setValue(ServerValue.TIMESTAMP);*/


        ChatUser chatUser=new ChatUser();
        chatUser.setPersodId(mCurrentUserId);
        chatUser.setPersonName(mCurrentUserName);

        try{
            chatUser.setPersonImage(mAuth.getCurrentUser().getPhotoUrl().toString());
        }catch (NullPointerException np){
            chatUser.setPersonImage(mCurrentUserPhoto);
            np.printStackTrace();
        }



        Messages messages=new Messages();

        messages.setMessage(message);
        messages.setSeen(false);
        messages.setType("text");
        messages.setFrom(mCurrentUserId);

        ChatModel model=new ChatModel(chatUser,messages,Calendar.getInstance().getTime().getTime()+"",null);

            /*messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);*/

        Map messageUserMap = new HashMap();

        messageUserMap.put(current_user_ref + "/" + push_id, model);
        messageUserMap.put(chat_user_ref + "/" + push_id, model);

        emojiconEditText.setText("");

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null){

                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                }

            }
        });


    }



    private void sendFileToStorage(ChatModel chatModel){


        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

        DatabaseReference user_message_push = mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push();

        String push_id = user_message_push.getKey();
        /*mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).child(push_id).child("timeStamp").setValue(ServerValue.TIMESTAMP);*/
        //Map messageMap = new HashMap();

       /* Messages messages=new Messages();

        messages.setMessage("");
        messages.setSeen(false);
        messages.setType("text");
        messages.setFrom(mCurrentUserId);*/


        /*ChatUser chatUser=new ChatUser();
        chatUser.setPersodId(mCurrentUserId);
        chatUser.setPersonName(mAuth.getCurrentUser().getDisplayName());
        chatUser.setPersonImage(mAuth.getCurrentUser().getPhotoUrl().toString());


        chatModel.setMessage(messages);
        chatModel.setUserModel(chatUser);*/

            /*messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);*/


        //chatModel.setUserModel(chatUser);

        Map messageUserMap = new HashMap();

        messageUserMap.put(current_user_ref + "/" + push_id, chatModel);
        messageUserMap.put(chat_user_ref + "/" + push_id, chatModel);

        emojiconEditText.setText("");

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
        mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null){

                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                }

            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sendPhoto:
                verifyStoragePermissions();
                photoCameraIntent();
                break;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendLocation:
                locationPlacesIntent();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    ChatActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
            // we already have permission, lets go ahead and call camera intent
            photoCameraIntent();
        }
    }


    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
}


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    photoCameraIntent();
                }
                break;
        }
    }


    private void locationPlacesIntent(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto+"camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //StorageReference storageRef = storage.getReferenceFromUrl(Utils.URL_STORAGE_REFERENCE).child(Utils.FOLDER_STORAGE_IMG);





        /*if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        @SuppressWarnings("VisibleForTests")
                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

*/







        if (requestCode == IMAGE_GALLERY_REQUEST){
            if (resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    sendFileFirebase(mImageStorage,selectedImageUri);
                }else{
                    //URI IS NULL
                }
            }
        }else if (requestCode == IMAGE_CAMERA_REQUEST){
            if (resultCode == RESULT_OK){
                if (filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imageCameraRef = mImageStorage.child("message_images").child( filePathImageCamera.getName() + ".jpg");//child(filePathImageCamera.getName()+"_camera");
                    sendFileFirebase(imageCameraRef,filePathImageCamera);
                }else{
                    //IS NULL
                }
            }
        }else if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place!=null){
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude+"",latLng.longitude+"");
                    ChatModel chatModel = new ChatModel(chatUser, Calendar.getInstance().getTime().getTime()+"",mapModel);
                    //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    sendFileToStorage(chatModel);
                }else{
                    //PLACE IS NULL
                }
            }
        }

    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file){
        if (storageReference != null){
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child("message_images").child( name + ".jpg");//child(name+"_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("sendFileFirebase","onFailure sendFileFirebase "+e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("sendFileFirebase","onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel = new FileModel("img",downloadUrl.toString(),name,"");

                    Messages messages=new Messages();
                    messages.setFrom(chatUser.getPersodId());
                    messages.setMessage("");
                    messages.setSeen(false);
                    messages.setType("janina");

                    ChatModel chatModel = new ChatModel(chatUser,messages,Calendar.getInstance().getTime().getTime()+"",fileModel);
                    //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    sendFileToStorage(chatModel);
                }
            });
        }else{
            //IS NULL
        }

    }

    /**
     * Envia o arvquivo para o firebase
     */
    private void sendFileFirebase(StorageReference storageReference, final File file){
        if (storageReference != null){
            Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            UploadTask uploadTask = storageReference.child("message_images").putFile(photoURI);//putFile(photoURI);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("sendFileFirebase","onFailure sendFileFirebase "+e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("sendFileFirebase","onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel = new FileModel("img",downloadUrl.toString(),file.getName(),file.length()+"");

                    Messages messages=new Messages();
                    messages.setFrom(chatUser.getPersodId());
                    messages.setMessage("");
                    messages.setSeen(false);
                    messages.setType("janina");


                    ChatModel chatModel = new ChatModel(chatUser,messages,Calendar.getInstance().getTime().getTime()+"",fileModel);
                    //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    sendFileToStorage(chatModel);
                }
            });
        }else{
            //IS NULL
        }

    }


    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {
        Intent intent = new Intent(this,FullScreenImageActivity.class);
        intent.putExtra("nameUser",nameUser);
        intent.putExtra("urlPhotoUser",urlPhotoUser);
        intent.putExtra("urlPhotoClick",urlPhotoClick);
        startActivity(intent);
    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseAdapter != null)
            firebaseAdapter.startListening();

    }
    @Override
    protected void onStop() {
        super.onStop();


        if (firebaseAdapter != null)
            firebaseAdapter.startListening();

        Log.e("Online","Going Off");

        if (mAuth.getCurrentUser() != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null){
            mUserRef.child("online").setValue("true");
        }

    }

}
