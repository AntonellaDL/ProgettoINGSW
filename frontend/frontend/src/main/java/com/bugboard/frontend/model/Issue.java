package com.bugboard.frontend.model;

public class Issue {
    private String id;
    private String title;
    private String description;
    private String status;
    private String type;
    private String priority;
    private String assignee;

    //id dell'assegnatario
    
    private Long assigneeId;
    //id del creatore
    private Long creatorId;
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    private byte[] imageData;
    private String imageName;

    public Issue(){}

    public Issue(String id, String title, String description, String status, String type, String priority, String assignee, byte[] imageData, String imageName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.priority = priority;
        this.assignee = assignee;
        this.imageData = imageData;
        this.imageName = imageName;
    }

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public String getType() {
        return type;
    }
    public String getPriority() {
        return priority;
    }
    public String getAssignee() {
        return assignee;
    }

    public Long getAssigeeId(){
        return assigneeId;
    }

    public Long getCreatorId(){
        return creatorId;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
    public void setAssigneeId(Long assigneeId){
        this.assigneeId=assigneeId;
    }
    public void setCreatorId(Long creatorId){
        this.creatorId=creatorId;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    @Override
    public String toString() {
        return title;
    }

    public java.util.List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void setComments(java.util.List<Comment> comments) {
        this.comments = comments;
    }

}
