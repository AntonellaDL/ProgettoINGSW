package com.bugboard.frontend.model.dto;

public class CreateIssueRequest{
    private String title;
    private String description;
    private String type;
    private String priority;
    private byte[] imageData;
    private String imageName;

    public CreateIssueRequest(){}

    public CreateIssueRequest(String title, String description, String type, String priority, byte[] imageData, String imageName){
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.imageData = imageData;
        this.imageName = imageName;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type=type;
    }

    public String getPriority(){
        return priority;
    }

    public void setPriority(String priority){
        this.priority = priority;
    }

    public byte[] getImageData(){
        return imageData;
    }

    public void setImageData(byte [] imageData){
        this.imageData = imageData;
    }

    public String getImageName(){
        return imageName;
    }

    public void setImageName(String imageName){
        this.imageName= imageName;
    }



}