package com.example.artbookapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artbookapp.adapters.ArtAdapter
import com.example.artbookapp.databinding.ActivityMainBinding
import com.example.artbookapp.entities.ArtModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList: ArrayList<ArtModel>
    private lateinit var artAdapter: ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // If toolbar isn't showing on the app set support action bar.
        setSupportActionBar(binding.mainToolbar)

        artList = ArrayList<ArtModel>()
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.adapter = artAdapter

        try {
            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM arts", null)

            val artIdIndex = cursor.getColumnIndex("id")
            val artNameIndex = cursor.getColumnIndex("artname")

            while (cursor.moveToNext()){
                val id = cursor.getInt(artIdIndex)
                val name = cursor.getString(artNameIndex)
                val art = ArtModel(id, name)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged() // veri seti degisti, sen de kendini degistir gibi bir anlam var

            cursor.close()
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }

    // Inflates options menu to the layout.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.addArt -> {
                val intent = Intent().setClass(this@MainActivity,ArtActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

}