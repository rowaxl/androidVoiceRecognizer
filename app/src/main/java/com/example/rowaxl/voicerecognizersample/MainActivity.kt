package com.example.rowaxl.voicerecognizersample

import android.Manifest
import android.animation.*
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {

    // voice recognizer class
    private val mRecognizer: SpeechToText = SpeechToText()

    // logs
    private lateinit var logView: TextView

    // animation
    private lateinit var imageView: ImageView
    private lateinit var grayView: View
    private var animationList = ArrayList<Animator>()
    private val animatorSet = AnimatorSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.button_start_record).setOnClickListener { startRecognizer() }
        findViewById<View>(R.id.button_start_record).isEnabled = false
        findViewById<View>(R.id.button_stop_record).setOnClickListener { stopRecognizer() }
        findViewById<View>(R.id.button_stop_record).isEnabled = false

        logView = findViewById(R.id.text_log)
        grayView = findViewById(R.id.gray_view)

        setAnimation()

        // voice recognizer need recording and modifying audio settings permission.
        requestPermissions()

        // initialize recognizer
        mRecognizer.init(this.applicationContext)

        // event catcher
        EventBus.getDefault().register(this@MainActivity)
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.MODIFY_AUDIO_SETTINGS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // this must be granted by user to use voice recognizer.
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                ),
                resources.getInteger(R.integer.permission_request_code)
            )
        } else {
            findViewById<View>(R.id.button_start_record).isEnabled = true
            findViewById<View>(R.id.button_stop_record).isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // enable to recording button
        findViewById<View>(R.id.button_start_record).isEnabled = true
        findViewById<View>(R.id.button_stop_record).isEnabled = true
    }

    private fun startRecognizer() {
        mRecognizer.startSpeech()
        attachAnimation()
    }

    private fun stopRecognizer() {
        mRecognizer.stopSpeech()
        detachAnimation()
    }

    private fun appendLog(log: String) {
        logView.append(log + System.getProperty("line.separator")!!)
    }

    private fun setAnimation() {
        // set microphone animation
        imageView = ImageView(this@MainActivity)
        imageView.setImageResource(R.drawable.icon_mic)
        val params = ConstraintLayout.LayoutParams(200, 200)
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        imageView.layoutParams = ConstraintLayout.LayoutParams(params)
        val layout:ConstraintLayout = this.findViewById(R.id.root_layout)
        layout.addView(imageView)
        animationList = setAnimationList(imageView)
        imageView.visibility = View.INVISIBLE
    }

    private fun setAnimationList(target: ImageView):ArrayList<Animator> {
        val list = ArrayList<Animator>()
        val expandX = PropertyValuesHolder.ofFloat( "scaleX", 1f, 1.5f)
        val expandY = PropertyValuesHolder.ofFloat( "scaleY", 1f, 1.5f)
        val alphaDown = PropertyValuesHolder.ofFloat("alpha", 1f, 0.6f)
        val expandAnimation = ObjectAnimator.ofPropertyValuesHolder(target, expandX, expandY, alphaDown).setDuration(300)

        val shrinkX = PropertyValuesHolder.ofFloat( "scaleX", 1.5f, 1f)
        val shrinkY = PropertyValuesHolder.ofFloat( "scaleY", 1.5f, 1f)
        val alphaUp = PropertyValuesHolder.ofFloat("alpha", 0.6f, 1f)
        val shrinkAnimation = ObjectAnimator.ofPropertyValuesHolder(target, shrinkX, shrinkY, alphaUp).setDuration(300)

        list.add(0, expandAnimation)
        list.add(1, shrinkAnimation)

        return list
    }

    private fun attachAnimation() {
        imageView.visibility = View.VISIBLE
        grayView.visibility = View.VISIBLE

        animatorSet.apply {
            playSequentially(animationList)
            addListener(object: AnimatorListenerAdapter() {
                var isCancelled: Boolean = false

                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                    if(!isCancelled)
                        animatorSet.start()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    this.isCancelled = true
                    super.onAnimationCancel(animation)
                }
            })
            start()
        }
    }

    private fun detachAnimation() {
        imageView.visibility = View.GONE
        grayView.visibility = View.GONE

        animatorSet.cancel()
    }

    @Subscribe
    fun recognizerOnResult(event:Events.RecognizerOnResult) {
        appendLog(getString(R.string.log_recognize_result) + event.getRecognizeResult())
    }

    @Subscribe
    fun recognizerOnStart(event:Events.RecognizerOnStart) {
        attachAnimation()

    }

    @Subscribe
    fun recognizerOnStop(event:Events.RecognizerOnStop) {
        detachAnimation()
    }
}
