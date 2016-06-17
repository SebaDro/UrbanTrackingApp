package drost_stein.fbg.hsbo.de.urbantrackingapp.featuresource;

import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.map.Feature;

import java.util.ArrayList;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * handles connections to the trackpoint Feature-Service for receiving features or uploading tracks
 * Created by Seba on 11.06.2016.
 */
public class TrackPointSource {
    private GeodatabaseFeatureServiceTable featureServiceTable;

    public TrackPointSource(GeodatabaseFeatureServiceTable table){
        this.featureServiceTable=table;
    }

    public void uploadTrackList(ArrayList<Track> trackList){
        for (Track track:trackList){
            uploadTrack(track);
        }
    }

    public void uploadTrack(Track track){
        ArrayList<Feature> trackPointFeatures=getTrackPointFeaturesFromTrack(track);
        for (Feature feature:trackPointFeatures){
            //featureServiceTable.addFeature(feature);
        }

    }

    private ArrayList<Feature> getTrackPointFeaturesFromTrack(Track track) {
        ArrayList<Feature> trackPointFeatures=new ArrayList<Feature>();
        for (TrackPoint point:track.getTrackPoints()){
            Feature trackPointFeature=getFeatureFromTrackPoint(point);
        }
        return trackPointFeatures;
    }

    private Feature getFeatureFromTrackPoint(TrackPoint point) {
        //TODO implement converting trackpoint to feature
        return null;
    }


}
