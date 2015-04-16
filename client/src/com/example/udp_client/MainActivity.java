package com.example.udp_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	 static {
	        System.loadLibrary("alljoyn_java");
	    }
	Handler mhandle;
	String ipstring;
	int portsend;
	SendThread mSendThread=null;
	Button listen;
	SimpleInterface mSimpleInterface;  	
	BusAttachment mBus;
	
    class SimpleService implements SimpleInterface, BusObject {

        public String Ping(String inStr) {      	
			Bundle b = new Bundle();
			Message msg = mhandle.obtainMessage();				
			b.putString("data", inStr);
			msg.setData(b);
			mhandle.sendMessage(msg);
            return inStr;
        }
        
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listen=(Button)findViewById(R.id.listenBtn);
		mSendThread=new SendThread();
		mSendThread.init();
		mhandle = new Handler() { // receive processing

			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String str = new String();
				str = msg.getData().getString("data");	
				String temp=((TextView)findViewById(R.id.receiveText)).getText().toString();
				temp+="\n";
				temp+=str;
				((TextView)findViewById(R.id.receiveText)).setText(temp);
			}
		};
		
	}
	public void ListenBtn(View v) {
	
//		listen.setText("ing");
//		if(mSendThread==null){
//			mSendThread=new SendThread();
//		}
//		
//		mSendThread.init("org.alljoyn.bus.samples.simple",
//					(short)42
//					);
		mSendThread.start();
	}

	public void SendBtn(View v) {
		
//		
		String temp=((EditText)findViewById(R.id.content)) .getText() .toString();
		mSendThread.send(temp);
	}


	 class SendThread extends Thread {
		 private BusAttachment mBus;
		 private ProxyBusObject mProxyObj;
		 private static final short CONTACT_PORT=42;
		 SessionOpts sessionOpts;
		 Mutable.IntegerValue sessionId;
		 boolean runflag=false;
		 String NAME;
		 public void init()
	 	{ 	
			 mBus = new BusAttachment(getClass().getName(), BusAttachment.RemoteMessage.Receive);

		     Status status = mBus.connect();
		      if (Status.OK != status) { 
		    	  Toast.makeText(getApplicationContext(), "connect error", Toast.LENGTH_SHORT).show();
		        return;
		      }
		      mBus.registerBusListener(new BusListener() {
		    	  @Override
		    	  public void foundAdvertisedName(String name,short transport, String namePrefix) {
		    		  
		       
		    		  short contactPort = CONTACT_PORT;
		    		  sessionOpts = new SessionOpts();
		    		  sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES; 
		    		  sessionOpts.isMultipoint = false; 
		    		  sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
		    		  sessionOpts.transports = SessionOpts.TRANSPORT_ANY; 
		    		  sessionId = new Mutable.IntegerValue();
		    		  NAME=name;
		    		  synchronized(SendThread.this){
		    		  runflag=true;
		    		  }
		    	  }
		      });
		
		    
		     
		      

		      status = mBus.findAdvertisedName("org.alljoyn.bus.samples.simple");
		      	if (status != Status.OK) { 
		      		Toast.makeText(getApplicationContext(), "findName error", Toast.LENGTH_SHORT).show();
		      		return;
		      	}

	 	}
	
		 public void send(String buff)
		 {
			 try {
				   // will return "Hello World"
				   String strPing = mSimpleInterface.Ping("Hello World");
				   // will return "The Eagle has landed!"
				  
				} catch (BusException e) {
				   //Handle exception here.
				}

		 }
		 public void run()
		 {   boolean go=true;
			 while(go)
			 {
				  synchronized(SendThread.this){
		    		  if(runflag==false)continue;
		    		  }
				
				
					 Status status = mBus.joinSession(NAME, 
							 CONTACT_PORT,
				    		   sessionId, 
				    		   sessionOpts,
				    		   new SessionListener());

				    		  mProxyObj = mBus.getProxyBusObject("org.alljoyn.bus.samples.simple", "/SimpleService",sessionId.value,new Class[] { SimpleInterface.class });
				    		  mSimpleInterface = mProxyObj.getInterface(SimpleInterface.class);
				    		  if(status==Status.OK)go=false;
				 
			 }
		 }
	 }

		 
		 

		 
	  
}
				
			
		
	   
    