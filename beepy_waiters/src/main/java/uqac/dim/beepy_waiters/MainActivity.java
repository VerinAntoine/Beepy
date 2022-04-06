package uqac.dim.beepy_waiters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.models.Table;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class MainActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!checkRestaurantExtra()) return;

        Restaurant restaurant = getIntent().getExtras().getParcelable(Restaurant.RESTAURANT_EXTRA);

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION)
                .orderBy("called", Query.Direction.DESCENDING)
                .orderBy("callTime", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    LinearLayout layout = findViewById(R.id.main_view);
                    layout.removeAllViews();

                    if(value != null && !value.isEmpty()) {
                        List<Table> tables = value.toObjects(Table.class);
                        Log.d("BeepyD", String.valueOf(tables.size()));

                        for (Table table : tables) {
                            Button button = new Button(this);
                            button.setText(table.getName() + " : " + table.isCalled());
                            layout.addView(button);
                            Log.i("Beepy", String.valueOf(table));
                        }
                    }

                });
    }

    private boolean checkRestaurantExtra() {
        if(!getIntent().getExtras().containsKey(Restaurant.RESTAURANT_EXTRA)) {
            Log.d("BeepyDebug", "Missing restaurant extra");
            Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
            startActivity(intent);
            finish();

            return false;
        }

        return true;
    }
}