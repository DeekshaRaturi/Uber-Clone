package com.example.uberclone

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation

class CustomeProgressDialog(context: Context): Dialog(context ) {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.progress_dialog)

        setCancelable(false)

        setCanceledOnTouchOutside(false)

        val rotate = RotateAnimation(

            0f, 360f,

            Animation.RELATIVE_TO_SELF, 0.5f,

            Animation.RELATIVE_TO_SELF, 0.5f

        )

        rotate.duration = 1000

        rotate.repeatCount = Animation.INFINITE


    }
}