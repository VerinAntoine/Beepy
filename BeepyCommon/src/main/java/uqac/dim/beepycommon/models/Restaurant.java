package uqac.dim.beepycommon.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Restaurant implements Parcelable {

    public static final String RESTAURANT_EXTRA = "restaurant";

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String geohash;

    public Restaurant() { }

    public Restaurant(String id, String name, double latitude, double longitude, String geohash) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
    }

    protected Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        geohash = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(geohash);
    }
}
