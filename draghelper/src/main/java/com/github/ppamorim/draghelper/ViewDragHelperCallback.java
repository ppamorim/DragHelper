package com.github.ppamorim.draghelper;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;

public class ViewDragHelperCallback extends ViewDragHelper.Callback {

  private DragHelper dragHelper;

  /**
   * The constructor get the instance of FlapBar and Bar
   *
   * @param dragHelper provide the instance of DragHelper
   */
  public ViewDragHelperCallback(DragHelper dragHelper) {
    this.dragHelper = dragHelper;
  }

  /**
   * Check if view on focus is the DraggerView
   *
   * @param child return the view on focus
   * @param pointerId return the id of view
   * @return if the child on focus is equals the DraggerView
   */
  @Override public boolean tryCaptureView(View child, int pointerId) {
    return child.equals(dragHelper.getDragView());
  }

  /**
   * Return the value of slide based
   * on left and width of the element
   *
   * @param child return the view on focus
   * @param left return the left size of DraggerView
   * @param dx return the scroll on x-axis
   * @return the offset of slide
   */
  @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
    return Math.min(Math.max(left, dragHelper.getPaddingLeft()),
        dragHelper.getWidth() - dragHelper.getDragView().getWidth());
  }

  /**
   * Return the value of slide based
   * on top and height of the element
   *
   * @param child return the view on focus
   * @param top return the top size of ContainerViewView
   * @param dy return the scroll on y-axis
   * @return the offset of slide
   */
  @Override public int clampViewPositionVertical(View child, int top, int dy) {
    return Math.min(Math.max(top, dragHelper.getPaddingTop()),
        dragHelper.getHeight() - dragHelper.getDragView().getHeight());
  }

  /**
   * Return the max value of view that can slide
   * based on #camplViewPositionHorizontal
   *
   * @param child return the view on focus
   * @return max horizontal distance that view on focus can slide
   */
  @Override public int getViewHorizontalDragRange(View child) {
    return (int) dragHelper.getHorizontalDragRange();
  }

  /**
   * Return the max value of view that can slide
   * based on #clampViewPositionVertical
   *
   * @param child return the view on focus
   * @return max vertical distance that view on focus can slide
   */
  @Override public int getViewVerticalDragRange(View child) {
    return (int) dragHelper.getVerticalDragRange();
  }

}
