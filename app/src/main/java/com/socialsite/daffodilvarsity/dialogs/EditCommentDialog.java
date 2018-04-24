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

package com.socialsite.daffodilvarsity.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.socialsite.daffodilvarsity.R;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

/**
 * Created by alexey on 12.05.17.
 */

public class EditCommentDialog extends DialogFragment {
    public static final String TAG = EditCommentDialog.class.getSimpleName();
    public static final String COMMENT_TEXT_KEY = "EditCommentDialog.COMMENT_TEXT_KEY";
    public static final String COMMENT_ID_KEY = "EditCommentDialog.COMMENT_ID_KEY";

    private CommentDialogCallback callback;
    private String commentText;
    private String commentId;

    private EmojiconEditText emojiconEditText;
    private ImageView emojiImageView;
    private LinearLayout root;
    private EmojIconActions emojIcon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() instanceof CommentDialogCallback) {
            callback = (CommentDialogCallback) getActivity();
        } else {
            throw new RuntimeException(getActivity().getTitle() + " should implements CommentDialogCallback");
        }

        commentText = (String) getArguments().get(COMMENT_TEXT_KEY);
        commentId = (String) getArguments().get(COMMENT_ID_KEY);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edit_comment, null);

        //final EditText editCommentEditText =  view.findViewById(R.id.editCommentEditText);
        root=  view.findViewById(R.id.root);
        emojiconEditText =  view.findViewById(R.id.emojicon_edit_text);
        emojiImageView =  view.findViewById(R.id.emoji_btn);
        emojIcon = new EmojIconActions(getActivity(), root, emojiconEditText, emojiImageView);

        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        Log.e("ShowEmojIcon","showed");
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("PostDetailsActivity","keyboard opened");
            }
            @Override
            public void onKeyboardClose() {
                Log.e("PostDetailsActivity","Keyboard closed");
            }
        });



        if (commentText != null) {
            emojiconEditText.setText(commentText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.title_edit_comment)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_title_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCommentText = emojiconEditText.getText().toString();

                        if (!newCommentText.equals(commentText) && callback != null) {
                            callback.onCommentChanged(newCommentText, commentId);
                        }

                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    public interface CommentDialogCallback {
        void onCommentChanged(String newText, String commentId);
    }
}
