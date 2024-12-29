package com.example.artbookapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.artbookapp.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: SQLiteDatabase
    private var selectedArtId: Int = -1
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
        registerLauncher()

        selectedArtId = intent.getIntExtra("artId", -1)
        if (selectedArtId != -1){
            val cursor = database.rawQuery("SELECT * FROM Arts WHERE id = ?", arrayOf(selectedArtId.toString()))
            val artNameIndex = cursor.getColumnIndex("artname")
            val artistNameIndex = cursor.getColumnIndex("artistname")
            val yearIndex = cursor.getColumnIndex("year")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artNameInput.setText(cursor.getString(artNameIndex))
                binding.artistNameInput.setText(cursor.getString(artistNameIndex))
                binding.artYearInput.setText(cursor.getString(yearIndex))

                var byteArrayImage = cursor.getBlob(imageIndex)
                val bitmapImage = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.size)
                binding.artImageView.setImageBitmap(bitmapImage)
                selectedBitmap = bitmapImage
            }
            cursor.close()
        }
    }

    fun addImage(view: View) {

        var permission :String? = null
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) { // If android version > 33
            permission = Manifest.permission.READ_MEDIA_IMAGES
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this@ArtActivity, permission) != PackageManager.PERMISSION_GRANTED){
            // Permission Denied, Request Permission

            if (ActivityCompat.shouldShowRequestPermissionRationale(this@ArtActivity, permission)){
                // Rationale permission, when user first say no to permission and changed mind.
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE) //LENGTH_INDEFINITE -> Wait till user interact.
                    .setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(permission)
                    }).show()
            }
            else {
                // Request permission
                permissionLauncher.launch(permission)
            }

        }
        else {
            // Permission OK
            if (ContextCompat.checkSelfPermission(this@ArtActivity, permission) == PackageManager.PERMISSION_GRANTED){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else { // Asking permission again if user chosed only give permission to few images.
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Permission") {
                        // Request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
            }

        }

    }

    fun saveArt(view: View) {
        val artName = binding.artNameInput.text.toString()
        val artistName = binding.artistNameInput.text.toString()
        val year = binding.artYearInput.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val bitmapByteArray = outputStream.toByteArray()

            try {
                var sqlString: String
                if (selectedArtId == -1){
                    // Create new entity
                    database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")
                    sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, artName)
                    statement.bindString(2, artistName)
                    statement.bindString(3, year)
                    statement.bindBlob(4, bitmapByteArray)
                    statement.execute()
                } else {
                    // Update entity
                    sqlString = "UPDATE arts SET artname = \"${artName}\" and artistname = \"${artistName}\" and year = \"${year}\" and image = ${bitmapByteArray} WHERE id = ${selectedArtId}"
                    database.execSQL(sqlString)
                }

            }catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Remove all other activities, go to new intent one.
            startActivity(intent)
        }
        else {
            Toast.makeText(this@ArtActivity, "You need the select image to save.", Toast.LENGTH_LONG).show()
        }
    }

    fun returnMainPage(view: View) {
        finish()
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    if (imageData != null){
                        try {
                            if (VERSION.SDK_INT >= 28){
                                val imgSource = ImageDecoder.createSource(contentResolver, imageData) // If android version > 28
                                selectedBitmap = ImageDecoder.decodeBitmap(imgSource)
                                binding.artImageView.setImageBitmap(selectedBitmap)
                            } else { // If android version < 28
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.artImageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            else {
                // Permission denied.
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result){
                // Permission granted.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                // Permission denied.
                Toast.makeText(this@ArtActivity, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maxSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        var bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            // Landscape image
            width = maxSize
            var scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else {
            // Portrait image
            height = maxSize
            var scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}