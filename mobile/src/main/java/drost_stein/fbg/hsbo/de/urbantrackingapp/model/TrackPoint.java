package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;


/**
 * It represents the user's position at a specific time and is part of a track.
 * Created by Sebastian Drost on 28.05.2016.
 */
public class TrackPoint extends BaseObservable {

    private long id;
    private long trackID;
    private double latitude;
    private double longitude;
    private double altitude;
    private double bearing;
    private float precision;
    private DateTime time;
    private String typeOfMovement;

    public TrackPoint(long id, long trackID, double lat, double lon, double alt, double bearing, float precision, DateTime time,  String type) {
        this.id = id;
        this.trackID = trackID;
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.bearing = bearing;
        this.precision = precision;
        this.time=time;
        this.typeOfMovement = type;
    }

    @Bindable
    public long getID() {
        return id;
    }

    @Bindable
    public long getTrackID() {
        return trackID;
    }

    @Bindable
    public double getLatitude() {
        return latitude;
    }

    @Bindable
    public double getLongitude() {
        return longitude;
    }

    @Bindable
    public double getAltitude() {
        return altitude;
    }

    @Bindable
    public double getBearing() {
        return bearing;
    }

    @Bindable
    public float getPrecision() {
        return precision;
    }


    @Bindable
    public DateTime getTime() {
        return time;
    }

    @Bindable
    public String getTypeOfMovement() {
        return typeOfMovement;
    }
}
