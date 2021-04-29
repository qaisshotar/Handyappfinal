 package com.example.handymanfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.handymanfinal.Model.WorkerInfoModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

 public class SplashScreenActivity extends AppCompatActivity {

     private  final static int LOGIN_REQUEST_CODE=7171;
     private List<AuthUI.IdpConfig> provisder;
     private FirebaseAuth firebaseAuth;
     private FirebaseAuth.AuthStateListener listener;

     @BindView(R.id.progress_bar)
     ProgressBar progress_bar;

     FirebaseDatabase database;
     DatabaseReference workerInfoRef;


     @Override
     protected void onStart() {
         super.onStart();
         delaysplaSplashScreen();
     }

     @Override
     protected void onStop() {
         super.onStop();
         if(firebaseAuth !=null && listener !=null)
             firebaseAuth.removeAuthStateListener(listener);
     }

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash_screen);

        init();

    }

     private void init() {

         ButterKnife.bind(this);

         database = FirebaseDatabase.getInstance();
         workerInfoRef=database.getReference(Common.WORKER_INFO_REFERENCE);

         provisder = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
         firebaseAuth= FirebaseAuth.getInstance();
         listener = myFirebaseAuth ->{

             FirebaseUser user = myFirebaseAuth.getCurrentUser();
             if(user!=null)
             {
                 checkUserFromFirebase();
             }
             else
                 showLoginLayout();
         };

     }

     private void checkUserFromFirebase() {
         workerInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if(dataSnapshot.exists()){

                             //Toast.makeText(SplashScreenActivity.this, "user already register", Toast.LENGTH_SHORT).show();
                         WorkerInfoModel workerInfoModel = dataSnapshot.getValue(WorkerInfoModel.class);
                                goToHomeActivity(workerInfoModel);
                         }
                         else {
                             showRegisterLayout();


                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });

     }

     private void goToHomeActivity(WorkerInfoModel workerInfoModel) {
         Common.currentUser = workerInfoModel;
         startActivity(new Intent(SplashScreenActivity.this,WotkerhomeActivity.class));
         finish();
     }

     private void showRegisterLayout() {
         AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogTheme);
         View itemView=getLayoutInflater().from(this).inflate(R.layout.layout_register,null);
         TextInputEditText edt_first_name =(TextInputEditText)itemView.findViewById(R.id.edt_first_name);
         TextInputEditText edt_last_name =(TextInputEditText)itemView.findViewById(R.id.edt_last_name);
         TextInputEditText edt_phone =(TextInputEditText)itemView.findViewById(R.id.edt_phone_number);


         Button btn_continue = (Button)itemView.findViewById(R.id.btn_register);

         if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null&&
                 TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
         edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

         builder.setView(itemView);
         AlertDialog dialog =builder.create();
         dialog.show();

         btn_continue.setOnClickListener(v -> {
        if(TextUtils.isEmpty(edt_first_name.getText().toString())){

            Toast.makeText(this, "please enter first name", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            if(TextUtils.isEmpty(edt_last_name.getText().toString())) {

                Toast.makeText(this, "please enter last name", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                if(TextUtils.isEmpty(edt_phone.getText().toString())) {

                    Toast.makeText(this, "please enter phone number", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                else {

                    WorkerInfoModel model = new WorkerInfoModel();
                    model.setFirstName(edt_first_name.getText().toString());
                    model.setLastName(edt_last_name.getText().toString());
                    model.setPhoneNumber(edt_phone.getText().toString());
                    model.setRating(0.0);
                    workerInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(model)
                            .addOnFailureListener(e ->
                            {
                                dialog.dismiss();
                                Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Register Succesfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                goToHomeActivity(model);

                            });
                }


            }

        }

         });
     }

     private void showLoginLayout() {
         AuthMethodPickerLayout authMethodPickerLayout=new AuthMethodPickerLayout
                 .Builder(R.layout.layout_sign_in)
                 .setPhoneButtonId(R.id.btn_phone_sign_in)
                 .build();

         startActivityForResult(AuthUI.getInstance()
         .createSignInIntentBuilder()
         .setAuthMethodPickerLayout(authMethodPickerLayout)
         .setIsSmartLockEnabled(false)

         .setAvailableProviders(provisder)
         .build(),LOGIN_REQUEST_CODE);
     }

     private void delaysplaSplashScreen() {

         progress_bar.setVisibility(View.VISIBLE);

         Completable.timer(3, TimeUnit.SECONDS,
                 AndroidSchedulers.mainThread())
                 .subscribe(() ->

                         firebaseAuth.addAuthStateListener(listener)
                         );
                 }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == LOGIN_REQUEST_CODE)
         {
             IdpResponse response = IdpResponse.fromResultIntent(data);
             if (resultCode==RESULT_OK)
             {
                 FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();

             }

             else 
             {
                Toast.makeText(this, "[ERORR]:"+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                 
             }
         }
     }
 }