package com.eso.realtime.Retrofit;

import com.eso.realtime.models.MyResponse;
import com.eso.realtime.models.Request;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({"Content-Type:application/json",
            "Authorization:key=AAAApCBnRy0:APA91bF8sIdTlCcqHIlENA6SaVg8z0AqAvPIvfOWQkhdksWaimYQIbnFHP3QzXESJncz7ci0n0TggMg3iO78ZoQMWduH2Si5xF10Cy6MgJvGyBQ1kSvHku0dpgLk53gFvXSEktWVOE-k"})
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
