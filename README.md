## Android仿微信文章悬浮窗效果
Android悬浮窗实现（含8.0权限适配）

### 序言

前些日子跟朋友聊天，朋友Z果粉，前些天更新了微信，说微信出了个好方便的功能啊，我问是啥功能啊，看看我大Android有没有，他说现在阅读公众号文章如果有人给你发微信你可以把这篇文章当作悬浮窗悬浮起来，方便你聊完天不用找继续阅读，听完是不是觉得这叫啥啊，我大Android微信版不是早就有这个功能了吗，我看文章的时候看到过有这个悬浮按钮，但是我一直没有使用过，试了一下还是挺方便的，就想着自己实现一下这个功能，下面看图，大家都习惯了无图言X

![](http://ooaap25kv.bkt.clouddn.com/18-10-9/44021423.jpg)

### 原理

看完动图我们来分析一下，如何在每个页面上都存在一个`View`呢，有些人可能会说，写在base里面，这样每次启动一个新的`Activity`都要往页面上`addView`一次，性能不好，再说了，我们作为一个优秀的程序员能干这种重复的事吗，这种方案果断打回去；既然这样的话那我们肯定要在全局加了，那么全局是哪呢？相信了解过`Activity`源码的朋友肯定知道，全局可以在`Window`层加啊，这样既能一次性搞定，又不影响性能，说干就干。

### 实现

#### 1、权限

首先我们要考虑的一个问题就是权限问题，因为要适配`Android 7.0 8.0`，添加悬浮窗是需要申请权限的，这里参考了[
Android 悬浮窗权限各机型各系统适配大全](https://blog.csdn.net/self_study/article/details/52859790)这篇文章，适配的比较全，可以直接拿来用。这里需要注意的是，为了适配`Android 8.0`，`Window`的类型需要配置一下：

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
	//Android 8.0
	mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
} else {
	//其他版本
	mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
}
```

#### 2、添加ViewGroup到Window

判断好权限之后，直接添加就可以了

```
@SuppressLint("CheckResult")
private void showWindow(Context context) {
    mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
    mView = LayoutInflater.from(context).inflate(R.layout.article_window, null);

    ImageView ivImage = mView.findViewById(R.id.aw_iv_image);
    String imageUrl = SPUtil.getStringDefault(ARTICLE_IMAGE_URL, "");
    RequestOptions requestOptions = RequestOptions.circleCropTransform();
    requestOptions.placeholder(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round);
    Glide.with(context).load(imageUrl).apply(requestOptions).into(ivImage);

    initListener(context);

    mLayoutParams = new WindowManager.LayoutParams();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }
    mLayoutParams.format = PixelFormat.RGBA_8888;   //窗口透明
    mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;  //窗口位置
    mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    mLayoutParams.width = 200;
    mLayoutParams.height = 200;
    mLayoutParams.x = mWindowManager.getDefaultDisplay().getWidth() - 200;
    mLayoutParams.y = 0;
    mWindowManager.addView(mView, mLayoutParams);
}
```

#### 3、View的拖拽实现

借助`WindowManager.LayoutParams`来实现，`mLayoutParams.x`和`mLayoutParams.y`分别表示`mView`左上角的横纵坐标，所以我们只需要改动这两个值就行了，当`ACTION_UP`时，计算当前`mView`的中心点相对窗口的位置，然后将`mView`动态滑动到窗口左边或者右边：

```
//设置触摸滑动事件
mView.setOnTouchListener(new View.OnTouchListener() {
    int startX, startY;  //起始点
    boolean isMove;  //是否在移动
    long startTime;
    int finalMoveX;  //最后通过动画将mView的X轴坐标移动到finalMoveX
    int statusBarHeight;  //解决mViewy坐标不准确的bug，这里需要减去状态栏的高度

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getX();
                startY = (int) event.getY();
                startTime = System.currentTimeMillis();
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
		}
                isMove = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                mLayoutParams.x = (int) (event.getRawX() - startX);
                mLayoutParams.y = (int) (event.getRawY() - startY - statusBarHeight);
                updateViewLayout();   //更新mView 的位置
                return true;
            case MotionEvent.ACTION_UP:
                long curTime = System.currentTimeMillis();
                isMove = curTime - startTime > 100;
                
                //判断mView是在Window中的位置，以中间为界
                if (mLayoutParams.x + mView.getMeasuredWidth() / 2 >= mWindowManager.getDefaultDisplay().getWidth() / 2) {
                    finalMoveX = mWindowManager.getDefaultDisplay().getWidth() - mView.getMeasuredWidth();
                } else {
                    finalMoveX = 0;
                }
                
                //使用动画移动mView
                ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, finalMoveX).setDuration(Math.abs(mLayoutParams.x - finalMoveX));
                animator.addUpdateListener((ValueAnimator animation) -> {
                    mLayoutParams.x = (int) animation.getAnimatedValue();
                    updateViewLayout();
                });
                animator.start();

                return isMove;
        }
        return false;
    }
});
```

#### 4、注意

为了让`Window`与`Activity`脱离，这里我们采用`Service`来做，通过`Service`来添加和移除`View`；在权限申请成功之后我们需要通知Service（其实是Activity，可能会有保存数据等操作）作相应改变（提供一个接口给Service），然后在Service中使用广播来通知Activity；最后一个需要注意的地方就是我们需要判断应用程序是否在前台还是后台来添加或移除Window，这里通过使用ActivityLifecycleCallbacks来监听Activity在前台的数量来判断应用程序是在前台还是后台

```
class ApplicationLifecycle : Application.ActivityLifecycleCallbacks {

    private var started: Int = 0

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
        started++
        if (started == 1) {
            Log.e("TAG", "应用在前台了！！！")
        }
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
        started--
        if (started == 0) {
            Log.e("TAG", "应用在后台了！！！")
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
    }
}

```

### 11.19日更新

![](https://ws1.sinaimg.cn/large/005MjwGuly1fxdgim9sbeg309j0hh49m.jpg)

新增一个关闭悬浮窗的功能（和微信一样），详细代码请看 WindowUtil 中 onTouch 方法

### 参考

- [Android 8.0 悬浮窗变动与用法](https://blog.csdn.net/mai763727999/article/details/78983375/)

- [
Android 悬浮窗权限各机型各系统适配大全](https://blog.csdn.net/self_study/article/details/52859790)


### 强烈推荐

FRDialog，一个用Builder模式重新打造一个dialog，案例中有三种Builder，分别是CommonBuilder、MDBuilder和RecyclerViewBuilder，如果还想实现其他的通用，继承自FRBaseDialogBuilder即可。

项目地址：[自定义万能FRDialog](https://github.com/AndroidFriendsGroup/FRDialog)

### 公众号

欢迎关注我的个人公众号【IT先森养成记】，专注大前端技术分享，包含Android，Java基础，Kotlin，HTML，CSS，JS等技术；在这里你能得到的不止是技术上的提升，还有一些学习经验以及志同道合的朋友，赶快加入我们，一起学习，一起进化吧！！！

![公众号：IT先森养成记](http://upload-images.jianshu.io/upload_images/490111-cfc591d001bf4cc6.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
