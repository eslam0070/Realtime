package com.eso.realtime.unit;

import com.eso.realtime.Retrofit.IFCMService;
import com.eso.realtime.Retrofit.RetrofitClient;
import com.eso.realtime.models.User;

import retrofit2.Retrofit;

public class Common {
    public static final String USER_INFORMATION = "UserInformation";
    public static final String USER_UID_SAVE_KEY = "Save UID";
    public static final String TOKENS = "Tokens";
    public static final String FROM_NAME = "FromName";
    public static final String ACCEPT_LIST = "acceptList";
    public static final String FROM_UID = "FromUid";
    public static final String TO_UID = "ToUid";
    public static final String TO_NAME = "ToName";
    public static User loggedUser;


    public static IFCMService getIFCMService(){
        return RetrofitClient.getClient("https://fcm.googleapis.com/")
                .create(IFCMService.class);
    }
}
