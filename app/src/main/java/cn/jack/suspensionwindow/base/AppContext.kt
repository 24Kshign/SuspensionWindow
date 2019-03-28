package cn.jack.suspensionwindow.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import cn.jack.suspensionwindow.window.WindowUtil
import java.lang.ref.WeakReference

/**
 * Created by 大灯泡 on 2019/3/28.
 */

object AppContext {

    private val TAG = "AppContext"
    var sApplication: Application? = null
    private val INNER_LIFECYCLE_HANDLER: InnerLifecycleHandler?

    val isAppVisable: Boolean
        get() = INNER_LIFECYCLE_HANDLER != null && INNER_LIFECYCLE_HANDLER.activityCount > 0

    val isAppBackground: Boolean
        get() = INNER_LIFECYCLE_HANDLER != null && INNER_LIFECYCLE_HANDLER.activityCount <= 0

    val topActivity: Activity?
        get() = if (INNER_LIFECYCLE_HANDLER == null) null else INNER_LIFECYCLE_HANDLER.mTopActivity!!.get()

    val appInstance: Application?
        get() {
            checkAppContext()
            return sApplication
        }

    val appContext: Context
        get() {
            checkAppContext()
            return sApplication!!.applicationContext
        }

    val resources: Resources
        get() {
            checkAppContext()
            return sApplication!!.resources
        }

    val isMainThread: Boolean
        get() = Looper.getMainLooper().thread === Thread.currentThread()

    init {
        var app: Application? = null
        try {
            app = Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null) as Application
            if (app == null)
                throw IllegalStateException("Static initialization of Applications must be on main thread.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current application from AppGlobals." + e.message)
            try {
                app = Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as Application
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to get current application from ActivityThread." + e.message)
            }

        } finally {
            sApplication = app
        }
        INNER_LIFECYCLE_HANDLER = InnerLifecycleHandler()
        if (sApplication != null) {
            sApplication!!.registerActivityLifecycleCallbacks(INNER_LIFECYCLE_HANDLER)
        }
    }

    private fun checkAppContext() {
        if (sApplication == null) {
            reflectAppContext()
        }
        if (sApplication == null) {
            throw IllegalStateException("app reference is null")
        }
    }

    private fun reflectAppContext() {
        var app: Application? = null
        try {
            app = Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null) as Application
            if (app == null)
                throw IllegalStateException("Static initialization of Applications must be on main thread.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current application from AppGlobals." + e.message)
            try {
                app = Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as Application
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to get current application from ActivityThread." + e.message)
            }

        } finally {
            sApplication = app
        }
        if (sApplication != null && INNER_LIFECYCLE_HANDLER != null) {
            sApplication!!.registerActivityLifecycleCallbacks(INNER_LIFECYCLE_HANDLER)
        }
    }


    private class InnerLifecycleHandler : Application.ActivityLifecycleCallbacks {
        private var created: Int = 0
        private var resumed: Int = 0
        private var paused: Int = 0
        private var started: Int = 0
        private var stopped: Int = 0
        var activityCount: Int = 0
        var mTopActivity: WeakReference<Activity>? = null

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
            ++created

        }

        override fun onActivityStarted(activity: Activity) {
            ++started
            ++activityCount
            if (activityCount == 1) {
                WindowUtil.getInstance().visibleWindow()
            }

        }

        override fun onActivityResumed(activity: Activity) {
            ++resumed
            mTopActivity = WeakReference(activity)

        }

        override fun onActivityPaused(activity: Activity) {
            ++paused

        }

        override fun onActivityStopped(activity: Activity) {
            ++stopped
            --activityCount
            if (activityCount == 0) {
                WindowUtil.getInstance().hideWindow()
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {

        }
    }

}
