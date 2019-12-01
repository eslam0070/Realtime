package com.eso.realtime.ui;

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
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import com.eso.realtime.R;
import com.eso.realtime.ViewHolder.UserViewHolder;
import com.eso.realtime.interfaces.IFirebaseLoadDone;
import com.eso.realtime.interfaces.IRecyclerItemClickListener;
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
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    IFirebaseLoadDone firebaseLoadDone;
    RecyclerView recycler_all_user;
    MaterialSearchBar mMaterialSearchBar;
    List<String> suggestList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);
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

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull User user) {
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

    @Override
    protected void onStop() {
        if (adapter != null)
            adapter.stopListening();
        else if (searchAdapter != null)
            searchAdapter.stopListening();
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

    private void startSearch(String text_search) {
        Query query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
                .orderByChild("name")
                .startAt(text_search);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull User user) {
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
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> istEmail) {
        mMaterialSearchBar.setLastSuggestions(istEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toasty.error(this,message,Toast.LENGTH_SHORT).show();
    }
}
