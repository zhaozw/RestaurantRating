package si.kubit.restaurantrating;


import java.util.ArrayList;

import si.kubit.restaurantrating.util.Util;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class FriendsItemizedOverlay extends ItemizedOverlay {
//public class FriendsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context context;
	private int screenWidth;
	private int screenHeight;
	private LinearLayout layout;
	private TextView venueName;
	private TextView userName;
	private ImageView showLocation;
	
	private final int SCALE_SIZE = 10;

	private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	private ArrayList<Boolean> overlayIsRestaurants = new ArrayList<Boolean>();
	
	public FriendsItemizedOverlay(Drawable defaultMarker, Context context, int screenWidth, int screenHeight, LinearLayout layout, TextView venueName, TextView userName, ImageView showLocation) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	    this.screenWidth = screenWidth;
	    this.screenHeight = screenHeight;
	    this.layout = layout;
	    this.venueName = venueName;
	    this.userName = userName;
	    this.showLocation = showLocation;
		layout.setVisibility(View.INVISIBLE);
	}

	public void addOverlay(OverlayItem overlay, boolean isRestaurant) {
		overlayItems.add(overlay);
		overlayIsRestaurants.add(isRestaurant);
	    populate();
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, false);
	    
	    for (int i=0; i<size(); i++) {
	    	OverlayItem overlayItem = ((OverlayItem)overlayItems.get(i));
	    	Drawable marker = overlayItem.getMarker(0);
	    	Bitmap bitmap = ((BitmapDrawable)marker).getBitmap();
	    	
	    	// Convert geo coordinates to screen pixels
			Point screenPoint = new Point();
			mapView.getProjection().toPixels(overlayItem.getPoint(), screenPoint);
			
			//overlayItems.get(i).setMarker(new BitmapDrawable(overlayMarkerImages.get(i)));
			
			float newWidth = screenWidth / SCALE_SIZE;
			float newHeight = screenWidth / SCALE_SIZE;
			
			float ratioX = newWidth / (float) bitmap.getWidth();
			float ratioY = newHeight / (float) bitmap.getHeight();
			float middleX = newWidth / 2.0f;
			float middleY = newHeight / 2.0f;
	
			Matrix matrix = new Matrix();
			matrix.setScale(ratioX, ratioY, middleX, middleY);
			matrix.postTranslate(screenPoint.x - newWidth / 2, screenPoint.y - newHeight);
			Paint paint = new Paint();
			paint.setFilterBitmap(true);
			canvas.drawBitmap(bitmap, matrix, paint);
	    }
		return true;
	}
	
	@Override
	protected boolean onTap(int index) {
		if (overlayIsRestaurants.get(index)) {
			showLocation.setVisibility(View.VISIBLE);
		} else {
			showLocation.setVisibility(View.INVISIBLE);			
		}
	    layout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_top));
	    layout.setVisibility(View.VISIBLE);
	    venueName.setText(((OverlayItem)overlayItems.get(index)).getTitle());
	    userName.setText(((OverlayItem)overlayItems.get(index)).getSnippet());
	  
	    return true;
	}

	
	@Override
	protected OverlayItem createItem(int i) {
	  return overlayItems.get(i);
	}

	@Override
	public int size() {
		return overlayItems.size();
	}
	
    public void removeOverlay(OverlayItem overlay) {
    	overlayItems.remove(overlay);
        populate();
    }


    public void clear() {
    	overlayItems.clear();
        populate();
    }	
}
