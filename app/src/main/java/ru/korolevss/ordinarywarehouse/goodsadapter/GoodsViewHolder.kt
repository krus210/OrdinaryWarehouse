package ru.korolevss.ordinarywarehouse.goodsadapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.goods_item_card.view.*
import ru.korolevss.ordinarywarehouse.EditActivity
import ru.korolevss.ordinarywarehouse.R
import ru.korolevss.ordinarywarehouse.model.Goods
import java.io.File

class GoodsViewHolder(
    private val view: View,
    var list: MutableList<Goods>,
    private val adapter: GoodsAdapter
) : RecyclerView.ViewHolder(view) {

    init {
        this.clickListener()
    }

    private fun clickListener() {
        view.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val goodsItem = list[adapterPosition]
                adapter.editClickListener?.onItemEditClicked(goodsItem)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(goodsItem: Goods) {
        with(view) {
            textViewGoodsItemName.text = goodsItem.name
            textViewGoodsItemPrice.text = """ 
                ${goodsItem.price} ${context.getString(R.string.currency)}
            """.trimIndent()
            val file = File(context.filesDir, goodsItem.attachmentImage)
            Glide.with(this)
                .load(file)
                .placeholder(R.drawable.ic_baseline_photo_24)
                .error(R.drawable.ic_baseline_error_24)
                .override(192, 192)
                .centerCrop()
                .into(imageViewGoodsItem)
        }
    }

}
