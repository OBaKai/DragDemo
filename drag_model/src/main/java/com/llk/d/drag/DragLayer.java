package com.llk.d.drag;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.llk.d.drag.del.DeleteZone;


public class DragLayer extends LinearLayout implements DragController.DraggingListener{
    DragController mDragController;
    RecyclerView mView;
    private int deleteZoneId;
    private DragController.DraggingListener listener;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDragController(DragController controller) {
        mDragController = controller;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDragController.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mDragController.dispatchUnhandledMove(focused, direction);
    }


    public RecyclerView getDragView() {
        return mView;
    }

    public void setDragView(RecyclerView view) {
        mView = view;
    }

    /**
     * App Model use the method
     * @param view
     */
    public void loadChildView(View view){
        if(view != null){
            DropTarget dropTarget = (DropTarget)view;
            mDragController.addDropTarget(dropTarget);
        }
    }

    /**
     * A drag has begun.
     *
     * @param source     An object representing where the drag originated
     * @param info       The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
     *                   or {@link DragController#DRAG_ACTION_COPY}
     */

    public void onDragStart(DragSource source, Object info, int dragAction) {
        // We are starting a drag.
        // Build up a list of DropTargets from the child views of the GridView.
        // Tell the drag controller about them.
        if (mView != null) {
            //将所有child view存入drop target容器里面
            for(int i = 0; i < mView.getChildCount(); i++){
                DropTarget view = (DropTarget) mView.getChildAt(i);
                mDragController.addDropTarget(view);
            }
        }

        // Always add the delete_zone so there is a place to get rid of views.
        // Find the delete_zone and add it as a drop target.
        // That gives the user a place to drag views to get them off the screen.
        View v = findViewById(getDeleteZoneId());
        if (v != null) {
            DeleteZone dz = (DeleteZone) v;
            mDragController.addDropTarget(dz);
        }

        if (listener != null) {
            listener.onDragStart(source, info, dragAction);
        }

    }

    /**
     * A drag-drop operation has eneded.
     */
    public void onDragEnd() {
        mDragController.removeAllDropTargets();

    }

    public void setDeleteZone(DeleteZone deleteZone) {
        mDragController.addDropTarget(deleteZone);
    }

    public int getDeleteZoneId() {
        return deleteZoneId;
    }

    public void setDeleteZoneId(int deleteZoneId) {
        this.deleteZoneId = deleteZoneId;
    }

    public void setDraggingListener(DragController.DraggingListener listener) {
        this.listener = listener;
    }

    public DragController.DraggingListener getDraggingListener() {
        return this.listener;
    }
}
