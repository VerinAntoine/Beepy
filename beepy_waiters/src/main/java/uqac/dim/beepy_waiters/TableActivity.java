package uqac.dim.beepy_waiters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.icu.util.Currency;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;

import java.util.List;
import java.util.Locale;

import uqac.dim.beepycommon.models.FoodOrder;
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

        String priceHint = "Price in ";
        priceHint += Currency.getInstance(Locale.getDefault()).getSymbol();
        ((EditText)findViewById(R.id.food_order_create_price)).setHint(priceHint);

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                .addSnapshotListener((value, error) -> {
                    if(value != null)
                        findViewById(R.id.table_answer).setEnabled(value.toObject(Table.class).isCalled());
                });

        findViewById(R.id.food_order_create_button).setOnClickListener(view -> {
            EditText nameEdit = findViewById(R.id.food_order_create_name);
            EditText priceEdit = findViewById(R.id.food_order_create_price);
            String name = nameEdit.getText().toString();
            String priceString = priceEdit.getText().toString();

            if (name.length() > 0 && priceString.length() > 0)
            {
                float price = Float.parseFloat(priceString);

                FoodOrder order = new FoodOrder(name, price, Timestamp.now());

                firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                        .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                        .collection(FirestoreKeys.FOOD_ORDERS_COLLECTION).add(order);

                nameEdit.setText("");
                priceEdit.setText("");
            }
        });

        findViewById(R.id.food_order_delete_all_button).setOnClickListener(view -> {
            firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                    .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                    .collection(FirestoreKeys.FOOD_ORDERS_COLLECTION)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                document.getReference().delete();
                                Log.d("Beepy", "Deleted document with ID " + document.getId());
                            }
                        else
                            Log.d("Beepy", "Error getting document: ", task.getException());
                    });
            Log.i("Beepy", "Deleted all ordered items for table " + table.getName());
        });

        firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                .collection(FirestoreKeys.FOOD_ORDERS_COLLECTION)
                .orderBy("ordered_at", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    LinearLayout layout  = findViewById(R.id.food_order_layout);
                    layout.removeAllViews();

                    float sum = 0;

                    if (value != null && !value.isEmpty())
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

                            view.findViewById(R.id.food_order_delete_button).setOnClickListener(buttonView -> {
                                firestore.collection(FirestoreKeys.RESTAURANTS_COLLECTION).document(restaurant.getId())
                                        .collection(FirestoreKeys.TABLES_COLLECTION).document(table.getId())
                                        .collection(FirestoreKeys.FOOD_ORDERS_COLLECTION)
                                        .whereEqualTo("ordered_at", order.getOrdered_at())
                                        .get().addOnCompleteListener(task -> {
                                            if (task.isSuccessful())
                                                for (QueryDocumentSnapshot document : task.getResult())
                                                {
                                                    document.getReference().delete();
                                                    Log.d("Beepy", "Deleted document with ID " + document.getId());
                                                }
                                            else
                                                Log.d("Beepy", "Error getting document: ", task.getException());
                                        });
                            });

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