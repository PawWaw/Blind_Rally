package com.polsl.blindrally.models;

import java.util.List;

public class Track {

    private String trackName;
    private List<Turn> turnList;

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public List<Turn> getTurnList() {
        return turnList;
    }

    public void setTurnList(List<Turn> turnList) {
        this.turnList = turnList;
    }
}
