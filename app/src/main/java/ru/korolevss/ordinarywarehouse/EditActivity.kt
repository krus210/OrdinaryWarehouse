package ru.korolevss.ordinarywarehouse

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_create_goods_item.*
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.korolevss.ordinarywarehouse.MainActivity.Companion.ID
import ru.korolevss.ordinarywarehouse.MainActivity.Companion.IMAGE
import ru.korolevss.ordinarywarehouse.MainActivity.Companion.NAME
import ru.korolevss.ordinarywarehouse.MainActivity.Companion.PRICE
import ru.korolevss.ordinarywarehouse.model.Goods
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.NumberFormatException
import java.util.*

class EditActivity : AppCompatActivity() {

    private companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST = 2
        var attachmentImage = ""
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val idOfItem = intent.getLongExtra(ID, 0)
        val nameOfItem = intent.getStringExtra(NAME) ?: ""
        editTextNameEdit.setText(nameOfItem)
        val priceOfItem = intent.getDoubleExtra(PRICE, 0.0)
        editTextPriceEdit.setText(priceOfItem.toString())
        attachmentImage = intent.getStringExtra(IMAGE) ?: ""
        val file = File(filesDir, attachmentImage)
        Glide.with(this)
            .load(file)
            .placeholder(R.drawable.ic_baseline_photo_24)
            .error(R.drawable.ic_baseline_error_24)
            .override(192, 192)
            .centerCrop()
            .into(imageViewGoodsItemEdit)

        imageViewGoodsItemEdit.setOnClickListener {
            dispatchTakePictureIntent()
        }

        buttonEdit.setOnClickListener {
            val name = editTextNameEdit.text.toString()
            try {
                val price: Double = editTextPriceEdit.text.toString().toDouble()
                when {
                    name.isEmpty() -> {
                        Toast.makeText(
                            this, getString(R.string.empty_name), Toast.LENGTH_SHORT
                        ).show()
                    }
                    attachmentImage.isEmpty() -> {
                        Toast.makeText(
                            this, getString(R.string.image_is_not_chosen), Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        editGoodsItem(idOfItem, name, price, attachmentImage)
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this, getString(R.string.incorrect_price), Toast.LENGTH_SHORT
                ).show()
            }
        }

        buttonDelete.setOnClickListener {
            deleteGoodsItem(idOfItem)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteGoodsItem(idOfItem: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = File(filesDir, "json.json")
                val listOfItems = readListOfItems(file)
                listOfItems.removeIf { it.id == idOfItem }
                val jsonString = Gson().toJson(listOfItems)
                file.writeText(jsonString)
                val fileOfImage = File(filesDir, attachmentImage)
                fileOfImage.delete()
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: IOException) {
                Log.d("DELETE_ITEM", "fail writing item to file")
                e.printStackTrace()
            }
        }
    }

    private fun editGoodsItem(idOfItem: Long, name: String, price: Double, attachmentImage: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = File(filesDir, "json.json")
                val listOfItems = readListOfItems(file)
                val indexOfItem = listOfItems.indexOfFirst { it.id ==  idOfItem}
                val itemCoordinates = listOfItems.first { it.id ==  idOfItem }.coordinates
                val copyItem = Goods(idOfItem, name, price, attachmentImage, itemCoordinates)
                listOfItems[indexOfItem] = copyItem
                val jsonString = Gson().toJson(listOfItems)
                file.writeText(jsonString)
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: IOException) {
                Log.d("EDIT_ITEM", "fail writing item to file")
                e.printStackTrace()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        if (switchAddPhotoFromGalleryEdit.isChecked) {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, GALLERY_REQUEST)
        } else {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent,
                        REQUEST_IMAGE_CAPTURE
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?
            loadImage(imageBitmap)
        } else if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
            loadImage(imageBitmap)
        }
    }

    private fun loadImage(imageBitmap: Bitmap?) {
        imageBitmap?.let {
            imageViewGoodsItemEdit.setImageBitmap(imageBitmap)
            attachmentImage = "${UUID.randomUUID()}.jpeg"
            lifecycleScope.launch(Dispatchers.IO) {
                val fileOutputStream: FileOutputStream by lazy {
                    this@EditActivity.openFileOutput(
                        attachmentImage,
                        Context.MODE_PRIVATE
                    )
                }
                try {
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                } catch (e: FileNotFoundException) {
                    Log.d("LOAD_IMAGE", "file not found")
                    e.printStackTrace()
                } finally {
                    fileOutputStream.close()
                }
            }
        }
    }
}