package com.socialsite.daffodilvarsity.model;

import java.util.ArrayList;

/**
 * Created by User on 3/21/2018.
 */

public class GroupInfo {

    private String groupKey;
    private String groupName;

    private ArrayList<String> memberList;

    public GroupInfo() {
    }

    public GroupInfo(String groupKey, String groupName, ArrayList<String> memberList) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.memberList = memberList;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }
}
