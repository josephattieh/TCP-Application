package com.josephattieh.project;




import java.awt.SecondaryLoop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Stack;


public class Server {
	private static ArrayList<Handler> handlers;
	public static void main(String[] args) throws Exception {
		
		ServerSocket welcomeSocket = new ServerSocket(6789); //welcome the user
		Socket connectionSocket, connectionSocket1 ;
		Scanner scan = new Scanner(new File("user_pass.txt")); //create a scanner to get the usernames and passwords from the text file
		
		ArrayList<String> usernames = new ArrayList<>();
		ArrayList<String> passwords = new ArrayList<>();
		
		String line;
		while (scan.hasNextLine()) {
			line = scan.nextLine();
			usernames.add(line.split(" ")[0]);
			passwords.add(line.split(" ")[1]);
		}
		
		File online = new File("OnlineUsers.txt"); //creates a file that will store the users that are online
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(online,false))); //clear that file in case it exists
		pw.close();
		File f1 = new File("Track.txt"); //creates a file that will store the messages of offline users
		PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(f1,false))); //clear that file in case it exists
		pw1.close();
		File time= new File("Time.txt"); //creates a file that will store the users that logged in with the time of the log in
		PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(time,false))); //clear that file in case it exists
		p.close();
		
		
		handlers = new ArrayList<>(); //arraylist that will containt the instances of the handler inner class
		Server S = new Server(); //creates an instance of this class in order to be able to create a new instance of the inner class
		while(true) {
			// Wait for contact
			System.out.println("Waiting for contact...");
			connectionSocket = welcomeSocket.accept(); //blocks until a connection is made
			System.out.println("Connected to: " + connectionSocket.getInetAddress());
			Handler temp =S.new Handler(connectionSocket,usernames, passwords); //a new handler is created for each client/connection
			handlers.add(temp); //handler instance created previously is added to the arraylist of handlers
			new Thread(temp).start(); //starts a new thread (our handler implements runnable)
			
		}
		
		
	}
	private  boolean sendMessage(String message) { //method that enables users to send private messages
		
			boolean found=false; //Message Format is Message <destination> <sender> : <message>
			String dest = message.split(" ")[1] ; //extract the destination of the message from the destination field 
			String tobeSent="";
			for(int i=2;i<message.split(" ").length;i++)
				tobeSent+= message.split(" ")[i]+" "; // extract what we need to send to the other client
			
			System.out.println("Message is being sent...");
			for(int y=handlers.size()-1; y>=0;y--) //iterates through the handlers instances
			{ 
				Handler temp = handlers.get(y);
				String check=temp.getUsername(); //get username from the handler to know the user that uses that handler
				if(check.compareTo(dest)==0) //if the username matches the destination
				{
					
					if(!temp.send(tobeSent)) { //try to send to client
						handlers.remove(temp); //if failed, remove the handler from the list(client disconnected)
						System.out.println("Sent");
					}
					else {
					found=true; //we found the username and the message was correctly sent
					break;
					}
				}
				
				
				
			}
		
			return found;
	}		
	private  boolean sendBroadcast(String message) {
		
		String tobeBroadCasted=""; //Broadcast format: Broadcast <sender> : <message>
		for(int i=1;i<message.split(" ").length;i++)
			tobeBroadCasted+= message.split(" ")[i]+" "; //extract the message to be broadcasted
		System.out.println("Broadcasting...");
		
		for(int i = handlers.size()-1; i >= 0;i--) { //looping through the handlers in the array
			Handler han = handlers.get(i); 
			if(!han.send(tobeBroadCasted)) { //try to send broadcast to all the clients
				handlers.remove(i); //if failed, remove the client (disconnected client)
				System.out.println("Broadcast sent");
			}
		}
		return true;
	}
	
	
	 class Handler implements Runnable { //handler that will run in a thread (so it allows multiuser capability)
		
		private Socket connectionSocket;
		private ArrayList<String> usernames , passwords;
		private String username;
		private DataOutputStream dos;
		private BufferedReader bif ;
		public Handler(Socket connectionSocket, ArrayList<String> usernames, ArrayList<String> passwords) {
			this.connectionSocket = connectionSocket;
			this.usernames = usernames;
			this.passwords = passwords;
			username = "";
			//constructor that accepts the socket of a user; the usernames and password (for authentication)
		}	

		public void run() {
			try {	
			String  password;
			String line, modifiedLine;
			
			// geting the input/output streams to communicate with the user's socket
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			dos = outToClient; bif = inFromClient;//setting the global variables in order to use them in all the class
			
			line = inFromClient.readLine();//client tells the server it's ready
			System.out.println(line);
			File f1= new File("OnlineUsers.txt");
			boolean bool = true; //this flag allows us to know whether to ask for the username and password again from the user
			while(bool) {
				modifiedLine = "Enter username"; //ask for the username from the client
				outToClient.writeBytes(modifiedLine+"\n"); //send that request so that the client could respond with the username enterred
				username=inFromClient.readLine(); //read the username enterred by the client	
				System.out.println("User Attempt to LogIn...");
				
				modifiedLine = "Enter password"; //ask for the password from the client
				outToClient.writeBytes(modifiedLine+"\n"); //send that request so that the client sends the password
				password=inFromClient.readLine(); //read the password enterred by the client
				
				
				if(usernames.contains(username)) { //checks if the username is present in the arraylist contatining the usernames
					if(passwords.get(usernames.indexOf(username)).compareTo(password.split(" ")[1])==0 && Integer.parseInt(password.split(" ")[0])<3 ) {
						//checks if the password matches the username enterred and that the number of attempts is less than 3 (double check here and on the client side)
						if(!isAlreadyOnline(username))	{//check if the username is already logged in (if the user is online)
						
							outToClient.writeBytes("Authenticated!"+"\n"); //if not logged in and credentials correct, send a message that means that the user got the permission to log in
							addToFile(f1, username); //the user is logged in, thus online so add its username to the OnlineUsers text file
							addToFile(new File("Time.txt"), username +" "+ (System.currentTimeMillis())); //add the username as well as the current time to the Time text file
						
							bool= false; //to exit the loop
						}
						else {
							outToClient.writeBytes("Already logged!"+"\n");//the user is already logged in on another console
							bool = true;
						}
					}
					else {
						outToClient.writeBytes("Not Authenticated!"+"\n"); //the password does not match the username
						bool = true;
					}
				}
				else {
					outToClient.writeBytes("Not Authenticated!"+"\n"); //the username is not present in the arraylist
					bool = true;
				}
			}

			File track = new File("Track.txt"); //file that will contain the messages of the offline user
			
			File onlinee = new File("OnlineUsers.txt");
			while(true) {
				
				String exit = inFromClient.readLine(); //keeps checking of incoming requests from client
				System.out.println(exit); //print the request
				if(exit.compareTo("Log Out Request")==0) {
					System.out.println("Log Out request"); //informing the server that a user has logged out
					deleteFromFile(onlinee, username); //remove the user from the list of online users
				}
				else if(exit.split(" ")[0].compareTo("Broadcast")==0) {
					// if the client is performing a broadcast
					sendBroadcast(exit);
				
				}
				else if(exit.split(" ")[0].compareTo("Message")==0) {		
				boolean online =sendMessage(exit);//attempt to send a message to the user
				//if failed
				if(!readFromFile(f1).contains(exit.split(" ")[1]))
					addToFile(track, exit); //store the message in the file
			
			
				}
				else if(exit.compareTo("WhoseOnline")==0) {
					String st = readFromFile(f1);
					System.out.println(st);
					outToClient.writeBytes("Other "+st+"\n");
				}
				else if(exit.compareTo("WhoLastHr")==0) {
					outToClient.writeBytes("Other "+LastHour(new File("Time.txt"))+"\n");
				}
				else if (exit.compareTo("Offline")==0) {
					StringBuilder sb = new StringBuilder();
					Scanner read = new Scanner(new File("Track.txt"));
					while(read.hasNextLine()) {
						line = read.nextLine();//scan line per line in the text file
						Scanner scannn = new Scanner(line);//this enables us to iterates through the words in a line
						String repl= "Read ";
						if(scannn.hasNext())
						scannn.next();//skip the first word
						if(scannn.hasNext())
						repl+= scannn.next()+" ";//get the name of the destination
						String message ="";
						while(scannn.hasNext()) {
							String st = scannn.next();
							message+= st+" "; //extract the message
							repl+= st+" "; //get what we need to replace in file in order to indicate the message was read (Read <destination> <sender> : <message>)
						}
						if(line.split(" ")[0].compareTo("Message")==0) {//if we are sending a message
						if(line.split(" ")[1].compareTo(username)==0) { //if the destination is the current user (the user who has been offline)
							outToClient.writeBytes(message+"\n");
							sb.append(repl); //append to stringbuilder the modified line which means that the line has been read (so next time we read the offline messages, this line will be disregarded)
						
						}
						else {
							sb.append(line); //do not change the line, since it was not read
						}
						}else {
							sb.append(line); //do not change this line
						}
						
						
						
					sb.append("\n");
					
					}
					read.close(); //close reader
					PrintWriter pwr = new PrintWriter(new BufferedWriter(new FileWriter(new File("Track.txt"),false)));
					pwr.print(sb.toString()); //replace the content of the file with the new content (update what has been read)
					pwr.close();
				}
				}
			} catch(Exception e) {}
			
		}
		public String LastHour (File f)throws Exception {
			//this method returns the users that have been online in the past hour
			long time = System.currentTimeMillis(); //store the current time
			Stack <String> s = new Stack<>(); //stack used to make sure that the user is showed one time, even if he/she connected more than
			//one time in the past hour
			Scanner scan = new Scanner(f);
			long nb;
			long onehour = 60*60*1000; //this is the value of one hour 60 min * 60 s *1000ms
			String line;
			while(scan.hasNextLine()) {
				line= scan.nextLine();
				nb = Long.parseLong(line.split(" ")[1]);//get the time from the text file
				if((time-nb)<=onehour) { //if the difference between the current time and the time we got is less or equal to 1 hour
					if(!s.contains(line.split(" ")[0])) //if the user has not been already added to the list of users who have connected
					s.add(line.split(" ")[0]);//add the user
				}
			}
			String toReturn="";
			if(s.isEmpty()) {
				//this happens in case users have logged and one hour has elapsed
				//this does not mean that no user is online, but it means that no user has logged in to server in past hour
				toReturn="No User has logged in in the past hour";
			}
			else {
		toReturn= s.pop();
			while(!s.isEmpty())
				toReturn+=" "+s.pop();} //get the list of users from the stack
		return toReturn;
		}
		public String readFromFile( File f) throws Exception {
			//this method returns everything in the file
			Scanner scan = new Scanner(f);
			String st ="";
			while(scan.hasNextLine()) {
				st+=scan.nextLine()+" ";
			}
			return st;
		}
		public void addToFile(File f , String st) throws IOException {
			//this methods allows us to write to a file without clearing its previous content, 
			//by setting true the boolean append
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f,true)));
			p.println(st);
			p.close();
			
			
		}
		public void deleteFromFile(File f , String st) throws IOException {
			//this method enables the user to delete from a file a certain line of string
			File temp = new File("temp.txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(temp)));
			Scanner scan = new Scanner(f);
			String line;
			while(scan.hasNextLine()) {
				line = scan.nextLine();
				if(line.compareTo(st)!=0) {
					pw.println(line);//print everything to the temp file except the line we do not want to copy
				}
			}
			scan.close();
			pw.close();
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			Scanner sca = new Scanner(temp);
			
			while(sca.hasNextLine()) {
				 line = sca.nextLine();
					p.println(line); //print everything back from the temp file
		
			}
			sca.close();
			p.close();
			temp.delete(); //delete the temp file
			
		}
		public boolean isAlreadyOnline(String st) throws Exception
		{ //this method checks if the user is online by reading from the file where we stored the online users
			File f = new File("OnlineUsers.txt");
			Scanner scan = new Scanner(f);
			while(scan.hasNext()) {
				if(st.compareTo(scan.next())==0) //compare the user to each online user 
					return true; //the user is o
			}
			return false; //the user is not online
		}

		public boolean send(String msg) {
			
			if(!connectionSocket.isConnected()) {
				return false; //if client not connected, don't bother sending the message
			}
			
			try {
				dos.writeBytes(msg +"\n");//send a message through the output stream of the socket
			}
			catch(Exception e) {
				
			}
			return true; //message sent
		}
		public String getUsername () {
		
			return username; //get the username of the client 
		}
	}}
