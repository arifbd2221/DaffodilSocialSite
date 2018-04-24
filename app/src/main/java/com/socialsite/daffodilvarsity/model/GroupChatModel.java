package com.socialsite.daffodilvarsity.model;

/**
 * Created by User on 3/21/2018.
 */

public class GroupChatModel {

    private Messages message;
    private String timeStamp;
    private FileModel file;
    private MapModel mapModel;

    private String persodId;
    private String personName;
    private String personImage;


    public GroupChatModel() {
    }

    public GroupChatModel(Messages message, String timeStamp, String persodId, String personName, String personImage) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.persodId = persodId;
        this.personName = personName;
        this.personImage = personImage;
    }


    public Messages getMessage() {
        return message;
    }

    public void setMessage(Messages message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        this.file = file;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
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
