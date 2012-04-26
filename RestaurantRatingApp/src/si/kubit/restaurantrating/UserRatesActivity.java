package si.kubit.restaurantrating;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import si.kubit.restaurantrating.conn.Comm;
import si.kubit.restaurantrating.conn.Foursquare;
import si.kubit.restaurantrating.objects.User;
import si.kubit.restaurantrating.objects.UserRate;
import si.kubit.restaurantrating.util.Util;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class UserRatesActivity extends ListActivity implements OnClickListener {
	private JSONArray jUserRates;

	private ListView lv;
	private UserRatesListAdapter listAdapter;
	private ArrayList<UserRate> userRatesList = null;
	//private Runnable viewUserRates;
	//private ProgressDialog m_ProgressDialog = null;
	private static final int AUTHORIZATION_REQUEST = 1338;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_rates);
        Log.d("**********************************", "START");
 
        //povezem na streznik
        Comm comm = new Comm(getString(R.string.server_url), null, null);
        ((RestaurantRating)getApplicationContext()).setComm(comm);

        //nastavim podatke za fsq dostop
    	Foursquare fsq = createFoursquare();
    	((RestaurantRating)getApplicationContext()).setFoursquare(fsq);

		//nastavim userja
        //prijava poteka
        //checkLoginFromPrefs()
        //ce je user f prefsih, preverim oauth ali ima avtoriziran dostop do 4sq
        	//ce nima oauth naredim autorizacijo in podatke o autorizaciji vpisem v bazo
        //ce user ni v prefsih
        	//naredim oauth
        	//naredim users/self/lists
        	//podatke o autorizaciji in ostale podatke (id, firstname, lastname) vpisem v bazo in v prefse
        //getUser("marko", "okram");
        
        checkUserOAuth();
		
        User user = Util.getUserFromPreferencies();	
		if (user == null) {
			//naredim users/self/lists
        	//podatke (id, firstname, lastname) vpisem v bazo in v prefse
	        getUser("marko", "okram"); //tole pol zakomentiram
		}
		Util.SetUserOAuth(((RestaurantRating)getApplicationContext()).getFoursquare().getUserOAuth());
        
		try  { 
			fsq.setCategories();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        userRatesList = new ArrayList<UserRate>();
        this.listAdapter = new UserRatesListAdapter(this, R.layout.user_rates_list, userRatesList);
        setListAdapter(this.listAdapter);

        lv = (ListView) findViewById(android.R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> a, View v, int position, long id) {		    	
		    	try {
		    		JSONObject jobject = (JSONObject) jUserRates.getJSONObject(position);

		    		Intent intentRestaurantRate = new Intent(UserRatesActivity.this, RestaurantRateActivity.class);
				  	Bundle extras = new Bundle();	
				  	extras.putString("restaurant_id", jobject.getString("restaurantId"));
				  	//extras.putString("restaurant", jobjectRestaurant.toString());
				  	extras.putBoolean("user_rate", true);
				  	intentRestaurantRate.putExtra("si.kubit.restaurantrating.RestaurantRateActivity", extras);
				  	intentRestaurantRate.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	    			UserRatesActivity.this.startActivity(intentRestaurantRate);

		    	} catch (Exception e) {e.printStackTrace();}
		    }				
		});	      
		
		View mapButtonSubmit = findViewById(R.id.button_map); 
		mapButtonSubmit.setOnClickListener(this);
		View settingsButtonSubmit = findViewById(R.id.button_settings); 
		settingsButtonSubmit.setOnClickListener(this);
		View friendsButtonSubmit = findViewById(R.id.button_friends); 
		friendsButtonSubmit.setOnClickListener(this);
		View rateButtonSubmit = findViewById(R.id.button_rate); 
		rateButtonSubmit.setOnClickListener(this);
		View userButtonSubmit = findViewById(R.id.button_user); 
		userButtonSubmit.setOnClickListener(this);
	
    } 
    
    private void checkUserOAuth()
    {
    	try { 
			User user = Util.getUserFromPreferencies();	
	    	if (user==null || user.getOauthToken()==null || user.getOauthToken().equals("null")) {
				//uporabnik nima autorizacije. Zahtevam
    			Intent authorization = new Intent(this, AuthorizationActivity.class); 
    			startActivityForResult(authorization, AUTHORIZATION_REQUEST); 
		    } else {
    			((RestaurantRating)getApplicationContext()).getFoursquare().setUserOAuth(user.getOauthToken());
		    }
   		} catch (Exception ne) {
   			ne.printStackTrace();
        	Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		}
    }      

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == AUTHORIZATION_REQUEST) {
    		if (resultCode == RESULT_OK) {
    			String accessToken = data.getStringExtra("accessToken");
    			//shrani oatuh za userja
    			((RestaurantRating)getApplicationContext()).getFoursquare().setUserOAuth(accessToken);
        	} else if (resultCode == RESULT_CANCELED) {
        		if (data != null) {
		        	Toast toast = Toast.makeText(this, getString(R.string.oauth_requered), Toast.LENGTH_LONG);
		        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
		        	toast.show();
        		}
	        }
    	}
    }

    private Foursquare createFoursquare() {
		//pridobim serverske nastavitev za komunikacijo z 4SQ
    	Foursquare fsq = null;
		try {
			String settings = ((RestaurantRating)getApplicationContext()).getComm().get("settings");
    		fsq = new Foursquare(settings);
    		
        } catch (SocketException e) {
        	e.printStackTrace();
        	Toast toast = Toast.makeText(this, getString(R.string.conn_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		} catch (Exception ne) {
   			ne.printStackTrace();
        	Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		}
    	
		return fsq;
    }
    
    private void getUser(String username, String password) {
		//preverim kateri uporabnik je prijavljen in ga vpi�em v shared preferncies
        try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", username));
	        nameValuePairs.add(new BasicNameValuePair("password", password));
	        
	        String user = ((RestaurantRating)getApplicationContext()).getComm().post("login",nameValuePairs);
	        Util.addPreferencies("user", user);
        } catch (SocketException e) {
        	Toast toast = Toast.makeText(this, getString(R.string.conn_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		} catch (Exception ne) {
   			ne.printStackTrace();
        	Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		}
    }

	@Override
	protected void onResume() { 
		super.onResume();
		getUsersRatesList();
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
		//finish(); 
    }

    public void onClick(View v) {
    	switch (v.getId()) { 
			case R.id.button_map:
    			Intent mapUsers = new Intent(this, MapFriendsActivity.class); 
    			startActivity(mapUsers); 
				break;
			case R.id.button_settings:
				break;
			case R.id.button_friends:
				getUsersRatesList();
				break;
			case R.id.button_rate:
    			Intent restaurants = new Intent(this, RestaurantsActivity.class); 
    			startActivity(restaurants); 
				break;
			case R.id.button_user:
				break;
    	}
	}
    
    
    private void getUsersRatesList()
    {
    	String userRates = "";
        try { 
        	userRates = ((RestaurantRating)getApplicationContext()).getComm().get("userrates");
        	jUserRates = (JSONArray)new JSONTokener(userRates).nextValue();
        	
        	getUserRates();
        } catch (SocketException e) {
        	Toast toast = Toast.makeText(this, getString(R.string.conn_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		} catch (Exception ne) {
   			Toast toast = Toast.makeText(this, getString(R.string.json_error), Toast.LENGTH_LONG);
        	toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        	toast.show();
   		}
    }      
 
    private void getUserRates(){
        try{
        	JSONArray jFriends = ((RestaurantRating)getApplicationContext()).getFoursquare().getFriends();
        	List friendsList = new ArrayList();
        	for(int i=0; i<jFriends.length(); i++) {
            		friendsList.add(jFriends.getJSONObject(i).getInt("id"));
        	}
        	User user = Util.getUserFromPreferencies();
        	
        	userRatesList = new ArrayList<UserRate>();
        	for(int i=0; i<jUserRates.length(); i++) {
        		JSONObject jobject = (JSONObject) jUserRates.getJSONObject(i);
        		UserRate ur = new UserRate();
        		if (!friendsList.contains(jobject.getInt("userId")) &&
        			jobject.getInt("userId") != user.getId()) 
        			continue;
        		ur.setAvgRate(jobject.getString("avgRate"));
        		ur.setUserName(jobject.getString("userName"));
        		ur.setUserSurname(jobject.getString("userSurname"));
        		ur.setRestaurantName(jobject.getString("restaurantName"));
        		ur.setRateHoursAgo(jobject.getString("rateHoursAgo"));
        		ur.setRateDateTime(jobject.getString("rateDateTime"));
        		userRatesList.add(ur);
        	}
          } catch (Exception e) { 
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
      }
	  
    private Runnable returnRes = new Runnable() {
    	public void run() {
    		listAdapter.clear();
            if(userRatesList != null && userRatesList.size() > 0){
            	listAdapter.notifyDataSetChanged();
                for(int i=0;i<userRatesList.size();i++)
                	listAdapter.add(userRatesList.get(i));
            }
            listAdapter.notifyDataSetChanged();
        }
      };

}