/*
 *
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
 *
 */

package com.socialsite.daffodilvarsity.adapters.holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.adapters.CommentsAdapter;
import com.socialsite.daffodilvarsity.managers.ProfileManager;
import com.socialsite.daffodilvarsity.managers.listeners.OnObjectChangedListener;
import com.socialsite.daffodilvarsity.model.Comment;
import com.socialsite.daffodilvarsity.model.Profile;
import com.socialsite.daffodilvarsity.utils.FormatterUtil;
import com.socialsite.daffodilvarsity.views.ExpandableTextView;

/**
 * Created by alexey on 10.05.17.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private final ImageView avatarImageView;
    private final ExpandableTextView commentTextView;
    private final TextView dateTextView;
    private final ProfileManager profileManager;
    private CommentsAdapter.Callback callback;
    private Context context;

    public CommentViewHolder(View itemView, final CommentsAdapter.Callback callback) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();
        profileManager = ProfileManager.getInstance(itemView.getContext().getApplicationContext());

        avatarImageView = (ImageView) itemView.findViewById(R.id.avatarImageView);
        commentTextView = (ExpandableTextView) itemView.findViewById(R.id.commentText);
        dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);

        if (callback != null) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        callback.onLongItemClick(v, position);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    public void bindData(Comment comment) {
        final String authorId = comment.getAuthorId();
        if (authorId != null)
            profileManager.getProfileSingleValue(authorId, createOnProfileChangeListener(commentTextView,
                    avatarImageView, comment.getText()));

        commentTextView.setText(comment.getText());

        CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, comment.getCreatedDate());
        dateTextView.setText(date);

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAuthorClick(authorId, v);
            }
        });
    }

    private OnObjectChangedListener<Profile> createOnProfileChangeListener(final ExpandableTextView expandableTextView, final ImageView avatarImageView, final String comment) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                String userName = obj.getUsername();

                if (userName == null)
                    userName="Demo";

                fillComment(userName, comment, expandableTextView);

                if (obj.getPhotoUrl() != null) {
                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .error(R.drawable.ic_stub)
                            .into(avatarImageView);
                }
            }
        };
    }

    private void fillComment(String userName, String comment, ExpandableTextView commentTextView) {
        Spannable contentString = new SpannableStringBuilder(userName + "   " + comment);
        contentString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text)),
                0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        commentTextView.setText(contentString);
    }
}
