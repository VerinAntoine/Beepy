package uqac.dim.beepy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Currency;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Locale;

import uqac.dim.beepycommon.models.FoodOrder;
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

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                .collection(FirestoreKeys.FOOD_ORDERS_COLLECTION)
                .orderBy("ordered_at", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    LinearLayout layout  = findViewById(R.id.food_order_layout);
                    layout.removeAllViews();

                    float sum = 0;

                    if(value != null && !value.isEmpty())
                    {
                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());



                        List<FoodOrder> orders = value.toObjects(FoodOrder.class);
                        for (FoodOrder order : orders)
                        {
                            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.food_order_element, null, false);
                            ((TextView)view.findViewById(R.id.food_order_name)).setText(order.getName());

                            String s = String.valueOf(order.getPrice());
                            s += Currency.getInstance(Locale.getDefault()).getSymbol();
                            ((TextView)view.findViewById(R.id.food_order_price)).setText(s);

                            sum += order.getPrice();
                            layout.addView(view);
                        }

                    }

                    String s = "Total: ";
                    s += sum;
                    s += Currency.getInstance(Locale.getDefault()).getSymbol();

                    ((TextView)findViewById(R.id.table_bill_total)).setText(s);
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