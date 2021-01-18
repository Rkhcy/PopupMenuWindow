package com.hu.hcy;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PopupMenuView {
    private static final int DEFAULT_SPAN_COUNT = 5;
    private static final int DEFAULT_INDICATOR_WIDTH = 14;
    private static final int DEFAULT_INDICATOR_HEIGHT = 7;
    private static final int DEFAULT_BACKGROUND_RADIUS_DP = 6;
    private static final int DEFAULT_MARGIN_HORIZONTAL = 16;

    private final Context mContext;
    private final String[] mLabels;
    private final View mAnchor;
    private final int[] mIcons;
    private final PopupItemClickListener mItemClickListener;

    private PopupWindow mPopupWindow;
    private ImageView mIndicator;

    private boolean isShowBottom;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mPopupWindowWidth;
    private int mPopupWindowHeight;
    private int mMarginHorizontal;
    private int[] mAnchorLocation;
    private int mBackgroundCornerRadius;
    private int mNormalBackgroundColor;
    private int mPressedBackgroundColor;

    public void show() {
        initView();
        int leftTopX = calculateLeftTopX();
        int leftTopY = calculateLeftTopY();
        translateIndicator(leftTopX);
        mPopupWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, leftTopX, leftTopY);
    }

    private int calculateLeftTopX() {
        int anchorCenterX = mAnchorLocation[0] + mAnchor.getWidth() / 2;
        int leftTopX = anchorCenterX - mPopupWindowWidth / 2;
        if (leftTopX < mMarginHorizontal) { //default margin left
            leftTopX = mMarginHorizontal;
        }
        int screenWidth = getScreenWidth(mContext);
        if (leftTopX + mPopupWindowWidth > screenWidth - mMarginHorizontal) { //default margin right
            leftTopX = screenWidth - mMarginHorizontal - mPopupWindowWidth;
        }
        return leftTopX;
    }

    private int calculateLeftTopY() {
        return isShowBottom ? mAnchorLocation[1] + mAnchor.getHeight() : mAnchorLocation[1] - mPopupWindowHeight;
    }

    private void translateIndicator(int leftTopX) {
        mIndicator.setTranslationX(mAnchorLocation[0] + mAnchor.getWidth() / 2.f
                - leftTopX - getViewWidth(mIndicator) / 2.f);
    }

    private void initView() {
        LinearLayout mPopupContentView = new LinearLayout(mContext);
        mPopupContentView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mPopupContentView.setOrientation(LinearLayout.VERTICAL);

        RecyclerView mMenuRecyclerView = new RecyclerView(mContext);
        mMenuRecyclerView.setPadding(dp2px(5), 0, dp2px(5), 0);
        mMenuRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int spanCount = Math.min(mLabels.length, DEFAULT_SPAN_COUNT);
        mMenuRecyclerView.setLayoutManager(new GridLayoutManager(mContext, spanCount));
        PopupDivider divider = new PopupDivider(mContext, DividerItemDecoration.VERTICAL, spanCount);
        divider.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_divider_line_horizontal));
        PopupMenuAdapter mAdapter = new PopupMenuAdapter(mContext, mLabels, mItemClickListener);
        mAdapter.setIcons(mIcons);
        mMenuRecyclerView.setAdapter(mAdapter);
        mMenuRecyclerView.addItemDecoration(divider);
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(mNormalBackgroundColor);
        backgroundDrawable.setCornerRadius(mBackgroundCornerRadius);
        mMenuRecyclerView.setBackground(backgroundDrawable);
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
        mPopupWindowWidth = getViewWidth(mPopupContentView);
        mPopupWindowHeight = getViewHeight(mPopupContentView) + getViewHeight(mIndicator);
        mAnchorLocation = new int[2];
        mAnchor.getLocationOnScreen(mAnchorLocation);
        isShowBottom = mAnchorLocation[1] - mPopupWindowHeight < getStatusBarHeight();
        if (isShowBottom) {
            mPopupContentView.addView(mIndicator, 0);
        } else {
            mPopupContentView.addView(mIndicator);
        }
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(mPopupContentView, mPopupWindowWidth, mPopupWindowHeight, true);
        }
    }

    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, mContext.getResources().getDisplayMetrics());
    }

    public int getStatusBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm == null || wm.getDefaultDisplay() == null) {
            return 0;
        }
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private PopupMenuView(Builder builder) {
        this.mContext = builder.mContext;
        this.mLabels = builder.mLabels;
        this.mAnchor = builder.mAnchor;
        this.mIcons = builder.mIcons;
        this.mItemClickListener = builder.mItemClickListener;
        initParams();
    }

    private void initParams() {
        mIndicatorWidth = dp2px(DEFAULT_INDICATOR_WIDTH);
        mIndicatorHeight = dp2px(DEFAULT_INDICATOR_HEIGHT);
        mMarginHorizontal = dp2px(DEFAULT_MARGIN_HORIZONTAL);
        mNormalBackgroundColor = mContext.getResources().getColor(R.color.popup_bg);
        mPressedBackgroundColor = mContext.getResources().getColor(R.color.popup_bg_pressed);
        mBackgroundCornerRadius = dp2px(DEFAULT_BACKGROUND_RADIUS_DP);
    }

    private StateListDrawable getPopupItemBackground() {
        StateListDrawable centerItemBackground = new StateListDrawable();
        GradientDrawable centerItemPressedDrawable = new GradientDrawable();
        centerItemPressedDrawable.setColor(mPressedBackgroundColor);
        GradientDrawable centerItemNormalDrawable = new GradientDrawable();
        centerItemNormalDrawable.setColor(Color.TRANSPARENT);
        centerItemBackground.addState(new int[]{android.R.attr.state_pressed}, centerItemPressedDrawable);
        centerItemBackground.addState(new int[]{}, centerItemNormalDrawable);
        return centerItemBackground;
    }

    private int getViewHeight(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredHeight();
    }

    private int getViewWidth(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredWidth();
    }

    private void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    public interface PopupItemClickListener {
        void onItemClick(View contextView, String label);
    }

    class PopupMenuAdapter extends RecyclerView.Adapter<PopupMenuAdapter.PopupMenuViewHolder> {
        private final Context mContext;
        private final String[] mLabels;
        private final PopupItemClickListener mItemClickListener;
        private int[] mIcons;

        public PopupMenuAdapter(Context context, String[] labels, PopupItemClickListener itemClickListener) {
            this.mContext = context;
            this.mLabels = labels;
            this.mItemClickListener = itemClickListener;
        }

        public void setIcons(int[] icons) {
            this.mIcons = icons;
        }

        @Override
        public PopupMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PopupMenuViewHolder(LayoutInflater.from(mContext).
                    inflate(R.layout.item_popup_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(PopupMenuViewHolder holder, int position) {
            holder.itemView.setBackground(getPopupItemBackground());
            holder.mLabel.setText(mLabels[position]);
            if (mIcons != null) {
                holder.mIcon.setBackgroundResource(mIcons[position]);
                holder.mIcon.setVisibility(View.VISIBLE);
            } else {
                holder.mIcon.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> {
                mItemClickListener.onItemClick(v, mLabels[position]);
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return mLabels != null ? mLabels.length : 0;
        }

        class PopupMenuViewHolder extends RecyclerView.ViewHolder {
            ImageView mIcon;
            TextView mLabel;

            public PopupMenuViewHolder(View itemView) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.icon);
                mLabel = itemView.findViewById(R.id.label);
            }
        }
    }

    static class PopupDivider extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
        public static final int VERTICAL = LinearLayout.VERTICAL;
        private static final String TAG = "DividerItem";
        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private Drawable mDivider;
        private int mOrientation;
        private final Rect mBounds = new Rect();
        private int mSpanCount = -1;

        PopupDivider(Context context, int orientation, int spanCount){
            this(context, orientation);
            mSpanCount = spanCount;
        }

        PopupDivider(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            if (mDivider == null) {
                Log.w(TAG, "@android:attr/listDivider was not set in the theme used for this "
                        + "DividerItemDecoration. Please set that attribute all call setDrawable()");
            }
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL && orientation != VERTICAL) {
                throw new IllegalArgumentException(
                        "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
            }
            mOrientation = orientation;
        }

        public void setDrawable(@NonNull Drawable drawable) {
            mDivider = drawable;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (parent.getLayoutManager() == null || mDivider == null) {
                return;
            }
            if (mOrientation == VERTICAL) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        private void drawVertical(Canvas canvas, RecyclerView parent) {
            canvas.save();
            final int left;
            final int right;
            //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
            if (parent.getClipToPadding()) {
                left = parent.getPaddingLeft();
                right = parent.getWidth() - parent.getPaddingRight();
                canvas.clipRect(left, parent.getPaddingTop(), right,
                        parent.getHeight() - parent.getPaddingBottom());
            } else {
                left = 0;
                right = parent.getWidth();
            }

            int childCount = parent.getChildCount();
            if (mSpanCount != -1) {
                childCount = childCount / mSpanCount + (childCount % mSpanCount > 0 ? 1 : 0);
            }
            for (int i = 0; i < childCount - 1; i++) {
                final View child = parent.getChildAt(i);
                parent.getDecoratedBoundsWithMargins(child, mBounds);
                final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
                final int top = bottom - mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            canvas.restore();
        }

        private void drawHorizontal(Canvas canvas, RecyclerView parent) {
            canvas.save();
            final int top;
            final int bottom;
            //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
            if (parent.getClipToPadding()) {
                top = parent.getPaddingTop();
                bottom = parent.getHeight() - parent.getPaddingBottom();
                canvas.clipRect(parent.getPaddingLeft(), top,
                        parent.getWidth() - parent.getPaddingRight(), bottom);
            } else {
                top = 0;
                bottom = parent.getHeight();
            }

            int childCount = parent.getChildCount();
            if (mSpanCount != -1) {
                childCount = mSpanCount;
            }
            for (int i = 0; i < childCount - 1; i++) {
                final View child = parent.getChildAt(i);
                parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
                final int right = mBounds.right + Math.round(child.getTranslationX());
                final int left = right - mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            canvas.restore();
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            if (mDivider == null) {
                outRect.set(0, 0, 0, 0);
                return;
            }
            int lastPosition = state.getItemCount() - 1;
            int position = parent.getChildAdapterPosition(view);
            if (mOrientation == VERTICAL) {
                if (position < lastPosition) {
                    outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            } else {
                if (position < lastPosition) {
                    outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        }
    }

    public static class Builder {
        private final Context mContext;
        private final String[] mLabels;
        private final View mAnchor;
        private int[] mIcons;
        private PopupItemClickListener mItemClickListener;

        public Builder(Context context, String[] labels, View anchor) {
            this.mContext = context;
            this.mLabels = labels;
            this.mAnchor = anchor;
        }

        public Builder setIcons(int[] icons) {
            this.mIcons = icons;
            return this;
        }

        public Builder setItemClickListener(PopupItemClickListener itemClickListener) {
            this.mItemClickListener = itemClickListener;
            return this;
        }

        public PopupMenuView build() {
            return new PopupMenuView(this);
        }
    }

}
