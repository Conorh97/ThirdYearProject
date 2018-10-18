package com.app.efficiclean.classes;

import java.util.HashMap;

public class Supervisor extends Staff {

    private boolean onDuty;
    public HashMap<String, Approval> approvals;

    public Supervisor() {
        super();
        approvals = new HashMap<String, Approval>();
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    public void setOnDuty(boolean onDuty) {
        this.onDuty = onDuty;
    }
}
