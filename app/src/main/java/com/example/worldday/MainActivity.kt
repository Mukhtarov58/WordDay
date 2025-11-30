package com.example.worldday



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {


    private lateinit var textViewTitle: TextView
    private lateinit var textViewDefinition: TextView
    private lateinit var imageView: ImageView
    private lateinit var buttonRefresh: MaterialButton
    private lateinit var buttonCalendar: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setSupportActionBar(toolbar)

        loadWordOfTheDay()

        buttonRefresh.setOnClickListener {
            loadWordOfTheDay()
        }

        buttonCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
    }

    private fun bindViews() {
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewDefinition = findViewById(R.id.textViewDefinition)
        imageView = findViewById(R.id.imageView)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonCalendar = findViewById(R.id.buttonCalendar)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)
    }
    private fun loadWordOfTheDay() {
        showLoading(true)

        coroutineScope.launch {
            val wordInfo = try {
                withContext(Dispatchers.IO) {
                    WordOfTheDay.fetchWordInfo()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            showLoading(false)

            if (wordInfo != null) {
                textViewTitle.text = wordInfo.request
                textViewDefinition.text = android.text.Html.fromHtml(wordInfo.message, 0)

                if (wordInfo.images.isNotEmpty()) {
                    Glide.with(this@MainActivity)
                        .load(wordInfo.images[0].image)
                        .centerCrop()
                        .into(imageView)
                } else {
                    imageView.setImageResource(android.R.color.transparent)
                }
            } else {
                Toast.makeText(this@MainActivity, "Не удалось загрузить слово", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        buttonRefresh.isEnabled = !show
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}