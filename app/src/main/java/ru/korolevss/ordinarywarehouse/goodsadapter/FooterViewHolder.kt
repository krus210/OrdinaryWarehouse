package ru.korolevss.ordinarywarehouse.goodsadapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_load_more.view.*

class FooterViewHolder(private val adapter: GoodsAdapter, view: View) :
    RecyclerView.ViewHolder(view) {

    init {
        with (view) {
            buttonLoadMore.setOnClickListener {
                adapter.loadMoreBtnClickListener?.onLoadMoreBtnClicked()
            }
        }
    }
}