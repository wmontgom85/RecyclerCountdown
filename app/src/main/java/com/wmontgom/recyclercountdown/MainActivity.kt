package com.wmontgom.recyclercountdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    private var job : Job? = null
    private val coroutineContext : CoroutineContext get() = Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        end_date.setOnClickListener { showDatePicker(it as EditText) }
        end_time.setOnClickListener { showTimePicker(it as EditText) }
        submit_coroutines.setOnClickListener { validateDateTime() }
        submit_rx.setOnClickListener { validateDateTime(true) }
    }

    fun showDatePicker(editText: EditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { view, pickedYear, pickedMonth, pickedDay ->
            c.set(Calendar.YEAR, pickedYear)
            c.set(Calendar.MONTH, pickedMonth)
            c.set(Calendar.DAY_OF_MONTH, pickedDay)

            val myFormat = "MM/dd/yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            editText.setText(sdf.format(c.time))
        }

        val dpd = DatePickerDialog(this@MainActivity, listener, year, month, day)

        dpd.show()
    }

    fun showTimePicker(editText: EditText) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        val listener = TimePickerDialog.OnTimeSetListener { view, h, m ->
            editText.setText(String.format("%d:%d", h, m))
        }

        val tpd = TimePickerDialog(this, listener, hour, minute, false)

        tpd.show()
    }

    fun validateDateTime(usingRx: Boolean = false) {
        job?.cancel()

        job = scope.launch {
            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US)

            try {
                sdf.parse(end_date.text.toString() + " " + end_time.text.toString()).let {
                    if (c.time.compareTo(it) >= 0) {
                        showMessage("Whoops!", "Please enter a future end date/time")
                    } else {
                        launchCountdown(usingRx, it.time)
                    }
                }
            } catch (tx: Throwable) {
                showMessage("Whoops!", "Please enter a valid end date and time")
            }
        }
    }

    fun launchCountdown(useRx: Boolean, time: Long) {
        val act = Intent(this@MainActivity, RecyclerCountdown::class.java)
        act.putExtra("useRx", useRx)
        act.putExtra("endDate", time/1000)
        startActivity(act)
    }

    fun showMessage(title: String?, message: String?) {
        // report error to UI
        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") {dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }
}