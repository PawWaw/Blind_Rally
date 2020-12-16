package com.polsl.blindrally;

import android.content.Context;

import com.polsl.blindrally.models.RankingList;
import com.polsl.blindrally.utils.FileUtils;

import java.util.List;

public class Ranking {

    public RankingList showRanking(Context ctx, String trackName) {
        FileUtils utils = new FileUtils(ctx);
        List<RankingList> rankingList = utils.readRanking();
        for (int i = 0; i < rankingList.size(); i++) {
            if (rankingList.get(i).getTrackName().equals(trackName)) {
                return rankingList.get(i);
            }
        }
        return null;
    }
}
