package flo.gameserver.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnTouchListener;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MainActivity1 extends Activity {

	String address = "192.168.178.92";
	int port = 1234;
	private Socket socketTCP;
	DatagramSocket socketUDP;
	private OutputStream streamOutTCP;
	float hold_x = -100;
	float hold_y = -100;
	String myID;
	Gson gson = new Gson();
	boolean hold = false;
	View mView;   
	TextView tv;
	final Context context = this;
	boolean connected = false;

	public enum PlayerAction{
		move,pressA,pressB
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mView = (GamePadView)findViewById(R.id.gamepadview_id);
		mView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!connected){
					if (event.getAction() == MotionEvent.ACTION_DOWN)
						notConnectedAlert();
					return true;
				}
				GamePadView view = (GamePadView)v;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					hold=true;
					hold_x = event.getX();
					hold_y = event.getY();
					view.setHoldCoordinates(hold_x,hold_y);
					view.invalidate(); //redraw dot

					return true;
				}else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					float cur_x=event.getX();
					float cur_y=event.getY();
					int vec_y = (int)(cur_y-hold_y);
					int vec_x = (int)(cur_x-hold_x);
					PlayerAction ac = PlayerAction.move;
					UserInput ui = new UserInput(myID,ac,vec_x,vec_y);
					String message = gson.toJson(ui);
					if(tv != null)
						tv.setText(message);
					sendDataUDP(message);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					hold=false;
					hold_x = -100;
					hold_y = -100;
					view.setHoldCoordinates(hold_x,hold_y);
					v.invalidate(); //redraw dot
				}
				return true;
			}
		});

		myID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); // muss vor reconnect aufgerufen werden
		reconnect();
	}
		

	


	public void onActionButtonAClick(View view) {
		PlayerAction ac = PlayerAction.pressA;
		UserInput ui = new UserInput(myID,ac,0,0);
		String message = gson.toJson(ui);
		sendDataUDP(message);
	}

	public void onActionButtonBClick(View view) {
		PlayerAction ac = PlayerAction.pressB;
		UserInput ui = new UserInput(myID,ac,0,0);
		String message = gson.toJson(ui);
		sendDataUDP(message);
	}

	 public void  notConnectedAlert(){
		 new AlertDialog.Builder(context)
		    .setTitle("Keine Verbindung")
		    .setMessage("Sie sind nicht verbunden. Überprüfen Sie die Server IP-Adresse unter Optionen.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {

		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
	 }

	public void  hostUnknownAlert(){
		new AlertDialog.Builder(context)
				.setTitle("Server IP-Adresse ungültig")
				.setMessage("Server IP-Adresse ungültig. Geben Sie eine gültige Server IP-Adresse ein.")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	public void  timeoutAlert(){
		new AlertDialog.Builder(context)
				.setTitle("Keine Verbindung")
				.setMessage("Es konnte keine Verbindung aufgebaut werden. Überprüfen Sie die Server IP-Adresse unter Optionen und versuchen Sie es erneut.")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	public void  connectedAlert(){
		new AlertDialog.Builder(context)
				.setTitle("Erfolgreiche Verbindung")
				.setMessage("Sie sind mit dem Server verbunden.")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_activity1, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	        change_ip();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	public void change_ip(){
		LayoutInflater layoutInflater = LayoutInflater.from(context);

            View promptView = layoutInflater.inflate(R.layout.ip_pompt, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);
            final EditText input = (EditText) promptView.findViewById(R.id.userInput);

            alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                        address=input.getText().toString();
						if(Patterns.IP_ADDRESS.matcher(address).matches())
							reconnect();
						else
							hostUnknownAlert();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                	public void onClick(DialogInterface dialog, int id) {
                		dialog.cancel();
                    }
                });

            AlertDialog alertD = alertDialogBuilder.create();

            alertD.show();
	}


	void reconnect(){
		killConnection();
		ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("Connecting...");
		new ConnectionTask(progress).execute();
	}

	void killConnection(){
		if(socketTCP != null){
			try {
				if(streamOutTCP !=null)
					streamOutTCP.close();
				if(socketTCP !=null)
					socketTCP.close();
				connected=false;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	private void sendDataTCP(String data) {
		byte[] toSendBytes = data.getBytes();
		byte[] toSendLenBytes = ByteBuffer.allocate(4).putInt(toSendBytes.length).array();
		    
		try {
			streamOutTCP.write(toSendLenBytes);
			streamOutTCP.write(toSendBytes);
			streamOutTCP.flush();
			} catch (IOException e) {
				e.printStackTrace();
		}
	}

	private void sendDataUDP(String data){
		new UDPTask(socketUDP).execute(data);
	}



	@Override
	public void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    killConnection();
	}


	public class UDPTask extends AsyncTask<String, Void, Void> {
		DatagramSocket socket;
		public UDPTask(DatagramSocket socket) {
			this.socket = socket;
		}

		public Void doInBackground(String... data) {
			if(android.os.Debug.isDebuggerConnected())
				android.os.Debug.waitForDebugger();
			byte[] toSendBytes = data[0].getBytes();

			try {
				InetAddress IPAddress = InetAddress.getByName(address);
				int length = toSendBytes.length;
				DatagramPacket sendPacket = new DatagramPacket(toSendBytes, length, IPAddress, port);
				socketUDP.send(sendPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public class ConnectionTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog progress;

		public ConnectionTask(ProgressDialog progress) {
			this.progress = progress;
		}

		public void onPreExecute() {
			progress.show();
		}

		public Void doInBackground(Void... unused) {
			if(android.os.Debug.isDebuggerConnected())
				android.os.Debug.waitForDebugger();
			try {

				if(socketTCP !=null)
					socketTCP.close();
				socketTCP = new Socket();
				socketTCP.connect(new InetSocketAddress(address, port), 3000);
				streamOutTCP = new DataOutputStream(socketTCP.getOutputStream());

				sendDataTCP(myID);
				socketUDP = new DatagramSocket();
				connected = true;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		public void onPostExecute(Void unused) {
			if(android.os.Debug.isDebuggerConnected())
				android.os.Debug.waitForDebugger();
			progress.dismiss();
			if(connected)
				connectedAlert();
			else
				notConnectedAlert();

		}
	}
}
