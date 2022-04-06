package uqac.dim.beepy_waiters.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Restaurant implements Parcelable {

    public static final String RESTAURANT_EXTRA = "restaurant";

    private String id;
    private String name;

    public Restaurant() { }

    public Restaurant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
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
    }
}
