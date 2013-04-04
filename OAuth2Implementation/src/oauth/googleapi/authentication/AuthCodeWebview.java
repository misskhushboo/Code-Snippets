package oauth.googleapi.authentication;

import java.util.LinkedList;
import java.util.List;

import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.service.SocketService;
import oauth.googleapi.ui.CalendarListActivity;
import oauth.googleapi.ui.GoogleCalendarBase;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.webkit.WebView;

public class AuthCodeWebview extends GoogleCalendarBase {

	static String TAG="AuthCodeWebview";
	SocketService mService;
	boolean  mIsBound;
	String broadcast_action="intercept.auth.code";
	Intent serviceIntent;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_webview);        
		WebView webview=(WebView)findViewById(R.id.webView1);
		String request= addParamsToUrl(AuthConstants.AUTH_CODE_URL);

		webview.loadUrl(request);
		Log.d(TAG,"Loading the url in webview:"+request);
		
		webview.getSettings().setJavaScriptEnabled(true);
		//Ist WAY	REDIRECT URI
		//webview.setWebViewClient(new OAuthWebViewClient(this, handler));				
		//After the webview client is set, see the Handler code in this class. This handler handles the response of the google server and initiates the application
		
		/*-----------------------------------------------------*/	
		
		// 2nd WAY	LOCALHOST REDIRECT URI
		// Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process. 
		serviceIntent=new Intent(this, SocketService.class);
		startService(serviceIntent);
	}
	
	
	protected String addParamsToUrl(String url){
		if(!url.endsWith("?"))
			url += "?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();

		params.add(new BasicNameValuePair("client_id",AuthConstants.CLIENT_ID ));
		params.add(new BasicNameValuePair("redirect_uri",AuthConstants.REDIRECT_URI_LOCALHOST));
		params.add(new BasicNameValuePair("response_type", AuthConstants.AUTH_CODE_RESPONSE_TYPE));
		params.add(new BasicNameValuePair("scope",AuthConstants.SCOPE));	

		String paramString = URLEncodedUtils.format(params, "utf-8");

		url += paramString;
		return url;
	}
	
	//Unregister the Broadcast receiever
	
	/*Handler handler=new Handler(){

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(msg==null){
				Log.d(TAG,"Message object is null. Please login to google account again");
				return;
			}
			
			Object obj=msg.obj;
			
			if(obj==null || !(obj instanceof JSONObject)){
				Log.d(TAG,"AuthCodeWebView Handler has null message body or not an instance of JSONObject");
				return;
			}
			JSONObject responseJSONObject=(JSONObject)obj;
			
			//Handling status code 401 from Google server
			if(msg.what==401){
				String message=getResources().getString(R.string.login_required_401_error);
				try {
					JSONObject error=responseJSONObject.getJSONObject("error");
					if(error!=null && error.has("message")){
						message=null;
						message=error.getString("message");
					}
				} catch (JSONException e) {
					Log.d(TAG,e.toString());
				}
				//showOkAlert(message, true, AuthCodeWebview.this);
				return;
			}
			
			//Handling status code 200 from Google server
			String accessToken="", refreshToken="", tokenType="";
			int expireTime=0;
			try {
				accessToken = responseJSONObject.getString("access_token");
				tokenType =responseJSONObject.getString("token_type");  //Bearer		//not saved as of now
				expireTime=responseJSONObject.getInt("expires_in");	//3600
				refreshToken=responseJSONObject.getString("refresh_token");
				
			} catch (JSONException e) {
				Log.d(TAG,e.toString());
			}
			GoogleCalendarDataStore dataStore=GoogleCalendarDataStore.getInstance();
			dataStore.setAccessToken(accessToken);
			dataStore.setExpirationTime(expireTime);
			dataStore.setRefreshToken(refreshToken);
			
			Intent intent=new Intent(AuthCodeWebview.this, CalendarListActivity.class);
			startActivity(intent);
		}
	};
*/
	
	

	/*ServiceConnection service_conn=new ServiceConnection() {
		// This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        // Because it is running in our same process, we should never
        // see this happen.
		public void onServiceDisconnected(ComponentName name) {
			mService=null;
			mIsBound=false;
		}
		// This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
		//Local Service is now connected.
		public void onServiceConnected(ComponentName name, IBinder service) {
			 mService = ((LocalBinder) service).getService();
			 mIsBound=true;
		}
	};*/
	
	//When activity closed, the service has to be unbound
	protected void onDestroy() {
		if(mIsBound){
			//unbindService(service_conn);
			stopService(serviceIntent);
			mIsBound=false;
		}
		super.onDestroy();
    }
	
}
