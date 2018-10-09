package cn.jack.suspensionwindow

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amTvGo.setOnClickListener {
            startActivity(Intent(this, ArticleListActivity::class.java))
//            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}