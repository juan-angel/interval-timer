package dev.randombits.intervaltimer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextClock
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

private const val ARG_PARAM1 = "active";
private const val ARG_PARAM2 = "rest";
private const val ARG_PARAM3 = "showTime";
private const val ARG_PARAM4 = "setAmount";

class TimerFragment : Fragment() {
    private var activeTime: Int? = null;
    private var restTime: Int? = null;
    private var showTime: Boolean? = null;
    private var setAmount: Int? = null;
    private var mainActivity: MainActivity? = null;
    private var timer: HiitTimer? = null;

    override fun onAttach(context: Context) {
        super.onAttach(context);
        mainActivity = context as MainActivity;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        arguments?.let {
            activeTime = it.getInt(ARG_PARAM1);
            restTime = it.getInt(ARG_PARAM2);
            showTime = it.getBoolean(ARG_PARAM3);
            setAmount = it.getInt(ARG_PARAM4);
        };
    }

    override fun onResume() {
        super.onResume();
        mainActivity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    override fun onPause() {
        super.onPause();
        mainActivity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // allowing it to continue running in the background
        // pauseTimer();
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false);

        val resultTextView: TextView = view.findViewById(R.id.timeLeft);
        val statusTextView: TextView = view.findViewById(R.id.status);
        val setCountView: TextView = view.findViewById(R.id.setCount);

        setCountView.text = setAmount.toString();

        timer = object : HiitTimer(activeTime!!, restTime!!, setAmount!!, ::stopTimer) {
            override fun onUpdate(millisRemaining: Long) {
                resultTextView.text = ((millisRemaining + 999) / 1000).toString();

                if (millisRemaining in 2951..3049) {
                    mainActivity!!.soundAlarm();
                }
            }

            override fun onStatusChange(status: TimerStatus, set: Int) {
                if (status === TimerStatus.ACTIVE) {
                    statusTextView.setTextColor(resources.getColor(R.color.activeBg));
                    statusTextView.text = getString(R.string.activeTime_label).uppercase();
                } else if (status === TimerStatus.REST) {
                    statusTextView.setTextColor(resources.getColor(R.color.restBg));
                    statusTextView.text = getString(R.string.restTime_label).uppercase();
                } else {
                    statusTextView.setTextColor(resources.getColor(R.color.text));
                    statusTextView.text = getString(R.string.prepareTime_label).uppercase();
                }

                if (set > 0)
                    setCountView.text = getString(R.string.setCount, set);
            }
        };
        timer!!.start();

        setFinishTime(view);
        view.findViewById<View>(R.id.time).visibility = if (showTime == true) View.VISIBLE else View.INVISIBLE;

        view.findViewById<View>(R.id.pauseBtn).setOnClickListener { pauseTimer(); }
        view.findViewById<View>(R.id.resumeBtn).setOnClickListener { resumeTimer(); }
        view.findViewById<View>(R.id.stopBtn).setOnClickListener { stopTimer(); }

        return view;
    }

    private fun setFinishTime(view: View) {
        val finishTimeLabel = StringBuilder();
        val finishTimeValue = Calendar.getInstance();
        val finishTime = view.findViewById<TextView>(R.id.finishTime);
        finishTime.visibility = if (showTime == true) View.VISIBLE else View.INVISIBLE;

        finishTimeValue.add(Calendar.SECOND, ((activeTime!! + restTime!!) * setAmount!!) + HiitTimer.PREP_TIME - restTime!!);
        finishTimeLabel.append(getString(R.string.endTime))
                        .append(": ").append(SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(finishTimeValue.time));
        finishTime.text = finishTimeLabel.toString();
    }

    private fun pauseTimer() {
        timer!!.pause();
        mainActivity!!.cancelAlarm();
        requireView().findViewById<View>(R.id.resumeBtn).isVisible = true;
        requireView().findViewById<View>(R.id.pauseBtn).isVisible = false;
        requireView().findViewById<TextView>(R.id.finishTime).text = String.format("%s: --:--", getString(R.string.endTime));
    }

    private fun resumeTimer() {
        requireView().findViewById<View>(R.id.resumeBtn).isVisible = false;
        requireView().findViewById<View>(R.id.pauseBtn).isVisible = true;
        timer!!.resume()
    }

    private fun stopTimer() {
        timer!!.stop();
        mainActivity!!.backToSettings();
    }

    companion object {
        @JvmStatic
        fun newInstance(active: Int, rest: Int, showTime: Boolean, setAmount: Int) =
            TimerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, active);
                    putInt(ARG_PARAM2, rest);
                    putBoolean(ARG_PARAM3, showTime);
                    putInt(ARG_PARAM4, setAmount);
                }
            };
    }
}
