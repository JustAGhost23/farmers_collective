package com.example.farmerscollective.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver


/**
 * Listens for the draw callbacks on the First frame for the `View` passed to the constructor
 * [FirstDrawListener.FirstDrawListener].
 *
 *
 * First frame can be associated with your Activity/Fragment load time and delays in that frame can
 * lead to a bad user experience. Below are some tips and references to understand and fix
 * common issues associated with the load time:
 * - https://developer.android.com/topic/performance/rendering
 * - https://youtu.be/Vw1G1s73DsY (App Launch time)
 * - https://youtu.be/AkafJ6NdrhY (App Launch time & Activity creation)
 * - https://youtu.be/we6poP0kw6E (Invalidations, Layouts, and Performance)
 * - https://www.youtube.com/playlist?list=PLOU2XLYxmsIKEOXh5TwZEv89aofHzNCiu (Android Performance Patterns)
 *
 *
 *
 * References:
 * - https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/view/Choreographer.java
 * - Inspired by Pierre-Yves Ricau see original posts:
 * - https://dev.to/pyricau/android-vitals-first-draw-time-m1d
 * - https://dev.to/pyricau/android-vitals-rising-to-the-first-drawn-surface-1j9e
 */
class FirstDrawListener private constructor(
    private val view: View,
    private val firstDrawCallback: OnFirstDrawCallback
) :
    ViewTreeObserver.OnDrawListener {
    private val mainHandler: Handler
    private var onDrawInvoked = false

    /**
     * Interface definition for a callback to be invoked for the draw callbacks on the First frame.
     */
    interface OnFirstDrawCallback {
        /**
         * Callback to be invoked when the first frame is about to be drawn on the Screen.
         * At this time the complete UI (including all views in the view hierarchy) have been
         * measured, laid out and given a frame.
         *
         *
         * Most of the time until this callback is greatly affected by developer's code.
         */
        fun onDrawingStart()

        /**
         * Callback to be invoked when the first frame has finished drawing.
         * At this time the complete UI is visible to the user for the first time.
         *
         *
         * Frame rendering is governed by the Android rendering pipeline and so the time difference
         * b/w onDrawingStart() and onDrawingFinish() is greatly effected by the device hardware.
         */
        fun onDrawingFinish()
    }

    private fun registerFirstDrawListener() {
        if (view.viewTreeObserver.isAlive && view.isAttachedToWindow) {
            view.viewTreeObserver.addOnDrawListener(this@FirstDrawListener)
        } else {
            // Workaround for a bug fixed in API 26
            // https://android.googlesource.com/platform/frameworks/base/+/9f8ec54244a5e0343b9748db3329733f259604f3
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    if (view.viewTreeObserver.isAlive) {
                        view.viewTreeObserver.addOnDrawListener(this@FirstDrawListener)
                    }

                    // We only want to listen to this event for the first time only
                    view.removeOnAttachStateChangeListener(this)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    // No-op
                }
            })
        }
    }

    override fun onDraw() {
        if (!onDrawInvoked) {
            onDrawInvoked = true

            // Report first draw start
            firstDrawCallback.onDrawingStart()

            // As part of the frame draw the Android Choreographer (coordinates the timing of
            // animations, input and drawing) enqueues a MSG_DO_FRAME message on the
            // Message Queue of the main thread. Processing of that frame and traversal (including
            // the measure pass, layout pass and finally the draw pass) all happens in just one
            // MSG_DO_FRAME message. So coming to this onDraw() callback means that the MSG_DO_FRAME
            // message has been currently processing. Since the Handler processes messages from the
            // Message Queue in a serial fashion we can detect when the drawing finishes by posting
            // a message to the front of Message Queue. When that message is processed by the
            // Handler we can safely assume that the drawing has just been finished completely.
            mainHandler.postAtFrontOfQueue { firstDrawCallback.onDrawingFinish() }

            // Remove the listener after the call is finished
            mainHandler.post {
                if (view.viewTreeObserver.isAlive) {
                    view.viewTreeObserver.removeOnDrawListener(this@FirstDrawListener)
                }
            }
        }
    }

    companion object {
        /**
         * Register a FirstDrawListener instance to the caller.
         *
         * @param view              for which to register the [OnFirstDrawCallback].
         * @param firstDrawCallback to be invoked on various stages of drawing of First frame.
         */
        fun registerFirstDrawListener(
            view: View,
            firstDrawCallback: OnFirstDrawCallback
        ): FirstDrawListener {
            return FirstDrawListener(view, firstDrawCallback)
        }
    }

    /**
     * Constructor for the class.
     *
     * @param view              for which to register the [OnFirstDrawCallback].
     * @param firstDrawCallback to be invoked on various stages of drawing of First frame.
     */
    init {
        mainHandler = Handler(Looper.getMainLooper())
        registerFirstDrawListener()
    }
}