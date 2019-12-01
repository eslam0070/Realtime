package com.eso.realtime.interfaces;

import java.util.List;

public interface IFirebaseLoadDone {

    void onFirebaseLoadUserNameDone(List<String> istEmail);
    void onFirebaseLoadFailed(String message);
}
