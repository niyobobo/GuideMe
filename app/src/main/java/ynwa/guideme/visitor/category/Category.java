package ynwa.guideme.visitor.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import ynwa.guideme.R;

public class Category extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private Button btn_continue;
    private List<CategoryModel> data;
    public FirebaseStorage storageReference;
    private DatabaseReference databaseReference;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        init();
        fetchData();
    }

    private void init() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Category");
        storageReference = FirebaseStorage.getInstance();
        data = new ArrayList<>();
        btn_continue = findViewById(R.id.btn_continue);
        recyclerView = findViewById(R.id.recycle_category);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        progress = findViewById(R.id.visitor_progress);
    }

    void fetchData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data.clear();
                if (progress.getVisibility() == View.VISIBLE)
                    progress.setVisibility(View.GONE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String name = snapshot.getValue(CategoryModel.class).getName();
                    StorageReference reference = FirebaseStorage.getInstance().getReference("Images").child("category").child(key).child("profile.jpg");
                    data.add(new CategoryModel(key, name, reference));
                }
                adapter = new CategoryAdapter(Category.this, data, btn_continue);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
            }
        });
    }
}
