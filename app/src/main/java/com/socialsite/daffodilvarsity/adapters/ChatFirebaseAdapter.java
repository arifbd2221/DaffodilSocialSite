package com.socialsite.daffodilvarsity.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.listeners.ClickListenerChatFirebase;
import com.socialsite.daffodilvarsity.model.ChatModel;
import com.socialsite.daffodilvarsity.model.ChatUser;
import com.socialsite.daffodilvarsity.utils.CircleTransform;
import com.socialsite.daffodilvarsity.utils.Utils;


import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

/**
 * Created by Alessandro Barreto on 23/06/2016.
 */
public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<ChatModel,ChatFirebaseAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private ClickListenerChatFirebase mClickListenerChatFirebase;

    private String myself;


    public ChatFirebaseAdapter(FirebaseRecyclerOptions<ChatModel> ref, String myself, ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(ref);

        this.myself = myself;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        Log.e("onCreateViewHolder","viewType: "+viewType);
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
            return new MyChatViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
            return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model =(ChatModel) getItem(position);
        ChatUser chatUser = null;

        if (model.getUserModel() != null)
            chatUser = model.getUserModel();

        Log.e("getItemViewType","position: "+position);

        if (model.getMapModel() != null) {
            if (model.getUserModel().getPersodId().equals(myself)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getFile() != null) {
            if (model.getFile().getType().equals("img") && model.getUserModel().getPersodId().equals(myself)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (chatUser != null && chatUser.getPersonName() != null) {
            Log.e("chatUser: "+chatUser.getPersodId(),"Current user: : "+myself);
                if(chatUser.getPersodId().equals(myself)){
                    return RIGHT_MSG;
                }
                else {
                    return LEFT_MSG;
                }

        }

        return 0;

    }


    @Override
    protected void onBindViewHolder(@NonNull MyChatViewHolder holder, int position, @NonNull ChatModel model) {

        if (model.getMessage() != null)
            Log.e("onBindViewHolder","Message is not null "+model.getMessage().getMessage());

        try{
            holder.setIvUser(model.getUserModel().getPersonImage());
            holder.setTxtMessage(model.getMessage().getMessage());
            holder.setTvTimestamp(model.getTimeStamp());
            holder.tvIsLocation(View.GONE);
            if (model.getFile() != null){
                holder.tvIsLocation(View.GONE);
                holder.setIvChatPhoto(model.getFile().getUrl_file());
            }else if(model.getMapModel() != null){
                holder.setIvChatPhoto(Utils.local(model.getMapModel().getLatitude(),model.getMapModel().getLongitude()));
                holder.tvIsLocation(View.VISIBLE);
            }
        }catch (NullPointerException np){
            np.printStackTrace();
        }

    }


    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp,tvLocation;
        EmojiconTextView txtMessage;
        ImageView ivUser,ivChatPhoto;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = (TextView)itemView.findViewById(R.id.timestamp);
            txtMessage = (EmojiconTextView)itemView.findViewById(R.id.txtMessage);
            tvLocation = (TextView)itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView)itemView.findViewById(R.id.img_chat);
            ivUser = (ImageView)itemView.findViewById(R.id.ivUserChat);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            ChatModel model = getItem(position);
            if (model.getMapModel() != null){
                mClickListenerChatFirebase.clickImageMapChat(view,position,model.getMapModel().getLatitude(),model.getMapModel().getLongitude());
            }else{
                mClickListenerChatFirebase.clickImageChat(view,position,model.getUserModel().getPersonName(),model.getUserModel().getPersonImage(),model.getFile().getUrl_file());
            }
        }

        public void setTxtMessage(String message){
            if (txtMessage == null)return;
            txtMessage.setText(message);
        }

        public void setIvUser(String urlPhotoUser){
            if (ivUser == null)return;
            Glide.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().transform(new CircleTransform(ivUser.getContext())).override(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp){
            if (tvTimestamp == null)return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url){
            if (ivChatPhoto == null)return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void tvIsLocation(int visible){
            if (tvLocation == null)return;
            tvLocation.setVisibility(visible);
        }
    }

    private CharSequence converteTimestamp(String mileSegundos){
        long time=0;
        try{
            time= Long.parseLong(mileSegundos);
        }catch (NumberFormatException np){
            np.printStackTrace();
        }


        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

}
