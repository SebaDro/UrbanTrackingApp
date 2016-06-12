package drost_stein.fbg.hsbo.de.urbantrackingapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

/**
 *Class for deserializing json objects to track objects
 * Created by Sebastian Drost on 12.06.2016.
 */
public class TrackDeserializer implements JsonDeserializer<Track> {
    @Override
    public Track deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonTrack=json.getAsJsonObject();
        Long id=jsonTrack.get("ID").getAsLong();
        Long userID=jsonTrack.get("USER_ID").getAsLong();
        DateTime startTime=new DateTime(jsonTrack.get("START_TIME").getAsJsonPrimitive().getAsString());
        DateTime endTime=new DateTime(jsonTrack.get("END_TIME").getAsJsonPrimitive().getAsString());
        JsonArray jsonTrackPoints=jsonTrack.get("TRACK_POINTS").getAsJsonArray();
        ArrayList trackPoints=new ArrayList< TrackPoint>();
        for (JsonElement p:jsonTrackPoints){
            JsonObject pointObject=p.getAsJsonObject();
            long pointID=pointObject.get("ID").getAsLong();
            long trackID=pointObject.get("TRACK_ID").getAsLong();
            double lat=pointObject.get("LAT").getAsDouble();
            double lon=pointObject.get("LON").getAsDouble();
            double alt=pointObject.get("ALT").getAsDouble();
            double bearing=pointObject.get("BEARING").getAsDouble();
            float precision=pointObject.get("PRECISION").getAsFloat();
            DateTime time=new DateTime(pointObject.get("TIME").getAsJsonPrimitive().getAsString());
            String type=pointObject.get("TYPE").getAsString();
            TrackPoint point=new TrackPoint(pointID,trackID,lat,lon,alt,bearing,precision,time,type);
            trackPoints.add(point);
        }
        Track track=new Track(id,userID,startTime,endTime,trackPoints);
        return track;
    }
}
