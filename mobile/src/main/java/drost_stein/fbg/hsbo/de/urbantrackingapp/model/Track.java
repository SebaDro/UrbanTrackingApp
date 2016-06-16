package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a track a user has moved for a period of time and contains
 * several TrackPoints.
 * Created by Sebastian Drost on 04.06.2016.
 */
public class Track extends BaseObservable implements Serializable{
    private long id;
    private long userID;
    private DateTime startTime;
    private DateTime endTime;
    private List<TrackPoint> trackPoints;

    /**
     * creates a track in case of starting tracking
     * @param id id of the track
     * @param userID id fo the user who is tracking
     * @param startTime start time of the tracking
     */
    public Track(long id, long userID, DateTime startTime) {
        this.id = id;
        this.userID = userID;
        this.startTime = startTime;
        this.trackPoints = new ArrayList<TrackPoint>();
    }

    /**
     * creates a track that is already completed
     * @param id id of the track
     * @param userID id fo the user who is tracking
     * @param startTime start time of the tracking
     * @param endTime end time of the tracking
     * @param trackPoints points of the track
     */
    public Track(long id, long userID, DateTime startTime,DateTime endTime, List<TrackPoint> trackPoints) {
        this.id = id;
        this.userID = userID;
        this.startTime = startTime;
        this.endTime=endTime;
        this.trackPoints = trackPoints;
    }

    @Bindable
    public long getId() {
        return id;
    }

    @Bindable
    public long getUserID() {
        return userID;
    }


    @Bindable
    public DateTime getStartTime() {
        return startTime;
    }

    @Bindable
    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Bindable
    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    /**
     * adds a track point to the track
     * @param point
     */
    public void addTrackPoint(TrackPoint point){
        this.trackPoints.add(point);
    }

}
