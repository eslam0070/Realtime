package com.eso.realtime.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eso.realtime.R;
import com.eso.realtime.Retrofit.IFCMService;
import com.eso.realtime.ViewHolder.UserViewHolder;
import com.eso.realtime.interfaces.IFirebaseLoadDone;
import com.eso.realtime.interfaces.IRecyclerItemClickListener;
import com.eso.realtime.models.MyResponse;
import com.eso.realtime.models.Request;
import com.eso.realtime.models.User;
import com.eso.realtime.unit.Common;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    IFirebaseLoadDone firebaseLoadDone;
    RecyclerView recycler_all_user;
    MaterialSearchBar mMaterialSearchBar;
    List<String> suggestList = new ArrayList<>();
    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);
        ifcmService = Common.getIFCMService();
        mMaterialSearchBar = findViewById(R.id.material_search_bar);
        mMaterialSearchBar.setCardViewElevation(10);
        mMaterialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<>();
                for (String search : suggestList){
                    if (search.toLowerCase().contains(mMaterialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                mMaterialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mMaterialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled){
                    if (adapter != null)
                        //if close search restore default
                        recycler_all_user.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        recycler_all_user = findViewById(R.id.recycler_all_people);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
        firebaseLoadDone = this;
        loadUserList();
        loadSearchData();
    }


    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User user) {
                if (user.getEmail().equals(Common.loggedUser.getEmail())){
                    holder.textView.setText(new StringBuilder(user.getEmail()).append("  (me) "));
                    holder.textView.setTypeface(holder.textView.getTypeface(),Typeface.ITALIC);
                }else {
                    holder.textView.setText(new StringBuilder(user.getEmail()));
                }
                //Event
                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        showDialogRequest(user);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user,parent,false);
                return new UserViewHolder(view);
            }
        };
        adapter.startListening();
        recycler_all_user.setAdapter(adapter);
    }

    private void loadSearchData() {
        final List<String> istUserEmail = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    User user = userSnapshot.getValue(User.class);
                    istUserEmail.add(user.getEmail());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(istUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void startSearch(String text_search) {
        Query query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
                .orderByChild("name")
                .startAt(text_search);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                if (model.getEmail().equals(Common.loggedUser.getEmail())){
                    holder.textView.setText(new StringBuilder(model.getEmail()).append("  (me) "));
                    holder.textView.setTypeface(holder.textView.getTypeface(),Typeface.ITALIC);
                }else {
                    holder.textView.setText(new StringBuilder(model.getEmail()));
                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user,parent,false);
                return new UserViewHolder(view);
            }
        };
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
    }

    private void showDialogRequest(final User model) {
        AlertDialog.Builder al = new AlertDialog.Builder(this,R.style.MyRequestDialog);
        al.setTitle("Request Friend");
        al.setMessage("Do you want to send request friend to "+model.getEmail());
        al.setIcon(R.drawable.ic_account_circle_black_24dp);
        al.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        al.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Add to ACCEPT_LIST
                DatabaseReference acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
                        .child(Common.loggedUser.getUid())
                        .child(Common.ACCEPT_LIST);
                acceptList.orderByKey().equalTo(model.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null)
                                    sendFriendRequest(model);
                                else
                                    Toasty.warning(AllPeopleActivity.this,"You and "+model.getEmail()+" already are friend",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }
        });
        al.show();
    }

    private void sendFriendRequest(final User model) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS);
        tokens.orderByKey().equalTo(model.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null)
                            Toasty.error(AllPeopleActivity.this,"Token Error",Toast.LENGTH_SHORT).show();
                        else {
                            //Create Request
                            Request request  = new Request();
                            //Create data
                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put(Common.FROM_UID,Common.loggedUser.getUid());
                            dataSend.put(Common.FROM_NAME,Common.loggedUser.getEmail());
                            dataSend.put(Common.TO_UID,model.getUid());
                            dataSend.put(Common.TO_NAME,model.getEmail());
                            request.setTo(dataSnapshot.child(model.getUid()).getValue(String.class));
                            request.setData(dataSend);
                            //Send
                            compositeDisposable.add(ifcmService.sendFriendRequestToUser(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<MyResponse>() {
                                @Override
                                public void accept(MyResponse myResponse) {
                                    if (myResponse.success == 1)
                                        Toasty.success(AllPeopleActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>(){
                                @Override
                                public void accept(Throwable throwable) {
                                    Toasty.warning(AllPeopleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toasty.error(AllPeopleActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> istEmail) {
        mMaterialSearchBar.setLastSuggestions(istEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toasty.error(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        if (adapter != null)
            adapter.stopListening();
        else if (searchAdapter != null)
            searchAdapter.stopListening();

        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (adapter != null)
            adapter.startListening();
        else if (searchAdapter != null)
            searchAdapter.startListening();
        super.onResume();
    }

}
