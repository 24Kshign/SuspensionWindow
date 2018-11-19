package cn.jack.suspensionwindow.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import cn.jack.suspensionwindow.R
import cn.jack.suspensionwindow.util.DisplayUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("TAG", "width--->${DisplayUtil.getScreenWidth(this@MainActivity)}")
        Log.e("TAG", "height--->${DisplayUtil.getScreenHeight(this@MainActivity)}")

        amTvGo.setOnClickListener {
            startActivity(Intent(this, ArticleListActivity::class.java))
//            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}