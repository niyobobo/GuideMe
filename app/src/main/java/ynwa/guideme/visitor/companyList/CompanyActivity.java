package ynwa.guideme.visitor.companyList;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;
import ynwa.guideme.visitor.category.CategoryModel;

public class CompanyActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private CategoryModel model;
    private DatabaseReference reference;
    public StorageReference storageReference;
    private List<CompanyModel> data;

    private RecyclerView recyclerView;
    private AdapterCompany adapter;
    private ProgressBar progressBar;
    private TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);
        init();
        setTitle(model.getName());
        getCompany();
    }

    void init() {
        model = getIntent().getParcelableExtra("info");
        recyclerView = findViewById(R.id.searchRecycle);
        data = new ArrayList<>();
        progressBar = findViewById(R.id.progress_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reference = FirebaseDatabase.getInstance().getReference("Company");
        storageReference = FirebaseStorage.getInstance().getReference();
        noData = findViewById(R.id.no_content);
    }

    private void getCompany() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                data.clear();
                progressBar.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot.getValue(CompanyModel.class).getCategory().equals(model.getId())) {

                        CompanyModel companyModel = snapshot.getValue(CompanyModel.class);
                        assert companyModel != null;
                        companyModel.setKey(snapshot.getKey());
                        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Images").child("company").child(snapshot.getKey()).child("picture.jpg");
                        companyModel.setStorageReference(reference);
                        data.add(companyModel);
                    }
                }
                adapter = new AdapterCompany(CompanyActivity.this, data, getSupportFragmentManager());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if (data.size() == 0)
                    noData.setVisibility(View.VISIBLE);
                else
                    noData.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                FORMATTED_TOAST.error(CompanyActivity.this, "Failed to get data");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return true;
    }
}
