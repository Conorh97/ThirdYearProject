package com.app.efficiclean.classes;

import java.util.Calendar;
import java.util.Date;

public class Break {

    private boolean accepted;
    private int breakLength;
    private Date breakTime;
    private String teamID;

    public Break() {
        accepted = false;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public void setBreakTimeDate(Date time) {
        breakTime = time;
    }

    public Date getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(String timeString) {
        //Get hour and minute values from formatted string
        int hour = Integer.parseInt(timeString.substring(0, 2));
        int minute = Integer.parseInt(timeString.substring(2, 4));

        //Create time instance for break
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        breakTime = time.getTime();
    }

    public int getBreakLength() {
        return breakLength;
    }

    public void setBreakLength(int breakLength) {
        this.breakLength = breakLength;
    }
}
