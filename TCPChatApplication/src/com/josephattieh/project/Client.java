package com.josephattieh.project;





import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Client {
	private static String nbr = "";
	private static ArrayList<String> BlockedUsers;
	public static void main(String[] args) throws Exception {
		String line, modifiedLine , username = null, password;
		
		TimeUnit BLOCK_USER = TimeUnit.MINUTES; //this represents the blocking period and will be used later
		
		
	
		 BlockedUsers = new ArrayList<>();//this arraylist holds the usernames of the users blocked by this client	
	
		 Socket clientSocket = new Socket("localhost", 6789);//Client Socket 
		
		
		// getting input / output streams
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

		
		String sss;
		ArrayList<String> usernames = new ArrayList<>(); //arraylist of usernames
		Scanner scan = new Scanner(new File("user_pass.txt")); //populating the usernames
		while (scan.hasNextLine()) {
			sss = scan.nextLine();
			usernames.add(sss.split(" ")[0]);
		}
		
		int attempts =0; //this is the number of attempts to log in before blocking the user
		
		outToServer.writeBytes("ready" + "\n");//telling server that client is ready


	while(true){	
			
		modifiedLine = inFromServer.readLine(); //wait for the server to communicate with client
		
		if(modifiedLine.compareTo("Enter username")==0){ //if the server requested the username of the client
				System.out.println("Enter Username:");
				username = inFromUser.readLine();  		//get the username from the client 
				outToServer.writeBytes(username+"\n");	//send it to the server
			}
		else if (modifiedLine.compareTo("Enter password")==0) { //if the server requested the password of the client
				System.out.println("Enter Password:");
				password = inFromUser.readLine();			//get the password from the client
				outToServer.writeBytes(attempts+" "+ password+"\n"); //send the number of attempts as well as the password to the server
		}
		else if(modifiedLine.compareTo("Not Authenticated!")==0) { //if the server said that the username does not match the password
				System.out.println("Login Unsuccessful");
				if(attempts==2) {			//if there are 3 attempts consumed
					BLOCK_USER.sleep(2);	//block the user for 2 minutes
					attempts=0;				//reset the number of attempts
				}
				else 
				{
					attempts++;				//increase the number of attempts used
				}
		}
		else if(modifiedLine.compareTo("Authenticated!")==0) { //if the login is successful
			System.out.println("Login Successful");
			break;//exit this loop 
		}
		else if(modifiedLine.compareTo("Already logged!")==0) {
			System.out.println("Logged on From Another Console");
			// we did not consider an already logged response from the server as failed attempt
		}
		

}	
		
		Client c = new Client();//create an instance of the client in order to be able to create instances of the inner class 
		new Thread (c.new MessageListener(clientSocket,username)).start(); //creates a message Listener and put it in a thread in order to display
		//messages directly to the users (it avoids the wait for the message to arrive and blocking the flow of the program by waiting in another
		//thread for the messages to arrive)
		
		System.out.println("Choose a Number");
		System.out.println("1 - See the Users who are online");
		System.out.println("2 - See the Users who have been online in the Last hour");
		System.out.println("3 - Send a broadcast to all the online users");
		System.out.println("4 - Send a private message to a certain user");
		System.out.println("5 - Block a certain user from sending messages");
		System.out.println("6 - Unblock a certain user from sending messages");
		System.out.println("7 - View Messages Received when you were offline");
		System.out.println("8 - Log Out");
		//showing the user's choices
		String nbr = "";
		while(true) {
		try {	
			Timer timer = new Timer();//creates a timer instance
			//this provides the ability for threads to schedule tasks for future execution in a background thread  
	        timer.schedule( new LogOut(clientSocket, outToServer, username), 15*60*1000 ); //this statement allows us to 
	        //log out the user in case he did not enter anything in the console for more than 15 min
			nbr =inFromUser.readLine();
			 timer.cancel();//if this statement is reached before 15 min have elapsed, the Task of logging out does not execute
			 // otherwise, it logs out
		}catch(Exception e ) {}
		
		try {
			if(nbr.compareTo("1")==0) {
				outToServer.writeBytes("WhoseOnline"+"\n");
				System.out.println("This is a list of Online users");
				//handled by thread
				
			}
			else if(nbr.compareTo("2")==0) {
				outToServer.writeBytes("WhoLastHr"+"\n");
				System.out.println("This is a list of users who have been online in the past hour");
				 //gets the users who have been online the last hour
			}
			else if(nbr.compareTo("3")==0) {
				System.out.println("Enter the message to be broadcasted:");
				line = inFromUser.readLine(); //reads the broadcast to be sent from the client
				outToServer.writeBytes("Broadcast "+username+ " : " +line +"\n");//sends the request to the server/handler
			}
			else if(nbr.compareTo("4")==0) {
	
					System.out.println("Choose a Number:"); //choosing the user to which the client want to send the private message to

					for(int i=0; i<usernames.size();i++) {
						System.out.println((i+1)+" - "+ usernames.get(i));
					} //display users alongside their usernames
					line = inFromUser.readLine(); //get the number from the client
					line = "Message "+ usernames.get(Integer.parseInt(line)-1)+" "+ username +" : "; //starts formulating the message to be sent
					//yo th server to be in format Message <destination> <sender> : <message>
					System.out.println("Enter the message to be sent:");
					line= line+ inFromUser.readLine(); //got the message from the user
					outToServer.writeBytes(line+"\n"); //send the request to the server to send the message
					System.out.println("Message Sent");
				}
			else if(nbr.compareTo("5")==0) {
				System.out.println("Choose a Number:");

				for(int i=0; i<usernames.size();i++) {
					System.out.println((i+1)+" - "+ usernames.get(i));
				}//choose a user in order to block him
				line = inFromUser.readLine();
				if(!BlockedUsers.contains(usernames.get(Integer.parseInt(line)-1)))
				{BlockedUsers.add(usernames.get(Integer.parseInt(line)-1)); //add the username of the blocked user only if it is not already in the arraylist (already blocked)
				System.out.println("User "+usernames.get(Integer.parseInt(line)-1) + " is blocked now.");
				}
				else {
					System.out.println("User " + usernames.get(Integer.parseInt(line)-1)+" is already blocked.");//inform the user that the user to be blocked has been already blocked
				}
				}else if(nbr.compareTo("6")==0) {
				System.out.println("Choose a Number");
				if(BlockedUsers.size()==0) { //inform the users that they cannot unblock if they have not blocked anyone before
					System.out.println("Haven't blocked anyone yet!");
				}else {
					for(int i=0; i<BlockedUsers.size();i++) {
					System.out.println((i+1)+" - "+ BlockedUsers.get(i));
				}//enable the user to choose the username of the client he wants to unblock
					line = inFromUser.readLine();
					BlockedUsers.remove(Integer.parseInt(line)-1);//remove the user we want to unblock from the arraylist
					System.out.println("User Unblocked!");
				}
			}
			else if(nbr.compareTo("8")==0) {
				outToServer.writeBytes("Log Out Request"+"\n");
				
				clientSocket.close(); //close socket and streams
				inFromUser.close();
				inFromServer.close();
				outToServer.close();
				break; //exit
			}
			else if(nbr.compareTo("7")==0) {
				//enables the user to read messages that he got when he was offline
				outToServer.writeBytes("Offline"+"\n");
				
			}
			
		}catch(Exception e) {}
		}


	}

	
	public static void addToFile(File f , String st) throws IOException {
		//this method enables us to write at the end of the file without deleting the content already present in the file
		PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f,true)));
		p.println(st);
		p.close();
		
		
	}
	
	private ArrayList<String> getBlockedUsers() {
		// this method returns the arraylist containing the blocked users 
		return BlockedUsers;
	}
	
	
	

class MessageListener implements Runnable {
	//this inner class is responsible for listeninbg for incoming messages from the user
	//it works in a thread in order to work in parallel
	//while the user is sending a message, he can be receiving one
	private Socket s;
	private String username;
	private ArrayList <String> BlockedUsers;
	public MessageListener( Socket  s , String Username) {
		this.s = s;
		this.username = username;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		BlockedUsers = getBlockedUsers();//this get the updated arraylist containing the blocked users 
		try{
			
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		//this re
		while(true)
		{
			String line = br.readLine();//reads the message to be printed
			if(line.split(" ")[0].compareTo("Other")==0)
				System.out.println(line.substring(6));
			else 
			if(!BlockedUsers.contains(line.split(" ")[0])) //checks if the sender is blocked or not
				System.out.println(line);
			
		}
	
		}
	catch(Exception e ){}
		
	}
	
}

}