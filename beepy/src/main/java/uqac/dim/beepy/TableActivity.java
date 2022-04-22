package uqac.dim.beepy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.models.Table;
import uqac.dim.beepycommon.utils.FirestoreKeys;


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

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        LinearLayout layout = findViewById(R.id.table_list);
                        layout.removeAllViews();

                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            Table table = document.toObject(Table.class);
                            Log.d("AZEAZEAZE", table.getName());
                            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.table_element, null, false);

                            ((TextView) view.findViewById(R.id.table_name)).setText(table.getName());
                            view.findViewById(R.id.table_select).setOnClickListener(v -> onSubmit(table));

                            layout.addView(view);
                        }
                    }
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

    private void onSubmit(Table table) {
        Intent intent = new Intent(TableActivity.this, MainActivity.class);
        intent.putExtra(Restaurant.RESTAURANT_EXTRA, restaurant);
        intent.putExtra(Table.TABLE_EXTRA, table);
        startActivity(intent);
    }

}
