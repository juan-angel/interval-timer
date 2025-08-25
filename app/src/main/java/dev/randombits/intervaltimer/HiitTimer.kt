package dev.randombits.intervaltimer

import android.os.CountDownTimer

enum class TimerStatus {
    PREPARE,
    ACTIVE,
    REST
}

abstract class HiitTimer(private val activeTime: Int, val restTime: Int, var set: Int, stopTimerFunction: () -> Unit) {
    var status: TimerStatus = TimerStatus.PREPARE;
    private var timer: CountDownTimer? = null;
    private var remainingTime: Long = 0;
    private var isRunning = false;
    private var stopTimerFn: () -> Unit = stopTimerFunction;
    companion object {
        const val PREP_TIME: Int = 10;
    }

    abstract fun onUpdate(millisRemaining: Long);
    abstract fun onStatusChange(status: TimerStatus, set: Int);

    fun start() {
        isRunning = true;
        status = TimerStatus.PREPARE;
        onStatusChange(status, set);

        timer = object : CountDownTimer(PREP_TIME * 1000L, 50) {
            override fun onTick(millisRemaining: Long) {
                if (isRunning)
                    onUpdate(millisRemaining);
                else
                    this.cancel();
            }

            override fun onFinish() {
                if (isRunning)
                    startActive();
                else
                    this.cancel();
            }
        }.start()
    }

    fun pause() {
        isRunning = false;
        timer?.cancel();
    }

    fun resume() {
        isRunning = true;

        when (status) {
            TimerStatus.PREPARE -> start();
            TimerStatus.ACTIVE -> startActive();
            TimerStatus.REST -> start();
        }
    }

    fun stop() {
        isRunning = false;
        timer?.cancel();
        timer = null;
    }

    private fun startActive() {
        var time = remainingTime;

        if (remainingTime == 0L) {
            time = activeTime.toLong() * 1000;

            if (status != TimerStatus.PREPARE)
                set--;

            status = TimerStatus.ACTIVE;
            onStatusChange(status, set);
        }

        timer = object : CountDownTimer(time, 50) {
            override fun onTick(millisRemaining: Long) {
                remainingTime = millisRemaining;
                if (isRunning)
                    onUpdate(millisRemaining);
                else
                    this.cancel();
            }

            override fun onFinish() {
                remainingTime = 0;
                if (isRunning)
                    startRest();
                else
                    this.cancel();
            }
        }.start();
    }

    private fun startRest() {
        if (set == 1) {
            stopTimerFn();
        } else {
            status = TimerStatus.REST;
            onStatusChange(status, set);

            timer = object : CountDownTimer(restTime.toLong() * 1000, 50) {
                override fun onTick(millisRemaining: Long) {
                    if (isRunning)
                        onUpdate(millisRemaining);
                    else
                        this.cancel();
                }

                override fun onFinish() {
                    if (isRunning)
                        startActive();
                    else
                        this.cancel();
                }
            }.start();
        }
    }
}
