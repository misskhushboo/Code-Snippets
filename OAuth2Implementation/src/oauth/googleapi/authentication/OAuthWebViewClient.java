package oauth.googleapi.authentication;

import java.util.ArrayList;
import java.util.List;

import oauth.googleapi.helper.AsynchronousTaskLoader;
import oauth.googleapi.helper.AuthConstants;
import oauth.googleapi.helper.GoogleCalendarDataStore;
import oauth.googleapi.helper.ServiceHelper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * @author Khushboo.kaur
 * This class is an implementation of WebViewClient. This implementation is useful when the application uses the first way of REDIRECT URI. 
 * In the second way (Using SocketService.java), this implementation is not required.
 */
public class OAuthWebViewClient extends WebViewClient {
	
	static String TAG="OAuthWebViewClient";
	Activity activity;
	Handler handler;
	
	public OAuthWebViewClient(Activity activity, Handler handler) {
		this.activity=activity;
		this.handler=handler;
	}
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url); 
		Log.d(TAG,"shouldOverrideUrlLoading:  Url="+url);
		return false; 
	}

	public void onPageFinished(WebView view, String url) {
		
		super.onPageFinished(view, url);
		Log.d(TAG,"onPageFinished:  url="+url+"  title="+view.getTitle());
		
		String title=view.getTitle();
		
		if(title!=null && title.startsWith("Success")){
			String parts[]=title.split("=");
			if(parts!=null && parts.length>=2 && parts[1]!=null){
				retrieveAccessCode(parts[1]);
			}
			else{
				Log.d(TAG,"The title cannot be split : "+title);
			}
		}
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
				params.add(new BasicNameValuePair("redirect_uri", AuthConstants.REDIRECT_URI));
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
}
