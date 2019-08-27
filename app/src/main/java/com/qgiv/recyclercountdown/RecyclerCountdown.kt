package com.qgiv.recyclercountdown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.qgiv.recyclercountdown.adapter.CountdownAdapter
import kotlinx.android.synthetic.main.activity_recycler_countdown.*

class RecyclerCountdown : AppCompatActivity() {
    lateinit var adapter : CountdownAdapter
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_countdown)

        linearLayoutManager = LinearLayoutManager(this)
        fish_list.layoutManager = linearLayoutManager

        adapter = CountdownAdapter()
        adapter.useRx = intent.getBooleanExtra("useRx", false)
        adapter.endDate = intent.getLongExtra("endDate", 0)

        fish_list.adapter = adapter
    }
}
