package com.example.tkkil.phuot.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tkkil.phuot.Adapter.GroupAdapter;
import com.example.tkkil.phuot.Folder.DirectionData;
import com.example.tkkil.phuot.Folder.GetNearbyPlaces;
import com.example.tkkil.phuot.Interface.ItemClickListener;
import com.example.tkkil.phuot.Models.Group;
import com.example.tkkil.phuot.Models.User;
import com.example.tkkil.phuot.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private static final int REQUEST_LOCATION_CODE = 99;
    private static final int REQUEST_SELECT_PICTURE = 999;
    NavigationView myNav;
    Bitmap bitmap;
    RecyclerView nav_rcvListGroup;
    //    FirebaseRecyclerAdapter adapterabc;
    Button nav_btnCreate, nav_btnJoin;
    ArrayList<Group> groups;
    GroupAdapter adapter_group;
    FirebaseRecyclerAdapter adapter, adapter_member;
    Toolbar toolbar;
    HashMap<String, Marker> hashMap;
    ImageView nav_restaurant, nav_gas, nav_hotel;
    Button nav_follow, nav_sos;
    boolean isStatusRestaurant = false;
    boolean isStatusGas = false;
    boolean isStatusHotel = false;
    boolean isStatusFollow = false;
    boolean isStatus = true;
    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    //    private Marker mCurrentLocationMarker;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private DrawerLayout myDrawer;
    private ProgressDialog loading;
    private Uri filepath;
    //Header-Nav
    private CircleImageView nav_avatar;
    private TextView nav_name, nav_email;
    //Dialog avatar
    private CircleImageView dialog_avatar;
    private User user;
    private String name;

    private ArrayList<Marker> gasPlaces = new ArrayList<>();
    private ArrayList<Marker> resPlaces = new ArrayList<>();
    private ArrayList<Marker> hotelPlaces = new ArrayList<>();
    private ArrayList<Polyline> polylinePlace = new ArrayList<>();
    private ArrayList<Marker> randomMarker = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermisstion();
        }
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        loading = new ProgressDialog(this, R.style.MyDialogTheme);
        loading.setTitle("LOADING");
        loading.setMessage("Please wait...");

        init();
        initToolbar();
        myRef.child("SOS").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                s = dataSnapshot.getValue(String.class);
                if (s != null) {
                    if (!isStatus) {
                        myRef.child("Users").child(s).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                                ab.setMessage("HELP ME, PLEASE! My name is " + user.getFullname());
                                ab.setTitle("SOS");
                                ab.setPositiveButton("OK", null);
                                final AlertDialog alertDialog = ab.create();
                                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    }
                                });
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkLocationPermisstion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hashMap = new HashMap<>();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        if (mClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, this);
        }

        /*locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, this);*/

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Double curLat = mLastLocation.getLatitude();
        Double curLong = mLastLocation.getLongitude();
        if (toolbar.getSubtitle() != null) {
            myRef.child("Groups/" + toolbar.getSubtitle() + "/members").child(mAuth.getCurrentUser().getUid()).setValue(curLat + " " + curLong);
//            Log.d("AAA", curLat + " " + curLong);
        }

        /*if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(curLat, curLong);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mCurrentLocationMarker = mMap.addMarker(markerOptions);*/


