package com.socialsite.daffodilvarsity.activities;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.holders.UsersViewHolder;
import com.socialsite.daffodilvarsity.model.Profile;

public class SearchPeople extends BaseActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private ProgressBar progressBar;
    private RecyclerView mResultList;
    private Toolbar toolbar;
    private Spinner spinner;


    private FirebaseRecyclerAdapter<Profile, UsersViewHolder> mPeopleRVAdapter;
    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerOptions<Profile> personsOptions;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people);



        mUserDatabase = FirebaseDatabase.getInstance().getReference("profiles");


        mSearchField =  findViewById(R.id.search_field);
        mSearchBtn =  findViewById(R.id.search_btn);
        mResultList =  findViewById(R.id.result_list);
        progressBar= findViewById(R.id.progressBar);
        toolbar=findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.search_people);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));


        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString().trim();

                if (hasInternetConnection()){
                    if( !TextUtils.isEmpty(searchText) ) {

                        hideKeyboard();

                        firebaseUserSearch(searchText.toLowerCase());
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    else {
                        mSearchField.setError("Type a name");
                    }
                }
                else {
                    showSnackBar("No Internet Connection");
                }

            }
        });

    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUserDatabase.limitToLast(10).orderByChild(spinner.getSelectedItem().toString()).startAt(searchText).endAt(searchText + "\uf8ff");

        firebaseSearchQuery.keepSynced(true);

        personsOptions = new FirebaseRecyclerOptions.Builder<Profile>().setQuery(firebaseSearchQuery, Profile.class).build();

        mPeopleRVAdapter = new FirebaseRecyclerAdapter<Profile, UsersViewHolder>(personsOptions) {

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.search_item_layout, parent, false);
                Log.w("UsersViewHolder","working here");
                return new UsersViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Profile model) {
                Log.w("onBindViewHolder","user id : "+model.getId()+","+model.getVarsityId());
                holder.setDetails(getApplicationContext(),model.getUsername(),model.getPhotoUrl(),model.getVarsityId(),model.getBatchNo(), model.getSection(),model.getId());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }
        };

        if (mPeopleRVAdapter == null){
            Log.w("mPeopleRVAdapter","is null");
        }
        else{
            Log.w("mPeopleRVAdapter","is not null");
        }
        mResultList.setAdapter(mPeopleRVAdapter);

        if (mPeopleRVAdapter != null)
            mPeopleRVAdapter.startListening();

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPeopleRVAdapter != null)
        mPeopleRVAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_people_menu, menu);

        MenuItem item = menu.findItem(R.id.filter_search);
        spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_by, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPeopleRVAdapter != null)
            mPeopleRVAdapter.startListening();

    }
}
