package si.kubit.restaurantrating.conn;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;
import android.util.Log;

public class Foursquare {

	private String clientId;
	private String clientSecret;
	private String redirectURI;

	private String foursquareVenusUrl;
	private String foursquareVenusSearchUrl;
	private String foursquareVenusTipsUrl;
	private String foursquareVenusPhotosUrl;
	private String foursquareUrl;
	private String foursquareTipsAddUrl;
	private String foursquarePhotosAddUrl;

	private String foursquareAuthenticateUrl;
	private String foursquareAccessTokenUrl;

	private String venuesCategory;

	public Foursquare() {
	}

	public Foursquare(String settings) {
		try {
			JSONObject jSettings = ((JSONArray)new JSONTokener(settings).nextValue()).getJSONObject(0);
			this.clientId = jSettings.getString("clientId");
			this.clientSecret = jSettings.getString("clientSecret");
			this.redirectURI = jSettings.getString("redirectURI");
			this.foursquareVenusUrl = jSettings.getString("foursquareVenusUrl");
			this.foursquareVenusSearchUrl = jSettings.getString("foursquareVenusSearchUrl");
			this.foursquareVenusTipsUrl = jSettings.getString("foursquareVenusTipsUrl");
			this.foursquareVenusPhotosUrl = jSettings.getString("foursquareVenusPhotosUrl");
			this.foursquareUrl = jSettings.getString("foursquareUrl");
			this.foursquareTipsAddUrl = jSettings.getString("foursquareTipsAddUrl");
			this.foursquarePhotosAddUrl = jSettings.getString("foursquarePhotosAddUrl");
			this.foursquareAuthenticateUrl = jSettings.getString("foursquareAuthenticateUrl");
			this.foursquareAccessTokenUrl = jSettings.getString("foursquareAccessTokenUrl");
			this.venuesCategory = jSettings.getString("venuesCategory");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	
	private String send(String foursquareUrl, String foursquareParams, String serverParams, String outputParam, String method) throws IOException {

		String jsonResponse = "";

		
		String server = foursquareUrl + 
						foursquareParams +
						"&" + serverParams;
		Log.d("URL=",server);
		
		URL  url = new URL(server);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        con.setRequestMethod(method);
        con.setDoInput(true);
        if (method.equals("POST"))
        	con.setDoOutput(true);
        
        if (outputParam != null) {
        	Log.d("OUTPUT=",outputParam);
    		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        	out.write(outputParam);
        	out.close();
        }
        
        try 
    	{
          BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
          String inputLine;

          while ((inputLine = in.readLine()) != null) {
        	  jsonResponse += inputLine;
          }
          in.close();
    	} catch(Exception e) { 
    		System.out.println(e);
    		e.printStackTrace();
    	} finally {
	   		con.disconnect();
	   	}
    	
    	return jsonResponse;
       
	}

	public JSONArray getTips(String venueId) throws Exception {
		String tips = send(this.foursquareVenusTipsUrl.replace("VENUE_ID", venueId), getFoursquareVenuesParams(), "", null, "GET");
		JSONObject jdata = (JSONObject)new JSONTokener(tips).nextValue();
		JSONObject responseTips = (JSONObject)((JSONObject)jdata.get("response")).get("tips");
		return (JSONArray)(responseTips.get("items"));
	}

	public String addTip(String venue_id, String text, String oauth) throws Exception {

		String tip = send(this.foursquareTipsAddUrl, 
								getFoursquareOAuthParams(oauth), 
								"venueId="+venue_id+"&text="+URLEncoder.encode(text,"UTF-8"),
								null, 
								"POST");
		
		JSONObject jdata = (JSONObject)new JSONTokener(tip).nextValue();
		JSONObject responseTips = (JSONObject)((JSONObject)jdata.get("response")).get("tip");
		
		return ((JSONObject)((JSONObject)jdata.get("response")).get("tip")).toString();
		
	}
	
	public JSONArray getPhotos(String venueId) throws Exception {
		String tips = send(this.foursquareVenusPhotosUrl.replace("VENUE_ID", venueId), getFoursquareVenuesParams(), "", null, "GET");
		JSONObject jdata = (JSONObject)new JSONTokener(tips).nextValue();
		JSONObject responsePhotos = (JSONObject)((JSONObject)jdata.get("response")).get("photos");
		return (JSONArray)((JSONObject)((JSONArray)(responsePhotos.get("groups"))).get(1)).get("items");
	}

	
	public String uploadPhoto(String venueId, String oauthToken, File file) throws Exception {
		String uri = this.foursquarePhotosAddUrl+"?venueId="+venueId+"&oauth_token="+oauthToken;
		return upload(uri, file);
	}
	
	static public String upload(String uri, File file) throws Exception
	{
	       HttpURLConnection httpurlconnection = (HttpURLConnection)(new URL(uri)).openConnection();
	       httpurlconnection.setDoInput(true);
	       httpurlconnection.setRequestMethod("POST");
	       httpurlconnection.setRequestProperty("Accept", "application/json");

	       String s3 = Long.toString(System.currentTimeMillis(), 36);
	       httpurlconnection.setRequestProperty("Content-Type", (new StringBuilder()).append("multipart/form-data; boundary=----").append(s3).toString());
	       httpurlconnection.setDoOutput(true);
	       DataOutputStream dataoutputstream = new DataOutputStream(httpurlconnection.getOutputStream());
	       int i = 0;

	       dataoutputstream.writeBytes((new StringBuilder()).append("------").append(s3).append("\r\n").toString());
	       dataoutputstream.writeBytes((new StringBuilder()).append("Content-Disposition: form-data; name=\"").append(Uri.fromFile(file).toString()).append("\"; filename=\"").append(Uri.fromFile(file).toString()).append("\"\r\n").toString());
	       dataoutputstream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
	       BufferedInputStream bufferedinputstream = new BufferedInputStream(new FileInputStream(file));
	       for(int k = 0; (k = bufferedinputstream.read()) != -1;)
	           dataoutputstream.write(k);

	       bufferedinputstream.close();
	       dataoutputstream.writeBytes("\r\n");

	       dataoutputstream.writeBytes((new StringBuilder()).append("------").append(s3).append("--\r\n\r\n").toString());
	       dataoutputstream.flush();
	       dataoutputstream.close();
	       

	       if(httpurlconnection.getResponseCode() != 200)
	           throw new IllegalArgumentException((new StringBuilder()).append(httpurlconnection.getResponseCode()).append(" ").append(httpurlconnection.getResponseMessage()).append(" ").append(read(httpurlconnection.getErrorStream())).toString());
	       else
	           return read(httpurlconnection.getInputStream());
	   }

	    private static String read(InputStream inputstream)  throws Exception
		{
		    StringBuffer stringbuffer = new StringBuffer();
		    InputStreamReader inputstreamreader = new InputStreamReader(new BufferedInputStream(inputstream), "UTF-8");
		    for(int i = inputstreamreader.read(); i != -1; i = inputstreamreader.read())
		        stringbuffer.append((char)i);
		
		    inputstreamreader.close();
		    return stringbuffer.toString();
		}

		private String getFoursquareVenuesParams () {
			SimpleDateFormat dfYear=new SimpleDateFormat("yyyyMMdd");
			return "?client_id=" + this.clientId + 
					"&client_secret=" + this.clientSecret +
					"&v=" + dfYear.format(new Date()); 
		}


		private String getFoursquareOAuthParams (String oauth) {
			SimpleDateFormat dfYear=new SimpleDateFormat("yyyyMMdd");
			return "?client_id=" + this.clientId + 
					"&client_secret=" + this.clientSecret +
					"&oauth_token=" + oauth +
					"&v=" + dfYear.format(new Date()); 
		}	

	    
}