//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
//
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

       /* if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                /*if (polylinePlace.size() > 0) {
                    for (int i = 0; i < polylinePlace.size(); i++) {
                        polylinePlace.get(i).remove();
                    }
                    randomMarker.get(0).remove();
                    randomMarker.clear();
                    polylinePlace.clear();
                }
                Object dataTransfer[] = new Object[5];
                String url = getDirectionUrl(latLng);
                DirectionData directionData = new DirectionData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = latLng;
                dataTransfer[3] = polylinePlace;
                dataTransfer[4] = randomMarker;

                directionData.execute(dataTransfer);*/

                if (toolbar.getSubtitle() != null) {
                    myRef.child("Groups").child(toolbar.getSubtitle().toString()).child("sharelocation").setValue(latLng.latitude + " " + latLng.longitude);
                    myRef.child("Groups").child(toolbar.getSubtitle().toString()).child("sharelocation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s = dataSnapshot.getValue(String.class);
                            String a[] = s.split(" ");
                            Double lat = Double.parseDouble(a[0]);
                            Double longg = Double.parseDouble(a[1]);
                            if (polylinePlace.size() > 0) {
                                for (int i = 0; i < polylinePlace.size(); i++) {
                                    polylinePlace.get(i).remove();
                                }
                                randomMarker.get(0).remove();
                                randomMarker.clear();
                                polylinePlace.clear();
                            }
                            LatLng latLng1 = new LatLng(lat,longg);
                            Object dataTransfer[] = new Object[5];
                            String url = getDirectionUrl(latLng1);
                            DirectionData directionData = new DirectionData();
                            dataTransfer[0] = mMap;
                            dataTransfer[1] = url;
                            dataTransfer[2] = latLng1;
                            dataTransfer[3] = polylinePlace;
                            dataTransfer[4] = randomMarker;

                            directionData.execute(dataTransfer);
//                            Toast.makeText(MainActivity.this, "" + s, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mMap.setOnMarkerClickListener(this);
    }

    private void initGoogleMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_btnCreate:
                addGroupDialog();
                break;
            case R.id.nav_btnJoin:
                onJoin();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (myDrawer.isDrawerOpen(GravityCompat.START)) {
            myDrawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder ab_exit = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
            ab_exit.setTitle("EXIT");
            ab_exit.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            ab_exit.setNegativeButton("NO", null);
            final AlertDialog dialog_ab_exit = ab_exit.create();
            dialog_ab_exit.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    dialog_ab_exit.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                }
            });
            dialog_ab_exit.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_info:
                startActivity(new Intent(MainActivity.this, ChangeInformationActivity.class));
                break;
            case R.id.action_change_password:
                startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.action_log_out:
                AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.MyDialogTheme);
                ab.setTitle("SIGN OUT");
                ab.setNegativeButton("NO", null);
                ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                final AlertDialog alertDialog = ab.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorRed));
                    }
                });
                alertDialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            filepath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                dialog_avatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addGroupDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_group, null);
        ab.setView(view);

        ab.setPositiveButton("CREATE", null);
        ab.setNegativeButton("CANCEL", null);

        final EditText edtNameGroup = view.findViewById(R.id.edtNameGroup);
        final EditText edtPwdGroup = view.findViewById(R.id.edtPwdGroup);

        TextView txtvTitle = view.findViewById(R.id.txtvTitle);
        txtvTitle.setText("ADD NEW GROUP");

        final AlertDialog dialog = ab.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                Button Positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Positive.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(edtNameGroup.getText().toString().trim())) {
                            edtNameGroup.setError("Please enter Name!");
                            return;
                        }
                        if (TextUtils.isEmpty(edtPwdGroup.getText().toString().trim())) {
                            edtPwdGroup.setError("Please enter Password!");
                            return;
                        } else {
                            if (edtPwdGroup.getText().toString().trim().length() < 6) {
                                edtPwdGroup.setError("Password is too short!");
                                return;
                            }
                        }
                        myRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(edtNameGroup.getText().toString().trim())) {
                                    Toast.makeText(MainActivity.this, "Can't create, group name is existed!", Toast.LENGTH_SHORT).show();
                                } else {
                                    final Group group = new Group();
                                    group.setName(edtNameGroup.getText().toString().trim());
                                    group.setHost(mAuth.getCurrentUser().getUid());
                                    group.setPass(edtPwdGroup.getText().toString().trim());
                                    group.setTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
                                    myRef.child("Groups").child(edtNameGroup.getText().toString().trim()).setValue(group)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        myRef.child("Groups").child(edtNameGroup.getText().toString().trim()).child("members").child(mAuth.getCurrentUser().getUid()).setValue(mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
                                                        Snackbar.make(myDrawer, "Success", Snackbar.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");
        myDrawer = findViewById(R.id.myDrawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, myDrawer, toolbar, R.string.openDrawer, R.string.closeDrawer);
        myDrawer.addDrawerListener(toggle);
        toggle.syncState();

        if (TextUtils.isEmpty(toolbar.getSubtitle())) {
            nav_follow.setVisibility(View.INVISIBLE);
        } else {

        }
    }

    private int onGetHeightStatus() {
        int result = 0;
        int resourceId = MainActivity.this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void onJoin() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_group, null);
        ab.setView(view);
        //Init
        TextView txtvTitle = view.findViewById(R.id.txtvTitle);
        final EditText edtNameGroup = view.findViewById(R.id.edtNameGroup);
        final EditText edtPwdGroup = view.findViewById(R.id.edtPwdGroup);

        txtvTitle.setText("JOIN GROUP");
        ab.setPositiveButton("JOIN", null);
        ab.setNegativeButton("CANCEL", null);
        final AlertDialog alertDialog = ab.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                Button Positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(edtNameGroup.getText().toString().trim())) {
                            edtNameGroup.setError("Please enter Name!");
                            return;
                        }
                        if (TextUtils.isEmpty(edtPwdGroup.getText().toString().trim())) {
                            edtPwdGroup.setError("Please enter Password!");
                            return;
                        } else {
                            if (edtPwdGroup.getText().toString().trim().length() < 6) {
                                edtPwdGroup.setError("Password is too short!");
                                return;
                            }
                        }
                        myRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(edtNameGroup.getText().toString().trim())) {
                                    if (dataSnapshot.hasChild(edtPwdGroup.getText().toString().trim())) {
                                        myRef.child("Groups").child(edtNameGroup.getText().toString().trim()).child("members").child(mAuth.getCurrentUser().getUid()).setValue(mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
                                    } else {
                                        Toast.makeText(MainActivity.this, "Wrong, can't join!", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Group is not exists!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void init() {
        initGoogleMap();

        nav_btnCreate = findViewById(R.id.nav_btnCreate);
        nav_btnJoin = findViewById(R.id.nav_btnJoin);
        nav_btnCreate.setOnClickListener(this);
        nav_btnJoin.setOnClickListener(this);

        myNav = findViewById(R.id.myNav);
        myNav.setPadding(0, onGetHeightStatus(), 0, 0);

        nav_avatar = findViewById(R.id.nav_avatar);
        nav_name = findViewById(R.id.nav_name);
        nav_email = findViewById(R.id.nav_email);

        nav_rcvListGroup = findViewById(R.id.nav_rcvListGroup);
        nav_rcvListGroup.setHasFixedSize(true);
        nav_rcvListGroup.setLayoutManager(new LinearLayoutManager(this));

        Query query = myRef.child("Groups")
                .orderByChild("members/" + mAuth.getCurrentUser().getUid())
                .startAt("");
        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ViewHolder holder, int position, final Group model) {
                holder.txtvNameGroup.setText(model.getName());

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        toolbar.setSubtitle(model.getName());
                        nav_follow.setVisibility(View.VISIBLE);
                        onLoadMember(model.getName());
                        myRef.child("Groups/" + model.getName() + "/members/").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if (dataSnapshot.getKey().contains(mAuth.getCurrentUser().getUid())) {

                                } else {
                                    s = dataSnapshot.getKey();
                                    String[] a = dataSnapshot.getValue().toString().split(" ");
                                    Double lat = Double.parseDouble(a[0]);
                                    Double lng = Double.parseDouble(a[1]);
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(new LatLng(lat, lng));
                                    Marker marker = mMap.addMarker(markerOptions);
                                    hashMap.put(s, marker);
//                                    Log.d("AAA", s);

//                                    myRef.child("Groups/"+model.getName()+"/members/"+mAuth.getCurrentUser().getUid()).setValue(mCurrentLocationMarker.getPosition().latitude+" " +mCurrentLocationMarker.getPosition().longitude);

//                                    myDrawer.closeDrawer(GravityCompat.START);
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                if (dataSnapshot.getKey().toString().contains(mAuth.getCurrentUser().getUid())) {
//                                    Log.d("CCC",dataSnapshot.getKey());
                                } else {
                                    String[] a = dataSnapshot.getValue().toString().split(" ");
                                    Double latt = Double.parseDouble(a[0]);
                                    Double longg = Double.parseDouble(a[1]);
                                    hashMap.get(dataSnapshot.getKey()).remove();
                                    hashMap.remove(dataSnapshot.getKey());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(new LatLng(latt, longg));
//                                    Log.d("BBB", latt + " " + longg);
                                    Marker marker = mMap.addMarker(markerOptions);
                                    hashMap.put(dataSnapshot.getKey(), marker);
                                }

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        mMap.clear();
                    }
                });


                myRef.child("Users").child(model.getHost()).child("fullname").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.txtvFullname.setText(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                myRef.child("Groups").child(model.getName()).child("host").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue().toString().contains(mAuth.getCurrentUser().getUid())) {
                            holder.btnDelete.setText("DELETE");
                            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                                    ab.setTitle("DELETE");
                                    ab.setMessage("Are you sure?");
                                    ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            myRef.child("Groups").child(model.getName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                                        mMap.clear();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    ab.setNegativeButton("NO", null);
                                    AlertDialog alertDialog = ab.create();
                                    alertDialog.show();
                                }
                            });
                        } else {
                            holder.btnDelete.setText("QUIT");
                            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                                    ab.setTitle("DELETE");
                                    ab.setMessage("Are you sure?");
                                    ab.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (model.getName() != null) {
                                                myRef.child("Groups/" + model.getName() + "/members/" + mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(MainActivity.this, "Quited!", Toast.LENGTH_SHORT).show();
                                                            toolbar.setSubtitle("");
                                                            onLoadMember("");
                                                            mMap.clear();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    ab.setNegativeButton("NO", null);
                                    AlertDialog alertDialog = ab.create();
                                    alertDialog.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        nav_rcvListGroup.setAdapter(adapter);
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            myRef.child("Users/" + mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = new User();
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        if (TextUtils.isEmpty(user.getAvatar())) {
                            user.setAvatar("AAA");
                        }
                        Picasso.with(MainActivity.this)
                                .load(user.getAvatar())
                                .placeholder(R.drawable.defaut_avatar)
                                .centerCrop()
                                .fit()
                                .into(nav_avatar);
                        nav_name.setText(user.getFullname());
                        nav_email.setText(user.getEmail());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        nav_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, nav_avatar, Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.avatar, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_change_avatar) {
                            final AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            ab.setTitle("CHANGE AVATAR");
                            ab.setIcon(R.drawable.ic_account_box_green_24dp);
                            @SuppressLint("InflateParams")
                            View dialog_change_avatar = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_change_avatar, null);
                            ab.setView(dialog_change_avatar);
                            dialog_avatar = dialog_change_avatar.findViewById(R.id.dialog_avatar);
                            ab.setPositiveButton("CHANGE", null);
                            ab.setNeutralButton("SELECT PICTURE", null);
                            ab.setNegativeButton("NO", null);
                            final AlertDialog alertDialog = ab.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBlue));
                                    Button b = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                    Button c = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    b.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_PICK);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                                        }
                                    });
                                    c.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog_avatar.getDrawable() == null) {
                                                Toasty.error(MainActivity.this, "Please choose picture!", Snackbar.LENGTH_SHORT, true).show();
                                            } else {
                                                loading.show();
                                                StorageReference storageReference = mStorage.child("images").child(UUID.randomUUID().toString());
                                                storageReference.putFile(filepath)
                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                                                myRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("avatar").setValue(downloadUri.toString());
                                                                loading.dismiss();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Snackbar.make(myDrawer, "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                loading.dismiss();
                                                            }
                                                        });
                                                alertDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });


        //NavigationView Right

        nav_restaurant = findViewById(R.id.nav_restaurant);
        nav_gas = findViewById(R.id.nav_gas);
        nav_hotel = findViewById(R.id.nav_hotel);
        nav_follow = findViewById(R.id.nav_follow);
        nav_sos = findViewById(R.id.nav_sos);

        nav_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStatusRestaurant) {
                    findNearbyPlaces(resPlaces, "restaurant");
                    nav_restaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_restaurant_white_24dp));
                    isStatusRestaurant = true;
                } else {
                    nav_restaurant.setImageDrawable(getResources().getDrawable(R.drawable.ic_restaurant_blue_24dp));
                    isStatusRestaurant = false;
                    for (int i = 0; i < resPlaces.size(); i++) {
                        resPlaces.get(i).remove();
                    }
                    resPlaces.clear();
                }
                myDrawer.closeDrawer(GravityCompat.END);
            }
        });
        nav_gas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStatusGas) {
                    findNearbyPlaces(gasPlaces, "gas_station");
                    nav_gas.setImageDrawable(getResources().getDrawable(R.drawable.ic_local_gas_station_white_24dp));
                    isStatusGas = true;
                } else {
                    nav_gas.setImageDrawable(getResources().getDrawable(R.drawable.ic_local_gas_station_blue_24dp));
                    isStatusGas = false;
                    for (int i = 0; i < gasPlaces.size(); i++) {
                        gasPlaces.get(i).remove();
                    }
                    gasPlaces.clear();
                }
                myDrawer.closeDrawer(GravityCompat.END);
            }
        });
        nav_hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStatusHotel) {
                    findNearbyPlaces(hotelPlaces, "hotel");
                    nav_hotel.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_white_24dp));
                    isStatusHotel = true;
                } else {
                    nav_hotel.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_blue_24dp));
                    isStatusHotel = false;
                    for (int i = 0; i < hotelPlaces.size(); i++) {
                        hotelPlaces.get(i).remove();
                    }
                    hotelPlaces.clear();
                }
                myDrawer.closeDrawer(GravityCompat.END);
            }
        });
        nav_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.child("SOS").push().setValue(mAuth.getCurrentUser().getUid());
                isStatus = false;
                myDrawer.closeDrawer(GravityCompat.END);
            }
        });

        nav_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStatusFollow) {
                    nav_follow.setText("Following");
                    isStatusFollow = true;
                    myRef.child("Groups").child(toolbar.getSubtitle().toString()).child("host").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String s = dataSnapshot.getValue(String.class);
                            if (s.contains(mAuth.getCurrentUser().getUid())) {
                                myRef.child("Groups").child(toolbar.getSubtitle().toString()).child("members").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String s = dataSnapshot.getValue(String.class);
                                        String[] a = s.split(" ");
                                        Double lat = Double.parseDouble(a[0]);
                                        Double longg = Double.parseDouble(a[1]);

                                        if (polylinePlace.size() > 0) {
                                            for (int i = 0; i < polylinePlace.size(); i++) {
                                                polylinePlace.get(i).remove();
                                            }
                                            randomMarker.get(0).remove();
                                            randomMarker.clear();
                                            polylinePlace.clear();
                                        }
                                        LatLng latLng1 = new LatLng(lat,longg);
                                        Object dataTransfer[] = new Object[5];
                                        String url = getDirectionUrl(latLng1);
                                        DirectionData directionData = new DirectionData();
                                        dataTransfer[0] = mMap;
                                        dataTransfer[1] = url;
                                        dataTransfer[2] = latLng1;
                                        dataTransfer[3] = polylinePlace;
                                        dataTransfer[4] = randomMarker;

                                        directionData.execute(dataTransfer);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    nav_follow.setText("Follow");
                    isStatusFollow = false;
                }
                myDrawer.closeDrawer(GravityCompat.END);
            }
        });
    }

    private void onLoadMember(final String name) {
        NavigationView myNav2 = findViewById(R.id.myNav2);
        myNav2.setPadding(0, onGetHeightStatus(), 0, 0);
        RecyclerView nav_rcvListMember = findViewById(R.id.nav_rcvListMember);
        nav_rcvListMember.setHasFixedSize(true);
        nav_rcvListMember.setLayoutManager(new LinearLayoutManager(this));

        Query query = myRef.child("Groups/" + name + "/members");
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>().setQuery(query, String.class).build();
        adapter_member = new FirebaseRecyclerAdapter<String, ViewHolderMember>(options) {

            @Override
            public ViewHolderMember onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
                return new ViewHolderMember(view);
            }

            @Override
            protected void onBindViewHolder(final ViewHolderMember holder, int position, final String model) {
                myRef.child("Groups/" + name + "/members").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue().toString().contains(model)) {
                            myRef.child("Users/" + dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    holder.item_name.setText(user.getFullname());
                                    if (TextUtils.isEmpty(user.getAvatar())) {
                                        user.setAvatar("a");
                                    } else {
                                        Picasso.with(MainActivity.this).load(user.getAvatar()).placeholder(R.drawable.defaut_avatar).into(holder.item_avatar);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        if (isLongClick) {
                            myDrawer.closeDrawer(GravityCompat.END);
                        } else {
                            myRef.child("Groups/" + name + "/members").addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if (dataSnapshot.getValue().toString().contains(model)) {
                                        myRef.child("Users/" + dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                if (user.getUid().equals(mAuth.getCurrentUser().getUid())) {
//                                                    Toast.makeText(MainActivity.this, "Can't beacasue that's you!", Toast.LENGTH_SHORT).show();
//                                                    Marker marker = hashMap.get(user.getUid());
                                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).zoom(15).build();
                                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                } else {
//                                                    Toast.makeText(MainActivity.this, "" + user.getUid(), Toast.LENGTH_SHORT).show();
                                                    Marker marker = hashMap.get(user.getUid());

                                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)).zoom(15).build();
                                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                    marker.showInfoWindow();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            myDrawer.closeDrawer(GravityCompat.END);
                        }
                    }
                });


            }
        };

        nav_rcvListMember.setAdapter(adapter_member);
        adapter_member.startListening();
    }

    private String getUrl(double lat, double lng, String place) {
        StringBuilder builder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        builder.append(lat + "," + lng);
        builder.append("&radius=5000&type=" + place + "&key=AIzaSyBePIombi3MiNTVQRto5ZEI_lBAsjHBhPM");

        return builder.toString();
    }

    private void findNearbyPlaces(ArrayList<Marker> places, String placeId) {
        String url = getUrl(mLastLocation.getLatitude(), mLastLocation.getLongitude(), placeId);
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = places;

        GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
        getNearbyPlaces.execute(dataTransfer);
        Toast.makeText(MainActivity.this, "Showing nearby " + placeId + "", Toast.LENGTH_SHORT).show();
    }

    private String getDirectionUrl(LatLng latLng) {
        StringBuilder builder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        builder.append("origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        builder.append("&destination=" + latLng.latitude + "," + latLng.longitude);
        builder.append("&key=AIzaSyC_xCz9Kx9M2oCctT6VRGs2k1lDqhgzlHk");

        return builder.toString();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (polylinePlace.size() > 0) {
            for (int i = 0; i < polylinePlace.size(); i++) {
                polylinePlace.get(i).remove();
                myRef.child("Groups").child(toolbar.getSubtitle().toString()).child("sharelocation").setValue(mLastLocation.getLatitude()+" " + mLastLocation.getLongitude());
            }
            randomMarker.get(0).remove();
            randomMarker.clear();
            polylinePlace.clear();
            mMap.clear();
        }

        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtvNameGroup, txtvQuantity, txtvFullname;
        Button btnDelete;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView) {
            super(itemView);
            txtvNameGroup = itemView.findViewById(R.id.txtvNameGroup);
            txtvQuantity = itemView.findViewById(R.id.txtvQuantity);
            txtvFullname = itemView.findViewById(R.id.txtvFullname);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }
    }

    public static class ViewHolderMember extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        CircleImageView item_avatar;
        TextView item_name;
        ItemClickListener itemClickListener;

        ViewHolderMember(View itemView) {
            super(itemView);
            item_avatar = itemView.findViewById(R.id.item_avatar);
            item_name = itemView.findViewById(R.id.item_name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }
    }
}
