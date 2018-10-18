package com.app.efficiclean.classes;

import java.sql.Timestamp;

public class Job {

    private String createdBy;
    private String roomNumber;
    private long timestamp;
    private int priorityCounter;
    private boolean isCompleted;
    private String assignedTo;
    private String description;
    public String key;

    public Job() {
        timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        isCompleted = false;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priorityCounter;
    }

    public void setPriority(int pCounter) {
        priorityCounter = pCounter;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void assignTo(String hk) {
        assignedTo = hk;
    }

    public boolean getStatus() {
        return isCompleted;
    }

    public void markCompleted() {
        isCompleted = true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
