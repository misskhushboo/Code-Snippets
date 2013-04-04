package oauth.googleapi.recievers;

import java.util.ArrayList;
import java.util.List;

import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.helper.ServiceHelper;
import oauth.googleapi.ui.CalendarListActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AuthorizationCodeReciever extends BroadcastReceiver {

	private static String  TAG="AuthorizationCodeReciever";
	Context context;
	
	public void onReceive(Context context, Intent intent) {
		String auth_code=intent.getStringExtra("auth_code");
		this.context=context;
		Log.d(TAG,"Reciever is called. The auth code is:"+auth_code);
		retrieveAccessCode(auth_code);
	}

	private void retrieveAccessCode(final String auth_code) {
		if(auth_code==null || auth_code.trim().length()==0 )
			return;
		
		GoogleCalendarDataStore.getInstance().setAuthorizationCode(auth_code);
		
		//AsynchronousTaskLoader async=new AsynchronousTaskLoader(activity, handler, bundle);
		//async.execute(AuthConstants.ACCESS_TOKEN_URL, auth_code);
		Thread t=new Thread(new Runnable() {
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("code",auth_code));
				params.add(new BasicNameValuePair("client_id",AuthConstants.CLIENT_ID));
				params.add(new BasicNameValuePair("redirect_uri", AuthConstants.REDIRECT_URI));	//Check this.
				params.add(new BasicNameValuePair("grant_type",AuthConstants.GRANT_TYPE));	

				JSONObject result=new ServiceHelper().getHttpPostResponse(AuthConstants.ACCESS_TOKEN_URL, params, null,AuthConstants.CONTENT_TYPE_ENCODING);
				Message message=handler.obtainMessage();
				message.obj=result;

				try {
					if(result.has("tpg_status_code"))
						message.what=result.getInt("tpg_status_code");
				} catch (JSONException e) {
					message.what=0;
					Log.d(TAG,"Http status code is unknown");
				}
				handler.handleMessage(message);
			}
		});
		t.start();
	}
	Handler handler=new Handler(){

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(msg==null){
				Log.d(TAG,"Message object is null. Please login to google account again");
				return;
			}
			
			Object obj=msg.obj;
			
			if(obj==null || !(obj instanceof JSONObject)){
				Log.d(TAG,"AuthorizationCodeReciever Handler has null message body or not an instance of JSONObject");
				return;
			}
			JSONObject responseJSONObject=(JSONObject)obj;
			
			//Handling status code 401 from Google server
			if(msg.what==401){
				String message=context.getResources().getString(R.string.login_required_401_error);
				try {
					JSONObject error=responseJSONObject.getJSONObject("error");
					if(error!=null && error.has("message")){
						message=null;
						message=error.getString("message");
					}
				} catch (JSONException e) {
					Log.d(TAG,e.toString());
				}
				return;
			}
			
			//Handling status code 200 from Google server
			String accessToken="", refreshToken="", tokenType="";
			int expireTime=0;
			try {
				accessToken = responseJSONObject.getString("access_token");
				tokenType =responseJSONObject.getString("token_type");  			//Bearer		//not saved as of now
				expireTime=responseJSONObject.getInt("expires_in");					//3600
				refreshToken=responseJSONObject.getString("refresh_token");
				
			} catch (JSONException e) {
				Log.d(TAG,e.toString());
			}
			GoogleCalendarDataStore dataStore=GoogleCalendarDataStore.getInstance();
			dataStore.setAccessToken(accessToken);
			dataStore.setExpirationTime(expireTime);
			dataStore.setRefreshToken(refreshToken);
			
			Intent intent=new Intent(context, CalendarListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	};
}
