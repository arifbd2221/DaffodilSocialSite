package com.socialsite.daffodilvarsity.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.activities.ChatListActivity;
import com.socialsite.daffodilvarsity.activities.GroupChatActivity;
import com.socialsite.daffodilvarsity.model.Conversation;
import com.socialsite.daffodilvarsity.model.GroupInfo;

/**
 * Created by User on 3/20/2018.
 */

public class Groups extends Fragment {


    public static String key;
    RecyclerView groupList;
    private FirebaseRecyclerAdapter<GroupInfo, Groups.GroupViewHolder> mGroupListAdapter;
    private DatabaseReference mgroupListDatabase;
    private FirebaseRecyclerOptions<GroupInfo> groupOptions;
    private ProgressBar progressBar;


    private String mCurrentuserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.group_fragment_layout,container,false);
        Log.e("onCreateView","Yes we are here");
        mCurrentuserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        mgroupListDatabase= FirebaseDatabase.getInstance().getReference().child("GroupMember").child(mCurrentuserId);
        groupList=view.findViewById(R.id.group_list);


        groupList.setHasFixedSize(true);
        groupList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        Log.e("onStart","Yes we are here");

        Query firebaseSearchQuery = mgroupListDatabase.orderByChild("groupName");
        firebaseSearchQuery.keepSynced(true);

        groupOptions = new FirebaseRecyclerOptions.Builder<GroupInfo>().setQuery(firebaseSearchQuery, GroupInfo.class).build();


        mGroupListAdapter = new FirebaseRecyclerAdapter<GroupInfo, GroupViewHolder>(groupOptions){

            @Override
            public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Log.e("onCreateViewHolder","Yes we are here");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item_layout,parent,false);

                return new GroupViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull final GroupInfo model) {
                Log.e("onBindViewHolder","Yes we are here");
                holder.groupName.setText(model.getGroupName());

                holder.group_list_item_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent groupChatIntent = new Intent(getActivity(), GroupChatActivity.class);
                        key=model.getGroupKey();
                        startActivity(groupChatIntent);


                    }
                });


            }
        };


        groupList.setAdapter(mGroupListAdapter);

        if (mGroupListAdapter != null){
            mGroupListAdapter.startListening();
        }

        Log.e("onStartBottom","Yes we are here");

    }


    private static class GroupViewHolder extends RecyclerView.ViewHolder{

        TextView groupName;
        RelativeLayout group_list_item_root;

        public GroupViewHolder(View itemView) {
            super(itemView);

            groupName=itemView.findViewById(R.id.group_name);
            group_list_item_root=itemView.findViewById(R.id.group_list_item_root);

        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (mGroupListAdapter != null){
            mGroupListAdapter.stopListening();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mGroupListAdapter != null){
            mGroupListAdapter.startListening();
        }

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh your fragment here

            if (mGroupListAdapter != null){
                mGroupListAdapter.startListening();
            }

        }
    }

}
