## 背景

前段时间做了个需求，要仿微信长按聊天内容，显示气泡样式的菜单弹窗，菜单弹窗的指示器指向聊天内容的中心，默认显示在上方，当上方空间不足时，显示在下方，就像下面这样：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/1.gif" style="zoom:50%;" />

## 思路

菜单弹窗由两部分组成，分别是包含菜单的圆角矩形框和三角形指示器。其中圆角矩形框的布局可以用 RecyclerView 来实现，三角形指示器可以手绘，重点是要区分这两个控件在垂直方向上的排列顺序。弹窗可以借助 Api PopupWindow 来实现。PopupWindow 的显示有 showAsDropDown 和 showAtLocation 这两种方式，后者的自由度更高，比较适用于当前的实现。

## 实现

先从简单的开始画个基本的骨架，暂时只考虑弹窗显示在上方的情况，那么排序方式就是 RecyclerView + 三角形指示器，可以用 LinearLayout 包裹它们：

```
    public void show() {
        mPopupContentView = new LinearLayout(mContext);
        mPopupContentView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mPopupContentView.setOrientation(LinearLayout.VERTICAL);

        mMenuRecyclerView = new RecyclerView(mContext);
        mMenuRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMenuRecyclerView.setLayoutManager(new GridLayoutManager(mContext, DEFAULT_SPAN_COUNT));
        mAdapter = new PopupMenuAdapter(mContext, mLabels, mItemClickListener);
        mAdapter.setIcons(mIcons);
        mMenuRecyclerView.setAdapter(mAdapter);
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(mContext.getResources().getColor(R.color.popup_bg));
        mMenuRecyclerView.setBackground(backgroundDrawable);

        mIndicator = new ImageView(mContext);
        mIndicator.setImageDrawable(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Path path = new Path();
                Paint paint = new Paint();
                paint.setColor(mContext.getResources().getColor(R.color.popup_bg));
                paint.setStyle(Paint.Style.FILL);
                path.moveTo(0, 0);
                    path.lineTo(mIndicatorWidth / 2f, mIndicatorHeight);
                    path.lineTo(mIndicatorWidth, 0);
                path.close();
                canvas.drawPath(path, paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            @Override
            public int getIntrinsicHeight() {
                return mIndicatorHeight;
            }

            @Override
            public int getIntrinsicWidth() {
                return mIndicatorWidth;
            }
        });
        mIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mPopupContentView.addView(mMenuRecyclerView);
        mPopupContentView.addView(mIndicator);
        mPopupWindow = new PopupWindow(mPopupContentView, getViewWidth(mPopupContentView),
                getViewHeight(mPopupContentView), true);
        int leftTopX = 250;
        int leftTopY = 800;
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }
```

运行效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/2.gif" style="zoom:50%;" />

上面的代码中写死了让弹窗的左上角坐标为(250，800)，实际的情况下这里的坐标应该计算得出，那这个坐标应该咋算呢？画个图理解一下。

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/3.png" style="zoom:50%;" />

如上图所示，A 点的坐标可以通过 getLocationOnScreen 方法获得，已知 A 坐标，咋求 B 坐标？

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/4.png" style="zoom:50%;" />

通过 A 点横坐标，可以计算出中心点 C 的横坐标（Ax + 1/2 控件的宽度），得到 C 点横坐标后，B 点的横坐标就可以算出来了（Cx-1/2 弹窗的宽度）；B 点的纵坐标分为两种情况，弹窗在上和在下：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/5.png" style="zoom:50%;" />

- 在上方：By = Ay - 弹窗高度
- 在下方：By = Ay + 控件高度

改下代码：

```
public void show() {
        ......

        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        int leftTopX = location[0] + mAnchor.getWidth() / 2 - getViewWidth(mPopupContentView) / 2;
        boolean isShowBottom = false; // todo 是否显示在控件下方
        int leftTopY = isShowBottom ? location[1] + mAnchor.getHeight() : location[1] - getViewHeight(mPopupContentView);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }
```

运行效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/6.gif" style="zoom:50%;" />

弹窗出现在了它出现的地方，接着处理下三角指示器，要让指示器的横坐标偏移到控件的中心位置。偏移可以通过 api setTranslationX 方法，那具体偏移的距离怎么算呢？画个图理解一下：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/7.png" style="zoom:50%;" />

如上图所示，要就算的就是 DC 的长度。B 点和 C 点的横坐标已知，三角形指示器的宽度已知， DC 的长度 =  Cx -Bx -1/2 指示器宽度。改下代码：

```
    public void show() {
        ......

        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        int leftTopX = location[0] + mAnchor.getWidth() / 2 - getViewWidth(mPopupContentView) / 2;
        int translateX = location[0] + mAnchor.getWidth() / 2 - leftTopX - getViewWidth(mIndicator) / 2;
        mIndicator.setTranslationX(translateX);
        boolean isShowBottom = false; // todo 是否显示在控件下方
        int leftTopY = isShowBottom ? location[1] + mAnchor.getHeight() : location[1] - getViewHeight(mPopupContentView);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }
```

显示效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/8.gif" style="zoom:50%;" />

接着要考虑弹窗显示在控件下方的情况，即上方的空间高度不足以放下弹窗。打开手机的显示布局边界开关，看下微信是怎么处理的：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/9.gif" style="zoom:50%;" />

以状态栏为界限，当**控件的纵坐标 - 弹窗的高度 < 状态栏的高度，那么弹窗就显示在控件的下方**，不知微信是有意为之还是咋的，貌似加了一些偏差范围，指示器插到了控件的里面，咦......改下代码：

