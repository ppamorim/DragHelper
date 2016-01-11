package com.github.ppamorim.draghelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragHelper extends FrameLayout {

  private static final float SENSITIVITY = 1.0f;
  private static final float DEFAULT_DRAG_LIMIT = 0.5f;
  private static final int INVALID_POINTER = -1;

  private int activePointerId = INVALID_POINTER;

  private float verticalDragRange;
  private float horizontalDragRange;
  private float dragLimit;

  private TypedArray attributes;
  private ViewDragHelper viewDragHelper;

  private View dragView;

  public DragHelper(Context context) {
    super(context);
  }

  public DragHelper(Context context, AttributeSet attrs) {
    super(context, attrs);
    initializeAttributes(attrs);
  }

  public DragHelper(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initializeAttributes(attrs);
  }

  public DragHelper setDragView(View dragView) {
    this.dragView = dragView;
    return this;
  }

  /**
   * Bind the attributes of the view and config
   * the DragView with these params.
   */
  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    if (!isInEditMode()) {
      mapGUI(attributes);
      attributes.recycle();
      configDragViewHelper();
    }
  }

  /**
   * Updates the view size if needed.
   * @param width The new width size.
   * @param height The new height size.
   * @param oldWidth The old width size, useful the calculate the diff.
   * @param oldHeight The old height size, useful the calculate the diff.
   */
  @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    setVerticalDragRange(height);
    setHorizontalDragRange(width);
  }

  /**
   * Detect the type of motion event (like touch)
   * at the DragView, this can be a simple
   * detector of the touch, not the listener ifself.
   *
   * @param ev Event of MotionEvent
   * @return View is touched
   */
  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (!isEnabled()) {
      return false;
    }
    final int action = MotionEventCompat.getActionMasked(ev);
    switch (action) {
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        viewDragHelper.cancel();
        return false;
      case MotionEvent.ACTION_DOWN:
        int index = MotionEventCompat.getActionIndex(ev);
        activePointerId = MotionEventCompat.getPointerId(ev, index);
        if (activePointerId == INVALID_POINTER) {
          return false;
        }
      default:
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }
  }

  /**
   * Handle the touch event intercepted from onInterceptTouchEvent
   * method, this method valid if the touch listener
   * is a valid pointer(like fingers) or the touch
   * is inside of the DragView.
   *
   * @param ev MotionEvent instance, can be used to detect the type of touch.
   * @return Touched area is a valid position.
   */
  @Override public boolean onTouchEvent(MotionEvent ev) {
    int actionMasked = MotionEventCompat.getActionMasked(ev);
    if ((actionMasked & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
      activePointerId = MotionEventCompat.getPointerId(ev, actionMasked);
    }
    if (activePointerId == INVALID_POINTER) {
      return false;
    }
    viewDragHelper.processTouchEvent(ev);
    return isViewHit(dragView, (int) ev.getX(), (int) ev.getY());
  }

  /**
   * This method is needed to calculate the auto scroll
   * when the user slide the view to the max limit, this
   * starts a animation to finish the view.
   */
  @Override public void computeScroll() {
    if (!isInEditMode() && viewDragHelper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  private boolean smoothSlideTo(View view, int x, int y) {
    if (viewDragHelper != null && viewDragHelper.smoothSlideViewTo(view, x, y)) {
      ViewCompat.postInvalidateOnAnimation(dragView);
      return true;
    }
    return false;
  }

  public float getVerticalDragRange() {
    return verticalDragRange;
  }

  public void setVerticalDragRange(float verticalDragRange) {
    this.verticalDragRange = verticalDragRange;
  }

  public float getHorizontalDragRange() {
    return horizontalDragRange;
  }

  public void setHorizontalDragRange(float horizontalDragRange) {
    this.horizontalDragRange = horizontalDragRange;
  }

  public float getDragLimit() {
    return dragLimit;
  }

  public View getDragView() {
    return dragView;
  }

  /**
   * Set the max limit drag to auto collapse the dragView,
   * default is 0.5 (center of the screen, vertical).
   *
   * @param dragLimit Value between 0.0f and 1.0f
   */
  public void setDragLimit(float dragLimit) {
    if (dragLimit > 0.0f && dragLimit < 1.0f) {
      this.dragLimit = dragLimit;
    } else {
      throw new IllegalStateException("dragLimit needs to be between 0.0f and 1.0f");
    }
  }

  private void initializeAttributes(AttributeSet attrs) {
    TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.draghelper_layout);
    this.dragLimit = attributes.getFloat(R.styleable.draghelper_layout_drag_limit,
        DEFAULT_DRAG_LIMIT);
    this.attributes = attributes;
  }

  /**
   * Map the layout attributes, the dragView and shadowView, after, find the view by id.
   *
   * @param attributes
   */
  private void mapGUI(TypedArray attributes) {
    if (getChildCount() == 1) {
      int dragViewId = attributes.getResourceId(
          R.styleable.draghelper_layout_drag_view_id, 0);
      if (dragViewId > 0) {
        dragView = findViewById(dragViewId);
      }
    } else {
      throw new IllegalStateException("DragHelper must contains only one direct child");
    }
  }

  /**
   * Configure the DragViewHelper instance adding a
   * instance of ViewDragHelperCallback, useful to
   * detect the touch callbacks from dragView.
   */
  private void configDragViewHelper() {
    viewDragHelper = ViewDragHelper.create(this, SENSITIVITY,
        new ViewDragHelperCallback(this));
  }

  /**
   * Detect if the touch on the screen is at the region of the view.
   * @param view Instance of the view that will be verified.
   * @param x X position of the touch.
   * @param y Y position of the touch.
   * @return Position is at the region of the view.
   */
  private boolean isViewHit(View view, int x, int y) {
    int[] viewLocation = new int[2];
    view.getLocationOnScreen(viewLocation);
    int[] parentLocation = new int[2];
    this.getLocationOnScreen(parentLocation);
    int screenX = parentLocation[0] + x;
    int screenY = parentLocation[1] + y;
    return screenX >= viewLocation[0]
        && screenX < viewLocation[0] + view.getWidth()
        && screenY >= viewLocation[1]
        && screenY < viewLocation[1] + view.getHeight();
  }

}
