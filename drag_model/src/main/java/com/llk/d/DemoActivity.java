package com.llk.d;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.llk.d.drag.AppItem;
import com.llk.d.drag.del.DeleteItemInterface;
import com.llk.d.drag.del.DeleteZone;
import com.llk.d.drag.DragController;
import com.llk.d.drag.DragLayer;
import com.llk.d.drag.DragSource;
import com.llk.d.drag.DraggableLayout;
import com.llk.d.pagerecycler.CircleIndicator;
import com.llk.d.pagerecycler.HorizontalPageLayoutManager;
import com.llk.d.pagerecycler.ScrollController;

import java.util.ArrayList;
import java.util.Collections;

/**
 * (: Author：liangkai
 * (: WorkSpace: TAS
 * (: CreateDate: 2017/5/18
 * (: Describe:
 */

public class DemoActivity extends Activity implements ScrollController.OnPageChangeListener, DragController.DraggingListener, DeleteItemInterface, View.OnLongClickListener, DemoAdapter.ItemDragListener {

    RecyclerView mListView = null;
    CircleIndicator mIndicator = null;
    ArrayList<AppItem> mList = new ArrayList<>();

    private HorizontalPageLayoutManager horizhontalPageLayoutManager;
    ScrollController mScrollController = new ScrollController();

    private DemoAdapter mAdapter = null;
    private int indicatorNumber;

    private DragLayer mDragLayer;
    private DeleteZone mDeleteZone;
    private DragController mDragController;
    private LinearLayout btnLayout = null;

    //行
    public static int mRow = 4;
    //列
    public static int mColumn = 3;

    public int pageSize = mRow * mColumn;

    /** 主动刷新替换源的数据(防止替换成功后源item没有刷新) */
    private int needUpdateReplaceDataPage = -1;

    public void clickAdd(View view){
        AppItem item = new AppItem("debug", R.drawable.ic_icon);
        item.itemPos = mList.size();
        mList.add(mList.size(), item);
        mAdapter.notifyDataSetChanged();

        updateIncatorNum();
    }

    private void updateIncatorNum() {
        int oldNum = indicatorNumber;

        int endPageIndex = oldNum -1;
        boolean isEnd = mScrollController.getCurrentPageIndex() == endPageIndex ? true : false;

        //refresh indicatorNumber
        indicatorNumber = (mList.size() / pageSize) + (mList.size() % pageSize == 0 ? 0 : 1);
        mIndicator.setNumber(indicatorNumber);

        if(indicatorNumber == oldNum + 1 && isEnd){
            mScrollController.arrowScroll(false);
        }else if(indicatorNumber == oldNum - 1 && isEnd){
            mScrollController.arrowScroll(true);
        }
    }

    public void clickRemove(View view){
        mList.remove(mList.size() -1);
        mAdapter.notifyDataSetChanged();

        updateIncatorNum();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initDebug();

        initDrag();

        initView();

        btnLayout = (LinearLayout) findViewById(R.id.layout_btn);
    }

    private void initDebug() {
        int size = 50;
        for (int i = 1; i <= size; i++) {
            int resId = getResources().getIdentifier("face_" + i,
                    "mipmap", getPackageName());
            AppItem item = new AppItem("item"+i, resId);
            item.itemPos = i -1;
            mList.add(item);
        }
    }

    @Override
    public void onPageChange(int index) {
        mIndicator.setOffset(index);

        if (needUpdateReplaceDataPage != -1 && index == needUpdateReplaceDataPage){
            mAdapter.notifyDataSetChanged();
            needUpdateReplaceDataPage = -1;
        }
    }

    private void initDrag() {
        mDragLayer = (DragLayer) findViewById(R.id.demo_draglayer);
        mDeleteZone = (DeleteZone) findViewById(R.id.demo_del_zone);
        mDragController = new DragController(this);

        //是为了把dragLayer里面的触摸、拦截事件传给dragController
        //把很多能力交给dragController处理
        mDragLayer.setDragController(mDragController);
        //设置监听
        mDragLayer.setDraggingListener(this);

        if (mDeleteZone != null) {
            mDeleteZone.setOnItemDeleted(this);
            mDeleteZone.setEnabled(true);
            mDragLayer.setDeleteZoneId(mDeleteZone.getId());
        }

        mDragController.setDraggingListener(mDragLayer);
        mDragController.setScrollController(mScrollController);
    }

