package com.github.ppamorim.draghelper;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.View;
import android.view.ViewGroup;

public class DragHelper {

  private static final float SENSITIVITY = 1.0f;

  private float verticalDragRange;
  private float horizontalDragRange;
  private float dragLimit;

  private ViewDragHelper viewDragHelper;

  private ViewGroup containerView;
  private View[] dragView;

  public DragHelper(ViewGroup containerView, View... dragView) {
    this.containerView = containerView;
    this.dragView = dragView;
  }

  public DragHelper setContainerView(ViewGroup containerView) {
    this.containerView = containerView;
    return this;
  }

  public DragHelper setDragView(View... dragView) {
    this.dragView = dragView;
    return this;
  }

  public void start() {
    configDragViewHelper();
  }

  /**
   * Configure the ViewDragHelper instance adding a
   * instance of ViewDragHelperCallback, useful to
   * detect the touch callbacks from dragView.
   */
  private void configDragViewHelper() {
    viewDragHelper = ViewDragHelper.create(containerView, SENSITIVITY,
        new ViewDragHelperCallback(this));
  }

  private boolean smoothSlideTo(View view, int x, int y) {
    if (viewDragHelper != null && viewDragHelper.smoothSlideViewTo(view, x, y)) {
      ViewCompat.postInvalidateOnAnimation(containerView);
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
    return dragView[position];
  }

  public ViewGroup getContainerView() {
    return containerView;
  }

  /**
   * Set the max limit drag to auto collapse the dragView,
   * default is 0.5 (center of the screen, vertical).
   *
   * @param dragLimit Value between 0.0f and 1.0f
   */
  public void setDraggerLimit(float dragLimit) {
    if (dragLimit > 0.0f && dragLimit < 1.0f) {
      this.dragLimit = dragLimit;
    } else {
      throw new IllegalStateException("dragLimit needs to be between 0.0f and 1.0f");
    }
  }

}
