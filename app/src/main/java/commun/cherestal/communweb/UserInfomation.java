package commun.cherestal.communweb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.util.ArrayList;

import commun.cherestal.communweb.app.App;

public class UserInfomation extends AppCompatActivity implements UserInfo{

    private static final String TAG = "UserInformation.class";

    public String path;

    private static String UUID = FirebaseAuth.getInstance().getUid();

    private static  ArrayList<LatLng> locations = new ArrayList<>();

    private static double latitude=0.0;
    private static double longitude=0.0;

    public static double idLatitude=0.0;
    public static double idLongitude=0.0;

    private static long instanceId;

    private static long markerId=0;

    private static long userInstance;

    private static double postLat=0.0;
    private static double postLng=0.0;

    private static double userLat=0.0;
    private static double userLng=0.0;

//    private Context mContext = getApplicationContext();

    private static String uuidByProfile;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    MapsFragment map = new MapsFragment();

   // Context userContext = getApplicationContext();


    /*@Override
    protected void onStart() {
        super.onStart();
        checkCurrentUser();
    }*/

    private static DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("/Users/ID");

    private final static DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("/Posts");

    public interface MyCallback {
        void onCallback(String value);

    }

    public  interface MyCallback2{
        void onCallback(String data1, String data2);
    }

    public  interface MyCallback3{
        void onCallback(String data0, String data1, String data2, String data3);
    }




