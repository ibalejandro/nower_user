package castofo_nower.com.co.nower.support;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PagerBuilder extends PagerAdapter {

  private ArrayList<View> cards = new ArrayList<>();

  public PagerBuilder(ArrayList<View> promoCards) {
    this.cards = promoCards;
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View promoView = cards.get(position);
    promoView.setTag(position);
    container.addView(promoView);

    return promoView;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public int getCount() {
    return cards.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return (view == object);
  }
}
