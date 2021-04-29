package com.example.handymanfinal.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.handymanfinal.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HomeViewModel homeViewModel;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment;


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }




    @Override
    public void onResume() {
        super.onResume();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


       init();
       mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
       mapFragment.getMapAsync(this);
        return root;
   }

  private void init() {
       locationRequest= new LocationRequest();
       locationRequest.setSmallestDisplacement(10f);
       locationRequest.setInterval(50000);
       locationRequest.setFastestInterval(3000);
       locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    LatLng newposition = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLatitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newposition, 18f));
                    Toast.makeText(getContext(), "Location "+newposition, Toast.LENGTH_SHORT).show();

                } else{
                   Snackbar.make(mapFragment.getView(), "Location Null!", Snackbar.LENGTH_LONG).show();

                }
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
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {

                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return false;
                            }
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "a" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnSuccessListener(location -> {
                                        LatLng userlating = new LatLng(location.getLatitude(), location.getLatitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlating, 18f));

                                    });
                         return true;
                     });
                        View locationbutton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1"))
                             .getParent()).findViewById(Integer.parseInt("2"));

                        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams)locationbutton.getLayoutParams();;
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,0,50);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"permission"+permissionDeniedResponse.getPermissionName()+""+"was denied",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

            try {

                 boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.handy_maps_style));
                 if(!success)
                     Log.e("EDMT_ERROR","Style parsing error");

            }catch (Resources.NotFoundException e) {
                Log.e("EDMT_ERROR",e.getMessage());
            }


    }
}