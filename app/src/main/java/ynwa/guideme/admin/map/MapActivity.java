package ynwa.guideme.admin.map;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import ynwa.guideme.Config.APP_CONFIG;
import ynwa.guideme.Config.FORMATTED_TOAST;
import ynwa.guideme.R;
import ynwa.guideme.admin.company.CompanyModel;
import ynwa.guideme.visitor.category.CategoryModel;

public class MapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    public MapView mapView;
    public GoogleMap google_map;

    private static final String TAG = "MapActivity___________";
    private int REQUEST_CHECK_SETTINGS = 0x1;

    /*
     * Search business on the Map
     * using autocompleteTextView
     */
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/";
    private Marker place;
    private GoogleApiClient googleApiClient;
    private DatabaseReference databaseReference, saveCompanyReference;
    private List<CategoryModel> spinner_data;
    private Button register;
    private Spinner spinner;
    private String category_db_key;
    private CompanyModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        displayLocationSettingsRequest(this);
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView = findViewById(R.id.MapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                google_map = googleMap;
                google_map.getUiSettings().setMapToolbarEnabled(false);

                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                google_map.setMyLocationEnabled(true);

                dataForMyLocation();

                CameraPosition position = new MapPreferences(MapActivity.this).getSavedCameraPosition();
                if (position != null)
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            }
        });

        AutoCompleteTextView autoComplete = findViewById(R.id.searchBusiness);
        autoComplete.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.autocomplete_search_result));
        autoComplete.setOnItemClickListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Category");
        saveCompanyReference = FirebaseDatabase.getInstance().getReference("Company");
        spinner_data = new ArrayList<>();
        register = findViewById(R.id.registerCompany);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_Dialog();
            }
        });

        getCategory();
    }

    public void dataForMyLocation() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            FORMATTED_TOAST.info(this, getString(R.string.failed_to_get_location));
        else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            google_map.animateCamera(position);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (google_map != null)
            new MapPreferences(this).saveMapState(google_map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {
                            status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FORMATTED_TOAST.hideKeyboard(this);
        String location = (String) parent.getItemAtPosition(position);
        findThisLocationOnMap(location);
    }

    private void findThisLocationOnMap(final String location) {
        String mLocation = location.replace(" ", "%20");
        String url = PLACES_API_BASE + "geocode/json?address=" + mLocation + "&key=" + APP_CONFIG.GOOGLE_API_KEY;
        Log.d(TAG, "findThisLocationOnMap: " + url);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            double latitude = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            double longitude = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                            String road = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");

                            model = new CompanyModel(latitude, longitude, road);
                            if (place != null)
                                place.remove();

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .snippet(location)
                                    .title(getString(R.string.location))
                                    .position(new LatLng(latitude, longitude));
                            place = google_map.addMarker(markerOptions);

                            LatLng ll = new LatLng(latitude, longitude);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, 15);
                            google_map.animateCamera(cameraUpdate);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FORMATTED_TOAST.error(MapActivity.this, getString(R.string.failed_to_get_location));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(MapActivity.this);
        requestQueue.add(stringRequest);
    }

    private class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        if (resultList != null) {
                            filterResults.count = resultList.size();
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }

    @Nullable
    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            String autocomplete_link = PLACES_API_BASE + "place/autocomplete" + "/json" + "?key=" + APP_CONFIG.GOOGLE_API_KEY +
                    "&components=country:rw" +
                    "&input=" + URLEncoder.encode(input, "utf8");

            URL url = new URL(autocomplete_link);

            Log.d(TAG, "autocomplete: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

    /*
     * section of registering business to the database,
     * we will first fetch categories so that the user can be easily register a business in
     * a certain category
     */

    private void getCategory() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String name = snapshot.getValue(CategoryModel.class).getName();
                    spinner_data.add(new CategoryModel(key, name));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FORMATTED_TOAST.error(MapActivity.this, getString(R.string.failed_to_get_data));
            }
        });
    }

    private void show_Dialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.register_company, null);

            ArrayAdapter<CategoryModel> adapter = new ArrayAdapter<>(MapActivity.this, R.layout.support_simple_spinner_dropdown_item, spinner_data);
            spinner = view.findViewById(R.id.spinnerCategory);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .show();

            TextView road = view.findViewById(R.id.road);
            TextView position = view.findViewById(R.id.coordinates);
            final EditText name = view.findViewById(R.id.name);
            final EditText contact = view.findViewById(R.id.contact);
            final EditText prices = view.findViewById(R.id.priceList);
            final EditText description = view.findViewById(R.id.description);

            road.setText(model.getRoad());
            position.setText(getString(R.string.lat_long, String.valueOf(model.getLatitude()),
                    String.valueOf(model.getLongitude())));

            Button confirm_registration = view.findViewById(R.id.add);
            confirm_registration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!name.getText().toString().isEmpty()
                            && category_db_key != null
                            && !contact.getText().toString().isEmpty()
                            && !prices.getText().toString().isEmpty()
                            && !description.getText().toString().isEmpty()) {

                        final android.app.AlertDialog dialog = new SpotsDialog(MapActivity.this);
                        dialog.setCancelable(false);
                        dialog.show();

                        CompanyModel data = new CompanyModel(
                                model.getLatitude(),
                                model.getLongitude(),
                                model.getRoad(),
                                name.getText().toString(),
                                contact.getText().toString(),
                                category_db_key,
                                prices.getText().toString(),
                                description.getText().toString());

                        saveCompanyReference.push().setValue(data, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                FORMATTED_TOAST.success(MapActivity.this, getString(R.string.company_registered));
                                dialog.dismiss();
                                alertDialog.dismiss();
                                finish();
                            }
                        });
                    } else
                        FORMATTED_TOAST.error(getApplicationContext(), getString(R.string.provide_all_information));
                }
            });
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CategoryModel model = (CategoryModel) spinner.getSelectedItem();
        category_db_key = model.getId();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
