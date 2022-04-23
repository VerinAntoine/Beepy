package uqac.dim.beepycommon.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FoodOrder implements Parcelable
{

    private String name;
    private float price;
    private Timestamp ordered_at;

    public FoodOrder()
    {}

    public FoodOrder(String name, float price, Timestamp ordered_at) //Timestamp date
    {
        this.name = name;
        this.price = price;
        this.ordered_at = ordered_at;
    }

    protected FoodOrder(Parcel in)
    {
        name = in.readString();
        price = in.readFloat();
        ordered_at = in.readParcelable(ClassLoader.getSystemClassLoader());
    }

    public static final Creator<FoodOrder> CREATOR = new Creator<FoodOrder>() {
        @Override
        public FoodOrder createFromParcel(Parcel in) {
            return new FoodOrder(in);
        }

        @Override
        public FoodOrder[] newArray(int size) {
            return new FoodOrder[size];
        }
    };

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public Timestamp getOrdered_at() {
        return ordered_at;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeFloat(price);
        ordered_at.writeToParcel(parcel, i);
    }

    @NonNull
    @Override
    public String toString()
    {
        return this.name + " | " + this.price + "$ | ";
    }
}
