package com.llk.d.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.llk.d.Lg;
import com.llk.d.pagerecycler.ScrollController;

import java.util.ArrayList;

public class DragController {
    private static final String TAG = "DragController";

    /**
     * Indicates the drag is a move.
     */
    public static int DRAG_ACTION_MOVE = 0;
    /**
     * Indicates the drag is a copy.
     */
    public static int DRAG_ACTION_COPY = 1;

    //测试性能开关
    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private Context mContext;

    //振动器
    //private Vibrator mVibrator;
    //震动时长
    //private static final int VIBRATE_DURATION = 35;

    // 临时变量
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /**
     * 是否在拖动中
     */
    private boolean isDragging;

    /**
     * 按下的x, y
     */
    private float mMotionDownX;
    private float mMotionDownY;

    /**
     * Info about the screen for clamping.
     */
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    /**
     * 拖拽view
     */
    private View mOriginator;

    /**
     * X offset from the upper-left corner of the cell to where we touched.
     */
    private float mTouchOffsetX;
    /**
     * Y offset from the upper-left corner of the cell to where we touched.
     */
    private float mTouchOffsetY;

    /**
     * 拖拽的来源
     */
    private DragSource dragSource;

    /**
     * 拖拽的相关信息
     */
    private Object dragInfo;

    /**
     * 跟着你手指移动的view
     */
    private DragView dragView;

    /**
     * Who can receive drop events
     */
    private ArrayList<DropTarget> dropTargets = new ArrayList<>();

    private DraggingListener mListener;

    /**
     * The window token used as the parent for the DragView.
     */
    private IBinder mWindowToken;

