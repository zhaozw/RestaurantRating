package si.kubit.restaurantrating;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import si.kubit.restaurantrating.conn.Foursquare;
import si.kubit.restaurantrating.util.Util;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapFriendsActivity extends MapActivity {
	
//	private static final int AUTHORIZATION_REQUEST = 1338;
	
	MapView mapView;
	List<Overlay> mapOverlays;
	protected MapController mapController;
	protected LocationManager locationUpdateRequester;
	protected LocationListener locationListener;
	private MyLocationOverlay myLocationOverlay;
	
	private FriendsItemizedOverlay friendsItemizedOverlay;
	private ArrayList<String> overlayVenueIds = new ArrayList<String>();
	private ArrayList<Boolean> overlayIsRestaurants = new ArrayList<Boolean>();

	private JSONArray jLocations;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_friends);
        
   		mapView = (MapView) findViewById(R.id.friends_map_view);
        mapView.setBuiltInZoomControls(true);
        MapController mc = mapView.getController();
        mc.setZoom(Integer.parseInt(getString(R.string.zoom_level)));
        
        myLocationOverlay = new MyLocationOverlay(this, mapView);
		
		mapOverlays = mapView.getOverlays();
        mapOverlays.add(myLocationOverlay);

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int screenWidth = display.getWidth();
		int screenHeight = display.getHeight();
		
		LinearLayout lf = (LinearLayout) findViewById(R.id.layout_friends);
		LinearLayout lfl = (LinearLayout) findViewById(R.id.layout_friends_location);
        lfl.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {		
    	        int i = friendsItemizedOverlay.getLastFocusedIndex();

        		if (overlayIsRestaurants.get(i)) {
		    		Intent intentRestaurantRate = new Intent(MapFriendsActivity.this, RestaurantRateActivity.class);
				  	Bundle extras = new Bundle();
				  	extras.putString("restaurant_id", overlayVenueIds.get(i));
				  	extras.putBoolean("user_rate", true);
				  	intentRestaurantRate.putExtra("si.kubit.restaurantrating.RestaurantRateActivity", extras);
				  	intentRestaurantRate.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				  	MapFriendsActivity.this.startActivity(intentRestaurantRate);
        		}
		    }
        });
        ImageView showLocation = (ImageView) findViewById(R.id.friends_location_next);
		TextView venue = (TextView) findViewById(R.id.text_venue);
		TextView userName = (TextView) findViewById(R.id.text_username);
		friendsItemizedOverlay = new FriendsItemizedOverlay(this.getResources().getDrawable(R.drawable.friends), this, screenWidth, screenHeight, lf, venue, userName, showLocation);
        mapOverlays.add(friendsItemizedOverlay);
        
        mapView.postInvalidate();
    }

    /*private void getFriendsLocations()
    {
    	try { 
			User user = Util.getUserFromPreferencies();	
	    	if (user.getOauthToken()==null || user.getOauthToken().equals("null")) {
				//uporabnik nima autorizacije. Zahtevam
    			Intent authorization = new Intent(this, AuthorizationActivity.class); 
    			startActivityForResult(authorization, AUTHORIZATION_REQUEST); 
			} else {
    			getLocations();
		    }
   		} catch (Exception ne) {
   			ne.printStackTrace();
        	Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		}
    }  */    

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == AUTHORIZATION_REQUEST) {
    		if (resultCode == RESULT_OK) {
    			String accessToken = data.getStringExtra("accessToken");
    			//shrani oatuh za userja
    			Util.SetUserOAuth(accessToken);
    			getLocations();
        	} else if (resultCode == RESULT_CANCELED) {
        		if (data != null) {
		        	Toast toast = Toast.makeText(this, getString(R.string.oauth_requered), Toast.LENGTH_LONG);
		        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
		        	toast.show();
        		}
	        }
    	}
    }
*/
    private void getLocations(){
        try{
        	Foursquare fsq = ((RestaurantRating)getApplicationContext()).getFoursquare();
			jLocations = fsq.getFriendsLocations();
			String subCategories = fsq.getCategory(fsq.getVenuesCategory()).toString();
			overlayVenueIds.clear();
			overlayIsRestaurants.clear();
			friendsItemizedOverlay.clear();
			
			for(int i=0; i<jLocations.length(); i++) {
        		JSONObject jobject = (JSONObject) jLocations.getJSONObject(i);
        		String date = (String) jobject.getString("createdAt");
		        Date checkinDate = new Date(Long.parseLong(date) * 1000);
		        Calendar now = Calendar.getInstance();
		        now.add(Calendar.HOUR, Integer.parseInt(getString(R.string.hour_interval)));
		        if (checkinDate.before(now.getTime())) continue;
		        
		        JSONObject jUser = (JSONObject) jobject.get("user");
        		if(jUser.getString("relationship").equals("self")) continue;
        		
        		JSONObject jVenue = (JSONObject) jobject.get("venue");
        		JSONObject jVenueLocation = (JSONObject) jVenue.get("location");
        		JSONObject jCategory = ((JSONArray) jVenue.get("categories")).getJSONObject(0);
        		
        		boolean isRestaurant = subCategories.contains(jCategory.getString("id")); 
        		overlayIsRestaurants.add(isRestaurant);
    	        
    	        int lat = (int) (Double.parseDouble(jVenueLocation.getString("lat")) * 1E6);
    			int lng = (int) (Double.parseDouble(jVenueLocation.getString("lng")) * 1E6);
    			GeoPoint point = new GeoPoint(lat, lng);
 
    			// Read the image
    			BitmapDrawable d = (BitmapDrawable)Util.ImageOperations((String)jUser.getString("photo"));
    			Bitmap markerImage = d.getBitmap();

    			OverlayItem overlayitem = new OverlayItem(point, jVenue.getString("name"), (String)jUser.getString("firstName") + " " + (jUser.has("lastName")?(String)jUser.getString("lastName"):""));
    			overlayitem.setMarker(new BitmapDrawable(markerImage));
    			OverlayItem.setState(new BitmapDrawable(markerImage), 0);
    	        friendsItemizedOverlay.addOverlay(overlayitem, isRestaurant);
    	        overlayVenueIds.add(jVenue.getString("id"));
        	}
	        mapView.invalidate();

           } catch (Exception e) { 
     			e.printStackTrace();
            	Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
            	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            	toast.show();
          }
    }

	
    @Override
    protected void onResume() { 
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		myLocationOverlay.runOnFirstFix(new Runnable(){public void run(){
            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            }});
//        getFriendsLocations();
        getLocations();
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	myLocationOverlay.disableMyLocation();
    	myLocationOverlay.disableCompass();
		
    }
    
	@Override
    protected boolean isRouteDisplayed() { 
		return false; 
	}     

}