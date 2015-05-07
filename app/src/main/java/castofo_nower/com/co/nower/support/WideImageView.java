package castofo_nower.com.co.nower.support;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


public class WideImageView extends ImageView {

  public WideImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    Drawable d = getDrawable();

    if (d != null) {
      int width = MeasureSpec.getSize(widthMeasureSpec);
      int height = (int) Math.ceil((float) width *
                   (float) d.getIntrinsicHeight() /
                   (float) d.getIntrinsicWidth());
      setMeasuredDimension(width, height);
    }
    else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

}
