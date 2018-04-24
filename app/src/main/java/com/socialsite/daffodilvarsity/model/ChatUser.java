package com.socialsite.daffodilvarsity.model;

/**
 * Created by User on 3/15/2018.
 */

public class ChatUser {

    private String persodId;
    private String personName;
    private String personImage;


    public ChatUser(){

    }


    public ChatUser(String persodId, String personName, String personImage) {
        this.persodId = persodId;
        this.personName = personName;
        this.personImage = personImage;
    }

    public String getPersodId() {

        return persodId;
    }

    public void setPersodId(String persodId) {
        this.persodId = persodId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonImage() {
        return personImage;
    }

    public void setPersonImage(String personImage) {
        this.personImage = personImage;
    }

}
