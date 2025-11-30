package com.example.worldday

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.Year

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var textViewSelectedWord: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        bindViews()
        setupCalendar()
    }
    private fun bindViews() {
        calendarView = findViewById(R.id.calendarView)
        textViewSelectedWord = findViewById(R.id.textViewSelectedWord)
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            displayWordForDate(selectedDate)
        }
    }

private fun displayWordForDate(date: LocalDate) {
        val dayOfYear = date.dayOfYear
        val word = WordOfTheDay.WORD_LIST[dayOfYear % WordOfTheDay.WORD_LIST.size]

        val formattedDate = "${date.dayOfMonth}.${date.monthValue}.${date.year}"
        textViewSelectedWord.text = "Слово $formattedDate: $word"
    }
}