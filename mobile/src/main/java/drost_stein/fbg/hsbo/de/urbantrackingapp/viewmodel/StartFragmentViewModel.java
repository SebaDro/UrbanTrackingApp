package drost_stein.fbg.hsbo.de.urbantrackingapp.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import drost_stein.fbg.hsbo.de.urbantrackingapp.BR;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 * Created by Seba on 04.06.2016.
 */
public class StartFragmentViewModel extends BaseObservable {
    private TrackPoint trackPoint;

    @Bindable
    public TrackPoint getTrackPoint() {
        return trackPoint;
    }

    public void setTrackPoint(TrackPoint trackPoint) {
        this.trackPoint = trackPoint;
        notifyPropertyChanged(BR.trackPoint);
    }
}
