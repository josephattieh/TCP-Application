package com.josephattieh.project;



import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class LogOut extends TimerTask{
	/* this class extends TimerTask. It is executed if the user is inactive for more than 15 min.
	 */
	private Socket clientSocket;
	private DataOutputStream outToServer;
	private String username;
	public LogOut(Socket clientSocket, DataOutputStream outToServer,String username) {
	 this.username=username;
	 this.clientSocket=clientSocket;
	 this.outToServer=outToServer;
	 
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		   try {     
			   //the next statements logs out the user
		                System.out.println( "Inactive for 15min. Logging out..." );
		                outToServer.writeBytes("Log Out Request"+"\n");
						clientSocket.close();
		                System.exit(0);
		            
	}catch(Exception e) {}
}
	

	

}
