/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.socialsite.daffodilvarsity.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.PostsByUserAdapter;
import com.socialsite.daffodilvarsity.adapters.holders.UsersViewHolder;
import com.socialsite.daffodilvarsity.enums.PostStatus;
import com.socialsite.daffodilvarsity.managers.PostManager;
import com.socialsite.daffodilvarsity.managers.ProfileManager;
import com.socialsite.daffodilvarsity.managers.listeners.OnObjectChangedListener;
import com.socialsite.daffodilvarsity.managers.listeners.OnObjectExistListener;
import com.socialsite.daffodilvarsity.model.Post;
import com.socialsite.daffodilvarsity.model.Profile;
import com.socialsite.daffodilvarsity.utils.LogUtil;
import com.socialsite.daffodilvarsity.utils.LogoutHelper;
import android.support.design.widget.FloatingActionButton;


public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static String userName;
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private TextView department,batch,section,gender,birthday,varsityid,maritialstatus,phone;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView;
    private TextView postsLabelTextView;
    private ProgressBar postsProgressBar;
    private FloatingActionButton addPeopleTOChatList;


    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mRootRef;
    private String currentUserId;
    private String userID,guestName,guestImage;

    private Profile profile,tempOwnProfile;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout swipeContainer;
    private TextView likesCountersTextView;
    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        addPeopleTOChatList= findViewById(R.id.add_people_to_chatList);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();


        mAuth = FirebaseAuth.getInstance();
        mRootRef= FirebaseDatabase.getInstance().getReference();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Log.w("ProfileActivity",getIntent().getComponent().getClassName().toString());

        if (getIntent().getIntExtra(UsersViewHolder.CHECK,0) == 100)
        {

            userID = getIntent().getStringExtra(UsersViewHolder.SEARCHEDPERSON);
            guestName =getIntent().getStringExtra("guestName");
            guestImage=getIntent().getStringExtra("guestImage");

            Log.e("Guest name: "+guestName,"Guest Id: "+userID);

            if ( !userID.equals(mAuth.getCurrentUser().getUid()) ){
                loadProfile(mAuth.getCurrentUser().getUid());
                addPeopleTOChatList.setVisibility(View.VISIBLE);
            }

        }
        else
            userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);





        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        // Set up the login form.
        progressBar =  findViewById(R.id.progressBar);
        imageView =  findViewById(R.id.imageView);
        nameEditText =  findViewById(R.id.nameEditText);
        postsCounterTextView =  findViewById(R.id.postsCounterTextView);
        likesCountersTextView =  findViewById(R.id.likesCountersTextView);
        postsLabelTextView =  findViewById(R.id.postsLabelTextView);
        postsProgressBar =  findViewById(R.id.postsProgressBar);


        phone=findViewById(R.id.phone);
        department=findViewById(R.id.department);
        batch=findViewById(R.id.batch);
        section=findViewById(R.id.section);
        gender=findViewById(R.id.gender);
        birthday=findViewById(R.id.birthday);
        maritialstatus=findViewById(R.id.maritialstatus);
        varsityid=findViewById(R.id.varsityid);



        swipeContainer =  findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshAction();
            }
        });

        addPeopleTOChatList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasInternetConnection()) {

                    if (tempOwnProfile != null){
                            Log.e("Current name: "+tempOwnProfile.getUsername(),"Current Id: "+tempOwnProfile.getId());

                        mRootRef.child("Chat").child(tempOwnProfile.getId()).child(userID).child("seen").setValue(true);
                        mRootRef.child("Chat").child(tempOwnProfile.getId()).child(userID).child("timestamp").setValue(ServerValue.TIMESTAMP);

                        mRootRef.child("Chat").child(userID).child(tempOwnProfile.getId()).child("seen").setValue(false);
                        mRootRef.child("Chat").child(userID).child(tempOwnProfile.getId()).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            profileManager.addToChatList(userID,tempOwnProfile.getId(),guestName,guestImage);
                            Log.e("Guest name: "+guestName,"Guest Id: "+userID);
                            profileManager.addToChatList(tempOwnProfile.getId(),userID,tempOwnProfile.getUsername(),tempOwnProfile.getPhotoUrl().toString());


                        showSnackBar(R.string.success);
                    }

                    else
                        showSnackBar("No Profile Have Found");

                } else {
                    showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                }
            }
        });



        loadPostsList();
        supportPostponeEnterTransition();
    }

    public void showFloatButtonRelatedSnackBar(int messageId) {
        showSnackBar(addPeopleTOChatList, messageId);
    }


    @Override
    public void onStart() {
        super.onStart();
        loadProfile();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        profileManager.closeListeners(this);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    postsAdapter.loadPosts();
                    showSnackBar(R.string.message_post_was_created);
                    setResult(RESULT_OK);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    if (data != null) {
                        PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                        if (postStatus.equals(PostStatus.REMOVED)) {
                            postsAdapter.removeSelectedPost();

                        } else if (postStatus.equals(PostStatus.UPDATED)) {
                            postsAdapter.updateSelectedPost();
                        }
                    }
                    break;
            }
        }
    }

    private void onRefreshAction() {
        postsAdapter.loadPosts();
    }

    private void loadPostsList() {
        if (recyclerView == null) {

            recyclerView =  findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, userID);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {
                    PostManager.getInstance(ProfileActivity.this).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
                        @Override
                        public void onDataChanged(boolean exist) {
                            if (exist) {
                                openPostDetailsActivity(post, view);
                            } else {
                                showSnackBar(R.string.error_post_was_removed);
                            }
                        }
                    });
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    String postsLabel = getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
                    postsCounterTextView.setText(buildCounterSpannable(postsLabel, postsCount));

                    likesCountersTextView.setVisibility(View.VISIBLE);
                    postsCounterTextView.setVisibility(View.VISIBLE);

                    if (postsCount > 0) {
                        postsLabelTextView.setVisibility(View.VISIBLE);
                    }

                    swipeContainer.setRefreshing(false);
                    hideLoadingPostsProgressBar();
                }

                @Override
                public void onPostLoadingCanceled() {
                    swipeContainer.setRefreshing(false);
                    hideLoadingPostsProgressBar();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
        }
    }

    private Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    private void openPostDetailsActivity(Post post, View v) {
        Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post.getId());
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(ProfileActivity.this,
                            new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                    );
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void loadProfile() {
        profileManager = ProfileManager.getInstance(this);
        profileManager.getProfileValue(ProfileActivity.this, userID, createOnProfileChangedListener());
    }

    private void loadProfile(String mCurrentOwnerToChatList) {
        profileManager = ProfileManager.getInstance(this);
        profileManager.getProfileValue(ProfileActivity.this, mCurrentOwnerToChatList, createOnProfileChangedListener(0));
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener(int i) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                tempOwnProfile=obj;
            }
        };
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillUIFields(obj);
            }
        };
    }

    private void fillUIFields(Profile profile) {
        if (profile != null) {
            this.profile=profile;
            String s=profile.getUsername();
            nameEditText.setText(s.substring(0).toUpperCase()+s.substring(1));
            s=profile.getDepartment();
            department.setText("Department: "+s.substring(0,1).toUpperCase()+s.substring(1));
            batch.setText("Batch No: "+profile.getBatchNo());
            section.setText("Section: "+profile.getSection());
            gender.setText("Gender: "+profile.getGender());
            varsityid.setText("ID: "+profile.getVarsityId());
            phone.setText("+88"+profile.getPhone());
            maritialstatus.setText("Status: "+profile.getMaritialStatus());
            birthday.setText(profile.getBirthday());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                scheduleStartPostponedTransition(imageView);
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
            } else {
                progressBar.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ic_stub);
            }

            int likesCount = (int) profile.getLikesCount();
            String likesLabel = getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
            likesCountersTextView.setText(buildCounterSpannable(likesLabel, likesCount));
        }
    }

    private void hideLoadingPostsProgressBar() {
        if (postsProgressBar.getVisibility() != View.GONE) {
            postsProgressBar.setVisibility(View.GONE);
        }
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startEditProfileActivity() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userID.equals(currentUserId)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_menu, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editProfile:
                startEditProfileActivity();
                return true;
            case R.id.signOut:
                LogoutHelper.signOut(mGoogleApiClient, this);
                startMainActivity();
                return true;
            case R.id.createPost:
                if (hasInternetConnection()) {
                    openCreatePostActivity();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
