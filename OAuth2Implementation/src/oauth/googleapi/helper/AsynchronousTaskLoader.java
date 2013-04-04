package oauth.googleapi.helper;

import java.util.ArrayList;
import java.util.List;

import oauth.googleapi.authentication.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsynchronousTaskLoader extends AsyncTask<String, Void, JSONObject> {

	static String TAG="AsynchronousTaskLoader";
	Activity activity;
	Handler handler;
	ServiceHelper helper;
	Bundle bundle;
	ProgressDialog dialog;
	
	public AsynchronousTaskLoader(Activity activity, Handler handler, Bundle bundle) {
		this.activity=activity;
		this.handler=handler;
		helper=new ServiceHelper();
		this.bundle=bundle;
	}
	
	protected JSONObject doInBackground(String...var) {

		JSONObject object=null;
		
		//String url=bundle.getString("url");
		String url=var[0];
		if(url==null || url.trim().equals("")){
			Log.d(TAG,"URL is invalid. Aborting Network Call.");
			return null;
		}
			
		if (url.equals(AuthConstants.ACCESS_TOKEN_URL)) {
			String auth_code = var[1];//GoogleCalendarDataStore.getInstance().getAuthorizationCode();
			Log.d(TAG,auth_code);
			
			if (auth_code == null || auth_code.trim().equals("")) {
				Log.d(TAG, "Auth Code is invalid. Aborting Network Call.");
				return null;
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("code",auth_code));
			params.add(new BasicNameValuePair("client_id",AuthConstants.CLIENT_ID));
			//params.add(new BasicNameValuePair("redirect_uri", AuthConstants.REDIRECT_URI));
			params.add(new BasicNameValuePair("grant_type",AuthConstants.GRANT_TYPE));
			object=helper.getHttpPostResponse(url,params, null,AuthConstants.CONTENT_TYPE_ENCODING);
		}
		else if(url.startsWith(AuthConstants.CALENDAR_LIST_URI)){
			String request=var[0];
			Log.d(TAG, request);
			object=helper.getHttpGetResponse(request, activity.getBaseContext());
		}
		
		if(object==null)
			Log.d(TAG,"doInBackground: JSONObject retrived from the server is null. Aborting the Network call");
		return object;
	}
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}
	
	protected void onPreExecute() {
		//super.onPreExecute();
		dialog=new ProgressDialog(activity);
		dialog.setMessage(activity.getResources().getString(R.string.please_wait_message));
		dialog.show();
	}

	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
		
		if(dialog!=null && dialog.isShowing())
	    	  dialog.cancel();
		if(result==null)
			return;
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
	
}
