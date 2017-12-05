package com.arman.sta;

import android.content.Context;
import android.graphics.Rect;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.arman.sta.StaIndicator.NO_POSITION;
import static com.arman.sta.StaIndicator.TOP_POS;

/**
 * Created by arman on 2017/11/24.
 */

public class StaLayout extends ViewGroup {

    private View topView;
    private int topViewHeight;

    private RecyclerView bindRecyclerView;
    private StaIndicator indicator = new StaIndicator();
    private StaIndexChangeListener staIndexChangeListener;

    private SparseArray<RecyclerView.ViewHolder> attachedList = new SparseArray<>();
    private SparseArray<RecyclerView.ViewHolder> cacheArray = new SparseArray<>();
    private ArrayList<Integer> tempForDetach = new ArrayList<>();

    private android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());

    public StaLayout(Context context) {
        this(context, null);
    }

    public StaLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StaLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount > 0) {
            topView = getChildAt(0);
        }
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null && p instanceof MarginLayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (topView != null) {
            measureChild(topView, widthMeasureSpec, heightMeasureSpec);
            topViewHeight = topView.getMeasuredHeight();
        }
        int size = attachedList.size();
        for (int i = 0; i < size; i++) {
            RecyclerView.ViewHolder viewHolder = attachedList.get(attachedList.keyAt(i));
            measureChild(viewHolder.itemView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        measureStaBounds();
        int size = attachedList.size();
        for (int i = 0; i < size; i++) {
            int position = attachedList.keyAt(i);
            RecyclerView.ViewHolder viewHolder = attachedList.get(position);
            View itemView = viewHolder.itemView;
            if (position == indicator.getStaIndex()) {
                itemView.layout(l, t + indicator.getStaTop(), r, t + indicator.getStaBottom());
            } else {
                itemView.layout(l, t, r, t);
            }
        }
        if (topView != null) {
            if (indicator.getStaIndex() == 0 || indicator.getStaIndex() == NO_POSITION) {
                topView.layout(l, t + indicator.getStaTop(), r, t + indicator.getStaBottom());
            } else {
                topView.layout(l, t, r, t);
            }
        }
    }

    public void setStaIndexChangeListener(StaIndexChangeListener staIndexChangeListener) {
        this.staIndexChangeListener = staIndexChangeListener;
    }

    public boolean isStaExpand() {
        return !indicator.isStaBPrepare();
    }

    public int getStaIndex() {
        return indicator.getStaIndex();
    }

    public void bindRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == bindRecyclerView) {
            return;
        }
        checkAdapter(recyclerView);
        checkLayoutManager(recyclerView);
        unBindRecyclerView();
        this.bindRecyclerView = recyclerView;
        this.bindRecyclerView.addOnScrollListener(listener);
        initIndicator(recyclerView, false);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        adapter.registerAdapterDataObserver(adapterDataObserver);
    }

    private void initIndicator(RecyclerView recyclerView, boolean dataChanged) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        StaticAdapter staticAdapter = (StaticAdapter) adapter;
        List<Integer> indexList = new ArrayList<>();
        int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            if (staticAdapter.isStaticItem(i)) {
                indexList.add(i);
            }
        }
        indicator.reset(indexList);
        if (itemCount > 0) {
            layoutSta(recyclerView, 0, 0, dataChanged);
        } else {
            detachRecyclerItem();
        }
    }

    private void checkLayoutManager(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() == null || !(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            throw new IllegalStateException("recyclerView need a " + LinearLayoutManager.class.getName());
        }
    }

    private void checkAdapter(RecyclerView recyclerView) {
        if (recyclerView.getAdapter() == null || !(recyclerView.getAdapter() instanceof StaticAdapter)) {
            throw new IllegalStateException("recyclerView need a " + StaticAdapter.class.getName());
        }
    }

    public void unBindRecyclerView() {
        if (this.bindRecyclerView != null) {
            try {
                RecyclerView.Adapter adapter = this.bindRecyclerView.getAdapter();
                adapter.unregisterAdapterDataObserver(adapterDataObserver);
            } catch (Exception e) {
                // do nothing
            }
            this.bindRecyclerView.removeOnScrollListener(listener);
            this.bindRecyclerView = null;
        }
        int size = attachedList.size();
        for (int i = 0; i < size; i++) {
            int adapterPosition = attachedList.keyAt(i);
            RecyclerView.ViewHolder viewHolder = attachedList.get(adapterPosition);
            removeView(viewHolder.itemView);
        }
        attachedList.clear();
        indicator.reset(new ArrayList<Integer>());
    }

    private RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            layoutSta(recyclerView, dx, dy, false);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                indicator.onScrollStop();
                requestLayout();
            }
        }
    };

    private Runnable notifyItemRunnable = new Runnable() {
        @Override
        public void run() {
            if (bindRecyclerView == null) {
                return;
            }
            mainHandler.removeCallbacks(notifyItemRunnable);
            final int staIndex = indicator.getStaIndex();
            RecyclerView.ViewHolder viewHolder = attachedList.get(staIndex);
            RecyclerView.Adapter adapter = bindRecyclerView.getAdapter();
            if (viewHolder != null
                    && staIndex >= 0
                    && staIndex < adapter.getItemCount()
                    && adapter.getItemViewType(staIndex) == viewHolder.getItemViewType()
                    && ((StaticAdapter) adapter).isNeedNotifyWhenAppear(viewHolder.itemView, viewHolder.getItemViewType(), staIndex)) {
                //noinspection unchecked
                adapter.bindViewHolder(viewHolder, staIndex);
            }
            final int lastStaIndex = indicator.getLastStaIndex();
            if (lastStaIndex >= 0 && lastStaIndex < adapter.getItemCount()) {
                LinearLayoutManager lm = (LinearLayoutManager) bindRecyclerView.getLayoutManager();
                View viewByPosition = lm.findViewByPosition(lastStaIndex);
                if (viewByPosition != null
                        && ((StaticAdapter) adapter).isNeedNotifyWhenAppear(viewByPosition, adapter.getItemViewType(lastStaIndex), lastStaIndex)) {
                    adapter.notifyItemChanged(lastStaIndex);
                }
            }
        }
    };

    private void layoutSta(RecyclerView recyclerView, int dx, int dy, boolean dataChanged) {
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibility = lm.findFirstVisibleItemPosition();
        int firstCompleteVisibility = lm.findFirstCompletelyVisibleItemPosition();
        int lastVisibility = lm.findLastVisibleItemPosition();
        //item 存在topMargin为正数的时候，第一个可视item和完全可视item向上偏移一个位置
        View firstView = lm.findViewByPosition(firstVisibility);
        if (firstView != null && firstView.getTop() > 0) {
            firstCompleteVisibility = firstVisibility;
            firstVisibility = Math.max(0, firstVisibility - 1);
        }
        indicator.onLayoutSta(firstVisibility, firstCompleteVisibility, lastVisibility, dy);
        attachRecyclerItem(recyclerView, indicator.getStaTIndex(), dataChanged);
        attachRecyclerItem(recyclerView, indicator.getStaIndex(), dataChanged);
        attachRecyclerItem(recyclerView, indicator.getStaBIndex(), dataChanged);
        detachRecyclerItem();
        measureStaBounds();
        if (indicator.justStaIndexChanged()) {
            if (bindRecyclerView != null) {
                mainHandler.postDelayed(notifyItemRunnable, 10);
            }
            if (staIndexChangeListener != null) {
                staIndexChangeListener.onStaIndexChanged(indicator.getLastStaIndex(), indicator.getStaIndex());
            }
        }
        invalidate();
        requestLayout();
    }

    private void measureStaBounds() {
        if (bindRecyclerView == null) {
            return;
        }
        LinearLayoutManager lm = (LinearLayoutManager) bindRecyclerView.getLayoutManager();
        if (indicator.isStaInList()) {
            View viewByPosition = lm.findViewByPosition(indicator.getStaIndex());
            if (viewByPosition != null) {
                int measuredWidth = viewByPosition.getMeasuredWidth();
                int measuredHeight = viewByPosition.getMeasuredHeight();
                Rect staRect = indicator.getStaRect();
                staRect.left = 0;
                staRect.top = TOP_POS;
                staRect.right = measuredWidth;
                staRect.bottom = staRect.top + measuredHeight;
            }
        } else {
            RecyclerView.ViewHolder viewHolder = attachedList.get(indicator.getStaIndex());
            if (viewHolder != null) {
                View viewByPosition = viewHolder.itemView;
                int measuredWidth = viewByPosition.getMeasuredWidth();
                int measuredHeight = viewByPosition.getMeasuredHeight();
                Rect staRect = indicator.getStaRect();
                staRect.left = 0;
                staRect.top = TOP_POS;
                staRect.right = measuredWidth;
                staRect.bottom = staRect.top + measuredHeight;
            }
        }

        if (indicator.isStaBInList()) {
            View viewByPosition = lm.findViewByPosition(indicator.getStaBIndex());
            int measuredWidth = viewByPosition.getMeasuredWidth();
            int measuredHeight = viewByPosition.getMeasuredHeight();
            Rect staRect = indicator.getStaBRect();
            staRect.left = 0;
            staRect.top = viewByPosition.getTop();
            staRect.right = measuredWidth;
            staRect.bottom = staRect.top + measuredHeight;
        }
    }

    private void attachRecyclerItem(RecyclerView recyclerView, int adapterPosition, boolean dataChanged) {
        if (adapterPosition == NO_POSITION) {
            return;
        }
        RecyclerView.ViewHolder viewHolder = attachedList.get(adapterPosition);
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        int itemViewType = adapter.getItemViewType(adapterPosition);
        if (viewHolder != null && viewHolder.getItemViewType() != itemViewType) {
            detachRecyclerItemAndCache(viewHolder, adapterPosition);
            viewHolder = null;
        }
        if (viewHolder == null) {
            viewHolder = cacheArray.get(itemViewType);
            if (viewHolder != null) {
                cacheArray.remove(itemViewType);
            } else {
                viewHolder = adapter.createViewHolder(this, itemViewType);
            }
            //noinspection unchecked
            adapter.bindViewHolder(viewHolder, adapterPosition);
            attachedList.put(adapterPosition, viewHolder);
            addView(viewHolder.itemView);
        } else if (dataChanged) {
            //noinspection unchecked
            adapter.bindViewHolder(viewHolder, adapterPosition);
        } else {
            StaticAdapter staticAdapter = (StaticAdapter) adapter;
            if (staticAdapter.isNeedNotifyWhenAppear(viewHolder.itemView, viewHolder.getItemViewType(), adapterPosition)) {
                //noinspection unchecked
                adapter.bindViewHolder(viewHolder, adapterPosition);
            }
        }
    }

    private void detachRecyclerItemAndCache(RecyclerView.ViewHolder viewHolder, int adapterPosition) {
        removeView(viewHolder.itemView);
        attachedList.remove(adapterPosition);
        cacheArray.put(viewHolder.getItemViewType(), viewHolder);
    }

    private void detachRecyclerItem() {
        int staTIndex = indicator.getStaTIndex();
        int staIndex = indicator.getStaIndex();
        int staBIndex = indicator.getStaBIndex();
        tempForDetach.clear();
        int size = attachedList.size();
        for (int i = 0; i < size; i++) {
            int position = attachedList.keyAt(i);
            if (position != staTIndex && position != staIndex && position != staBIndex) {
                tempForDetach.add(position);
            }
        }
        for (Integer position : tempForDetach) {
            RecyclerView.ViewHolder viewHolder = attachedList.get(position);
            detachRecyclerItemAndCache(viewHolder, position);
        }
    }

    private RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            initIndicator(bindRecyclerView, true);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            initIndicator(bindRecyclerView, true);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            initIndicator(bindRecyclerView, true);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            initIndicator(bindRecyclerView, true);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            initIndicator(bindRecyclerView, true);
        }
    };

    public interface StaticAdapter {
        boolean isStaticItem(int position);

        boolean isNeedNotifyWhenAppear(View itemView, int viewType, int position);
    }

    public interface StaIndexChangeListener {
        void onStaIndexChanged(int oldIndex, int curIndex);
    }
}
