package com.llk.recycler.drag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class DraggableLayout extends LinearLayout implements DragSource, DropTarget {

    private ImageView image;
    private TextView text;
    private int cellNumber;
    private AppItem gridItem;
    private DragListener listener;
    private boolean isDelete;

    public DraggableLayout(Context context, AttributeSet attrs,
                           int defStyle) {
        super(context, attrs, defStyle);
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableLayout(Context context) {
        super(context);
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset,
                       int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset,
                            int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset,
                           int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset,
                           int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
                              int yOffset, DragView dragView, Object dragInfo) {
        return (cellNumber >= 0) && source != this;
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y,
                                     int xOffset, int yOffset, DragView dragView, Object dragInfo,
                                     Rect recycle) {
        return null;
    }

    @Override
    public void setDragController(DragController dragger) {
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
        if (listener != null) {
            listener.onDropCompleted(this, target, success);
        }
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public ImageView getImage() {
        return image;
    }


    public void setText(TextView text) {
        this.text = text;
    }

    public TextView getText() {
        return text;
    }

    public void setItem(AppItem gridItem) {
        this.gridItem = gridItem;
    }

    public AppItem getItem() {
        return this.gridItem;
    }

    public void setDragListener(DragListener listener) {
        this.listener = listener;
    }


    @Override
    public boolean isDelete() {
        return isDelete;
    }

    public void canDelete(boolean b) {
        this.isDelete = b;
    }


}
