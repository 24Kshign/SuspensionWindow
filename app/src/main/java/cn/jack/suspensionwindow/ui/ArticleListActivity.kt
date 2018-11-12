package cn.jack.suspensionwindow.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.jack.suspensionwindow.R
import cn.jack.suspensionwindow.bean.ArticleBean
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_article_list.*


/**
 * Created by manji
 * Date：2018/9/28 上午10:52
 * Desc：
 */
class ArticleListActivity : FragmentActivity() {

    private lateinit var mList: ArrayList<ArticleBean>

    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_list)

        mList = ArrayList()
        initData()

        mAdapter = MyAdapter(this, mList)
        aal_recyclerview.layoutManager = LinearLayoutManager(this)
        aal_recyclerview.adapter = mAdapter
    }

    private fun initData() {
        var itemBean: ArticleBean = ArticleBean(1, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYYibbSNficfWFKbibMVJ9WeOWIn6feWm9FrZKG9DBqzRDic7LSMbu69sIDXbbsNI6nicsB37BO64PpUAXIw/0?wx_fmt=png"
                , "从源码的角度浅谈Activity，Window，View三者的关系", "2018.09.27",
                "https://mp.weixin.qq.com/s/oPucnbYujfSpwDAbwGI5Mw")
        mList.add(itemBean)
        itemBean = ArticleBean(2, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY9sFjRWIyFo6KCxKSnicMaklZQ31B2lo3zBZfa51O4OdD3sv28tPXxPlAYCpxlptMoiav4UXC6oQ20g/0?wx_fmt=png"
                , "如何优雅的使用ThreadLocal", "2018.09.25",
                "https://mp.weixin.qq.com/s/of0au0FwKzVe0spQhkeYgQ")
        mList.add(itemBean)
        itemBean = ArticleBean(3, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY8hoA09shUgib8x3FdKGPhPnkicvlGpcaA7S0yTNObmsLV3eZhWEfUYZjx6VwLiaSY24VHSBAVxsHsSw/0?wx_fmt=png"
                , "深入浅出Java中clone方法", "2018.09.23",
                "https://mp.weixin.qq.com/s/W8E6Qt6i5qR-1QQTuKCHNg")
        mList.add(itemBean)
        itemBean = ArticleBean(4, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY9Dw1YZNxQYnsjT2Z4f5ak2wn7SS86eL8wy2zDqYBlWTpTFFH6jaLCy0AK7nyicqMpwWdlDD6aQ6QQ/0?wx_fmt=png"
                , "Android不正经布局之ConstraintLayout布局解析", "2018.09.18",
                "https://mp.weixin.qq.com/s/cJJjo7TX6BPCUsGJooD69g")
        mList.add(itemBean)
        itemBean = ArticleBean(5, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYYicrkD1ua0KeQUCENQdg157l2VlJDE45gnTlhtGScfoPB2jgRzBWtsKUacOric9S7JCibMPkk4Sbt8Xw/0?wx_fmt=png"
                , "Android开发人员不得不学习Vue.js", "2018.09.13",
                "https://mp.weixin.qq.com/s/0yNnnXzhtQlQa_9Lrx_Mwg")
        mList.add(itemBean)
        itemBean = ArticleBean(6, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY98GY0XicKQNvNharaP8JkI3NO80CcdcF8HwR1TibOLaGtKxLNYdM5fZsgQoae6nM1yjJXDNkJF31tw/0?wx_fmt=png"
                , "一个Android程序员的北漂之路", "2018.09.10",
                "https://mp.weixin.qq.com/s/SGdQCZZZz4iOn2dJqytYLQ")
        mList.add(itemBean)
        itemBean = ArticleBean(7, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY8dy5Zlpv2WyejEscndrh3T6YRJTxpsMTGhl22dGBiczh9pjgZzRDpP4HOqC0RicU48g6qOUxStF8Og/0?wx_fmt=png"
                , "Android程序员不得不了解的JavaScript基础（一）", "2018.09.02",
                "https://mp.weixin.qq.com/s/4PtAJOF8_B8cUvG59lbemQ")
        mList.add(itemBean)
        itemBean = ArticleBean(8, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYY97B4xibmfUUanj6Pj9ypahjdf7pFnfKDIjj4T7qJvCq9PcuRIzpU9UlPdIkJrqAYqlDsol2CYmrrA/0?wx_fmt=png"
                , "Android程序员不得不了解的JavaScript基础（二）", "2018.08.30",
                "https://mp.weixin.qq.com/s/nSXk3sRFqo21xvL93s56EA")
        mList.add(itemBean)
        itemBean = ArticleBean(9, "https://mmbiz.qpic.cn/mmbiz_png/ldey36QiaYYib1EicP3ywwlTIiavkNNHxwOENTzPb4SDDUv0PibXiccR16XNZFAjaGxvk1xK5HZxtE8TfrwEChnFia5AA/0?wx_fmt=png"
                , "Kotlin入门教程——快使用Kotlin吧", "2018.08.27",
                "https://mp.weixin.qq.com/s/0-ge5AKCly95K6m-wLyjUg")
        mList.add(itemBean)
    }

    private class MyAdapter : RecyclerView.Adapter<MyViewHolder> {

        var mList: ArrayList<ArticleBean>? = null
        var mContext: Context

        constructor(context: Context, list: ArrayList<ArticleBean>) {
            mList = list
            mContext = context
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.article_recycle_item, viewGroup, false))
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            myViewHolder.setData(mList?.get(i))

            myViewHolder.itemView.setOnClickListener {
                WebViewActivity.start(mContext, mList?.get(i))
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }
    }

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imageView: ImageView = itemView.findViewById(R.id.ari_iv_image)
        var tvTitle: TextView = itemView.findViewById(R.id.ari_tv_title)
        var tvTime: TextView = itemView.findViewById(R.id.ari_tv_time)

        fun setData(item: ArticleBean?) {
            if (null != item) {
                with(item) {

                    val options = RequestOptions()
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher_round)
                            .priority(Priority.HIGH)

                    Glide.with(itemView.context)
                            .load(imageUrl)
                            .transition(DrawableTransitionOptions())
                            .apply(options)
                            .into(imageView)

                    tvTime.text = time
                    tvTitle.text = title
                }
            }
        }
    }
}
