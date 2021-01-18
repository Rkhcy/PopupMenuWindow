package com.hu.hcy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<String> mDataList;
    private OnItemLongClickListener mOnItemLongClickListener;

    public MyAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mDataList = list;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_content, parent, false));
    }

    @Override
    public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
        holder.mTv.setText(mDataList.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, position);
            }
            return false;
        });
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTv;
        public MyViewHolder(View itemView) {
            super(itemView);
            mTv = itemView.findViewById(R.id.textView);
        }
    }
}
