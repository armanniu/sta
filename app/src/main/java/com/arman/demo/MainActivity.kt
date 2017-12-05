package com.arman.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.arman.sta.StaLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_main.layoutManager = LinearLayoutManager(this)

        val list: MutableList<MainItem> = mutableListOf()
        list.add(MainItem(MainItem.TYPE_TITLE, "美女"))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.meinv_0.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.meinv_1.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.meinv_2.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.meinv_3.toString()))
        list.add(MainItem(MainItem.TYPE_TITLE, "野兽"))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.animal_0.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.animal_1.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.animal_2.toString()))
        list.add(MainItem(MainItem.TYPE_IMAGE, R.mipmap.animal_3.toString()))
        rv_main.adapter = Adapter(list)
        sta_main.bindRecyclerView(rv_main)

    }

    private class Adapter(val list: MutableList<MainItem>) : RecyclerView.Adapter<BaseViewHolder>(), StaLayout.StaticAdapter {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return when (viewType) {
                MainItem.TYPE_IMAGE -> ImageViewHolder(parent)
                MainItem.TYPE_TITLE -> TitleViewHolder(parent)
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: BaseViewHolder?, position: Int) {
            holder ?: return
            when (holder.itemViewType) {
                MainItem.TYPE_TITLE -> (holder as TitleViewHolder).tvTitle.text = list[position].value
                MainItem.TYPE_IMAGE -> {
                    Glide.with(holder.itemView)
                            .asBitmap()
                            .load(list[position].value.toInt())
                            .into((holder as ImageViewHolder).ivImage)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return list[position].type
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun isStaticItem(position: Int): Boolean {
            return list[position].type == MainItem.TYPE_TITLE
        }

        override fun isNeedNotifyWhenAppear(itemView: View?, viewType: Int, position: Int): Boolean {
            return false
        }
    }

    private class TitleViewHolder(parent: ViewGroup) : BaseViewHolder(parent, R.layout.item_main_title) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_main_title)
    }

    private class ImageViewHolder(parent: ViewGroup) : BaseViewHolder(parent, R.layout.item_main_image) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_main_image)
    }
}
