package ynwa.guideme.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.MainActivity;
import ynwa.guideme.R;
import ynwa.guideme.admin.category.TravelInfo;
import ynwa.guideme.admin.company.Company;
import ynwa.guideme.admin.map.MapActivity;
import ynwa.guideme.admin.orders.OrdersActivity;
import ynwa.guideme.visitor.category.CategoryModel;

public class AdminHome extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private ImageView category_image;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri filepath_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.floating_home);
        fab.setOnClickListener(this);
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Category");
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TravelInfo(), getString(R.string.category));
        adapter.addFragment(new Company(), getString(R.string.company));
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0)
            fab.setImageResource(R.drawable.ic_add_category);
        else
            fab.setImageResource(R.drawable.ic_add_company);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {

        if (tabLayout.getSelectedTabPosition() == 0)
            add_category_to_server();
        else
            startActivity(new Intent(this, MapActivity.class));
    }

    private void add_category_to_server() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.add_category, null);
            final AlertDialog category_dialog = new AlertDialog.Builder(this).setView(view).show();

            final EditText cat_name = view.findViewById(R.id.category_name);

            category_image = view.findViewById(R.id.category_icon);
            category_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(AdminHome.this);
                }
            });

            Button add_category = view.findViewById(R.id.add_category);
            add_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(cat_name.getText().toString()) && category_image.getDrawable() != null) {
                        saveCategoryInformation(cat_name.getText().toString());
                        category_dialog.dismiss();
                    } else
                        FORMATTED_TOAST.error(getApplicationContext(), getString(R.string.provide_all_information));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filepath_uri = result.getUri();
                category_image.setImageURI(filepath_uri);
            } else
                FORMATTED_TOAST.warning(this, getString(R.string.canceled));
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.logout_before_exit))
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        logout();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void saveCategoryInformation(String categoryName) {

        final android.app.AlertDialog dialog = new SpotsDialog(this);
        dialog.setCancelable(false);
        dialog.show();

        CategoryModel categoryData = new CategoryModel(UUID.randomUUID().toString(), categoryName);
        databaseReference.push().setValue(categoryData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if (databaseReference.getKey() != null) {

                    StorageReference reference = storageReference.child("Images")
                            .child("category")
                            .child(databaseReference.getKey())
                            .child("profile.jpg");

                    reference.putFile(filepath_uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    dialog.dismiss();
                                    FORMATTED_TOAST.success(AdminHome.this, getString(R.string.category_added));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    dialog.dismiss();
                                    Toast.makeText(AdminHome.this, exception.getMessage(), Toast.LENGTH_LONG).show();

                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                break;
            case R.id.view_order:
                startActivity(new Intent(this, OrdersActivity.class));
                break;
            case R.id.add_new_admin:
                addNewUser();
                break;
        }
        return true;
    }

    void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(AdminHome.this, MainActivity.class));
            finish();
        }
    }

    @SuppressLint("InflateParams")
    private void addNewUser() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            View view = layoutInflater.inflate(R.layout.layout_add_new_user, null);

            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .show();

            final EditText username = view.findViewById(R.id.editText);
            final EditText password = view.findViewById(R.id.editText2);
            Button register = view.findViewById(R.id.button);
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(username.getText().toString()) && TextUtils.isEmpty(password.getText().toString()))
                        FORMATTED_TOAST.info(getApplicationContext(), getString(R.string.provide_all_information));
                    else if (!Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()).matches())
                        FORMATTED_TOAST.info(getApplicationContext(), getString(R.string.collect_email));
                    else if (password.length() < 6)
                        FORMATTED_TOAST.info(getApplicationContext(), getString(R.string.password_is_short));
                    else {

                        final android.app.AlertDialog dialog = new SpotsDialog(AdminHome.this);
                        dialog.setCancelable(false);
                        dialog.show();

                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.createUserWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        dialog.dismiss();
                                        alertDialog.dismiss();
                                        FORMATTED_TOAST.success(getApplicationContext(), getString(R.string.user_registered));
                                    }
                                });
                    }

                }
            });
        }
    }
}