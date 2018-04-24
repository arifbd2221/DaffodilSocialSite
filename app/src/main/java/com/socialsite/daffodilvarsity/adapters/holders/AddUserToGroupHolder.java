package com.socialsite.daffodilvarsity.adapters.holders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.activities.ProfileActivity;

/**
 * Created by User on 3/20/2018.
 */

public class AddUserToGroupHolder extends RecyclerView.ViewHolder{

    private RelativeLayout root;
    public CheckBox checkBox;

    public AddUserToGroupHolder(View itemView) {
        super(itemView);
        checkBox=itemView.findViewById(R.id.select_this_person_for_group);
    }

    public void setDetails(final Context ctx, final String userName, final String userImage, String varsityid, int batch, String section, final String uID){

        root=itemView.findViewById(R.id.root);
        final ImageView profileImage=itemView.findViewById(R.id.search_profile_photo);
        final TextView name=itemView.findViewById(R.id.search_profile_name);
        TextView varsityID=itemView.findViewById(R.id.search_profile_varsity_id);
        TextView batchNO=itemView.findViewById(R.id.search_profile_batch);
        TextView sectionOrder=itemView.findViewById(R.id.search_profile_section);
        final ProgressBar progressBar=itemView.findViewById(R.id.progressBar);
        if (userImage != null) {
            Glide.with(ctx)
                    .load(userImage)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade()
                    .error(R.drawable.ic_stub)
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
                    .into(profileImage);
        } else {
            progressBar.setVisibility(View.GONE);
            profileImage.setImageResource(R.drawable.ic_stub);
        }

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent profileIntent=new Intent(ctx, ProfileActivity.class);
                profileIntent.putExtra("guestName",userName);
                profileIntent.putExtra("guestImage",userImage);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                ctx.startActivity(profileIntent);*/
            }
        });

        name.setText(userName);
        varsityID.setText(varsityid);
        batchNO.setText("Batch: "+batch);
        sectionOrder.setText("Section: "+section);

    }

}
