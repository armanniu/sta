package com.arman.demo

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by arman on 2017/12/5.
 * base {@link RecyclerView.ViewHolder}
 */
open class BaseViewHolder(parent: ViewGroup, @LayoutRes layoutId: Int)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))