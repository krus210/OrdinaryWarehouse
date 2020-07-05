package ru.korolevss.ordinarywarehouse.goodsadapter

import androidx.recyclerview.widget.DiffUtil
import ru.korolevss.ordinarywarehouse.model.Goods

class ItemDiffUtilCallback(
    private val oldList: MutableList<Goods>,
    private val newList: MutableList<Goods>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldModel = oldList[oldItemPosition]
        val newModel = newList[newItemPosition]
        return oldModel.id == newModel.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldModel = oldList[oldItemPosition]
        val newModel = newList[newItemPosition]
        return oldModel.name == newModel.name
                && oldModel.price == newModel.price
                && oldModel.attachmentImage == newModel.attachmentImage

    }

}