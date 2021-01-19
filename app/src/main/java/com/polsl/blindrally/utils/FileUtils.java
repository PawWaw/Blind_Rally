package com.polsl.blindrally.utils;

import android.content.Context;
import android.util.Log;

import com.polsl.blindrally.models.RankPosition;
import com.polsl.blindrally.models.RankingList;
import com.polsl.blindrally.models.Track;
import com.polsl.blindrally.models.Turn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private final Context mContext;

    public FileUtils(Context ctx) {
        this.mContext = ctx;
    }

    public List<Track> readTracks() {
        List<Track> list = new ArrayList<>();

        try {
            String trackDir = "tracks";
            String[] locales = mContext.getAssets().list(trackDir);
            for (String locale : locales) {
                InputStream is = mContext.getAssets().open(trackDir + "/" + locale);
                Track track = new Track();
                List<Turn> turns = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                track.setTrackName(reader.readLine());

                while ((line = reader.readLine()) != null) {
                    Turn turnTemp = new Turn();
                    String[] values = line.split(",");
                    turnTemp.setDistance(Integer.parseInt(values[0]));
                    turnTemp.setAngle(Integer.parseInt(values[1]));
                    turnTemp.setMessage(values[2]);
                    turns.add(turnTemp);
                }
                track.setTurnList(turns);
                list.add(track);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<RankingList> readRanking() {
        List<RankingList> rankingList = new ArrayList<>();

        try {
            String rankDir = "ranks";
            String[] locales = mContext.getAssets().list(rankDir);
            for (String locale : locales) {
                InputStream is = mContext.getAssets().open(rankDir + "/" + locale);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                RankingList ranking = new RankingList();
                List<RankPosition> tempList = new ArrayList<>();
                String line;
                ranking.setTrackName(reader.readLine());

                int i = 1;
                while ((line = reader.readLine()) != null) {
                    RankPosition rankTemp = new RankPosition();
                    String[] values = line.split(",");
                    rankTemp.setName(values[0]);
                    String temp = values[1];
                    rankTemp.setScore(Integer.parseInt(temp.trim()));
                    tempList.add(rankTemp);
                    i++;
                }
                ranking.setRanks(tempList);
                rankingList.add(ranking);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rankingList;
    }
}
