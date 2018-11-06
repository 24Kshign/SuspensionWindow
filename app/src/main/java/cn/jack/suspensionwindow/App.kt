package cn.jack.suspensionwindow

import android.app.Activity
import android.app.Application
import cn.jack.test.window.ApplicationLifecycle
import java.lang.ref.WeakReference

/**
 * Created by manji
 * Date：2018/9/29 上午11:02
 * Desc：
 */
class App : Application() {

    private var CurrentActivity: WeakReference<Activity>? = null

    companion object {
        @JvmStatic
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerActivityLifecycleCallbacks(ApplicationLifecycle())
    }


    //region 设置获取当前的activity
    fun getCurrentActivity(): Activity? {
        return if (CurrentActivity != null) {
            CurrentActivity!!.get()
        } else null

    }

    fun setCurrentActivity(activity: Activity?) {
        CurrentActivity = if (activity != null) {
            WeakReference(activity)
        } else {
            null
        }
    }
    //endregion
}
