package com.mhv.stepcounter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdaptor extends RecyclerView.Adapter<CustomAdaptor.MyViewHolder> {

    private Context context;
    private ArrayList id, steps, date;
    CustomAdaptor(Context context, ArrayList id, ArrayList steps, ArrayList date) {
        this.context = context;
        this.id = id;
        this.steps = steps;
        this.date = date;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.id_text.setText(String.valueOf(id.get(position)));
        holder.steps_text.setText(String.valueOf(steps.get(position)));
        holder.date_text.setText(String.valueOf(date.get(position)));
        holder.fix_text.setText("Steps");
    }

    @Override
    public int getItemCount() {
        return id.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView id_text, steps_text, date_text, fix_text;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            id_text = itemView.findViewById(R.id._id);
            steps_text = itemView.findViewById(R.id.steps);
            date_text = itemView.findViewById(R.id.date);
            fix_text = itemView.findViewById(R.id.fix);
        }
    }
}
