package oauth.googleapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import oauth.googleapi.authentication.AuthCodeWebview;
import oauth.googleapi.authentication.R;
import oauth.googleapi.helper.AuthConstants;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SocketService extends Service {

	Socket socket;
	ServerSocket serverSocket;
	
	String response;
	private static String TAG="SocketService";
	String SERVER_IP;
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     * We need to extend the Binder when the Service is bound by the calling Activity.
     */
	/*public class LocalBinder extends Binder {
		
		SocketService socketService;
		public LocalBinder(SocketService socketService) {
			this.socketService=socketService;
		}

		public SocketService getService() {
	         return socketService;
	    }
	}
*/
	public IBinder onBind(Intent arg0) {
		//return new LocalBinder(this);
		return null;
	}
	/*------------------------------------------------------------------------------*/
	public void onCreate() {
		super.onCreate();
		Thread t=new Thread(listener);
		t.start();
	}
	
	Runnable listener=new Runnable(){
		public void run(){
			try {
				InetAddress add=null;
				try {
					add = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				SERVER_IP=add.getHostAddress();
				
				serverSocket=new ServerSocket(AuthConstants.SERVER_PORT);
				Log.d(TAG,"Ip address on which the mobile is listening:"+SERVER_IP+"  on port="+AuthConstants.SERVER_PORT);
				socket= serverSocket.accept();
				Log.d(TAG,"Connected with the Google Redirect Uri");
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try{
				InputStream stream=socket.getInputStream();
			    BufferedReader stdIn = new BufferedReader(new InputStreamReader(stream));
			    
			    String userInput="";

			    while ((userInput = stdIn.readLine()) != null) {
			        response=response+userInput;
			    }
			    Log.d(TAG,"Auth code Response:"+response);
			    if(response.contains("code") & response.contains("=")){
		        	String parts[]=response.split("=");
		        	
		        	if(parts!=null && parts[0]!=null){
		        		Log.d(TAG, parts[1]);
		        		String partition[]=parts[1].split(" ");
		        	
		        		if(partition!=null && partition[0]!=null)
		        			Log.d(TAG,"Auth code is:"+partition[0]);
		        		sendMessageToActivity(partition[0]);
		        	}
		        }
			    }catch ( Exception e){
			        String neil = e.getMessage();
			        neil = neil + "";
			        Log.d(TAG,"1. "+e.toString());
			    }
			    try {
			    	if(socket!=null)
			    		socket.close();
				} catch (IOException e) {
					 Log.d(TAG,"2. "+e.toString());
				}		
		}
	};
	
    protected void onStop() {
       
        try {
             serverSocket.close();
             Log.d(TAG,"Socket Service class is stopped. Now preserving battery!");
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    
    private void sendMessageToActivity(String auth_code) {
    	Intent intent=new Intent("intercept.auth.code");
    	intent.putExtra("auth_code", auth_code);
    	intent.setPackage(getResources().getString(R.string.package_broadcast));		//Making it local broadcast.
    	sendBroadcast(intent);
	}

}
