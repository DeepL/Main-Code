package com.cms.demo.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by NoName on 5/14/2016.
 */
public class StoreTimeAdapter extends RecyclerView.Adapter<StoreTimeAdapter.MyViewHolder> {

    private List<StoreTime> timeList;
    int i=1;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time,location,message;

        public MyViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.time);
            location=(TextView)view.findViewById(R.id.location);
            message=(TextView)view.findViewById(R.id.display_punch);
        }
    }

    public StoreTimeAdapter(List<StoreTime> timeList) {
        this.timeList = timeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.store_time, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        StoreTime get_time = timeList.get(position);
       holder.time.setText("* "+get_time.getTitle());
        holder.location.setText("Address: "+get_time.getLocationName());
        holder.message.setText("  Message: "+get_time.getMessage());

    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

}
