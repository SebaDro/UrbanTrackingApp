package drost_stein.fbg.hsbo.de.urbantrackingapp.featuresource;

import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.map.Feature;
import com.esri.core.table.TableException;

import java.util.ArrayList;
import java.util.List;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;

/**
 * handles connections to the trackpoint Feature-Service for receiving features or uploading tracks
 * Created by Seba on 11.06.2016.
 */
public class TrackPointSource {
    private GeodatabaseFeatureServiceTable featureServiceTable;
    private TrackPointAssembler assembler;

    public TrackPointSource(GeodatabaseFeatureServiceTable table){
        this.featureServiceTable=table;
        assembler=new TrackPointAssembler(table);
    }

    /**
     * adds a list of tracks to the feature service table
     * @param trackList list of tracks that has to be added to the table
     */
    public void addTrackListToTable(ArrayList<Track> trackList){
        for (Track track:trackList){
            addTrackToTable(track);
        }
    }

    /**
     * Gets a list of fetures for the track points of the given track and adds it to the feature service table
     * @param track track that has to be added to the table
     */
    public void addTrackToTable(Track track){
        List<Feature> trackPointFeatures= assembler.getFeaturesFromTrackPoints(track.getTrackPoints());
        try {
            featureServiceTable.addFeatures(trackPointFeatures);
        } catch (TableException e) {
            e.printStackTrace();
        }
    }


}
