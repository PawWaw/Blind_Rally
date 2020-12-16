package com.polsl.blindrally.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.polsl.blindrally.models.RankPosition;
import com.polsl.blindrally.models.Turn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private final Context mContext;
    private InputStream is;
    private final AssetManager am;

    public FileUtils(Context ctx) {
        this.mContext = ctx;
        am = mContext.getAssets();
    }

    public List<List<Turn>> readTracks() {
        List<List<Turn>> list = new ArrayList<>();

        try {
            String trackDir = "tracks";
            String[] locales = am.list(trackDir);
            for (String locale : locales) {
                is = am.open(trackDir + "/" + locale);
                List<Turn> turns = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = reader.readLine()) != null) {
                    Turn turnTemp = new Turn();
                    String[] values = line.split(",");
                    turnTemp.setDistance(Integer.parseInt(values[0]));
                    turnTemp.setAngle(Integer.parseInt(values[1]));
                    turnTemp.setMessage(values[2]);
                    turns.add(turnTemp);
                }
                list.add(turns);
                am.close();
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<RankPosition> readRanking() {
        List<RankPosition> rankingList = new ArrayList<>();

        AssetManager am = mContext.getAssets();

        try {
            String rankDir = "ranks";
            String[] locales = am.list(rankDir);
            for (String locale : locales) {
                is = am.open(rankDir + "/" + locale);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                int i = 1;
                while ((line = reader.readLine()) != null) {
                    RankPosition rankTemp = new RankPosition();
                    String[] values = line.split(",");
                    rankTemp.setName(values[0]);
                    rankTemp.setTime(values[1]);
                    rankTemp.setPosition(i);
                    rankingList.add(rankTemp);
                    i++;
                }
                am.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rankingList;
    }
}
