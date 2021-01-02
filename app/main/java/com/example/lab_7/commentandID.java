package com.example.lab_7;

import com.google.firebase.database.PropertyName;

public class commentandID {
    @PropertyName("comments_content")
    public String comments_content;
    @PropertyName("commenter_id")
    public String commenter_id;
    @PropertyName("commenter_URL")
    public String commenter_URL;

    public commentandID(){ }

    public commentandID(String comments_content, String commenter_id,String commenter_URL) {
        this.comments_content = comments_content;
        this.commenter_id = commenter_id;
        this.commenter_URL=commenter_URL;
    }
    public String getcomments_content(String content) {
        return comments_content;
    }

    public String getcommenter_id(String ID) {
        return commenter_id;
    }

    public String getcommenter_URL(String ID) {
        return commenter_URL;
    }

    public String CommentShowlist(){
        return commenter_id +":  "+comments_content;
    }



}