package ynwa.guideme.admin.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import ynwa.guideme.visitor.category.CategoryModel;

public class TravelInfo extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<CategoryModel> data;
    private ImageView noData;
    private DatabaseReference databaseReference;
    public FirebaseStorage storage;
    private ProgressBar progress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel_info, container, false);
        init(view);
        fetchCategory();
        return view;
    }

    void init(View view) {
        data = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycle_admin_category);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        noData = view.findViewById(R.id.no_category);
        progress = view.findViewById(R.id.progress_admin);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
    }

    void fetchCategory() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data.clear();
                if (progress.getVisibility() == View.VISIBLE)
                    progress.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.child("Category").getChildren()) {
                    String key = snapshot.getKey();
                    String name = snapshot.getValue(CategoryModel.class).getName();
                    StorageReference reference = FirebaseStorage.getInstance().getReference("Images").child("category").child(key).child("profile.jpg");
                    data.add(new CategoryModel(key, name, reference));
                }
                adapter = new CategoryAdapter(getContext(), data, databaseReference);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                if (data.isEmpty())
                    noData.setVisibility(View.VISIBLE);
                else
                    noData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }
}