```
public void show() {
        ......

        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        int leftTopX = location[0] + mAnchor.getWidth() / 2 - getViewWidth(mPopupContentView) / 2;
        int translateX = location[0] + mAnchor.getWidth() / 2 - leftTopX - getViewWidth(mIndicator) / 2;
        mIndicator.setTranslationX(translateX);
        isShowBottom = location[1] - mPopWindowHeight < getStatusBarHeight();
        int leftTopY = isShowBottom ? location[1] + mAnchor.getHeight() : location[1] - getViewHeight(mPopupContentView);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }
```

显示效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/10.gif" style="zoom:50%;" />

上图长按 item3 ，逐渐向上滑动，直到空间不足时，弹窗显示在下方。当弹窗显示在下方的情况，指示器应该在上方。改下代码：

```
    public void show() {
        mPopupContentView = new LinearLayout(mContext);
        mPopupContentView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mPopupContentView.setOrientation(LinearLayout.VERTICAL);

        mMenuRecyclerView = new RecyclerView(mContext);
        mMenuRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mMenuRecyclerView.setLayoutManager(new GridLayoutManager(mContext, DEFAULT_SPAN_COUNT));
        mAdapter = new PopupMenuAdapter(mContext, mLabels, mItemClickListener);
        mAdapter.setIcons(mIcons);
        mMenuRecyclerView.setAdapter(mAdapter);
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(mContext.getResources().getColor(R.color.popup_bg));
        mMenuRecyclerView.setBackground(backgroundDrawable);

        mIndicator = new ImageView(mContext);
        mIndicator.setImageDrawable(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Path path = new Path();
                Paint paint = new Paint();
                paint.setColor(mContext.getResources().getColor(R.color.popup_bg));
                paint.setStyle(Paint.Style.FILL);
                if (isShowBottom) {
                    path.moveTo(0, mIndicatorHeight);
                    path.lineTo(mIndicatorWidth, mIndicatorHeight);
                    path.lineTo(mIndicatorWidth / 2f, 0);
                } else {
                    path.moveTo(0, 0);
                    path.lineTo(mIndicatorWidth / 2f, mIndicatorHeight);
                    path.lineTo(mIndicatorWidth, 0);
                }
                path.close();
                canvas.drawPath(path, paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            @Override
            public int getIntrinsicHeight() {
                return mIndicatorHeight;
            }

            @Override
            public int getIntrinsicWidth() {
                return mIndicatorWidth;
            }
        });
        mIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mPopupContentView.addView(mMenuRecyclerView);
        mPopWindowHeight = getViewHeight(mMenuRecyclerView) + getViewHeight(mIndicator);
        mPopWindowWidth = getViewWidth(mPopupContentView);
        mPopupWindow = new PopupWindow(mPopupContentView, mPopWindowWidth, mPopWindowHeight, true);

        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        int leftTopX = location[0] + mAnchor.getWidth() / 2 - getViewWidth(mPopupContentView) / 2;
        int translateX = location[0] + mAnchor.getWidth() / 2 - leftTopX - getViewWidth(mIndicator) / 2;
        mIndicator.setTranslationX(translateX);
        isShowBottom = location[1] - mPopWindowHeight < getStatusBarHeight();
        if (isShowBottom) {
            mPopupContentView.addView(mIndicator, 0);
        } else {
            mPopupContentView.addView(mIndicator);
        }
        int leftTopY = isShowBottom ? location[1] + mAnchor.getHeight() : location[1] - getViewHeight(mPopupContentView);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }
```

运行效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/11.gif" style="zoom:50%;" />

到这基本上就把弹窗画好了。

## 优化

### 水平方向默认间距

上面的实现其实是有问题的，因为被按压的控件可能处在屏幕的任意位置，在一些比较刁钻的区域，会出现弹窗紧贴着屏幕的情况，比如下面这样：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/12.gif" style="zoom:50%;" />

微信处理这种情况是在水平方向设置了默认的间距，思路其实还是设置弹窗左上角的横坐标值，那此时的横坐标怎么定呢?显然分两种情况，一种是弹窗左边贴到屏幕左边，另一种是弹窗右边贴到屏幕右方。

第一种情况，弹窗算出来的左上角横坐标太小了，可以设个默认的间距，比如 16dp 解决：

```
    private int calculateLeftTopX() {
        int anchorCenterX = mAnchorLocation[0] + mAnchor.getWidth() / 2;
        int leftTopX = anchorCenterX - mPopupWindowWidth / 2;
        if (leftTopX < mMarginHorizontal) { //default margin left
            leftTopX = mMarginHorizontal;
        }
        return leftTopX;
    }
```

第二种情况，弹窗算出来的左上角横坐标太大了，横坐标 + 弹窗的宽度 >=屏幕的宽度，因此需要提前预留出距离右边的间距：

```
 private int calculateLeftTopX() {
        int anchorCenterX = mAnchorLocation[0] + mAnchor.getWidth() / 2;
        ......
        int screenWidth = getScreenWidth(mContext);
        if (leftTopX + mPopupWindowWidth > screenWidth - mMarginHorizontal) { //default margin right
            leftTopX = screenWidth - mMarginHorizontal - mPopupWindowWidth;
        }
        return leftTopX;
    }
```

运行效果：

<img src="https://blog-1258142182.cos.ap-chengdu.myqcloud.com/popupmenuwindow/13.gif" style="zoom:50%;" />

## 总结

提供一个创建菜单弹窗的思路，主要是要去计算几个坐标，当画下来后思路会清晰很多。

Enjoy –☺
