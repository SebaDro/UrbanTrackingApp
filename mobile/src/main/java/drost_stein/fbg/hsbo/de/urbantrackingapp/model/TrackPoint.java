package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import drost_stein.fbg.hsbo.de.urbantrackingapp.BR;

/**
 * Created by Sebastian Drost on 28.05.2016.
 */
public class TrackPoint extends BaseObservable{

    private double latitude;
    private double longitude;
    private long trackID;

    public TrackPoint(double latitude, double longitude, long trackID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.trackID = trackID;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        notifyPropertyChanged(BR.longitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        notifyPropertyChanged(BR.latitude);
    }

    public void setTrackID(long trackID) {
        this.trackID = trackID;
        notifyPropertyChanged(BR.trackID);
    }

    @Bindable
    public double getLongitude() {

        return longitude;
    }

    @Bindable
    public double getLatitude() {
        return latitude;
    }

    @Bindable
    public double getTrackID() {
        return trackID;
    }
}
