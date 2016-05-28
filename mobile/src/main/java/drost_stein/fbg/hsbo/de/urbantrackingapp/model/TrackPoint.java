package drost_stein.fbg.hsbo.de.urbantrackingapp.model;

/**
 * Created by Sebastian Drost on 28.05.2016.
 */
public class TrackPoint {

    private String latitude;
    private String longitude;
    private long trackID;

    public TrackPoint(String latitude, String longitude, long trackID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.trackID = trackID;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setTrackID(long trackID) {
        this.trackID = trackID;
    }

    public String getLongitude() {

        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public long getTrackID() {
        return trackID;
    }
}
