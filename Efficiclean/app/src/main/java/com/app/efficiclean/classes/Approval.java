package com.app.efficiclean.classes;

public class Approval {

    protected Job job;
    protected String createdBy;
    protected Boolean isApproved;
    protected int priorityCounter;

    public Approval() {
        isApproved = false;
        priorityCounter = 0;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public int getPriorityCounter() {
        return priorityCounter;
    }

    public void incrementPriorityCounter() {
        this.priorityCounter++;
    }
}
