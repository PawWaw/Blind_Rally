package com.polsl.blindrally;

import android.content.Context;

import com.polsl.blindrally.models.RankPosition;
import com.polsl.blindrally.utils.FileUtils;

import java.util.List;

public class Ranking {

    public List<RankPosition> showRanking(Context ctx) {
        FileUtils utils = new FileUtils(ctx);
        return utils.readRanking();
    }
}
