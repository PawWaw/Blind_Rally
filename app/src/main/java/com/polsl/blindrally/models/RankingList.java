package com.polsl.blindrally.models;

import java.util.List;

public class RankingList {

    private String trackName;
    private List<RankPosition> ranks;

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public List<RankPosition> getRanks() {
        return ranks;
    }

    public void setRanks(List<RankPosition> ranks) {
        this.ranks = ranks;
    }
}
