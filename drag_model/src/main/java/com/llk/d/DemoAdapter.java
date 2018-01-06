package com.llk.d;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.llk.d.drag.AppItem;
import com.llk.d.drag.DragListener;
import com.llk.d.drag.DraggableLayout;

import java.util.ArrayList;

/**
 * (: Author：liangkai
 * (: WorkSpace: TAS
 * (: CreateDate: 2017/5/18
 * (: Describe:
 */

public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.BaseViewHolder> implements DragListener {

    private ArrayList<AppItem> mList = new ArrayList<>();
    private Context mContext;

    public DemoAdapter(ArrayList<AppItem> list, Context context){
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        return new BaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AppItem appItem = mList.get(position);
        holder.tv_title.setText(appItem.getText());
        holder.icon.setImageResource(appItem.getIcon());

        if(longClickListener != null){
            holder.layout.setOnLongClickListener(longClickListener);
        }
        if(clickListener != null){
            holder.layout.setOnClickListener(clickListener);
        }

        DraggableLayout layout = (DraggableLayout) holder.layout;
        AppItem item = mList.get(position);
        layout.setItem(item);
        layout.setImage(holder.icon);
        layout.setText(holder.tv_title);
        layout.canDelete(item.isDelete());
        layout.setDragListener(this);
    }

    private ItemDragListener dragListener = null;
    public interface ItemDragListener{
        void onDragStarted(View source);
        void onDropCompleted(View source, View target, boolean success);
    }

    public void setDragListener(ItemDragListener listener){
        dragListener = listener;
    }

    @Override
    public void onDragStarted(View source) {
        if(dragListener != null){
            dragListener.onDragStarted(source);
        }
    }

    @Override
    public void onDropCompleted(View source, View target, boolean success) {
        if(dragListener != null){
            dragListener.onDropCompleted(source, target, success);
        }
    }

    private View.OnLongClickListener longClickListener = null;
    public void setLongClickListener(View.OnLongClickListener listener){
        longClickListener = listener;
    }

    private View.OnClickListener clickListener = null;
    public void setClickListener(View.OnClickListener listener){
        clickListener = listener;
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    class BaseViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tv_title;
        LinearLayout layout;

        public BaseViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }
}
