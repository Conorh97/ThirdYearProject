package com.app.efficiclean.classes;

public class HazardApproval extends Approval {

    private String description;

    public HazardApproval() {
        super();
        priorityCounter = 2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
