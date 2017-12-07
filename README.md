# sta

> A layout that manage items which fixed to the parent top,which copy from RecyclerView.Adapter
>
> I'm very sorry,my english in terrible shape,please teach me.

## General usage

1. find your RecyclerView from a layout
2. set a LinearLayoutManager to your RecyclerView
3. set a Adapter which need implements StaLayout.StaticAdapter to your RecyclerView
4. find StaLayout and bind the RecyclerView to it


**Example:**

In activity_main.xml

~~~xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.arman.demo.MainActivity">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv_main"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <com.arman.sta.StaLayout
        android:id="@+id/sta_main"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</RelativeLayout>
~~~

In MainActivity.kt

~~~kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    rv_main.layoutManager = LinearLayoutManager(this)
    ...
    rv_main.adapter = Adapter(list)
    sta_main.bindRecyclerView(rv_main)
}
~~~

In Adapter

~~~kotlin
class Adapter(val list: MutableList<MainItem>) : RecyclerView.Adapter<>(), StaLayout.StaticAdapter {
        override fun getItemCount(): Int {
            return list.size
        }
        override fun isStaticItem(position: Int): Boolean {
        	//Return which position need fix to screen top
        	//For example
            return position == 0
        }

        override fun isNeedNotifyWhenAppear(itemView: View?, viewType: Int, position: Int): Boolean {
        	//If itemView status changed,return true,then, StaLayout will use this adapter to bind data to the itemView.Otherwise return false,and the itemView stays the same shape.
            return false
        }
    }
~~~




## StaLayout.StaticAdapter

This interface have two method

**boolean isStaticItem(int position)** 

This method decide the item which in the position need to fix to the screen top.When it return true,then the StaLayout will get the Adapter which be set in a RecyclerView,and call the method `onCreateViewHolder `and `onBindViewHolder` of the Adapter to get a item and add it.Otherwise,the position will be jumped.

**isNeedNotifyWhenAppear(View itemView, int viewType, int position)**

Normally, you can directly return false,and do nothing.

If the item in a variable state,such as CheckBox,ProgressBar,etc,and you need check the item's status in this method.When the RecyclerView is scrolling and the method return true,the Adapter will be notify to bind data with the item.