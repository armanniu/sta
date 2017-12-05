package com.arman.sta;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arman on 2017/11/24.
 */

public class StaIndicator {
    public static final int NO_POSITION = -1;
    public static final int TOP_POS = 0;

    private List<Integer> staIndexList = new ArrayList<>();
    private int firstVisibility = NO_POSITION;
    private int firstCompleteVisibility = NO_POSITION;
    private int lastVisibility = NO_POSITION;
    private int lastStaIndex = NO_POSITION;
    private int staIndex = NO_POSITION;
    private int staTIndex = NO_POSITION;
    private int staBIndex = NO_POSITION;
    private int scrollDy = 0;
    private Rect staRect = new Rect();
    private Rect staBRect = new Rect();

    public void reset(List<Integer> staIndexList) {
        this.staIndexList = staIndexList;
        firstVisibility = NO_POSITION;
        firstCompleteVisibility = NO_POSITION;
        lastVisibility = NO_POSITION;
        staIndex = NO_POSITION;
        staTIndex = NO_POSITION;
        staBIndex = NO_POSITION;
        lastStaIndex = NO_POSITION;
        scrollDy = 0;
//        staRect.left = 0;
//        staRect.top = TOP_POS;
//        staRect.right = 0;
//        staRect.bottom = TOP_POS;
//        staBRect.left = 0;
//        staBRect.top = TOP_POS;
//        staBRect.right = 0;
//        staBRect.bottom = TOP_POS;
    }

    public void onLayoutSta(int firstVisibility, int firstCompleteVisibility, int lastVisibility, int dy) {
        this.firstVisibility = firstVisibility;
        this.firstCompleteVisibility = firstCompleteVisibility;
        this.lastVisibility = lastVisibility;
        this.scrollDy = dy;
        lastStaIndex = staIndex;
        staIndex = NO_POSITION;
        staBIndex = NO_POSITION;
        staTIndex = NO_POSITION;
        for (Integer integer : staIndexList) {
            if (integer <= firstVisibility) {
                staTIndex = staIndex;
                staIndex = integer;
            } else {
                staBIndex = integer;
                break;
            }
        }
    }

    public void onScrollStop() {
        this.scrollDy = 0;
    }

    public Rect getStaRect() {
        return staRect;
    }

    public Rect getStaBRect() {
        return staBRect;
    }

    public int getStaIndex() {
        return staIndex;
    }

    public int getLastStaIndex() {
        return lastStaIndex;
    }

    public int getStaTIndex() {
        return staTIndex;
    }

    public int getStaBIndex() {
        return staBIndex;
    }

    public boolean isStaInList() {
        return checkPosition() && staIndex >= firstVisibility && staIndex <= lastVisibility;
    }

    public boolean isStaBInList() {
        return checkPosition() && staBIndex <= lastVisibility && staBIndex >= firstVisibility;
    }

    public boolean isStaBOutOfBottom() {
        return checkPosition() && staBIndex > lastVisibility;
    }

    public boolean isStaBPrepare() {
        return checkPosition() && isStaBInList() && staRect.bottom > staBRect.top;
    }

    public boolean checkPosition() {
        return firstVisibility != NO_POSITION &&
                firstCompleteVisibility != NO_POSITION &&
                lastVisibility != NO_POSITION &&
                staIndex != NO_POSITION;
    }

    public boolean isFastScroll() {
//        return Math.abs(scrollDy) > 100;
        return false;
    }

    public boolean justStaIndexChanged() {
        return lastStaIndex != staIndex;
    }

    public int getStaHeight() {
        return staRect.bottom - staRect.top;
    }

    public int getStaTop() {
        if (isFastScroll() || !isStaBPrepare()) {
            return TOP_POS;
        }
        return staBRect.top - staRect.bottom;
    }

    public int getStaBottom() {
        if (isFastScroll()) {
            return TOP_POS;
        }
        if (!isStaBPrepare()) {
            return getStaHeight();
        }
        return getStaHeight() - (staRect.bottom - staBRect.top);
    }

    public int getStaBTop() {
        if (isFastScroll()) {
            return TOP_POS;
        }
        if (!isStaBPrepare()) {
            return getStaHeight();
        }
        return getStaBottom();
    }

    public int getStaBBottom() {
        if (isFastScroll()) {
            return TOP_POS;
        }
        return getStaHeight();
    }


}