    /** Reads user last updated latitude */
    public static void readUserLat(final MyCallback myCallback) {
        userRef.child(UUID).child("Location").child("latitude").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                myCallback.onCallback(String.valueOf(value));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    /** Reads user last updated longitude */
    public static void readUserLng(final MyCallback myCallback) {
        userRef.child(UUID).child("Location").child("longitude").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                myCallback.onCallback(String.valueOf(value));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public static void readIdLat(final long id, final MyCallback2 myCallback){
        userRef.getRoot().child("Locations").child(Long.toString(id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child("latitude").getValue(String.class);
                String value1 = dataSnapshot.child("longitude").getValue(String.class);
                idLatitude=Double.parseDouble(value);
                idLongitude=Double.parseDouble(value1);
                Log.d(TAG, "This is the value" + value +value1);
                myCallback.onCallback(value, value1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static void readIdLatLng(final long id, final long id1, final MyCallback3 myCallback){
        userRef.getRoot().child("Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.child(String.valueOf(id)).child("latitude").getValue(String.class);
                String value1 = dataSnapshot.child(String.valueOf(id)).child("longitude").getValue(String.class);
                //String value = dataSnapshot.child("latitude").getValue(String.class);
                //String value2 = dataSnapshot.child("longitude").getValue(String.class);
                String value2 = dataSnapshot.child(String.valueOf(id1)).child("latitude").getValue(String.class);
                String value3 = dataSnapshot.child(String.valueOf(id1)).child("longitude").getValue(String.class);

               // idLatitude=Double.parseDouble(value);
                //idLongitude=Double.parseDouble(value1);
                //Log.d(TAG, "This is the value" + value +value1);
                myCallback.onCallback(value, value1, value2, value3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Read latitude and longitude from user location in Firebase
    public void onInfoReady() {


        //Log.d(TAG, userContext.toString());
        if(mAuth!=null){
            allInfo();
        }



        final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference(path);

        // Read from the database
        dataRef.addValueEventListener(new ValueEventListener() {
            // This method is called once with the initial value and again whenever data at this location is updated.
            @Override
            public void onDataChange(@NonNull DataSnapshot  dataSnapshot) {
                // This method iterates through children of dataRef

                    for(@NonNull DataSnapshot ds: dataSnapshot.getChildren()){
                        if(!ds.getKey().equals(UUID)){
                            Double lat = ds.child("Location").child("latitude").getValue(Double.class);
                            Double lng = ds.child("Location").child("longitude").getValue(Double.class);
                            String isSharing = ds.child("Location").child("isSharing").getValue(String.class);
                            Long id =  ds.child("Instance_ID").getValue(Long.class);
                            if(lat!=null && lng!=null) {
                                latitude = lat;
                                longitude = lng;
                                if(id!=null){
                                    //retrieve the User instance id from firebase after lat long for marker
                                    markerId = id;
                                }

                                if(isSharing.equalsIgnoreCase("True")){
                                    setMarker(true, lat, lng);
                                }
                                else{
                                    setMarker(false, lat, lng);
                                }

                                //locations.add(new LatLng(latitude, longitude));
                               // Log.d("TAG", lat+ " + " + lng);

                            }
                            Log.d("TAG", lat+ " + " + lng);
                        }

                    }
            }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                    }
            });
        }

    //set marker for user for each data change in location
    public void setMarker(boolean isSharing, double latitude, double longitude){
        LatLng allLatLng = new LatLng(latitude, longitude);
        ((MapsFragment) map).setCurrLocationMarker(allLatLng, markerId, isSharing);

        Log.d(TAG, "The marker Id is" + markerId);
    }

    public void removeMarker(){

    }

    public void checkCurrentUser() {
        // [START check_current_user]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UUID = FirebaseAuth.getInstance().getUid();

        } else {
            // No user is signed in
            Toast.makeText(this, "Please signed into your account.", Toast.LENGTH_SHORT).show();
        }
        // [END check_current_user]
    }

    public void allInfo(){
        String info;
        if(mAuth!=null){
            //info = mAuth.getUid() + "  " + mAuth.getCurrentUser().getDisplayName()+ " " + mAuth.getCurrentUser().getEmail() + " " + mAuth.getCurrentUser().getPhoneNumber();
           // Log.w(TAG, info);
        }


    }


    //Set instance id in Firebase
    public static void setProfileId(){
        instanceId = App.getInstance().getId();
        userRef.child(UUID).child("Instance_ID").setValue(instanceId);
    }

    //Retrieves Profile id from Firebase. Same value as App.getInstance().getId()
    public static long getProfileId(){
        userRef.child(UUID).child("Instance_ID");

        userRef.child(UUID).child("Instance_ID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                instanceId = (long) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
        return instanceId;
    }

    public static void postToDatabase(Object object){
        String objectName = object.toString();
        userRef.child(UUID).child(objectName).setValue(object);
    }

    //rot 13 method for user's name input
    public static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    /** Uploads user post hashcode and location */
    public static void sendPostToFirebase(JSONObject response,String lat, String lng){
        int post = response.hashCode();
        final String postID = Integer.toString(post);

        if(!lat.equals("0.000000") || (!lng.equals("0.000000"))){
            postsRef.child(postID).child(UUID).child("latitude").setValue(Double.parseDouble(lat));
            postsRef.child(postID).child(UUID).child("longitude").setValue(Double.parseDouble(lng));
        }else{
            readUserLat(new MyCallback() {
                @Override
                public void onCallback(String value) {
                    postsRef.child(postID).child(UUID).child("latitude").setValue(value);
                }
            });

            readUserLng(new MyCallback() {
                @Override
                public void onCallback(String value) {
                    postsRef.child(postID).child(UUID).child("longitude").setValue(value);
                }
            });

        }

    }


    /** Uploads user instance/profile id associated with Auth UUID */
    public static void updateProfileID(){
        String profileID = Long.toString(App.getInstance().getId());
        userRef.getRoot().child("ProfileIDs").child(profileID).setValue(UUID);

        Log.d(TAG, "The root was accesses, profile ID is updated");
    }





    /** Gets UUID associated with instance/profile id */
    public static String getUUIDbyProfileId(long id) {
        final long uid = id;

        userRef.getRoot().child("ProfileIDs").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String uuidProfile = dataSnapshot.child(Long.toString(uid)).getValue(String.class);
                if(uuidProfile!=null){
                    uuidByProfile=uuidProfile;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
     return uuidByProfile;
    }

    /** uploads user location associated with instance/profile id */
    public static void setLatLngById(long id) {
        final String uid = Long.toString(id);
        readUserLat(new MyCallback() {
            @Override
            public void onCallback(String value) {
                userRef.getRoot().child("Locations").child(uid).child("latitude").setValue(value);
            }
        });

        readUserLng(new MyCallback() {
            @Override
            public void onCallback(String value) {
                userRef.getRoot().child("Locations").child(uid).child("longitude").setValue(value);
            }
        });
    }

    /** retrieves lat and long by user profile/instance id*/
    public static void getUserLocbyId(long id){
        readIdLat(id, new MyCallback2() {
            @Override
            public void onCallback(final String value1, String value2) {
                idLatitude = Double.parseDouble(value1);
                idLongitude = Double.parseDouble(value2);

                StreamFragment.idLatitude = idLatitude;
                StreamFragment.idLongitude = idLongitude;
                locations.add(new LatLng(idLatitude, idLongitude));


            }
        }) ;

    }
    /** retrieves lat and long of two users by profile/instance id
     * computes distance between the two locations(lat/lng)
     * */
    public static void distanceBetweenUser(long id0, final long id1) {
       // final String uid1 = Long.toString(id0);
        //final String uid2 = Long.toString(id1);


        readIdLatLng(id0, id1, new MyCallback3() {

            @Override
            public void onCallback(String data0, String data1, String data2, String data3) {
                LatLng latLng0 = new LatLng(Double.parseDouble(data0), Double.parseDouble(data1));
                LatLng latLng1 = new LatLng(Double.parseDouble(data2), Double.parseDouble(data3));

                //calculates distance between two users' lat and long
                Double distance = SphericalUtil.computeDistanceBetween(latLng0, latLng1);
                if(distance!=0.0){
                    StreamFragment.distbetween = distance;
                }

                //Store distance value in shared preference for fast update and retrieval
                SharedPreferences sharedPref = App.getInstance().getSharedPreferences(Context.LOCATION_SERVICE, 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("distanceBetween", String.valueOf(distance));
                editor.apply();

            }
        });


    }




    @Override
    public String getUid() {
        return null;
    }

    @Override
    public String getProviderId() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public Uri getPhotoUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getEmail() {
        return null;
    }

    @Nullable
    @Override
    public String getPhoneNumber() {
        return null;
    }

    @Override
    public boolean isEmailVerified() {
        return false;
    }

}
