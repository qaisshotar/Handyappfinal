package com.example.handymanfinal.Utils;

import android.content.Context;
import android.widget.Toast;

import com.example.handymanfinal.Common;
import com.example.handymanfinal.Model.TokenModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UserUtils {
    public static void updateToken(Context context, String token) {

        TokenModel tokenModel = new TokenModel(token);

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid -> {

                });

    }
}
