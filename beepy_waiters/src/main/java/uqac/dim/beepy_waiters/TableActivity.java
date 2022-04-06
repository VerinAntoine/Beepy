package uqac.dim.beepy_waiters;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.models.Table;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class TableActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private Restaurant restaurant;
    private Table table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        if(!checkRestaurantExtra()) return;
        if(!checkTableExtra()) return;

        restaurant = getIntent().getExtras().getParcelable(Restaurant.RESTAURANT_EXTRA);
        table = getIntent().getExtras().getParcelable(Table.TABLE_EXTRA);

        findViewById(R.id.table_answer).setOnClickListener(this::onAnswerClicked);

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                .addSnapshotListener((value, error) -> {
                    if(value != null)
                        findViewById(R.id.table_answer).setEnabled(value.toObject(Table.class).isCalled());
                });
    }

    private boolean checkRestaurantExtra() {
        if(!getIntent().getExtras().containsKey(Restaurant.RESTAURANT_EXTRA)) {
            Log.d("BeepyDebug", "Missing restaurant extra");
            Intent intent = new Intent(TableActivity.this, RestaurantActivity.class);
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
            Intent intent = new Intent(TableActivity.this, MainActivity.class);
            intent.putExtra(Restaurant.RESTAURANT_EXTRA, (Restaurant) bundle.getParcelable(Restaurant.RESTAURANT_EXTRA));
            startActivity(intent);
            finish();
            return false;
        }

        return true;
    }

    private void onAnswerClicked(View v) {
        table.uncall();

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId()).set(table);
    }
}