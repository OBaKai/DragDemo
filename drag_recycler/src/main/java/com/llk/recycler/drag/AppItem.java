package com.llk.recycler.drag;

/**
 * (: Author：llk
 * (: WorkSpace: PhoneLink
 * (: CreateDate: 2017/4/19
 * (: Describe:
 */

public class AppItem {
    private String text;
    private int drawableId;
    private boolean deletable = true;

    @Override
    public String toString() {
        return "AppItem{" +
                "text='" + text + '\'' +
                ", screenId=" + screenId +
                ", itemPos=" + itemPos +
                '}';
    }

    /**
     * 在第几页上
     */
    public int screenId = -1;

    /**
     * 在屏幕上的第几列
     */
    public int itemPos = -1;


    public AppItem(String text, int icon){
        this.text = text;
        this.drawableId = icon;
    }

    public AppItem(String text, int icon, int screenId, int pos) {
        this.text = text;
        this.drawableId = icon;

        this.screenId = screenId;
        this.itemPos = pos;
    }

    public String getText() {
        return text;
    }

    public void setText(String t) {
        this.text = t;
    }

    public int getIcon() {
        return drawableId;
    }

    public void setIcon(int drawable) {
        this.drawableId = drawable;
    }

    public boolean isDelete() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }
}
