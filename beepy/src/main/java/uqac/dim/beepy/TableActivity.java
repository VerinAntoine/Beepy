package uqac.dim.beepy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import uqac.dim.beepy.model.Restaurant;
import uqac.dim.beepy.model.Table;
import uqac.dim.beepy.utils.FirestoreKeys;

public class TableActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        Bundle bundle = getIntent().getExtras();

        if(!checkRestaurantExtra()) return;

        restaurant = bundle.getParcelable(Restaurant.RESTAURANT_EXTRA);

        ((TextView) findViewById(R.id.table_restaurant)).setText(restaurant.getName());

        findViewById(R.id.table_submit).setOnClickListener(this::onSubmit);
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

    private void onSubmit(View v) {
        String name = ((TextView) findViewById(R.id.table_input)).getText().toString();

        DocumentReference document = firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION)
                .document(restaurant.getId()).collection(FirestoreKeys.TABLES_COLLECTION).document(name);
        document.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Table table;
                DocumentSnapshot snapshot = task.getResult();

                if(snapshot.exists()) {
                    table = snapshot.toObject(Table.class);
                }else{
                    table = new Table();
                    table.setId(name);
                    table.setName(name);

                    document.set(table);
                }

                Intent intent = new Intent(TableActivity.this, MainActivity.class);
                intent.putExtra(Restaurant.RESTAURANT_EXTRA, restaurant);
                intent.putExtra(Table.TABLE_EXTRA, table);
                startActivity(intent);
            }
        });
    }

}
