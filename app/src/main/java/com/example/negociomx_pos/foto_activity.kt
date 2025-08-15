package com.example.negociomx_pos

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.negociomx_pos.room.BLL.BLLUtil
import kotlin.math.max
import kotlin.math.min

class foto_activity : AppCompatActivity() {

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private lateinit var imgFoto: ImageView
    private lateinit var imgAceptar:ImageView

    val bllUtil= BLLUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto)

        imgFoto=findViewById(R.id.imgFotoActivity)
        imgAceptar=findViewById(R.id.imgAceptarFotoActivity)

        imgAceptar.setOnClickListener{
            finish()
        }

        if(intent.extras?.isEmpty==false)
        {
            val nombreArchivo= intent.extras?.getString("nombreArchivoFoto","").toString()
            val bitmap=bllUtil.getBitmapFromFilename(nombreArchivo)

            imgFoto.setImageBitmap(bitmap)
        }

        scaleGestureDetector=ScaleGestureDetector(this,ScaleListener())
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(motionEvent)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            scaleFactor *= scaleGestureDetector.scaleFactor
            scaleFactor = max(0.1f, min(scaleFactor, 10.0f))
            imgFoto.scaleX = scaleFactor
            imgFoto.scaleY = scaleFactor
            return true
        }
    }
}