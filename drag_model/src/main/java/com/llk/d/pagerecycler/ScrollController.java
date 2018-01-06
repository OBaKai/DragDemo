package com.llk.d.pagerecycler;

import android.support.v7.widget.RecyclerView;
import android.util.Log;


/**
 * Description:分页滑动效果
 * author:wjc on 2017/4/20 11:46
 */

public class ScrollController {

    private RecyclerView mRecyclerView = null;
    private OnPageChangeListener mOnPageChangeListener;
    private int lastPageIndex = -1;
    //x 的偏移量
    private int offsetX = 0;
    private MyOnFlingListener mOnFlingListener = new MyOnFlingListener();
    private MyOnScollListener mMyOnScollListener = new MyOnScollListener();
    private RecyclerView.LayoutManager layoutManager;

    public void setUpRecycleView(RecyclerView recycleView) {
        if (recycleView == null) {
            throw new IllegalArgumentException("recycleView must be not null");
        }
        mRecyclerView = recycleView;
        //处理滑动
        recycleView.setOnFlingListener(mOnFlingListener);
        //设置滚动监听，记录滚动的状态，和总的偏移量
        recycleView.addOnScrollListener(mMyOnScollListener);

        layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            offsetX = 0;
        }
    }

    public int getCurrentPageIndex() {
        int viewWidth = mRecyclerView.getWidth();
        if (viewWidth == 0) return 0;
        return (offsetX + mRecyclerView.getWidth() / 2) / viewWidth;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public void smoothScrollToPage(int page) {
        smoothScrollToOffset(page * mRecyclerView.getWidth());
    }

    /**
     * true: 向左翻页, false: 向右翻页
     * @param isLeft
     */
    public void arrowScroll(boolean isLeft){
        if(isLeft){
            smoothScrollToPage(getCurrentPageIndex() - 1);
        }else {
            smoothScrollToPage(getCurrentPageIndex() + 1);
        }
    }

    private void smoothScrollToOffset(int endPoint) {
        if (endPoint < 0) {
            endPoint = 0;
        }
//        Lg.d("smoothScrollToOffset() called with: " + "endPoint = [" + endPoint + "]" +
//                " endPoint - offsetX:" + (endPoint - offsetX));
        Log.e("lk", "smoothScrollToOffset offsetX: " + offsetX);
        mRecyclerView.smoothScrollBy(endPoint - offsetX, 0);//, new DecelerateInterpolator(1)
    }


    private void notifyPageIndexChange() {
        if (getCurrentPageIndex() != lastPageIndex) {
            lastPageIndex = getCurrentPageIndex();
            if (null != mOnPageChangeListener) {
                mOnPageChangeListener.onPageChange(getCurrentPageIndex());
            }
        }
    }


    private void animateToCenter() {
        if (offsetX != mRecyclerView.getWidth() * getCurrentPageIndex()) {
            smoothScrollToOffset(mRecyclerView.getWidth() * getCurrentPageIndex());
        }
    }

    public interface OnPageChangeListener {
        void onPageChange(int index);
    }

    /**
     * return true ,自己处理滑动事件
     */
    class MyOnFlingListener extends RecyclerView.OnFlingListener {
        @Override
        public boolean onFling(int velocityX, int velocityY) {
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                return true;
            }
            int startScollPageIndex = getCurrentPageIndex();
            //记录滚动开始和结束的位置
            int endPoint = 0;
            if (velocityX < -1000) {//速度小，计算offesX是否过了一半
                startScollPageIndex--;
            } else if (velocityX > 1000) {//直接滚动到下一页
                startScollPageIndex++;
            }
            endPoint = startScollPageIndex * mRecyclerView.getWidth();
            if (endPoint < 0) {
                endPoint = 0;
            }

            Log.e("lk", String.format("vX:%d, vY:%d, viewWidth:%d, PageIndex:%d, endPoint:%d", velocityX
                    , velocityY, mRecyclerView.getWidth(), startScollPageIndex, endPoint));
            smoothScrollToOffset(endPoint);
            return true;
        }
    }

    /* An OnScrollListener can be added to a RecyclerView to receive messages
       when a scrolling event has occurred on that RecyclerView. */
    class MyOnScollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            //停止滚动的状态，或者滚动速度非常小，触发不了 onFling 时，自动设置
            //Lg.i("onScrollStateChanged() called with: newState = [" + newState + "]");
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                animateToCenter();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //滚动结束记录滚动的偏移量
            offsetX += dx;
            //Lg.v("onScrolled() " + " dx = [" + dx + "]" + ",offsetX=" + offsetX);
            notifyPageIndexChange();
        }
    }

}