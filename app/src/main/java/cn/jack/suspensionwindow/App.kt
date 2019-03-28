package cn.jack.suspensionwindow

import android.app.Application

/**
 * Created by manji
 * Date：2018/9/29 上午11:02
 * Desc：
 */
class App : Application() {

    companion object {
        @JvmStatic
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        registerActivityLifecycleCallbacks(ApplicationLifecycle())
    }
}
