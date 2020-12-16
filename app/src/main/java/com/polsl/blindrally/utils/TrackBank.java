package com.polsl.blindrally.utils;

import android.content.Context;

import com.polsl.blindrally.models.Track;
import com.polsl.blindrally.models.Turn;

import java.util.List;

public class TrackBank {

    public List<Track> getTracks(Context ctx) {
        FileUtils utils = new FileUtils(ctx);
        return utils.readTracks();
    }
}