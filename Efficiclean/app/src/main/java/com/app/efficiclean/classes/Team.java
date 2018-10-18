package com.app.efficiclean.classes;

import java.util.ArrayList;

public class Team {

    private String status;
    private Job currentJob;
    private Job returnedJob;
    private int breakRemaining;
    private int cleanCounter;
    private int priorityCounter;
    private ArrayList<String> members;
    protected String key;

    public Team() {
        members = new ArrayList<String>();
        breakRemaining = 60;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBreakRemaining() {
        return breakRemaining;
    }

    public void setBreakRemaining(int breakRemaining) {
        this.breakRemaining = breakRemaining;
    }

    public int getCleanCounter() {
        return cleanCounter;
    }

    public void setCleanCounter(int cleanCounter) {
        this.cleanCounter = cleanCounter;
    }

    public int getPriorityCounter() {
        return priorityCounter;
    }

    public void setPriorityCounter(int priorityCounter) {
        this.priorityCounter = priorityCounter;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(Job currentJob) {
        this.currentJob = currentJob;
    }

    public Job getReturnedJob() {
        return returnedJob;
    }

    public void setReturnedJob(Job returnedJob) {
        this.returnedJob = returnedJob;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public String removeMember(int i) {
        String thisMember = "";
        if (i < members.size() && i >= 0) {
            thisMember = members.remove(i);
        }
        return thisMember;
    }

    public String getMember(int i) {
        String thisMember = "";
        if (i < members.size() && i >= 0) {
            thisMember = members.get(i);
        }
        return thisMember;
    }

    public String getKey() {
        return key;
    }
}
