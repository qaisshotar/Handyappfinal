package com.example.handymanfinal.Model.EventBus;

public class WorkerRequestReceived {
    private String key;
    private String arivelocation;

    public WorkerRequestReceived(String key, String arivelocation) {
        this.key = key;
        this.arivelocation = arivelocation;

    }
public String  getKey(){
        return key;
    }
    public void setKey(String key){
        this.key=key;
    }
        public String getArivelocation(){
        return arivelocation;
        }
public  void setArivelocation(String arivelocation){
        this.arivelocation =arivelocation;

}

}
