package ru.korolevss.ordinarywarehouse

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.korolevss.ordinarywarehouse.goodsadapter.GoodsAdapter
import ru.korolevss.ordinarywarehouse.goodsadapter.ItemDiffUtilCallback
import ru.korolevss.ordinarywarehouse.model.Goods
import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException

class MainActivity : AppCompatActivity(), GoodsAdapter.OnLoadMoreBtnClickListener,
    GoodsAdapter.OnItemEditClickListener {

    companion object {
        const val CREATE_EDIT_REQUEST_CODE = 1
        const val ID = "ID"
        const val NAME = "NAME"
        const val IMAGE = "IMAGE"
        const val PRICE = "PRICE"
    }

    private val file: File by lazy { File(filesDir, "json.json") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener {
            val intent = Intent(this, CreateGoodsItemActivity::class.java)
            startActivityForResult(intent, CREATE_EDIT_REQUEST_CODE)
        }
        loadData()
        swipeContainer.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        lifecycleScope.launch {
            try {
                val listOfItems = if (!file.exists()) {
                    mutableListOf()
                } else {
                    readListOfItems(file)
                }
                swipeContainer.isRefreshing = false
                with(container.adapter as GoodsAdapter) {
                    val itemDiffUtilCallback = ItemDiffUtilCallback(list, listOfItems)
                    val itemDiffResult = DiffUtil.calculateDiff(itemDiffUtilCallback)
                    newPosts(listOfItems)
                    itemDiffResult.dispatchUpdatesTo(this)
                }
            } catch (e: IOException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.fail_read_file),
                    Toast.LENGTH_SHORT
                ).show()
                swipeContainer.isRefreshing = false
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch() {
            try {
                determinateBar.isVisible = true
                val listOfItems = if (!file.exists()) {
                    mutableListOf()
                } else {
                    readListOfItems(file)
                }
                with(container) {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    val subListOfItems = getSubListOfItems(listOfItems, 0)
                    adapter = GoodsAdapter(subListOfItems).apply {
                        loadMoreBtnClickListener = this@MainActivity
                        editClickListener = this@MainActivity
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.fail_read_file),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                determinateBar.isVisible = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREATE_EDIT_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK)  {
                Log.d("ACTIVITY_RESULT", "YES")
                lifecycleScope.launch {
                    try {
                        determinateBar.isVisible = true
                        val listOfItems = if (!file.exists()) {
                            mutableListOf()
                        } else {
                            readListOfItems(file)
                        }
                        with(container.adapter as GoodsAdapter) {
                            val itemDiffUtilCallback = ItemDiffUtilCallback(list, listOfItems)
                            val itemDiffResult = DiffUtil.calculateDiff(itemDiffUtilCallback)
                            newPosts(listOfItems)
                            itemDiffResult.dispatchUpdatesTo(this)
                        }
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.fail_read_file),
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        determinateBar.isVisible = false
                    }
                }
            }
        }
    }

    override fun onLoadMoreBtnClicked() {
        lifecycleScope.launch {
            try {
                determinateBar.isVisible = true
                val listOfItems = if (!file.exists()) {
                    mutableListOf()
                } else {
                    readListOfItems(file)
                }
                with(container.adapter as GoodsAdapter) {
                    val lastIdOfAdapter = list.last().id
                    val startIndex = listOfItems.indexOfFirst { it.id == lastIdOfAdapter } + 1
                    val subListOfItems = getSubListOfItems(listOfItems, startIndex)
                    list.addAll(subListOfItems)
                    notifyItemRangeInserted(startIndex, subListOfItems.size)
                }
            } catch (e: IOException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.fail_read_file),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                determinateBar.isVisible = false
            }
        }
    }

    override fun onItemEditClicked(itemOfGoods: Goods) {
        Log.d("ITEM", "YES")
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra(ID, itemOfGoods.id)
        intent.putExtra(NAME, itemOfGoods.name)
        intent.putExtra(PRICE, itemOfGoods.price)
        intent.putExtra(IMAGE, itemOfGoods.attachmentImage)
        startActivityForResult(intent, CREATE_EDIT_REQUEST_CODE)
    }

    private fun getSubListOfItems(
        listOfItems: MutableList<Goods>,
        startIndex: Int
    ): MutableList<Goods> {
        val amountOfItems = 5
        var lastIndex = startIndex + amountOfItems - 1
        if (lastIndex > listOfItems.lastIndex) {
            lastIndex = listOfItems.lastIndex
        }
        if (startIndex >= lastIndex) {
            return mutableListOf()
        }
        return listOfItems.subList(startIndex, lastIndex + 1)
    }
}