    private boolean isEnableDrag = true;
    @Override
    public boolean onLongClick(View view) {
        if (!view.isInTouchMode() && isEnableDrag) {
            return false;
        }

        Lg.d("onLongClick ********************* Drag started");
        DragSource dragSource = (DragSource) view;
        mDragController.startDrag(view, dragSource, dragSource, DragController.DRAG_ACTION_MOVE);

        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mDeleteZone.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDragEnd() {
        mDeleteZone.setVisibility(View.GONE);
        btnLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void itemDeleted(DragSource source) {

    }

    private void initView() {
        mListView = (RecyclerView) findViewById(R.id.demo_listview);
        mIndicator = (CircleIndicator) findViewById(R.id.demo_indicator);
        mAdapter = new DemoAdapter(mList, this);
        mAdapter.setLongClickListener(this);
        mAdapter.setDragListener(this);
        mListView.setAdapter(mAdapter);

        horizhontalPageLayoutManager = new HorizontalPageLayoutManager(mColumn, mRow, this);
        horizhontalPageLayoutManager.setDragLayer(mDragLayer);
        indicatorNumber = (mList.size() / pageSize) + (mList.size() % pageSize == 0 ? 0 : 1);

        mListView.setLayoutManager(horizhontalPageLayoutManager);

        //添加分页
        mScrollController.setUpRecycleView(mListView);
        mScrollController.setOnPageChangeListener(this);

        //添加分页指示器--圆形
        mIndicator.setNumber(indicatorNumber);

        mDragLayer.setDragView(mListView);
    }

    @Override
    public void onDragStarted(View source) {
        Lg.d("========onDragStarted soure= " + source);
    }

    @Override
    public void onDropCompleted(View source, View target, boolean success) {
        Lg.d("========onDropCompleted success : " + success);

        if (success && source != target) {
            AppItem sourceItem = ((DraggableLayout) source).getItem();
            //删除操作
            if (target instanceof DeleteZone) {
                if(sourceItem == null){
                    Lg.e("sourceItem is null in delete action !!!");
                    return;
                }

                if (sourceItem.isDelete()) {
                    if(mList.contains(sourceItem)){
                        mList.remove(sourceItem);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(DemoActivity.this, "Can't delete this item", Toast.LENGTH_SHORT).show();
                }
            }
            //item之间的替换操作
            else {
                if(sourceItem == null){
                    Lg.e("sourceItem is null in replace action !!!");
                    return;
                }

                AppItem targetItem = ((DraggableLayout) target).getItem();
                if(targetItem == null){
                    Lg.e("targetItem is null in replace action !!!");
                    return;
                }

                executeItemReplaceAction(sourceItem, targetItem);
            }
        }

        if(mDragLayer.getDraggingListener() != null){
            mDragLayer.getDraggingListener().onDragEnd();
        }
    }

    private void executeItemReplaceAction(AppItem sourceItem, AppItem targetItem) {

        //来源item信息
        int sourcePos = sourceItem.itemPos;

        //目标item位置
        int targetPos = targetItem.itemPos;

        //update source item date
        if((sourcePos / pageSize) != (targetPos / pageSize)){
            needUpdateReplaceDataPage = sourcePos / pageSize;
        }

        Lg.d("sourcePos: " + sourcePos + " targetPos: " + targetPos
            + " needUpdateReplaceDataPage: " + needUpdateReplaceDataPage);
        //位置交换
        Collections.swap(mList, sourcePos, targetPos);
        refreshItemList();
        mAdapter.notifyDataSetChanged();
    }

    private void refreshItemList(){
        for(int i = 0; i < mList.size(); i++){
            mList.get(i).itemPos = i;
        }
    }
}
