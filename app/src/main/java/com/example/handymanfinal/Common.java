package com.example.handymanfinal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.handymanfinal.Model.WorkerInfoModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class Common {
    public static final String WORKER_INFO_REFERENCE="WorkerInfo";
    public static final String WORKER_LOCATION_REFERENCES ="Workerlocation";
    public static final String TOKEN_REFERENCE ="token" ;
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT ="body" ;



    public static final String USER_ARIVE_LOCATION="ariveLocation" ;
    public static final String USER_KEY ="UserKey" ;
    public static final String REQUSET_WORKER_TITLE ="RequestWorker" ;



    public static WorkerInfoModel currentUser;
    public static String buildWelcomeMessage(){
        if (Common.currentUser !=null)
        {
            return new StringBuilder("Welcome")
                    .append(Common.currentUser.getFirstName())
                    .append("")
                    .append(Common.currentUser.getLastName()).toString();



        }
        else
            return "";

    }

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "handyman_bulid";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "handyman bulidup", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Hhandyman buildup");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(android.R.color.holo_red_dark);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_baseline_directions_car_24));

        if (pendingIntent != null)
        {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id,notification);







    }


    public static List< LatLng> decodepoly(String polyLine) {


        List poly = new ArrayList();
        int index=0,len= polyLine.length();
        int lat=0,lng=0;
        while(index < len)
        {
            int b,shift=0,result=0;
            do{
                b=polyLine.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;

            }while(b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do{
                b = polyLine.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift +=5;
            }while(b >= 0x20);
            int dlng = ((result & 1)!=0 ? ~(result >> 1): (result >> 1));
            lng +=dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;

    }
}
