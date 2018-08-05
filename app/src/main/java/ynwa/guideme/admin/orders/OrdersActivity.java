package ynwa.guideme.admin.orders;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<OrdersModel> list;
    private OrdersAdapter adapter;
    private DatabaseReference reference;
    private ProgressBar progress;
    private TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        list = new ArrayList<>();
        recyclerView = findViewById(R.id.recycleOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        progress = findViewById(R.id.progress_order);
        reference = FirebaseDatabase.getInstance().getReference();
        noData = findViewById(R.id.no_orders);
        fetchOrders();
    }

    private void fetchOrders() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progress.setVisibility(View.GONE);
                list.clear();
                for (DataSnapshot snapshot : dataSnapshot.child("Orders").getChildren()) {
                    OrdersModel ordersModel = snapshot.getValue(OrdersModel.class);
                    String company_name = dataSnapshot.child("Company").child(ordersModel.getCompany()).getValue(CompanyModel.class).getName();
                    ordersModel.setCompany(company_name);
                    ordersModel.setKey(snapshot.getKey());
                    list.add(ordersModel);
                }
                Collections.reverse(list);
                adapter = new OrdersAdapter(list, OrdersActivity.this);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                if (list.isEmpty())
                    noData.setVisibility(View.VISIBLE);
                else
                    noData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
                FORMATTED_TOAST.error(getApplicationContext(), databaseError.toString());
            }
        });
    }
}
