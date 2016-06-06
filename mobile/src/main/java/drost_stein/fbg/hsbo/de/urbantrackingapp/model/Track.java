package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Represents a track a user has moved for a period of time and contains
 * several TrackPoints.
 * Created by Sebastian Drost on 04.06.2016.
 */
public class Track extends BaseObservable {
    private long id;
    private long userID;
    private DateTime startTime;
    private DateTime endTime;
    private List<TrackPoint> trackPoints;

    public Track(long id, long userID, DateTime startTime,DateTime endTime, List<TrackPoint> trackPoints) {
        this.id = id;
        this.userID = userID;
        this.startTime = startTime;
        this.endTime = endTime;
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

    @Bindable
    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

}
