package uqac.dim.beepy_waiters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import uqac.dim.beepycommon.models.Restaurant;
import uqac.dim.beepycommon.models.Table;
import uqac.dim.beepycommon.utils.FirestoreKeys;

public class MainActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!checkRestaurantExtra()) return;

        restaurant = getIntent().getExtras().getParcelable(Restaurant.RESTAURANT_EXTRA);

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION)
                .orderBy("called", Query.Direction.DESCENDING)
                .orderBy("callTime", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    LinearLayout layout = findViewById(R.id.main_view);
                    layout.removeAllViews();

                    if(value != null && !value.isEmpty()) {
                        List<Table> tables = value.toObjects(Table.class);

                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        for (Table table : tables) {
                            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.table_element, null, false);

                            ((TextView) view.findViewById(R.id.table_name)).setText(table.getName());
                            view.findViewById(R.id.table_select).setOnClickListener(v -> onTableSelected(table));

                            view.setBackgroundColor(table.isCalled() ?
                                getResources().getColor(R.color.waiting_table_color, null) :
                                getResources().getColor(R.color.not_waiting_table_color, null));

                            ((ImageView)view.findViewById(R.id.table_state_icon)).setImageResource(table.isCalled() ?
                                R.mipmap.waiting_table_icon_foreground :
                                R.mipmap.not_waiting_table_icon_foreground);

                            layout.addView(view);
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

    private void onTableSelected(Table table) {
        Intent intent = new Intent(MainActivity.this, TableActivity.class);
        intent.putExtra(Restaurant.RESTAURANT_EXTRA, restaurant);
        intent.putExtra(Table.TABLE_EXTRA, table);
        startActivity(intent);
    }
}