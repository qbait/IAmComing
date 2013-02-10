package pl.qbait.iamcoming;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapActivity extends SherlockFragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private Marker marker;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpMapIfNeeded();

        if (savedInstanceState != null && savedInstanceState.containsKey("latitude")) {
            double latitude = savedInstanceState.getDouble("latitude");
            double longitude = savedInstanceState.getDouble("longitude");
            LatLng location = new LatLng(latitude, longitude);
            setMarker(location);
        } else if (preferences.isLocationSaved()) {
            setMarkerInCenter(preferences.getLocation());
        } else {
            showCurrenLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (marker != null) {
            outState.putDouble("latitude", marker.getPosition().latitude);
            outState.putDouble("longitude", marker.getPosition().longitude);
        }
        super.onSaveInstanceState(outState);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setMarker(latLng);
    }

    private void setMarkerInCenter(LatLng latLng) {
        setMarker(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void setMarker(LatLng latLng) {
        mMap.clear();
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("save")
                .icon(BitmapDescriptorFactory.defaultMarker(160.0f)));
        marker.showInfoWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        styleSearchView(searchView);

        searchView.setQueryHint("Search…");

        MenuItem.OnMenuItemClickListener currentLocationListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showCurrenLocation();
                return true;
            }
        };

        menu.add("Search")
                .setIcon(R.drawable.ic_menu_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        menu.add("Current location").setOnMenuItemClickListener(currentLocationListener)
                .setIcon(R.drawable.ic_menu_mylocation)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    searchLocation(query);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void showCurrenLocation() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                Log.d("TAG", "got location: " + location);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                setMarkerInCenter(latLng);
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

    private void saveLocation(Marker marker) {
        LatLng markerLocation = marker.getPosition();
        preferences.setLocation(markerLocation);
    }

    void searchLocation(String locationName) throws IOException {
        Geocoder myGeocoder = new Geocoder(this);
        List<Address> addresses;
        addresses = myGeocoder.getFromLocationName(locationName, 5);

        if (addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            setMarkerInCenter(latLng);
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Google Map");
            adb.setMessage("Please Provide the Proper Place");
            adb.setPositiveButton("Close", null);
            adb.show();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        saveLocation(marker);
        finish();
    }

    private static void styleSearchView(SearchView searchView) {
        View searchPlate = searchView.findViewById(R.id.abs__search_plate);
        searchPlate.setBackgroundResource(R.drawable.textfield_searchview_holo_dark);
    }
}
