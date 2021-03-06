package com.github.ppamorim.draghelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import java.util.WeakHashMap;

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

  private WeakHashMap<Integer, View> views;

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

  /**
   * Bind the attributes of the view and config
   * the DragView with these params.
   */
  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    if (!isInEditMode()) {
      mapGUI();
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
    if (activePointerId == INVALID_POINTER || views == null) {
      return false;
    }
    viewDragHelper.processTouchEvent(ev);
    boolean isClickAtView = false;
    for(View view : views.values()) {
      if(!isClickAtView) {
        isClickAtView = isViewHit(view, (int) ev.getX(), (int) ev.getY());
      }
    }
    return isClickAtView;
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
      ViewCompat.postInvalidateOnAnimation(views.get((Integer) view.getTag()));
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

  public View getDragView(int position) {
    return views != null ? views.get(position) : null;
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
   * Map the views, search these views on the layout.
   *
   */
  private void mapGUI() {
    int count = getChildCount();
    if(views == null) {
      views = new WeakHashMap<>(count);
    }
    if (count > 0) {
      for(int i = 0; i < count; i++) {
        View view = getChildAt(i);
        view.setTag(i);
        views.put(i, view);
      }
    } else {
      throw new IllegalStateException("DragHelper must contains one or more direct child");
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

  /**
   * Perform the save of the instance state of some params that's used at dragView.
   * @return Parcelable
   */
  @Override public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.views = (View[]) this.views.values().toArray();
    ss.horizontalDragRange = this.horizontalDragRange;
    ss.verticalDragRange = this.verticalDragRange;
    ss.dragLimit = this.dragLimit;
    return ss;
  }

  /**
   * Called when the view is restored
   * @param state Return the state
   */
  @Override public void onRestoreInstanceState(Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    this.horizontalDragRange = ss.horizontalDragRange;
    this.verticalDragRange = ss.verticalDragRange;
    this.dragLimit = ss.dragLimit;
  }

  private static class SavedState extends BaseSavedState {

    private View[] views;
    private float horizontalDragRange;
    private float verticalDragRange;
    private float dragLimit;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      this.views = (View[]) in.readArray(View.class.getClassLoader());
      this.horizontalDragRange = in.readFloat();
      this.verticalDragRange = in.readFloat();
      this.dragLimit = in.readFloat();
    }

    @Override public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeArray(views);
      out.writeFloat(horizontalDragRange);
      out.writeFloat(verticalDragRange);
      out.writeFloat(dragLimit);
    }

    public static final Parcelable.Creator<SavedState> CREATOR
        = new Parcelable.Creator<SavedState>() {
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }

}