    private View mMoveTarget;

    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;

    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DraggingListener {

        /**
         * A drag has begun
         * @param source     An object representing where the drag originated
         * @param info       The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *                   or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has eneded
         */
        void onDragEnd();
    }

    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public DragController(Context context) {
        mContext = context;
        //mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Starts a drag.
     * @param v          拖拽的view
     * @param source     拖拽的来源
     * @param dragInfo   与被拖动的对象相关的数据
     * @param dragAction 动作 {@link #DRAG_ACTION_MOVE} or {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {
        mOriginator = v;

        //生成一个bitmap
        Bitmap bitmap = getViewBitmap(v);
        if (bitmap == null) {
            return;
        }

        int[] loc = mCoordinatesTemp;
        //获取当前view的x, y
        v.getLocationOnScreen(loc);
        int screenX = loc[0];
        int screenY = loc[1];

        startDrag(bitmap,
                screenX, screenY,
                0, 0, bitmap.getWidth(), bitmap.getHeight(),
                source, dragInfo, dragAction);
        Lg.e("**************** source: " + source + " dragInfo: " + dragInfo);

        bitmap.recycle();

        //在这里，拖动的view会被隐藏
        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Starts a drag.
     *
     * @param bitmap        拖拽图像位图显示
     * @param screenX       The x position on screen of the left-top of the bitmap.
     * @param screenY       The y position on screen of the left-top of the bitmap.
     * @param textureLeft   The left edge of the region inside b to use.
     * @param textureTop    The top edge of the region inside b to use.
     * @param textureWidth  The width of the region inside b to use.
     * @param textureHeight The height of the region inside b to use.
     * @param source        An object representing where the drag originated
     * @param dragInfo      The data associated with the object that is being dragged
     * @param dragAction    The drag action: either {@link #DRAG_ACTION_MOVE} or
     *                      {@link #DRAG_ACTION_COPY}
     */
    public void startDrag(Bitmap bitmap, int screenX, int screenY,
                          int textureLeft, int textureTop, int textureWidth, int textureHeight,
                          DragSource source, Object dragInfo, int dragAction) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);

        if (mListener != null) {
            mListener.onDragStart(source, dragInfo, dragAction);
        }

        int registrationX = ((int) mMotionDownX) - screenX;
        int registrationY = ((int) mMotionDownY) - screenY;

//        Lg.e(String.format("mMotionDownX= %f, mMotionDownY= %f, screenX= %d, screenY= %d"
//                ,mMotionDownX, mMotionDownY, screenX, screenY));

        mTouchOffsetX = mMotionDownX - screenX;
        mTouchOffsetY = mMotionDownY - screenY;

        isDragging = true;
        dragSource = source;
        this.dragInfo = dragInfo;

        //震动
        //mVibrator.vibrate(VIBRATE_DURATION);

        DragView dragView = new DragView(mContext, bitmap,
                registrationX, registrationY,
                textureLeft, textureTop,
                textureWidth, textureHeight);
        this.dragView = dragView;

        dragView.show(mWindowToken, (int) mMotionDownX, (int) mMotionDownY);
    }

    /**
     * Stop dragging.
     */
    private void endDrag() {
        if (isDragging) {
            isDragging = false;
            if (mOriginator != null) {
                mOriginator.setVisibility(View.VISIBLE);
            }
            if (mListener != null) {
                mListener.onDragEnd();
            }
            if (dragView != null) {
                dragView.remove();
                dragView = null;
            }
        }

        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.stopMethodTracing();
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        //Lg.e("DragController onInterceptTouchEvent");

        if (action == MotionEvent.ACTION_DOWN) {
            //恢复一下displayMetrics屏幕参数值
            recordScreenSize();
        }

        //获取按下的坐标
        final int downX = clamp((int) ev.getRawX(), 0, displayMetrics.widthPixels);
        final int downY = clamp((int) ev.getRawY(), 0, displayMetrics.heightPixels);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = downX;
                mMotionDownY = downY;
                mLastDropTarget = null;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    drop(downX, downY);
                }
                endDrag();
                break;
        }

        return isDragging;
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        //Lg.e("DragController onTouchEvent");

        if (!isDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int downX = clamp((int) ev.getRawX(), 0, displayMetrics.widthPixels);
        final int downY = clamp((int) ev.getRawY(), 0, displayMetrics.heightPixels);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = downX;
                mMotionDownY = downY;
                break;
            case MotionEvent.ACTION_MOVE:
                //移动拖拽视图
                dragView.move((int) ev.getRawX(), (int) ev.getRawY());

                //左右滑屏
                monitorPageTurning(ev, displayMetrics, dragView);

                // Drop on someone?
                final int[] coordinates = mCoordinatesTemp;
                //Lg.e(String.format("coordinates x= %d, y= %d", coordinates[0], coordinates[1]));
                DropTarget dropTarget = findDropTarget(downX, downY, coordinates);

                if (dropTarget != null) {
                    if (mLastDropTarget == dropTarget) {
                        dropTarget.onDragOver(dragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);
                    } else {
                        if (mLastDropTarget != null) {
                            mLastDropTarget.onDragExit(dragSource, coordinates[0], coordinates[1],
                                    (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);
                        }

                        dropTarget.onDragEnter(dragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);
                    }
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(dragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);
                    }
                }

                mLastDropTarget = dropTarget;
                break;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    drop(downX, downY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                endDrag();
        }

        return true;
    }

    private void monitorPageTurning(MotionEvent ev, DisplayMetrics metrics, DragView itemView) {
        if(isDragging){
            //右边有效翻页区域（不能是屏幕宽度，因为getRawX到达不了这个宽度值）
            int rightValidZone = metrics.widthPixels - VALID_ZONE;
            //左边有效翻页区域
            int leftValidZone = VALID_ZONE;

            //右翻页
            if(rightValidZone <= ev.getRawX()){
                if(!isRightControlPageTurn){
                    handler.sendEmptyMessageDelayed(1, REMAIN_TIME);
                    isRightControlPageTurn = true;
                }
            }else {
                handler.removeMessages(1);
                isRightControlPageTurn = false;
            }

            //左翻页
            if(leftValidZone >= ev.getRawX()){
                if(!isLeftControlPageTurn){
                    handler.sendEmptyMessageDelayed(2, REMAIN_TIME);
                    isLeftControlPageTurn = true;
                }
            }else {
                handler.removeMessages(2);
                isLeftControlPageTurn = false;
            }
        }
    }

    private static final int VALID_ZONE = 80;
    private static final int REMAIN_TIME = 500;
    private boolean isRightControlPageTurn = false;
    private boolean isLeftControlPageTurn = false;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Lg.e("Page Turning to right !!!");
                    scrollUtil.arrowScroll(false);
                    isRightControlPageTurn = false;
                    break;
                case 2:
                    Lg.e("Page Turning to left !!!");
                    scrollUtil.arrowScroll(true);
                    isLeftControlPageTurn = false;
                    break;
                default:
                    break;
            }
        }
    };

    private ScrollController scrollUtil;
    public void setScrollController(ScrollController controller){
        scrollUtil = controller;
    }


    /**
     * Call this from a drag source view like this:
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return dragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        Lg.e("DragController dispatchKeyEvent");
        return isDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    void setMoveTarget(View view) {
        mMoveTarget = view;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        Lg.e("DragLayer dispatchUnhandledMove focused= " + focused + " direction= " + direction);
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    //放下操作
    private boolean drop(float x, float y) {

        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        this.mListener.onDragEnd();
        if (dropTarget == null) {
            dropTarget = new DraggableLayout(this.mContext);
        }

        dropTarget.onDragExit(dragSource, coordinates[0], coordinates[1],
                (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);

        if (dropTarget.acceptDrop(dragSource, coordinates[0], coordinates[1],
                (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo))
        {
            dropTarget.onDrop(dragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, dragView, dragInfo);
            View v = (View) dropTarget;
            Lg.e("drop============== view: " + v);
            dragSource.onDropCompleted(v, true);
            return true;
        }
        else {
            dragSource.onDropCompleted((View) dropTarget, false);
            return true;
        }
    }

    /**
     * 寻找放下位置的目标
     * @param x
     * @param y
     * @param dropCoordinates
     * @return
     */
    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = this.dropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            final DropTarget target = dropTargets.get(i);
            target.getHitRect(r);
            target.getLocationOnScreen(dropCoordinates);
            r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
            if (r.contains(x, y)) {
                dropCoordinates[0] = x - dropCoordinates[0];
                dropCoordinates[1] = y - dropCoordinates[1];
                return target;
            }
        }
        return null;
    }

    /**
     * Get the screen size so we can clamp events to the screen size so even if
     * you drag off the edge of the screen, we find something.
     */
    private void recordScreenSize() {
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
    }

    /**
     * 防止坐标值越界
     */
    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val >= max) {
            return max - 1;
        } else {
            return val;
        }
    }

    /**
     * Draw the view into a bitmap.
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listener which will be notified when a drag starts or ends.
     */
    public void setDraggingListener(DraggingListener l) {
        mListener = l;
    }

    public DraggingListener getDraggingListener() {
        return mListener;
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        if(!dropTargets.contains(target)){
            dropTargets.add(target);
        }
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        dropTargets.remove(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeAllDropTargets() {
        dropTargets = new ArrayList<>();
    }

}
