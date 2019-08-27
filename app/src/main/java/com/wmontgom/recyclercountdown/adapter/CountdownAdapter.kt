package com.wmontgom.recyclercountdown.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wmontgom.recyclercountdown.R
import com.wmontgom.recyclercountdown.inflate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class CountdownAdapter : RecyclerView.Adapter<CountdownAdapter.FishHolder>() {
    var useRx = false
    var endDate : Long = 0L

    override fun getItemCount(): Int {
        return 6
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FishHolder {
        val inflatedView = parent.inflate(R.layout.recycler_view_holder, false)

        if (useRx) {
            return RxFishHolder(inflatedView)
        }
        return CoFishHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: FishHolder, position: Int) {
        val context = holder.itemView.context

        holder.t_remaining = endDate

        when(position) {
            0 -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_1))
            1 -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_2))
            2 -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_3))
            3 -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_4))
            4 -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_5))
            else -> holder.setImage(ContextCompat.getDrawable(context, R.mipmap.fish_6))
        }
    }

    override fun onViewAttachedToWindow(holder: FishHolder) {
        super.onViewAttachedToWindow(holder)
        holder.startCountdown()
    }

    override fun onViewDetachedFromWindow(holder: FishHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.stopCountdown()
    }

    /**
     * FishHolder for counting down using Coroutines
     */
    class CoFishHolder(v: View) : FishHolder(v) {
        private var job : Job? = null
        private val coroutineContext : CoroutineContext get() = Dispatchers.Main
        private val scope = CoroutineScope(coroutineContext)

        override fun startCountdown() {
            job?.cancel()

            scope.launch {
                while(true) {
                    System.out.println("ticking")
                    tickDown()
                    delay(1000)
                }
            }
        }

        fun tickDown() {
            val t_string = t_remaining()
            if (t_string.equals("Ended")) stopCountdown()
            timer?.text = t_string
        }

        override fun stopCountdown() {
            job?.cancel()
        }
    }

    /**
     * FishHolder for counting down using Rx
     */
    class RxFishHolder(v: View) : FishHolder(v) {
        private val stopped = AtomicBoolean()
        private var disposable: Disposable? = null
        private var t_string : String = ""

        override fun startCountdown() {
            stopped.set(false)

            disposable?.let {
                if (!it.isDisposed) it.dispose()
            }

            disposable = Observable.interval(1, TimeUnit.SECONDS)
                .startWith(-1L)
                .takeWhile({ tick -> !stopped.get() })
                .map{
                    System.out.println("ticking")
                    t_string = t_remaining()
                }
                .replay(1)
                .refCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { setText() }
        }

        override fun stopCountdown() {
            disposable?.let {
                if (!it.isDisposed) it.dispose()
            }
        }

        fun setText() {
            if (t_string.equals("Ended")) stopCountdown()
            timer?.text = t_string
        }
    }

    /**
     * Base FishHolder class
     */
    open class FishHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageContainer : RelativeLayout? = null
        var timer : TextView? = null
        var t_remaining : Long = 0

        init {
            imageContainer = v.findViewById(R.id.image_container)
            timer = v.findViewById(R.id.timer)
        }

        fun setImage(res : Drawable?) {
            imageContainer?.background = res
        }

        /**
         * Calculates and returns the remaining time text
         */
        fun t_remaining() : String {
            val timeRemaining = t_remaining - System.currentTimeMillis() / 1000

            return when {
                timeRemaining > 0 -> {
                    val days = timeRemaining / 86400
                    val hours = timeRemaining / 3600 % 24
                    val minutes = timeRemaining / 60 % 60
                    val seconds = timeRemaining % 60

                    return when {
                        days > 0 -> "Ends in ${days}d ${hours}h ${minutes}m ${seconds}s"
                        hours > 0 -> "Ends in ${hours}h ${minutes}m ${seconds}s"
                        minutes > 0 -> "Ends in ${minutes}m ${seconds}s"
                        else -> "Ends in ${seconds}s"
                    }
                }
                else -> "Ended"
            }
        }

        open fun startCountdown() {}
        open fun stopCountdown() {}
    }
}