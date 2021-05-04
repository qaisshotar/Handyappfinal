package com.example.handymanfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

public class WotkerhomeActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;

private DrawerLayout drawer;
private NavigationView navigationView;
private NavController navController;

private AlertDialog waitingDialog;
private  StorageReference storageReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wotkerhome);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("ABC");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



         drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                //.setDrawerLayout(drawer)
                .build();
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);




        init();

    }

    private void init() {
        waitingDialog =new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("waiting")
                .create();
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId()==R.id.nav_sign_out)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(WotkerhomeActivity.this);
                builder.setTitle("sign out")
                        .setMessage("Do you really want to sign out?")
                        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("SIGN OUT", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(WotkerhomeActivity.this,SplashScreenActivity.class);
                            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK );
                            startActivity(intent);
                            finish();




                        })
                        .setCancelable(false);
                AlertDialog dialog =builder.create();
                dialog.setOnShowListener(dialog1 -> {

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(R.color.colorAccent));

                });
                dialog.show();
            }


            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_name =(TextView)headerView.findViewById(R.id.text_name);
        TextView txt_phone =(TextView)headerView.findViewById(R.id.text_phone);
        TextView txt_star=(TextView)headerView.findViewById(R.id.text_star);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser !=null? Common.currentUser.getPhoneNumber():"");
        txt_star.setText(Common.currentUser!=null? String.valueOf(Common.currentUser. getRating()):"0.0");



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wotkerhome, menu);
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}