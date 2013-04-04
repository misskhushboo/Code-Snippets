package oauth.googleapi.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author khushboo
 * This encaptulates the network layer. All the network communication, HTTP or HTTPS, are done through this class.
 */
public final class ServiceHelper{
	
	private static String TAG="ServiceHelper";
	
	public JSONObject getHttpPostResponse(String requestUrl, List<NameValuePair> requestBody, JSONObject requestJSONObject, String content_type){

		JSONObject responseJSONObject = null;
		try 
		{
    		HttpClient httpClient = new DefaultHttpClient();	
    		HttpPost post = new HttpPost(requestUrl);//URL
            post.setHeader("Content-Type",content_type);
            
            if(requestBody!=null)
            	post.setEntity(new UrlEncodedFormEntity(requestBody,"utf-8"));
            else{
            	post.setEntity(new StringEntity(requestJSONObject.toString(), "UTF-8"));
            	Log.d(TAG,"Sending JSON="+requestJSONObject.toString());
            }
    		Log.d(TAG,"HttpPost Request Url="+requestUrl);
    		HttpResponse response=null;
			try {
				response = httpClient.execute(post);
			} catch (IOException e) {
				Log.d(TAG,"1. "+ e.toString());
				return null;
			}
    		if(response==null){
    			Log.d(TAG,"2. HttpResponse is null");
    			return null;
    		}
            HttpEntity responseEntity = response.getEntity();
			
			//char[] buffer = new char[(int)responseEntity.getContentLength()];
            char[] buffer = new char[5000];
			InputStream stream = responseEntity.getContent();
			int count=-1;
			StringBuilder out = new StringBuilder();

			do{
				Reader reader = new InputStreamReader(stream);
				count=reader.read(buffer, 0, buffer.length);
				if(count>0)
					 out.append(buffer, 0, count);
			}while(count>=0);
			
			stream.close();
			String str=out.toString();
			int status_code=response.getStatusLine().getStatusCode();
			
			Log.d(TAG,"code="+status_code);
			Log.d(TAG,"Reason="+response.getStatusLine().getReasonPhrase());
			Log.d(TAG,"JSONResponse="+str);
			
			if(status_code==200 || status_code==401){
				responseJSONObject = new JSONObject(out.toString());
				responseJSONObject.put("tpg_status_code", status_code);
			}
			
		} catch (ClientProtocolException e) {
			Log.d(TAG,"3. Service Helper Exception", e);
			
		} catch (IOException e) {
			Log.d(TAG,"4. Service Helper Exception", e);
		} catch (Exception e) {
			Log.d(TAG,"5. Service Helper Exception", e);
		}
		return responseJSONObject;		
	}
	
	
	public JSONObject getResponse(String requesturl, Context context){		//Only Http connection
		if (!(checkNetworkConnectivity(context)))
			return null ;

		InputStream in = null;
		int BUFFER_SIZE = 2000;
		String  text = "";
		
		JSONObject responseJSONObject=null;
		try {
			in = openHttpConnection(requesturl, context);
			if(in==null){
				Log.d(TAG,"Inputstream connection cannot be made");
				return null;
			}
			InputStreamReader isr = new InputStreamReader(in);
			int charRead;

			char[] inputBuffer = new char[BUFFER_SIZE];

			while ((charRead = isr.read(inputBuffer))>0)
			{                    
				//---convert the chars to a String---
				String readString = String.copyValueOf(inputBuffer, 0, charRead);                    
				text += readString;
				inputBuffer = new char[BUFFER_SIZE];
				Log.d(TAG,text);
			}
			responseJSONObject = new JSONObject(text);
		}catch (IOException e) {
			Log.d(TAG,e.toString());
		}
		catch (JSONException e) {
			Log.d(TAG,e.toString());
		}
		finally{
			if(in!=null)
				try{
					in.close();
				}catch(IOException e){
					Log.d(TAG,"Exception while closing the inputstream");
				}
		}
		return responseJSONObject;
	}
	
	private InputStream openHttpConnection(String urlStr, Context context) {

		InputStream in = null;
		int resCode = -1;

		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			Log.d(TAG,"1. "+e.getMessage());
			return null;
		}
		URLConnection urlConn;
		try {
			urlConn = url.openConnection();
		} catch (IOException e) {
			Log.d(TAG,"2. "+e.toString());
			return null;
		}

		HttpURLConnection httpConn = (HttpURLConnection)urlConn;

		httpConn.setReadTimeout(10000 /* milliseconds */);
		httpConn.setConnectTimeout(15000 /* milliseconds */);
		httpConn.setAllowUserInteraction(false);
		httpConn.setInstanceFollowRedirects(true);
		try {
			httpConn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			Log.d(TAG,e.toString());
			return null;
		}
		httpConn.setDoOutput(true);
		httpConn.setRequestProperty("Content-Type", AuthConstants.CONTENT_TYPE_ENCODING);

		try {
			httpConn.connect();
		} catch (IOException e) {
			Log.d(TAG,"3. "+e.toString());
			return null;
		} 
		try{
			Log.d(TAG,"Response code="+httpConn.getResponseCode());
			Log.d(TAG,"Content Length="+httpConn.getContentLength()+" content type="+httpConn.getContentType());
		}catch(Exception e){
			Log.d(TAG,"4."+e.toString());
		}
		try {
			in = httpConn.getInputStream();
		} catch (IOException e) {
			Log.d(TAG,"5."+e.toString());
		}                                 
		return in;
	}

	public boolean checkNetworkConnectivity(Context context) {
	    ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    } else {
	       return false;
	    }
	}
	
	public JSONObject getHttpGetResponse(String requestUrl, Context context){
		
		JSONObject responseJSONObject=null;
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(requestUrl);
        
        HttpResponse response=null;
		try {
			Log.d(TAG,"GetHttpGetResponse: "+requestUrl);
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			Log.d(TAG,"1. "+e.toString());
			return null;
		} catch (IOException e) {
			Log.d(TAG,"2. "+e.toString());
			return null;
		}
        int status_code=response.getStatusLine().getStatusCode();
        Log.d(TAG,"Response Code returned against the request="+status_code);
        
        BufferedReader in=null;
		try {
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			Log.d(TAG,"3. "+e.toString());
		} catch (IOException e) {
			Log.d(TAG,"4. "+e.toString());
		}
        
		StringBuffer sb = new StringBuffer("");
        String l = "";
        String nl = System.getProperty("line.separator");
        try {
			while ((l = in.readLine()) !=null){
			    sb.append(l + nl);
			}
			String data = sb.toString();
			Log.d(TAG,data);
		} catch (IOException e) {
			Log.d(TAG,"5. "+e.toString());
		}
        try {
			in.close();
		} catch (IOException e) {
			Log.d(TAG,"6. "+e.toString());
		}
        String data = sb.toString();
        
        try {
			if(status_code==200 || status_code==401){
				responseJSONObject = new JSONObject(data);
				responseJSONObject.put("tpg_status_code", status_code);
			}
		} catch (JSONException e) {
			Log.d(TAG,"6. "+e.toString());
		}
        return responseJSONObject;
    } 
}
