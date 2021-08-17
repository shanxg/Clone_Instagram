package com.example.cloneinstagram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloneinstagram.R;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class AdapterImageFilters extends RecyclerView.Adapter<AdapterImageFilters.ViewHolderFilters> {


    private List<ThumbnailItem> filters;
    private Context context;

    public AdapterImageFilters(List<ThumbnailItem> filters, Context context) {
        this.filters = filters;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderFilters onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_filters, parent, false);

        return new ViewHolderFilters(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFilters holder, int position) {

        ThumbnailItem item = filters.get(position);

        holder.filterText3.setText(item.filterName);
        holder.filterView3.setImageBitmap(item.image);

    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public class ViewHolderFilters extends RecyclerView.ViewHolder {

        private ImageView filterView3;
        private TextView filterText3;

        public ViewHolderFilters(@NonNull View itemView) {
            super(itemView);

            filterView3 = itemView.findViewById(R.id.filterView3);
            filterText3 = itemView.findViewById(R.id.filterText3);
        }

    }
}