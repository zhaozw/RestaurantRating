package si.kubit.restaurantrating;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class UserRatesActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_rates);
        Log.d("**********************************", "START");
		GetUsersRatesList();

		
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

	@Override
	protected void onResume() { 
		super.onResume();
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
		finish(); 
    }

    public void onClick(View v) {
    	switch (v.getId()) { 
			case R.id.button_map:
				break;
			case R.id.button_settings:
				break;
			case R.id.button_friends:
				break;
			case R.id.button_rate:
    			Intent restaurants = new Intent(this, RestaurantsActivity.class); 
    			startActivity(restaurants); 
				break;
			case R.id.button_user:
				break;
    	}
	}
    
    
    private void GetUsersRatesList()
    {
    	String userRates = "";
        Comm c = new Comm(getString(R.string.server_url), null, null);
        try { 
        	userRates = c.get("userrates");
        } catch (Exception e) {
			showMessageBox(Constants.MESSAGE_BOX_CLOSE_TIME+"", "false", getString(R.string.json_error), getString(R.string.json_title));
   		}
    	
        ListView lv = (ListView) findViewById(R.id.user_rates_list);
        UserRatesListAdapter listAdapter = new UserRatesListAdapter(this, userRates, getApplicationContext());
		lv.setAdapter(listAdapter);
    }      
 
    
	private void showMessageBox(String closeTime, String redirect, String msg, String title) {
		Intent messageBox = new Intent(this, MessageBox.class);
    	messageBox.putExtra("redirect", redirect);
    	messageBox.putExtra("closeTime", closeTime);
		messageBox.putExtra("msg", msg);
		messageBox.putExtra("title", title);
		startActivityForResult(messageBox, 1);
	}    
}