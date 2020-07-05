package ru.korolevss.ordinarywarehouse

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_create_goods_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.korolevss.ordinarywarehouse.model.Goods
import java.io.*
import java.lang.NumberFormatException
import java.util.*

class CreateGoodsItemActivity : AppCompatActivity() {

    private companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST = 2
        var attachmentImage = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_goods_item)

        imageViewGoodsItemCreate.setOnClickListener {
            dispatchTakePictureIntent()
        }
        buttonCreate.setOnClickListener {
            val name = editTextName.text.toString()
            try {
                val price: Double = editTextPrice.text.toString().toDouble()
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
                        saveGoodsItem(name, price, attachmentImage)
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this, getString(R.string.incorrect_price), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveGoodsItem(name: String, price: Double, attachmentImage: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = File(filesDir, "json.json")
                if (!file.exists()) {
                    val item = Goods(1, name, price, attachmentImage)
                    val listOfItems = listOf(item)
                    val jsonString = Gson().toJson(listOfItems)
                    file.writeText(jsonString)
                } else {
                    val listOfItems = readListOfItems(file)
                    val id = if (listOfItems.isEmpty()) {
                        1
                    } else {
                        listOfItems[0].id + 1
                    }
                    val item = Goods(id, name, price, attachmentImage)
                    listOfItems.add(0, item)
                    val jsonString = Gson().toJson(listOfItems)
                    file.writeText(jsonString)
                }
                setResult(Activity.RESULT_OK)
                finish()
            } catch (e: IOException) {
                Log.d("CREATE_ITEM", "fail writing item to file")
                e.printStackTrace()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        if (switchAddPhotoFromGallery.isChecked) {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, GALLERY_REQUEST)
        } else {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
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
            imageViewGoodsItemCreate.setImageBitmap(imageBitmap)
            attachmentImage = "${UUID.randomUUID()}.jpeg"
            lifecycleScope.launch(Dispatchers.IO) {
                val fileOutputStream: FileOutputStream by lazy {
                    this@CreateGoodsItemActivity.openFileOutput(
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

fun readListOfItems(file: File): MutableList<Goods> {
    val bufferedReader: BufferedReader = file.bufferedReader()
    val inputString = bufferedReader.use { it.readText()}
    val itemType = object : TypeToken<MutableList<Goods>>() {}.type
    return Gson().fromJson<MutableList<Goods>>(inputString, itemType)
}