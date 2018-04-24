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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.socialsite.daffodilvarsity.R;
import com.socialsite.daffodilvarsity.managers.ProfileManager;
import com.socialsite.daffodilvarsity.managers.listeners.OnObjectChangedListener;
import com.socialsite.daffodilvarsity.managers.listeners.OnProfileCreatedListener;
import com.socialsite.daffodilvarsity.model.Profile;
import com.socialsite.daffodilvarsity.utils.ValidationUtil;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EditProfileActivity extends PickImageActivity implements OnProfileCreatedListener {
    private static final String TAG = EditProfileActivity.class.getSimpleName();

    // UI references.
    private EditText nameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    private ImageView edit_profile_name,edit_profile_phone,edit_profile_birthday,edit_profile_varsityid,edit_profile_department,
                        edit_profile_varsitybatchno,edit_profile_varsitysection,edit_profile_maritialstatus,edit_profile_gender,edit_profile_age;


    private TextView full_name_text,phone_text,birthday_text,email_text,varsityid_text,varsitydepartment_text,varsitybatchno_text,varsitysection_text,maritialstatus_text,gender_text,age_text;


    private String name;
    private String phone;
    private String birthday;
    private String varsityId;
    private String department;
    private int batchNo;
    private int age;
    private String section;
    private String maritialStatus;
    private String gender;

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile2);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);
        edit_profile_name=findViewById(R.id.edit_profile_name);
        edit_profile_birthday=findViewById(R.id.edit_profile_birthday);
        edit_profile_phone=findViewById(R.id.edit_profile_phone);
        edit_profile_varsitysection=findViewById(R.id.edit_profile_varsitysection);
        edit_profile_varsitybatchno=findViewById(R.id.edit_profile_varsitybatchno);
        edit_profile_varsityid=findViewById(R.id.edit_profile_varsityid);
        edit_profile_department=findViewById(R.id.edit_profile_department);
        edit_profile_gender=findViewById(R.id.edit_profile_gender);
        edit_profile_maritialstatus=findViewById(R.id.edit_profile_maritialstatus);
        edit_profile_age=findViewById(R.id.edit_profile_age);
        //nameEditText = (EditText) findViewById(R.id.nameEditText);



        full_name_text=findViewById(R.id.full_name_text);
        birthday_text=findViewById(R.id.birthday_text);
        phone_text=findViewById(R.id.phone_text);
        email_text=findViewById(R.id.email_text);
        varsityid_text=findViewById(R.id.varsityid_text);
        gender_text=findViewById(R.id.gender_text);
        maritialstatus_text=findViewById(R.id.maritialstatus_text);
        varsitybatchno_text=findViewById(R.id.varsitybatchno_text);
        varsitysection_text=findViewById(R.id.varsitysection_text);
        varsitydepartment_text=findViewById(R.id.varsitydepartment_text);
        age_text=findViewById(R.id.age_text);



        showProgress();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ProfileManager.getInstance(this).getProfileSingleValue(firebaseUser.getUid(), createOnProfileChangedListener());

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(v);
            }
        });





        edit_profile_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });

        edit_profile_phone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });
        edit_profile_birthday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });
        edit_profile_varsityid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });
        edit_profile_department.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialogwithSpinner(v);
            }
        });
        edit_profile_varsitybatchno.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });
        edit_profile_varsitysection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialogwithSpinner(v);
            }
        });
        edit_profile_gender.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialogwithSpinner(v);
            }
        });
        edit_profile_maritialstatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialogwithSpinner(v);
            }
        });


        edit_profile_age.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buidlDialog(v);
            }
        });



    }

    @Override
    public ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void onImagePikedAction() {
        startCropImageActivity();
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                if ( profile != null ){
                    full_name_text.setText(profile.getUsername());
                    email_text.setText(profile.getEmail());
                    phone_text.setText(profile.getPhone()+"");
                    birthday_text.setText(profile.getBirthday());
                    varsitybatchno_text.setText(profile.getBatchNo()+"");
                    varsitydepartment_text.setText(profile.getDepartment());
                    varsityid_text.setText(profile.getVarsityId());
                    gender_text.setText(profile.getGender());
                    varsitysection_text.setText(profile.getSection());
                    maritialstatus_text.setText(profile.getMaritialStatus());
                    age_text.setText(profile.getAge()+"");
                }
                fillUIFields();
            }
        };
    }

    private void fillUIFields() {
        if (profile != null) {
            //nameEditText.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(profile.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
            }
        }
        hideProgress();
        //nameEditText.requestFocus();
    }

    private void attemptCreateProfile() {

        // Reset errors.
        //nameEditText.setError(null);

         //Store values at the time of the login attempt.
        //nameEditText.getText().toString().trim();

        /*boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            //nameEditText.setError(getString(R.string.error_field_required));
            //focusView = nameEditText;
            cancel = true;
        } else if (!ValidationUtil.isNameValid(name)) {
            //nameEditText.setError(getString(R.string.error_profile_name_length));
            //focusView = edit_profile_name;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            //focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

        }*/
        Log.e("attemptCreateProfile","inside it");
        showProgress();
        if (name != null)
        profile.setUsername(name.toLowerCase());
        if (phone != null)
        profile.setPhone(phone);
        if (birthday != null)
        profile.setBirthday(birthday);
        if (department != null)
        profile.setDepartment(department.toLowerCase());
        if (gender  != null)
        profile.setGender(gender);
        if ( batchNo != 0 )
        profile.setBatchNo(batchNo);
        if (maritialStatus != null)
        profile.setMaritialStatus(maritialStatus);
        if (section != null)
        profile.setSection(section.toLowerCase());
        if ( age != 0 )
        profile.setAge(age);
        if (varsityId != null)
        profile.setVarsityId(varsityId);
        ProfileManager.getInstance(this).createOrUpdateProfile(profile, imageUri, this);


    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        handleCropImageResult(requestCode, resultCode, data);
    }



    @Override
    public void onProfileCreated(boolean success) {
        hideProgress();

        if (success) {
            finish();
        } else {
            showSnackBar(R.string.error_fail_update_profile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                Log.e("onOptionsItemSelected","pressed");
                if (hasInternetConnection()) {
                    attemptCreateProfile();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void buidlDialog(final View v){

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edit_profile_info, null);
        final EditText editText=view.findViewById(R.id.editProfileInfo);
        final AlertDialog.Builder aler=new AlertDialog.Builder(this);
        aler.setView(view);
        if (v.getId() == R.id.edit_profile_name){
            aler.setTitle("Edit Profile Name");

        }

        else if (v.getId() == R.id.edit_profile_birthday){
            aler.setTitle("Edit Birthday");
            editText.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        }

        else if (v.getId() == R.id.edit_profile_varsityid)
            aler.setTitle("Edit Varsity ID");
        else if (v.getId() == R.id.edit_profile_varsitybatchno){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            aler.setTitle("Edit Varsity Batch NO");
        }

        else if (v.getId() == R.id.edit_profile_phone){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            aler.setTitle("Edit Phone NO");
        }

        else if (v.getId() ==R.id.edit_profile_age){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            aler.setTitle("Edit Age");
        }



        aler.setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (v.getId() == R.id.edit_profile_name)
                {
                    if (    !editText.getText().toString().isEmpty() ) {
                        name = editText.getText().toString().trim();
                        full_name_text.setText(name);
                    }

                }
                else if (v.getId() == R.id.edit_profile_birthday){
                    if (!editText.getText().toString().isEmpty()) {
                        birthday = editText.getText().toString().trim();
                        birthday_text.setText(birthday);
                    }
                }

                else if (v.getId() == R.id.edit_profile_varsityid)
                {
                    if (!editText.getText().toString().isEmpty()){
                        varsityId=editText.getText().toString().trim();
                        varsityid_text.setText(varsityId);

                    }

                }
                else if (v.getId() == R.id.edit_profile_varsitybatchno) {
                    if (!editText.getText().toString().isEmpty()) {
                        batchNo = Integer.parseInt(editText.getText().toString().trim());
                        varsitybatchno_text.setText(batchNo + "");

                    }
                }
                else if (v.getId() == R.id.edit_profile_phone)
                {
                    if(!editText.getText().toString().isEmpty()) {
                        phone = editText.getText().toString().trim();
                        phone_text.setText(phone + "");

                    }
                }

                else if (v.getId() ==R.id.edit_profile_age){
                    if (!editText.getText().toString().isEmpty()) {
                        age = Integer.parseInt(editText.getText().toString().trim());
                        age_text.setText(age + "");

                    }
                }

                dialog.dismiss();
            }
        });

        aler.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog=aler.create();
        dialog.show();

    }



    private void buildDialogwithSpinner(final View v){

        final Resources resources=getResources();

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edit_profile_info_spinner, null);
        final NiceSpinner spinner=view.findViewById(R.id.nice_spinner);
        final AlertDialog.Builder aler=new AlertDialog.Builder(this);
        aler.setView(view);

        if(R.id.edit_profile_department == v.getId()){
            aler.setTitle("Choose Department");
            List<String> departments=new LinkedList<>(Arrays.asList(resources.getStringArray(R.array.departments_name)));
            spinner.attachDataSource(departments);
        }

        else if(R.id.edit_profile_gender == v.getId()){
            aler.setTitle("Select Gender");
            List<String> gender=new LinkedList<>(Arrays.asList(resources.getStringArray(R.array.gender)));
            spinner.attachDataSource(gender);
        }

        else if (R.id.edit_profile_varsitysection == v.getId()){
            aler.setTitle("Choose Section");
            List<String> sections=new LinkedList<>(Arrays.asList(resources.getStringArray(R.array.section)));
            spinner.attachDataSource(sections);
        }

        else if (R.id.edit_profile_maritialstatus == v.getId()){
            aler.setTitle("Choose Status");
            List<String> status=new LinkedList<>(Arrays.asList(resources.getStringArray(R.array.maritial_status)));
            spinner.attachDataSource(status);
        }

        aler.setView(view);


        aler.setPositiveButton(R.string.postive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(R.id.edit_profile_department == v.getId()) {

                    if (!resources.getStringArray(R.array.departments_name)[spinner.getSelectedIndex()].isEmpty()) {
                        department=resources.getStringArray(R.array.departments_name)[spinner.getSelectedIndex()];
                        varsitydepartment_text.setText(department);
                    }
                }
                else if (R.id.edit_profile_gender == v.getId()){
                    if(!resources.getStringArray(R.array.gender)[spinner.getSelectedIndex()].isEmpty()) {
                        gender=resources.getStringArray(R.array.gender)[spinner.getSelectedIndex()];
                        gender_text.setText(gender);
                    }
                }
                else if (R.id.edit_profile_varsitysection == v.getId()){
                    if(! resources.getStringArray(R.array.section)[spinner.getSelectedIndex()].isEmpty()) {
                        section=resources.getStringArray(R.array.section)[spinner.getSelectedIndex()];
                        varsitysection_text.setText(section);
                    }
                }
                else if (R.id.edit_profile_maritialstatus == v.getId()){
                    if (! resources.getStringArray(R.array.maritial_status)[spinner.getSelectedIndex()].isEmpty() ){
                        maritialStatus=resources.getStringArray(R.array.maritial_status)[spinner.getSelectedIndex()];
                                maritialstatus_text.setText(maritialStatus);
                    }

                }
                dialog.dismiss();
            }
        });

        aler.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog=aler.create();
        dialog.show();;


    }

}

