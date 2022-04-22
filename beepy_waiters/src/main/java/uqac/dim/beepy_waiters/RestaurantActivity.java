package uqac.dim.beepy_waiters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class RestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int ZOOM_DEFAULT = 15;

    private Restaurant selectedRestaurant;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private GoogleMap map;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        findViewById(R.id.restaurant_select).setOnClickListener(this::onSubmit);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.restaurant_map);
        if(mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        checkPermissionLocation();

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_style));
        map.setOnMapClickListener(latLng -> {
            selectedRestaurant = null;
            findViewById(R.id.restaurant_select).setVisibility(View.INVISIBLE);
        });

        map.setOnMarkerClickListener(marker -> {
            selectedRestaurant = (Restaurant) marker.getTag();
            findViewById(R.id.restaurant_select).setVisibility(View.VISIBLE);
            return false;
        });
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUi() {
        if(map == null) return;

        try{
            if(locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        }catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if(map == null) return;

        try{
            if(locationPermissionGranted) {
                Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if(lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()),
                                    ZOOM_DEFAULT
                            ));
                            fetchNearestRestaurants();
                        }
                    }
                });
            }
        }catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void checkPermissionLocation() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUi();
            getDeviceLocation();
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                updateLocationUi();
                getDeviceLocation();
            }
        }else super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void fetchNearestRestaurants() {
        GeoLocation location = new GeoLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(location, 1000);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds bound : bounds) {
            Query q = firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION)
                    .orderBy("geohash")
                    .startAt(bound.startHash)
                    .endAt(bound.endHash);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(t -> {
                    for(Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snapshot = task.getResult();
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Restaurant restaurant = document.toObject(Restaurant.class);

                            assert restaurant != null;
                            map.addMarker(new MarkerOptions()
                                    .title(restaurant.getName())
                                    .position(new LatLng(
                                            restaurant.getLatitude(),
                                            restaurant.getLongitude()
                                    ))
                            ).setTag(restaurant);
                        }
                    }
                });
    }

    private void onSubmit(View v) {
        Intent intent = new Intent(RestaurantActivity.this, TableActivity.class);
        intent.putExtra(Restaurant.RESTAURANT_EXTRA, selectedRestaurant);
        startActivity(intent);
    }
}