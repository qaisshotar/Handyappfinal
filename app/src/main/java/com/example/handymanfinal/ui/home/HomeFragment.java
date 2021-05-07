package com.example.handymanfinal.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.handymanfinal.Common;
import com.example.handymanfinal.Model.EventBus.WorkerRequestReceived;
import com.example.handymanfinal.R;
import com.example.handymanfinal.Remote.IgoogleAPI;
import com.example.handymanfinal.Remote.RetrofitClient;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    @BindView(R.id.chip_decline)
    Chip chip_decline;
    @BindView(R.id.layout_accept)
    CardView layout_accept;
    @BindView(R.id.circularProgressbar)
    CircularProgressBar circularProgressBar;
    @BindView(R.id.txt_estimate_time)
    TextView txt_estimate_time;
    @BindView(R.id.txt_estimate_distance)
    TextView txt_estimate_distance;



    @OnClick(R.id.chip_decline)
    void onDeclineClick(){
        if (workerRequestReceived!=null)
        {
if (countDownEvent!=null)
    countDownEvent.dispose();
chip_decline.setVisibility(View.GONE);
layout_accept.setVisibility(View.GONE);
mMap.clear();
            //UserUtils.eclineRequest(root_layout,getContext(),workerRequestReceived.getKey());
            workerRequestReceived= null;
        }
    }


    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IgoogleAPI igoogleAPI;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;
    private List < LatLng > polylineList;

    private WorkerRequestReceived workerRequestReceived;
    private Disposable countDownEvent;


    private GoogleMap mMap;
    private HomeViewModel homeViewModel;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;

    DatabaseReference onlineRef, currentUserRef, workerLocation;
    GeoFire geoFire;
    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists())
                currentUserRef.onDisconnect().removeValue();

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);

        if (EventBus.getDefault().hasSubscriberForEvent(WorkerRequestReceived.class))
            EventBus.getDefault().removeStickyEvent(WorkerRequestReceived.class);
        EventBus.getDefault().unregister(this);

        compositeDisposable.clear();


        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
      EventBus.getDefault().register(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(root);
        init();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    private void initViews(View root) {
        ButterKnife.bind(this, root);
    }

    private void init() {

        igoogleAPI= RetrofitClient.getInstance().create(IgoogleAPI.class);
        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(10);
        locationRequest.setFastestInterval(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // fire
        onlineRef = FirebaseDatabase.getInstance().getReference().child("info/connected");
        workerLocation = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCES);
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCES).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(workerLocation);
        registerOnlineSystem();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    LatLng newposition = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLatitude());
                    Location location = new Location(locationResult.getLastLocation().getProvider());
                    location.setLatitude(locationResult.getLastLocation().getLatitude());
                    location.setLongitude(locationResult.getLastLocation().getLongitude());
                    onLocationChanged(location);
                    Toast.makeText(getContext(), "Location " + newposition, Toast.LENGTH_SHORT).show();

                } else {
                    Snackbar.make(mapFragment.getView(), "Location Null!", Snackbar.LENGTH_LONG).show();

                }
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()),
                        (key, error) -> {
                            if (error != null)
                                Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(mapFragment.getView(), "you're online ", Snackbar.LENGTH_LONG).show();

                        });
            }


        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));

        mMap.setOnMyLocationButtonClickListener(() -> {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            LatLng userlating = new LatLng(location.getLatitude(), location.getLatitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlating, 18f));
            return true;
        });

        Toast.makeText(getContext(), "Location Changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        } else {
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            View locationbutton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                    .getParent()).findViewById(Integer.parseInt("2"));

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();

                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                            params.setMargins(0, 0, 0, 50);

                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(), "permission" + permissionDeniedResponse.getPermissionName() + "" + "was denied",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.handy_maps_style));
            if (!success)
                Log.e("EDMT_ERROR", "Style parsing error");

        } catch (Resources.NotFoundException e) {
            Log.e("EDMT_ERROR", e.getMessage());
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onWorkerRequestReceived(WorkerRequestReceived event) {

         workerRequestReceived = event;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               Snackbar.make(requireView(),getString(R.string.permission_require),Snackbar.LENGTH_LONG).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show()).addOnSuccessListener(location -> {


                    compositeDisposable.add((Disposable) igoogleAPI.getDirections("driving","less_driving",
                            new StringBuilder()
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLatitude())
                            .toString(),
                            event.getArivelocation(),
                            getString(R.string.google_api_key))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(returnResult-> {
                                try {
                                    JSONObject jsonObject = new JSONObject(returnResult);
                                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject route = jsonArray.getJSONObject(i);
                                        JSONObject poly = route.getJSONObject("overview_polyline");
                                        String polyLine = poly.getString("points");
                                        polylineList = Common.decodepoly(polyLine);
                                    }
                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(android.R.color.black);
                                    polylineOptions.width(12);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polylineList);
                                    greyPolyLine = mMap.addPolyline(polylineOptions);

                                    blackPolylineOptions = new PolylineOptions();
                                    blackPolylineOptions.color(Color.BLACK);
                                    blackPolylineOptions.width(5);
                                    blackPolylineOptions.startCap(new SquareCap());
                                    blackPolylineOptions.jointType(JointType.ROUND);
                                    blackPolylineOptions.addAll(polylineList);
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                                    valueAnimator.setDuration(1100);
                                    valueAnimator.setRepeatCount(valueAnimator.INFINITE);
                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(value -> {

                                        List < LatLng > points = greyPolyLine.getPoints();
                                        int percentValue = (int) value.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List < LatLng > p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    });
                                    valueAnimator.start();

                                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                                  LatLng destination = new LatLng(Double.parseDouble(event.getArivelocation().split(",")[0]),
                                            Double.parseDouble(event.getArivelocation().split(",")[1]));


                                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                            .include(origin)
                                            .include(destination)
                                            .build();

                                    JSONObject object = jsonArray.getJSONObject(0);
                                    JSONArray legs = object.getJSONArray("legs");
                                    JSONObject legObject = legs.getJSONObject(0);

                                    JSONObject time = legObject.getJSONObject("duration");
                                    String duration = time.getString("text");

                                    JSONObject distanceEstimate = legObject.getJSONObject("distance");
                                    String distance = distanceEstimate.getString("text");

                                    txt_estimate_time.setText(duration);
                                    txt_estimate_distance.setText(distance);

                                    mMap.addMarker(new MarkerOptions()
                                            .position(destination)
                                            .icon(BitmapDescriptorFactory.defaultMarker())
                                            .title("arive location"));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));


                                    chip_decline.setVisibility(View.VISIBLE);
                                    layout_accept.setVisibility(View.VISIBLE);

                                    Observable.interval(100, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(x -> {
                                                circularProgressBar.setProgress(circularProgressBar.getProgress() + 1f);
                                            })
                                            .takeUntil(aLong -> aLong == 100)
                                            .doOnComplete(() -> {
                                                Toast.makeText(getContext(), "unreal accept", Toast.LENGTH_SHORT).show();
                                            }).subscribe();
                                } catch (Exception e)
                                {

                                    Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            })); })
        ;}}







