package com.example.handymanfinal;

import com.example.handymanfinal.Model.WorkerInfoModel;


public class Common {
    public static final String WORKER_INFO_REFERENCE="WorkerInfo";
    public static final String WORKER_LOCATION_REFERENCES ="Workerlocation";

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
}
