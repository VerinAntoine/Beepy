package uqac.dim.beepy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.models.Table;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class MainActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private Restaurant restaurant;
    private Table table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();

        if(!checkRestaurantExtra()) return;
        if(!checkTableExtra()) return;

        restaurant = bundle.getParcelable(Restaurant.RESTAURANT_EXTRA);
        table = bundle.getParcelable(Table.TABLE_EXTRA);

        ((TextView) findViewById(R.id.main_restaurant)).setText(restaurant.getName());
        ((TextView) findViewById(R.id.main_table)).setText(table.getName());

        findViewById(R.id.main_button_waiter).setOnClickListener(this::onWaiterCall);

        getFirestoreDocument().addSnapshotListener((value, error) -> {
            if(value != null) {
                Table table = value.toObject(Table.class);
                assert table != null;
                findViewById(R.id.main_button_waiter).setEnabled(!table.isCalled());
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

    private boolean checkTableExtra() {
        Bundle bundle = getIntent().getExtras();
        if(!bundle.containsKey(Table.TABLE_EXTRA)) {
            Log.d("BeepyDebug", "Missing table extra");
            Intent intent = new Intent(MainActivity.this, TableActivity.class);
            intent.putExtra(Restaurant.RESTAURANT_EXTRA, (Restaurant) bundle.getParcelable(Restaurant.RESTAURANT_EXTRA));
            startActivity(intent);
            finish();
            return false;
        }

        return true;
    }

    private void onWaiterCall(View v) {
        table.call();

        getFirestoreDocument().set(table);
    }

    private DocumentReference getFirestoreDocument() {
        return firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId());
    }

}