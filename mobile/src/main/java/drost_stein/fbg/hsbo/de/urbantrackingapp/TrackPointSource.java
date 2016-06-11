package drost_stein.fbg.hsbo.de.urbantrackingapp;

import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;

/**
 * handles connections to the trackpoint Feature-Service for receiving features or uploading tracks
 * Created by Seba on 11.06.2016.
 */
public class TrackPointSource {
    private GeodatabaseFeatureServiceTable featureServiceTable;

    public TrackPointSource(GeodatabaseFeatureServiceTable table){
        this.featureServiceTable=featureServiceTable;
    }

    public void uploadTrackPoints(){
        //TODO implement uploading track points
        ;
    }
}
