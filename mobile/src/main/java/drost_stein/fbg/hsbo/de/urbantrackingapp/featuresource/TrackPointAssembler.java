package drost_stein.fbg.hsbo.de.urbantrackingapp.featuresource;

import com.esri.core.map.Feature;

import java.util.HashMap;
import java.util.Map;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * Created by Seba on 16.06.2016.
 */
public class TrackPointAssembler {
    private final String TRACKPOINT_ID="trackpoint_id";
    private final String TRACK_ID="track_id";
    private final String USER_ID="user_id";
    private final String LATITUDE="latitude";
    private final String LONGITUDE="longitude";
    private final String ALTITUDE="altitude";
    private final String BEARING="bearing";
    private final String PRECISION="precision";
    private final String TRACKING_TIME="tracking_time";
    private final String TYPE_OF_MOVEMENT="type_of_movement";
    public TrackPointAssembler(){

    }

    public Feature getFeatureFromTrackPoint(TrackPoint point){
        Map<String,Object> attributes=new HashMap<>();

        return null;
    }
}
