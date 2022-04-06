package uqac.dim.beepy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class RestaurantActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        findViewById(R.id.restaurant_submit).setOnClickListener(this::onSubmit);
    }

    private void onSubmit(View v) {
        String name = ((TextView) findViewById(R.id.restaurant_input)).getText().toString();

        DocumentReference document = firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(name);
        document.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Restaurant restaurant;
                DocumentSnapshot snapshot = task.getResult();

                if (snapshot.exists()) {
                    restaurant = task.getResult().toObject(Restaurant.class);
                } else {
                    restaurant = new Restaurant();
                    restaurant.setId(name);
                    restaurant.setName(name);

                    document.set(restaurant);
                }

                Intent intent = new Intent(RestaurantActivity.this, TableActivity.class);
                intent.putExtra(Restaurant.RESTAURANT_EXTRA, restaurant);
                startActivity(intent);
                finish();
            }
        });
    }
}
