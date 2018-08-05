package ynwa.guideme.admin.company;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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

public class Company extends Fragment {

    private ImageView noData;
    private RecyclerView recycle;
    private CompanyAdapter companyAdapter;
    private List<CompanyModel> data;
    private ProgressBar progressBar;
    private DatabaseReference reference;
    private StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company, container, false);
        init(view);
        fetchCompany();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCompany();
    }

    private void init(@NonNull View view) {
        noData = view.findViewById(R.id.no_content);
        recycle = view.findViewById(R.id.recycle_company);
        recycle.setHasFixedSize(true);
        recycle.setLayoutManager(new LinearLayoutManager(getContext()));
        data = new ArrayList<>();
        progressBar = view.findViewById(R.id.progressCompany);
        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void fetchCompany() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                data.clear();

                for (DataSnapshot snapshot : dataSnapshot.child("Company").getChildren()) {

                    CompanyModel model = snapshot.getValue(CompanyModel.class);
                    String category_name = dataSnapshot.child("Category").child(model.getCategory()).getValue(CategoryModel.class).getName();
                    model.setCategory(category_name);
                    model.setKey(snapshot.getKey());
                    StorageReference reference = FirebaseStorage.getInstance().getReference().child("Images").child("company").child(snapshot.getKey()).child("picture.jpg");
                    model.setStorageReference(reference);
                    data.add(model);

                }
                companyAdapter = new CompanyAdapter(getContext(), data, storageReference, Company.this, reference);
                companyAdapter.notifyDataSetChanged();
                recycle.setAdapter(companyAdapter);

                if (data.isEmpty())
                    noData.setVisibility(View.VISIBLE);
                else
                    noData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                progressBar.setVisibility(View.GONE);
                if (data.isEmpty())
                    noData.setVisibility(View.VISIBLE);
                else
                    noData.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        companyAdapter.onActivityResult(requestCode, resultCode, data);
    }
}
