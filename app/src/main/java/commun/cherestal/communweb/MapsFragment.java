package commun.cherestal.communweb;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;

import com.facebook.share.Share;
import com.google.android.gms.location.LocationServices;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import android.location.LocationManager;


import com.google.android.gms.location.LocationServices;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.List;
import java.util.Map;

import commun.cherestal.communweb.adapter.PeopleNearbyListAdapter;
import commun.cherestal.communweb.app.App;
import commun.cherestal.communweb.model.Profile;
import commun.cherestal.communweb.service.TrackingService;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link //MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener{
    private OnFragmentInteractionListener mListener;

    private static GoogleMap mMap;
    private  Context mContext;
    private Activity activity;
    private ArrayList<Profile> itemsList;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private CameraPosition mCameraPosition;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;


    // A default location (Null island) and default zoom to use when location permission is
// not granted.
    private final LatLng mDefaultLocation = new LatLng(0, 0);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
// location retrieved by the Fused Location Provider.
    private static Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    boolean mRequestingLocationUpdates;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    private LocationRequest mLocationRequest;

    private boolean locationEnabled;


//Used to change radius from location

    private static double mapRadius = 8046.72;
    private double communRadius;
    Circle drawncircle;
    private int milePosition;



    // Location mLastLocation;
    Marker mCurrLocationMarker;
    //Other users' location marker
    private static Marker usersMarker;
    private static ArrayList<Marker> markerArrayList = new ArrayList<Marker>();
    private static ArrayList<MarkerOptions> markerOptionsArrayList;
    Marker otherMarkers;

    HashMap<Long, MarkerOptions> markerOptionsHashMap = new HashMap<>();
    HashMap<Long, Marker> markerHashMap = new HashMap<>();
    HashMap<Integer, Marker> allMarkersList = new HashMap<>();

    protected LocationManager lm;



    public MapsFragment() {
        // Required empty public constructor
}

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        //mContext=getActivity();
        MainActivity main;
        if(context instanceof MainActivity){
            mContext= context;

            main=(MainActivity)context;
           // Log.d(TAG, "This is MainActivity Context");
        }
        if(context instanceof MapsActivity){
            mContext= context;
           // Log.d(TAG, "This is MapsActivity Context");
        }else{
           // Log.d(TAG, context.toString());
        }
        //mContext = context;
        Log.d(TAG, context.toString()+ " on attach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        isLocationEnabled();


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            //getLastKnownLocation();

        }else if(mLastKnownLocation!=null){
            //onLocationChanged(mLastKnownLocation);
        }
        else{
            //getDeviceLocation();
        }

        Log.d(TAG, getActivity().toString() + " FusedLocationProvider");


        mContext =this.getActivity().getApplicationContext();
        if(mContext instanceof MapsActivity){
            Log.d(TAG, "This is MapsActivity Context");
        }
        Log.d(TAG, mContext.toString() + " on Create");

        //UserInfomation.distanceBetweenUser(14, 15);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       // mContext = getContext();
        Log.d(TAG, mContext.toString()+ "onCreate View");


        activity =getActivity();


        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // mContext = getContext();
        activity =getActivity();
        Log.d(TAG, mContext.toString() + "on Created");



        // initiate  views
        final SeekBar simpleSeekBar=(SeekBar) getView().findViewById(R.id.seekBar2);

        //Convert mapRadius value back into progress change, save as seekBar value
        Double seekBarValue = mapRadius/8046.72;
        int seekBarProgress = seekBarValue.intValue();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.seekBar_value), seekBarProgress);
        editor.apply();

        int mProgress = sharedPref.getInt(getString(R.string.seekBar_value), 0);
        simpleSeekBar.setProgress(mProgress);

        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue =0;
            double distanceTo;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(drawncircle!=null){
                    drawncircle.remove();
                }
                distanceTo=8046.72*progress;
                progressChangedValue = progress;
                milePosition=progress*5;
                mapRadius=distanceTo;

                if(mLastKnownLocation!=null){
                    drawCircle(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //drawncircle.remove();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(mContext, "Communities within: " + milePosition+ " miles",
                        Toast.LENGTH_SHORT).show();
                communRadius =mapRadius;
                markOtherUsers();
                Log.d(TAG, "Stopped tracking touch" + Double.toString(communRadius));

                drawncircle.remove();
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       // mContext = getActivity();
        activity =getActivity();
        Log.d(TAG, mContext.toString() + "on Activity created");
    }

    @Override
    public void onStart() {
        super.onStart();
       // mContext = getActivity();
        Log.d(TAG, mContext.toString() + "on Start");
        activity=getActivity();

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;
        // Prompt the user for permission.
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Turn on the My Location layer and the related control on the map.
//        updateLocationUI();

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, mContext.toString()+ "on Map Ready");
            getLocationPermission();
        }
        else{
            getDeviceLocation();
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                long id = (long) marker.getTag();

               Log.d(TAG, "THis is the Hash Code" + id );

               if(id!=UserInfomation.getProfileId()){
                   Intent intent = new Intent(getActivity(), ProfileActivity.class);
                   intent.putExtra("profileId", id);
                   startActivity(intent);
                }
                    return false;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(mLocationPermissionGranted){
                    startTrackerService();
                }
                return false;
            }
        });






