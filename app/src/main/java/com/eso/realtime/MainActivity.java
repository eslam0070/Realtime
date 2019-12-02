package com.eso.realtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.eso.realtime.models.User;
import com.eso.realtime.ui.HomeActivity;
import com.eso.realtime.unit.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    DatabaseReference user_information;
    private static final int MY_REQUEST_CODE = 7117;
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paper.init(this);

        //init firebase
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        //init provider
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        //Request permission location
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        showSignInOption();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toasty.error(MainActivity.this, response.getPermissionName(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void showSignInOption() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build(), MY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                //Check if user exists on Database
                user_information.orderByKey().equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //if user is not exists
                                if (dataSnapshot.getValue() == null) {
                                    //if key uid is not exists
                                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()) {
                                        Common.loggedUser = new User(firebaseUser.getUid(), firebaseUser.getEmail());
                                        //Add to database
                                        user_information.child(Common.loggedUser.getUid())
                                                .setValue(Common.loggedUser);
                                    }
                                } else // if user is available
                                    {
                                        Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }
                                //Save UID to storage to update location from background
                                Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                updateToken(firebaseUser);
                                setupUI();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toasty.error(MainActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void setupUI() {
        //Navigation Home
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference token  = FirebaseDatabase.getInstance().getReference(Common.TOKENS);
        //Get Token
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token.child(firebaseUser.getUid()).setValue(instanceIdResult.getToken());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}