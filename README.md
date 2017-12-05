# sta

> A layout that manage items which fixed to the parent top,which copy from RecyclerView.Adapter
>
> I'm very sorry,my english in terrible shape,please teach me.

## General usage

1. find your RecyclerView from a layout
2. set a LinearLayoutManager to your RecyclerView
3. set a Adapter which need implements StaLayout.StaticAdapter to your RecyclerView
4. find StaLayout and bind the RecyclerView to it



## StaLayout.StaticAdapter

This interface have two method

**boolean isStaticItem(int position)** 

This method decide the item which in the position need to fix to the screen to.When it return true,then the StaLayout will get the Adapter which be set in a RecyclerView,and call the method `onCreateViewHolder `and `onBindViewHolder` of the Adapter to get a item and add it.Otherwise,the position will be jumped.

**isNeedNotifyWhenAppear(View itemView, int viewType, int position)**

Normally, you can directly return false,and do nothing.

If the item in a variable state,such as CheckBox,ProgressBar,etc,and you need check the item's status in this method.When the RecyclerView is scrolling and the method return true,the Adapter will be notify to bind data with the item.