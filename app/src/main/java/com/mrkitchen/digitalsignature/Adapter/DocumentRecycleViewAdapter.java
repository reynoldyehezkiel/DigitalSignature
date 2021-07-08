package com.mrkitchen.digitalsignature.Adapter;

import android.content.Context;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;

import com.mrkitchen.digitalsignature.R;

import java.io.File;
import java.text.*;
import java.util.*;

public class DocumentRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<File> items;

    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, File value, int position);

        void onItemLongClick(View view, File obj, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public DocumentRecycleViewAdapter(Context context, List<File> items) {
        this.items = items;
        ctx = context;
        selected_items = new SparseBooleanArray();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView brief;
        public TextView size;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.fileImageView);
            name = v.findViewById(R.id.fileItemTextview);
            brief = v.findViewById(R.id.dateItemTimeTextView);
            size = v.findViewById(R.id.sizeItemTimeTextView);
            lyt_parent = v.findViewById(R.id.listItemLinearLayout);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mainitemgrid, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final File obj = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.name.setText(obj.getName());
            Date lastModDate = new Date(obj.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String strDate = formatter.format(lastModDate);
            view.brief.setText(strDate);
            view.size.setText(GetSize(obj.length()));

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener == null) return;
                    mOnItemClickListener.onItemClick(v, obj, position);
                }
            });
            view.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemClickListener == null) return false;
                    mOnItemClickListener.onItemLongClick(v, obj, position);
                    return true;
                }
            });
            toggleCheckedIcon(holder, position);
            view.image.setImageResource(R.drawable.ic_pdf);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private void toggleCheckedIcon(RecyclerView.ViewHolder holder, int position) {
        OriginalViewHolder view = (OriginalViewHolder) holder;
        if (selected_items.get(position, false)) {
            view.lyt_parent.setBackgroundColor(Color.parseColor("#4A32740A"));
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            view.lyt_parent.setBackgroundColor(Color.parseColor("#ffffff"));
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }


    public String GetSize(long size) {
        String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int index = 0;
        double m = size;
        DecimalFormat dec = new DecimalFormat("0.00");
        for (index = 0; index < dictionary.length; index++) {
            if (m < 1024) {
                break;
            }
            m = m / 1024;
        }
        return dec.format(m).concat(" " + dictionary[index]);

    }


    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }

}