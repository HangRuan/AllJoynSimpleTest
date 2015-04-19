package com.example.udp_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	 static {
	        System.loadLibrary("alljoyn_java");
	    }
	Handler mhandle;
	String ipstring;
	int portsend;
	ListenThread lis=null;
	Button listen;
	SimpleService mSimpleService;
	BusAttachment mBus;
    /* The class that is our AllJoyn service.  It implements the SimpleInterface. */
    class SimpleService implements SimpleInterface, BusObject {

        /*
         * This is the code run when the client makes a call to the Ping method of the
         * SimpleInterface.  This implementation just returns the received String to the caller.
         *
         * This code also prints the string it received from the user and the string it is
         * returning to the user to the screen.
         */
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
		mSimpleService=new SimpleService();
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
	
		listen.setText("ing");
		if(lis==null){
			lis=new ListenThread();
		}
		
		lis.init("org.alljoyn.bus.samples.simple",
					Short.parseShort("42")
					);
		lis.start();
	}

	public void SendBtn(View v) {
		/*ipstring= ((EditText)findViewById(R.id.ip)) .getText() .toString();
		portsend=Integer.parseInt(  ((EditText)findViewById(R.id.port)) .getText() .toString() );
		String temp=((EditText)findViewById(R.id.content)) .getText() .toString();
		new SendHandleParam(ipstring,temp.getBytes()).start();*/
	}


	private class ListenThread extends Thread {
		private DatagramSocket sock = null;
		private boolean ListenRunning = false;
		private String buffstr = null;
		String SERVICE_NAME ;short CONTACT_PORT;
        
	
		public String getString() {
			return buffstr;
		}

		public void kill() {
			 mBus.unregisterBusObject(mSimpleService);
             mBus.disconnect();
             
		}

		public boolean init(String name,short port) {
			SERVICE_NAME=name;
			CONTACT_PORT=port;
			return true;
		}

		public void run() 
		{
			 org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
            
             mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

             mBus.registerBusListener(new BusListener());

             
             Status status = mBus.registerBusObject(mSimpleService, "/SimpleService");
     
             if (status != Status.OK) {
                 finish();
                 return;
             }

             status = mBus.connect();
      
             if (status != Status.OK) {
                 finish();
                 return;
             }
   
             Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

             SessionOpts sessionOpts = new SessionOpts();
             sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
             sessionOpts.isMultipoint = false;
             sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;

             
             sessionOpts.transports = SessionOpts.TRANSPORT_ANY + SessionOpts.TRANSPORT_WFD;

             status = mBus.bindSessionPort(contactPort, sessionOpts, new SessionPortListener() {
                 @Override
                 public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
                     if (sessionPort == CONTACT_PORT) {
                         return true;
                     } else {
                         return false;
                     }
                 }
             });
           
             if (status != Status.OK) {
               
                 return;
             }

             /*
              * request a well-known name from the bus
              */
             int flag = BusAttachment.ALLJOYN_REQUESTNAME_FLAG_REPLACE_EXISTING | BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE;

             status = mBus.requestName(SERVICE_NAME, flag);
    
             if (status == Status.OK) {
                 /*
                  * If we successfully obtain a well-known name from the bus
                  * advertise the same well-known name
                  */
                 status = mBus.advertiseName(SERVICE_NAME, sessionOpts.transports);
     
                 if (status != Status.OK) {
                     /*
                      * If we are unable to advertise the name, release
                      * the well-known name from the local bus.
                      */
                     status = mBus.releaseName(SERVICE_NAME);
       
                  
                     return;
                 }
             }

            
         }
	}
}
				
			
		
	   
    