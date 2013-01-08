/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.qbait.iamcoming;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker.showInfoWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getSupportMenuInflater().inflate(R.menu.activity_map, menu);
        createSearchEditTextLogic(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void createSearchEditTextLogic(Menu menu) {
        final MenuItem searchMenuItem = menu.findItem(R.id.menu_map_search);
        final EditText searchEditText = (EditText) searchMenuItem.getActionView();

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchMenuItem.collapseActionView();
                    try {
                        searchLocation(searchEditText.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("TAG", "Search");
                    return true;
                }
                return false;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        searchEditText.requestFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
                Log.d("TAG", "expanded");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    }
                });
                Log.d("TAG", "collapsed");
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_map_current_location:
                showCurrenLocation();
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
}
