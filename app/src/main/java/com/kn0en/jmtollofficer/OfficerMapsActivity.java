package com.kn0en.jmtollofficer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class OfficerMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, NavigationView.OnNavigationItemSelectedListener {

    final int LOCATION_REQUEST_CODE = 1;

    Location mLastLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private DatabaseReference officerUserHeaderRef;
    private LatLng myLatLng;

    Boolean isLoggingOut = false;

    private LinearLayout mRiderInfo, mOfficerField;

    private CircleImageView mImageHeader;
    private TextView mNameHeader, mPhoneHeader;
    MaterialButton mRideStatus;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mWorkingSwitch;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    Toolbar toolbar;

    FirebaseAuth mAuth;
    String officerId;

    private RadioGroup mRuasGroup;
    RadioButton ruasButton;
    private TextInputEditText mCarNumberOfficer;
    private String mRuas,mCarNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            navigationView.setCheckedItem(R.id.nav_maps);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(OfficerMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
                mapFragment.getMapAsync(this);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mRiderInfo = (LinearLayout) findViewById(R.id.riderInfo);
        mOfficerField = (LinearLayout) findViewById(R.id.officerField);

        mRideStatus = (MaterialButton) findViewById(R.id.btnRideStatus);

        //working switch
        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);

        //Navigation header item
        View itemView = navigationView.getHeaderView(0);
        mImageHeader = (CircleImageView) itemView.findViewById(R.id.image_header);
        mNameHeader = (TextView) itemView.findViewById(R.id.name_header);
        mPhoneHeader = (TextView) itemView.findViewById(R.id.phone_header);

        //officer auth
        mAuth = FirebaseAuth.getInstance();
        officerId = mAuth.getCurrentUser().getUid();
        officerUserHeaderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Officers").child(officerId);

        getOfficerInfoHeader();
        getRuasInfo();

        mWorkingSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                connectOfficer();
                mOfficerField.setVisibility(View.GONE);
                mRiderInfo.setVisibility(View.VISIBLE);
            } else {
                disconnectOfficer();
                mOfficerField.setVisibility(View.VISIBLE);
                mRiderInfo.setVisibility(View.GONE);
            }
        });

        mRideStatus.setOnClickListener(view -> {
            disconnectOfficer();
            mOfficerField.setVisibility(View.VISIBLE);
            mRiderInfo.setVisibility(View.GONE);
            mWorkingSwitch.setChecked(false);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                Intent intentProfile = new Intent(OfficerMapsActivity.this, OfficerSettingActivity.class);
                startActivity(intentProfile);
                navigationView.setCheckedItem(R.id.nav_profile);
                break;
            case R.id.nav_password:
                Intent intentPassword = new Intent(OfficerMapsActivity.this, UpdatePasswordActivity.class);
                startActivity(intentPassword);
                navigationView.setCheckedItem(R.id.nav_password);
                break;
            case R.id.nav_maps:
                mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                navigationView.setCheckedItem(R.id.nav_maps);
                break;
            case R.id.nav_history:
                Intent intentHistory = new Intent(OfficerMapsActivity.this, HistoryActivity.class);
                intentHistory.putExtra("riderOrOfficerOrOperator", "Officers");
                startActivity(intentHistory);
                navigationView.setCheckedItem(R.id.nav_history);
                break;
            case R.id.nav_tutorial:
                Intent intentTutorial = new Intent(OfficerMapsActivity.this, TutorialSlidePagerActivity.class);
                intentTutorial.putExtra("riderOrOfficer", "Officers");
                startActivity(intentTutorial);
                navigationView.setCheckedItem(R.id.nav_tutorial);
                break;
            case R.id.nav_signOut:
                isLoggingOut = true;

                disconnectOfficer();

                FirebaseAuth.getInstance().signOut();
                Intent intentSignOut = new Intent(OfficerMapsActivity.this, SplashScreenActivity.class);
                startActivity(intentSignOut);
                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OfficerMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (getApplicationContext() != null) {

            mLastLocation = location;

            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            saveLocInformation();

            FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mFirebaseUser != null) {
                String userId = mFirebaseUser.getUid(); //Do what you need to do with the id

                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("officersAvailable");
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

            }
        }
    }

    private void getOfficerInfoHeader() {
        officerUserHeaderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null) {
                        String mName = map.get("name").toString();
                        mNameHeader.setText(mName);
                    }
                    if (map.get("phone") != null) {
                        String mPhone = map.get("phone").toString();
                        mPhoneHeader.setText(mPhone);
                    }
                    if (map.get("profileImageUrl") != null) {
                        String mProfileImageHeader = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageHeader).into(mImageHeader);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void connectOfficer() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OfficerMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());
    }

    private void disconnectOfficer() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getPendingIntent());

        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            String userId = mFirebaseUser.getUid(); //Do what you need to do with the id

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("officersAvailable");
            GeoFire geoFire = new GeoFire(refAvailable);
            geoFire.removeLocation(userId);
        }
    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this,MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void getRuasInfo(){
        mCarNumberOfficer = (TextInputEditText) findViewById(R.id.officer_nopol);
        mRuasGroup = (RadioGroup) findViewById(R.id.ruasGroup);

        officerUserHeaderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                    if (map.get("carNumber") != null){
                        mCarNumber = map.get("carNumber").toString();
                        mCarNumberOfficer.setText(mCarNumber);
                    }
                    if (map.get("ruas") != null) {
                        mRuas = map.get("ruas").toString();
                        switch (mRuas) {
                            case "Kanan":
                                mRuasGroup.check(R.id.kanan);
                                break;
                            case "Kiri":
                                mRuasGroup.check(R.id.kiri);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveLocInformation() {
        int selectedRuas = mRuasGroup.getCheckedRadioButtonId();
        ruasButton = (RadioButton) findViewById(selectedRuas);
        if (ruasButton.getText() == null){
            return;
        }
        mRuas = ruasButton.getText().toString();
        mCarNumber = mCarNumberOfficer.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("ruas", mRuas);
        userInfo.put("carNumber", mCarNumber);
        userInfo.put("officerLat",myLatLng.latitude);
        userInfo.put("officerLng",myLatLng.longitude);
        userInfo.put("riderRequest/location/from/officerLocationLat",myLatLng.latitude);
        userInfo.put("riderRequest/location/from/officerLocationLng",myLatLng.longitude);

        officerUserHeaderRef.updateChildren(userInfo);
    }
}