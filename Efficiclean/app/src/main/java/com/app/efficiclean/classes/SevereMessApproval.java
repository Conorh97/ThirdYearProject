package com.app.efficiclean.classes;

public class SevereMessApproval extends Approval {

    private String description;

    public SevereMessApproval() {
        super();
        priorityCounter = 1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
