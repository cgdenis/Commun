package commun.cherestal.communweb.service;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import commun.cherestal.communweb.R;
import commun.cherestal.communweb.UserInfomation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();

    private BroadcastReceiver sendBroadcastReceiver;
    private BroadcastReceiver deliveryBroadcastReceiver;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";



    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private String locationPath="";

    private String UUID;

    Context serviceContext;


    private String locationSharing;

    UserInfomation userInfo = new UserInfomation();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        checkCurrentUser();
        locationSharing = "True";
        showOtherUsers();
       // userInfo.setMarker();
        userInfo.path = locationPath;
        userInfo.onInfoReady();


        serviceContext = getBaseContext();
        Log.d(TAG, serviceContext.toString());



      //Create the persistent notification//
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification//
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))
                //Make this notification ongoing so it can’t be dismissed by the user//

                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracking_enabled);
        startForeground(1, builder.build());
    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Unregister the BroadcastReceiver when the notification is tapped//
            unregisterReceiver(stopReceiver);
            //Stop the Service//
            stopSelf();

            //Notify Firebase user stops sharing
            locationSharing = "false";

            //Get a reference to the database, so your app can perform read and write operations//
            DatabaseReference userSharing = database.getReference(locationPath).child(UUID).child("Location").child("isSharing");
            userSharing.setValue(locationSharing);
        }
    };

    public void showOtherUsers(){
        UserInfomation users = new UserInfomation();
        //users.setMarker();
    }






    public void checkCurrentUser() {
        // [START check_current_user]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UUID = FirebaseAuth.getInstance().getUid();
            loginToFirebase();
        } else {
            // No user is signed in
            Toast.makeText(this, "No GPS Tracking ", Toast.LENGTH_SHORT).show();
        }
        // [END check_current_user]
    }



    private void loginToFirebase(){

        requestLocationUpdates();
        locationSharing="True";
    }



    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }


    //Stop tracking when application is paused or destroyd
    @Override
    public void onDestroy(){
        registerReceiver(stopReceiver, new IntentFilter("Stop"));
        unregisterReceiver(stopReceiver);
        stopSelf();
        locationSharing="false";
        super.onDestroy();



    }

    public void postToDatabase(Location location){

        //Get a reference to the database, so your app can perform read and write operations//
        DatabaseReference myRef = database.getReference(locationPath).child(UUID).child("Location");
        myRef.child("latitude").setValue(location.getLatitude());
        myRef.child("longitude").setValue(location.getLongitude());
        myRef.child("fromMockProvider").setValue(location.isFromMockProvider());
        myRef.child("time").setValue(location.getTime());

    }


    //Initiate the request to track the device's location//

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//
        request.setInterval(10000);

        //Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = getString(R.string.firebase_path);
        locationPath=path;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {



                    //DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
                    //Location location = locationResult.getLastLocation();

                   // DatabaseReference myRef = database.getReference(path).child(UUID).child("Location");

                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                       if(locationSharing.equalsIgnoreCase("True")) {
                           //Save the location data to the database//
                           postToDatabase(location);

                           DatabaseReference userSharing = database.getReference(locationPath).child(UUID).child("Location").child("isSharing");
                           userSharing.setValue(locationSharing);
                           //userInfo.setMarker();




                       }

                    }
                }
            }, null);
        }
    }
}