package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;

import java.io.Serializable;


/**
 * It represents the user's position at a specific time and is part of a track.
 * Created by Sebastian Drost on 28.05.2016.
 */
public class TrackPoint extends BaseObservable implements Serializable {

    private long id;
    private long trackID;
    private String userID;
    private double latitude;
    private double longitude;
    private double altitude;
    private double bearing;
    private double speed;
    private float precision;
    private DateTime time;
    private String typeOfMovement;

    public TrackPoint(long id, long trackID,String userID, double lat, double lon, double alt, double bearing, double speed, float precision, DateTime time, String type) {
        this.id = id;
        this.trackID = trackID;
        this.userID=userID;
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.bearing = bearing;
        this.speed=speed;

        this.precision = precision;
        this.time=time;
        this.typeOfMovement = type;
    }



    @Bindable
    public long getId() {
        return id;
    }

    @Bindable
    public long getTrackID() {
        return trackID;
    }

    @Bindable
    public String getUserID() {
        return userID;
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
    public double getSpeed() {
        return speed;
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
