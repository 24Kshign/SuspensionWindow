@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package cn.jack.suspensionwindow.bean

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by manji
 * Date：2018/9/28 上午11:41
 * Desc：
 */

data class ArticleBean(
        val id: Int,
        val imageUrl: String,
        val title: String,
        val time: String,
        val jumpUrl: String
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeString(imageUrl)
        writeString(title)
        writeString(time)
        writeString(jumpUrl)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ArticleBean> = object : Parcelable.Creator<ArticleBean> {
            override fun createFromParcel(source: Parcel): ArticleBean = ArticleBean(source)
            override fun newArray(size: Int): Array<ArticleBean?> = arrayOfNulls(size)
        }
    }
}