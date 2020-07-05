package ru.korolevss.ordinarywarehouse.goodsadapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.korolevss.ordinarywarehouse.R
import ru.korolevss.ordinarywarehouse.model.Goods

class GoodsAdapter(var list: MutableList<Goods>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var loadMoreBtnClickListener: OnLoadMoreBtnClickListener? = null
    var editClickListener: OnItemEditClickListener? = null

    interface OnLoadMoreBtnClickListener {
        fun onLoadMoreBtnClicked()
    }

    interface OnItemEditClickListener {
        fun onItemEditClicked(itemOfGoods: Goods)
    }

    companion object {
        private const val TYPE_ITEM_GOODS = 0
        private const val ITEM_FOOTER = 1
    }

    fun newPosts(newData: List<Goods>) {
        this.list.clear()
        this.list.addAll(newData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.goods_item_card, parent, false)
        return when (viewType) {
            ITEM_FOOTER -> FooterViewHolder(
                this,
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_load_more, parent, false)
            )
            else -> GoodsViewHolder(view, list, this)

        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            list.size -> ITEM_FOOTER
            else -> TYPE_ITEM_GOODS
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != list.size) {
            val goodsItem = list[position]
            with(holder as GoodsViewHolder) {
                bind(goodsItem)
            }
        }
    }
}



