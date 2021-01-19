package com.polsl.blindrally.models;

public class RankPosition implements Comparable<RankPosition>{

    private String position;
    private String name;
    private Integer score;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }


    @Override
    public int compareTo(RankPosition o) {
        return this.getScore().compareTo(o.getScore());
    }
}