/*
        // initiate  views
        final SeekBar simpleSeekBar=(SeekBar) getView().findViewById(R.id.seekBar2);

        //Convert mapRadius value back into progress change, save as seekBar value
        Double seekBarValue = mapRadius/8046.72;
        int seekBarProgress = seekBarValue.intValue();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.seekBar_value), seekBarProgress);
        editor.apply();

        int mProgress = sharedPref.getInt(getString(R.string.seekBar_value), 0);
        simpleSeekBar.setProgress(mProgress);

        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue =0;
            double distanceTo;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawncircle.remove();
                distanceTo=8046.72*progress;
                progressChangedValue = progress;
                milePosition=progress*5;
                mapRadius=distanceTo;

                drawCircle(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //drawncircle.remove();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(mContext, "Communities within: " + milePosition+ " miles",
                        Toast.LENGTH_SHORT).show();
                communRadius =mapRadius;
                markOtherUsers();
                Log.d(TAG, "Stopped tracking touch" + Double.toString(communRadius));

                drawncircle.remove();
            }
        });*/



    }

    public void isLocationEnabled(){

        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                buildAlertMessageNoGps();
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        //Stop tracking and uploading user location
        mContext.stopService(new Intent(getActivity(), TrackingService.class));

        //stop location updates when Activity is no longer active
        if (mFusedLocationProviderClient != null) {
           // mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            Log.d(TAG, mContext.toString() + " Map Fragment was paused");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationEnabled=true;
        //startTrackerService();
        Log.d(TAG, mContext.toString() + " Map Fragment was Resumed");
        startLocationUpdates();
       // getLastKnownLocation();
        getDeviceLocation();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            getLocationPermission();
            Log.d(TAG, "Checking Location Again");

        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }


    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                    mRequestingLocationUpdates);
            super.onSaveInstanceState(outState);
        }
        else {

            itemsList = new ArrayList<Profile>();

        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mLocationPermissionGranted = true;
                getDeviceLocation();
                Log.d(TAG,  " getLocationPermission Granted");
            }
            else {
                //requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                Log.d(TAG,  " Not Granted getLocationPermission");
            }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                   // getLastKnownLocation();
                    getDeviceLocation();

                    //getLastKnownLocation();
                  // startTrackerService();
                }
                else {
                    //If the user denies the permission request, then display a toast with some more information//
                    Toast.makeText(getActivity(), "Please enable location services to allow GPS tracking", Toast.LENGTH_SHORT).show();

                }
                return;

            }

        }
        updateLocationUI();
    }

    /**
     * starts TrackerService.java to track user location and update to Firebase
     */
    private void startTrackerService() {
        if(locationEnabled){
            mContext.startService(new Intent(getActivity(), TrackingService.class));
            Log.d(TAG, mContext.toString() + " Tracker Service");
            //Notify the user that tracking has been enabled//
            Toast.makeText(getActivity(), "GPS tracking enabled", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getActivity(), "Tracking Service could not get your location", Toast.LENGTH_SHORT).show();
        }

    }



    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            if(mLastKnownLocation!=null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                onLocationChanged(mLastKnownLocation);
                                locationEnabled = true;
                                updateLocationUI();

                            }

                        } else {

                            Log.d(TAG, "Current location is null. Using defaults.");
                            getLastKnownLocation();
                            Log.e(TAG, "Exception: %s", task.getException());

                            if(mLastKnownLocation!=null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }

                            //mMap.moveCamera(CameraUpdateFactory
                            // .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            startLocationUpdates();
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }

                });
            }else {
                getLocationPermission();

            }

        }
        catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                locationEnabled =true;
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {
        mLastKnownLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("I was here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        Log.d(TAG, mContext.toString() + " On Location Changed Called");
        drawCircle(new LatLng(location.getLatitude(), location.getLongitude()));

        mCurrLocationMarker.setTag(App.getInstance().getId());

    }

    /**
     *hides other user markers new current location, based on user set radius
     */
    public void markOtherUsers(){
        LatLng myLatLang = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        if(usersMarker!=null){
            if (SphericalUtil.computeDistanceBetween(myLatLang, usersMarker.getPosition()) > mapRadius) {
                usersMarker.setVisible(false);
                Log.d("TAG", "Location is soooooo removed");
            }
            else{
                usersMarker.setVisible(true);
            }
        }
    }

    /**
     * shows markers for all other users on map. Called from setCurrLocationMarker
     */
    private void setAllMarkers(long id, boolean isSharing) {
        Marker userMarkers;
        MarkerOptions markerOptions;
        if(isSharing){

        }
       if(markerHashMap.containsKey(id)){
           Marker marker = markerHashMap.get(id);
           if(marker!=null){
               marker.remove();
           }
           markerHashMap.remove(id);
       }
       markerOptions = markerOptionsHashMap.get(id);
       userMarkers = mMap.addMarker(markerOptions);
        userMarkers.setTag(id);
       markerHashMap.put(id, userMarkers);

       usersMarker=userMarkers;

        userMarkers.setVisible(false);
        if(mLastKnownLocation!=null) {
            LatLng myLatLang = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            if (SphericalUtil.computeDistanceBetween(myLatLang, userMarkers.getPosition()) < mapRadius) {
                Log.d("TAG", "Location is Less" + Double.toString(mapRadius));
                userMarkers.setVisible(true);

                //tag users' location with its instance id
               // userMarkers.setTag(id);

            } else {
                userMarkers.remove();
                Log.d("TAG", "Location was added but REMOVED");
            }
        }else {
            userMarkers.setVisible(true);
        }
        Log.d("TAG", "Location is not NULL");

        Log.d(TAG, "THe size of the hashmap is: " + markerHashMap.size());
        Log.d(TAG, "THe size of the Marker Options map is: " + markerOptionsHashMap.size());
    }

    public void removeMarker(long instanceId){
        if(markerHashMap.containsKey(instanceId)){
            Marker marker = markerHashMap.get(instanceId);
            marker.remove();
        }
        markerHashMap.remove(instanceId);

    }

    /**
     * entry point for all markers from other users. Called from UserInformation.class
     */
    public void setCurrLocationMarker(LatLng latLng, long instanceId, boolean isSharing) {

        if (!isSharing || mapRadius < 100) {
            removeMarker(instanceId);
        } else {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptionsHashMap.put(instanceId, markerOptions);
            setAllMarkers(instanceId, isSharing);
        }
    }



    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }
    }

    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        final CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(mapRadius);

        // Border color of the circle
        circleOptions.strokeColor(Color.GREEN);

        // Fill color of the circle
        circleOptions.fillColor(Color.argb(50, 155, 255, 240));

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        drawncircle = mMap.addCircle(circleOptions);




       /* // initiate  views
        final SeekBar simpleSeekBar=(SeekBar) getView().findViewById(R.id.seekBar2);

        //Convert mapRadius value back into progress change, save as seekBar value
        Double seekBarValue = mapRadius/8046.72;
        int seekBarProgress = seekBarValue.intValue();
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.seekBar_value), seekBarProgress);
        editor.apply();

        int mProgress = sharedPref.getInt(getString(R.string.seekBar_value), 0);
        simpleSeekBar.setProgress(mProgress);

        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue =0;
            double distanceTo;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawncircle.remove();
                distanceTo=8046.72*progress;
                progressChangedValue = progress;
                milePosition=progress*5;
                mapRadius=distanceTo;
                drawCircle(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //drawncircle.remove();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(mContext, "Communities within: " + milePosition+ " miles",
                        Toast.LENGTH_SHORT).show();
                communRadius =mapRadius;
                markOtherUsers();
                Log.d(TAG, "Stopped tracking touch" + Double.toString(communRadius));

                drawncircle.remove();
            }
        });
*/

    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastKnownLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                if(mLocationPermissionGranted){
                    onLocationChanged(location);
                    startTrackerService();

                }



            }
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    };



    private  void getLastKnownLocation() {

        try {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                onLocationChanged(location);
                                mLastKnownLocation=location;
                                if(mLocationPermissionGranted){
                                    startTrackerService();
                                }

                                drawCircle(new LatLng(location.getLatitude(), location.getLongitude()));
                                updateLocationUI();



                            }
                            else{
                                startLocationUpdates();
                                getDeviceLocation();
                                Log.d(TAG, "Location not Found");

                            }
                        }


                    });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        getDeviceLocation();


                       // getLastKnownLocation();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }


    //Take radius value in miles, set mapRadius value in Feet
    public static void setMapRadius(double radius){

        mapRadius = radius*1609.344;
    }

    //Return current radius from user location in meters
    public static double getMapRadius(){
        return mapRadius;
    }

    public static double compareMarkerDistance(Marker userMarkerOne, Marker userMarkerTwo){
        return SphericalUtil.computeDistanceBetween(userMarkerOne.getPosition(), userMarkerTwo.getPosition());
    }

    public static double getProfileIdFromMarker(Marker userMarker){
        return (double) userMarker.getTag();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.stopService(new Intent(getActivity(), TrackingService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.stopService(new Intent(getActivity(), TrackingService.class));
       mContext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.stopService(new Intent(getActivity(), TrackingService.class));

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
