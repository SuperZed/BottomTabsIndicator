package com.zed.tabsindicator;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class TabsIndicator extends LinearLayout {

    private ViewPager viewPager;
    private List<TabView> TabViews = new ArrayList<>();
    /**
     * 子View的数量
     */
    private int childCount;
    /**
     * 当前的条目索引
     */
    private int currentItem = 0;

    public TabsIndicator(Context context) {
        this(context, null);
    }

    public TabsIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        init();
    }

    private void init() {
        if (viewPager == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        childCount = getChildCount();
        if (viewPager.getAdapter().getCount() != childCount) {
            throw new IllegalArgumentException("LinearLayout的子View数量必须和ViewPager条目数量一致");
        }
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof TabView) {
                TabView TabView = (TabView) getChildAt(i);
                TabViews.add(TabView);
                //设置点击监听
                TabView.setOnClickListener(new MyOnClickListener(i));
            } else {
                throw new IllegalArgumentException("TabIndicator的子View必须是TabView");
            }
        }
        //对ViewPager添加监听
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        TabViews.get(currentItem).setIconAlpha(1.0f);
    }

    private class MyOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //滑动时的透明度动画
            if (positionOffset > 0) {
                TabViews.get(position).setIconAlpha(1 - positionOffset);
                TabViews.get(position + 1).setIconAlpha(positionOffset);
            }
            //滑动时保存当前按钮索引
            currentItem = position;
        }
    }

    private class MyOnClickListener implements OnClickListener {

        private int currentIndex;

        public MyOnClickListener(int i) {
            this.currentIndex = i;
        }

        @Override
        public void onClick(View v) {
            //点击前先重置所有按钮的状态
            resetState();
            TabViews.get(currentIndex).setIconAlpha(1.0f);
            //不能使用平滑滚动，否者颜色改变会乱
            viewPager.setCurrentItem(currentIndex, false);
            //点击是保存当前按钮索引
            currentItem = currentIndex;
        }
    }

    /**
     * 重置所有按钮的状态
     */
    private void resetState() {
        for (int i = 0; i < childCount; i++) {
            TabViews.get(i).setIconAlpha(0);
        }
    }

    private static final String STATE_INSTANCE = "instance_state";
    private static final String STATE_ITEM = "state_item";

    /**
     * @return 当View被销毁的时候，保存数据
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_ITEM, currentItem);
        return bundle;
    }

    public TabView getCurrentItemView() {
        return TabViews.get(currentItem);
    }

    public TabView getTabView(int p) {
        return TabViews.get(p);
    }

    /**
     * @param state 用于恢复数据使用
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            currentItem = bundle.getInt(STATE_ITEM);
            //重置所有按钮状态
            resetState();
            //恢复点击的条目颜色
            TabViews.get(currentItem).setIconAlpha(1.0f);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
