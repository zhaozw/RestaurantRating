package si.kubit.restaurantrating;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RestaurantTipsListAdapter extends BaseAdapter {

	  private Activity activity;
	  private JSONArray jdata;
	  private static LayoutInflater inflater=null;
	  private Context context;
	 
	  public RestaurantTipsListAdapter(Activity a, JSONArray jdata, Context context) {
	      this.activity = a;  
	      this.context = context;
	      this.jdata = jdata;
	      
	      RestaurantTipsListAdapter.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);      
	  }
		
	  public int getCount() {
	  	return jdata.length();    	
	  }
	 
	  public Object getItem(int position) {
	      return position;
	  }
	 
	  public long getItemId(int position) {
	      return position;
	  }
	 
	  public static class ViewHolder{
	      public TextView textTip;
	      public TextView textUser;
	      public ImageView imageUser;
	  }
	 
	  public View getView(int position, View convertView, ViewGroup parent) {
	      View vi=convertView;
	      ViewHolder holder;
	      if(convertView==null){
	    	  vi = inflater.inflate(R.layout.restaurant_tips_list, null);
	 
	          holder=new ViewHolder();
	          holder.textTip=(TextView)vi.findViewById(R.id.text_tip);
	          holder.textUser=(TextView)vi.findViewById(R.id.text_user);
	          holder.imageUser = (ImageView)vi.findViewById(R.id.icon_user);
	          
	          vi.setTag(holder);
	      }
	      else {
	          holder=(ViewHolder)vi.getTag();
	      }

	      try {
	    	  JSONObject jobject = (JSONObject) jdata.getJSONObject(position);
		      holder.textTip.setText(jobject.getString("text"));
			  holder.textUser.setText(((JSONObject)jobject.getJSONObject("user")).getString("firstName"));
			  String imageStr = ((JSONObject)jobject.getJSONObject("user")).getString("photo");
			  Drawable image = ImageOperations(imageStr);
			  holder.imageUser.setImageDrawable(image);
	      } catch (Exception e) {}
	      
	      return vi;
	  }
	  
	  
	  private Drawable ImageOperations(String urlString) {
			try {
				URL url = new URL(urlString);
				InputStream is = (InputStream) url.getContent();
				Drawable d = Drawable.createFromStream(is, "src");
				return d;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}	
	  
}