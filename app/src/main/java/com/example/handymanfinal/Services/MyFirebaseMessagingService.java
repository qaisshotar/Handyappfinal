package com.example.handymanfinal.Services;

import androidx.annotation.NonNull;

import com.example.handymanfinal.Common;
import com.example.handymanfinal.Model.EventBus.WorkerRequestReceived;
import com.example.handymanfinal.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

       @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
            UserUtils.updateToken(this,s);
    }

   @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
      super.onMessageReceived(remoteMessage);
      Map<String,String>dataRecv = remoteMessage.getData();
     if (dataRecv!= null)
     {
         if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUSET_WORKER_TITLE))
         {
             EventBus.getDefault().postSticky(new WorkerRequestReceived(
              dataRecv.get(Common.USER_KEY),
               dataRecv.get(Common.USER_ARIVE_LOCATION))
               );

            }
         else {
             Common.showNotification(this,new Random().nextInt(),
              dataRecv.get(Common.NOTI_TITLE),
              dataRecv.get(Common.NOTI_CONTENT),
                    null);

            }}
    }
}
