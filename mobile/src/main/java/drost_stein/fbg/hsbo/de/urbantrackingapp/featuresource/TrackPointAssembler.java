package drost_stein.fbg.hsbo.de.urbantrackingapp.featuresource;

import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.symbol.Symbol;
import com.esri.core.table.TableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final String SPEED="speed";
    private final String PRECISION="coord_precision";
    private final String TRACKING_TIME="tracking_time";
    private final String TYPE_OF_MOVEMENT="type_of_movement";

    private GeodatabaseFeatureServiceTable featureServiceTable;

    public TrackPointAssembler(GeodatabaseFeatureServiceTable featureServiceTable){
        this.featureServiceTable=featureServiceTable;
    }

    /**
     * Creates a feature with the specified attributes from a TrackPoint
     * @param point TrackPoint
     * @return Feature that represents a TrackPoint
     */
    public Feature getFeatureFromTrackPoint(TrackPoint point){
        Map<String,Object> attributes=new HashMap<>();
        attributes.put(TRACKPOINT_ID,point.getId());
        attributes.put(TRACK_ID,point.getTrackID());
        attributes.put(USER_ID,point.getUserID());
        attributes.put(LATITUDE,point.getLatitude());
        attributes.put(LONGITUDE,point.getLongitude());
        attributes.put(ALTITUDE,point.getAltitude());
        attributes.put(BEARING,point.getBearing());
        attributes.put(SPEED,point.getSpeed());
        attributes.put(PRECISION,point.getPrecision());
        attributes.put(TRACKING_TIME,point.getTime());
        attributes.put(TYPE_OF_MOVEMENT,point.getTypeOfMovement());

        Point projectedPoint= GeometryEngine.project(point.getLongitude(),point.getLatitude(),SpatialReference.create(SpatialReference.WKID_WGS84_WEB_MERCATOR_AUXILIARY_SPHERE));
        Feature gdbFeature=null;
        try {
            gdbFeature=featureServiceTable.createNewFeature(attributes,projectedPoint);
        } catch (TableException e) {
            e.printStackTrace();
        }
        return gdbFeature;
    }

    /**
     * gets a list of features from a list of TrackPoints
     * @param trackPointList list of TrackPoints
     * @return list of features that represents the given TrackPoints
     */
    public List<Feature> getFeaturesFromTrackPoints(List <TrackPoint> trackPointList){
        ArrayList<Feature> featureList=new ArrayList<>();
        for (TrackPoint point:trackPointList){
            Feature feature=getFeatureFromTrackPoint(point);
            featureList.add(feature);
        }
        return featureList;
    }
}
