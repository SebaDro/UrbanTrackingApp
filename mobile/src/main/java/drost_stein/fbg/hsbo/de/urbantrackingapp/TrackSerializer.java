package drost_stein.fbg.hsbo.de.urbantrackingapp;

import drost_stein.fbg.hsbo.de.urbantrackingapp.model.Track;
import drost_stein.fbg.hsbo.de.urbantrackingapp.model.TrackPoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Class for serializing track objects to json
 * Created by Sebastian Drost on 12.06.2016.
 */
public class TrackSerializer implements JsonSerializer<Track>{


    @Override
    public JsonElement serialize(Track src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonTrack=new JsonObject();
        jsonTrack.addProperty("ID",src.getId());
        jsonTrack.addProperty("USER_ID",src.getUserID());
        jsonTrack.add("START_TIME",new JsonPrimitive(src.getStartTime().toString()));
        jsonTrack.add("END_TIME",new JsonPrimitive(src.getEndTime().toString()));
        JsonArray trackPointArray=new JsonArray();
        for (TrackPoint p:src.getTrackPoints()){
            JsonObject jsonPoint=new JsonObject();
            jsonPoint.addProperty("ID",p.getId());
            jsonPoint.addProperty("TRACK_ID",p.getTrackID());
            jsonPoint.addProperty("LAT",p.getLatitude());
            jsonPoint.addProperty("LON",p.getLongitude());
            jsonPoint.addProperty("ALT",p.getAltitude());
            jsonPoint.addProperty("BEARING",p.getBearing());
            jsonPoint.addProperty("SPEED",p.getSpeed());
            jsonPoint.addProperty("PRECISION",p.getPrecision());
            jsonPoint.add("TIME",new JsonPrimitive(p.getTime().toString()));
            jsonPoint.addProperty("TYPE",p.getTypeOfMovement());
            trackPointArray.add(jsonPoint);
        }
        jsonTrack.add("TRACK_POINTS",trackPointArray);

        return jsonTrack;
    }
}
