package com.eso.realtime.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.eso.realtime.R;
import com.eso.realtime.interfaces.IRecyclerItemClickListener;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textView;
    private IRecyclerItemClickListener iRecyclerItemClickListener;

    public void setiRecyclerItemClickListener(IRecyclerItemClickListener iRecyclerItemClickListener) {
        this.iRecyclerItemClickListener = iRecyclerItemClickListener;
    }

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.txt_user_email);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        iRecyclerItemClickListener.onItemClickListener(v,getAdapterPosition());
    }


}
