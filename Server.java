import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Server {
	
	//Data Structures
	public HashMap<String, PrintWriter> activeUserMap =  new HashMap<String, PrintWriter>();
	public HashMap<String, String> userToColor = new HashMap<String, String>();
	public HashMap<String, String> isPrivate = new HashMap<String, String>();
	public HashMap<String, List<String>> userPrivateSession = new HashMap<String, List<String>>();
	//Color Constants
	public static final String red = 		"\033[1;31m";
	public static final String green = 		"\033[1;32m";
	public static final String yellow = 	"\033[1;33m";
	public static final String blue = 		"\033[1;34m";
	public static final String magenta = 	"\033[1;35m";
	public static final String cyan = 		"\033[1;36m";
	public static final String black = 		"\033[1;30;47m";
	public static final String white = 		"\033[1;37;40m";
	public static final String clear = 		"\033[0m";
	
	public static String space = "                                                                                                    ";
	public static String[] colors = {"\033[1;31m","\033[1;32m","\033[1;33m","\033[1;34m","\033[1;35m","\033[1;36m","\033[1;30;47m","\033[1;37;40m"};
	
	//History variables
	public int cno;
	public File history = new File("chat.txt");
	public File preferences = new File("pref.txt");
	
	//network variables
	int port;
	ServerSocket serverSocket;
	
	
	public static void main(String args[])throws Exception{  //args[0] = port

		
		Server	server = new Server();
		Socket socket;
		
		socket = null;
		server.port = Integer.parseInt(args[0]); //should add error handling
		server.serverSocket = new ServerSocket(server.port, 20);	
		server.serverSocket.setReuseAddress(true);
		
		server.loadColorHistory(server.preferences);
		
		server.writeToHistory("Started up on port "+Integer.toString(server.port), "Server");
		System.out.println(clear+"Started up server on port "+Integer.toString(server.port));
		
		while ( (socket = server.serverSocket.accept()) != null ) {
			System.out.println( "Accepted an incoming connection" );
			// creates a SessionThread for each client that connects
			new Session(socket, server).start();
			
		}
		
	
		server.serverSocket.close();

	}
	
	private void loadColorHistory(File p)throws Exception{
		
		Scanner scanner = new Scanner(p);
		String s;
		String name;
		String color;
		
		while (scanner.hasNextLine()) { 
			s = scanner.nextLine();
			if(!s.equals("") && s.length() >= 101){
				name = s.substring(0,99).trim();
				color = s.substring(100);
				userToColor.put(name, color); 
				cinc();
			}
		}
		
	}
	
	private void cinc(){
		
		if (cno < 7){ cno++;}else{cno=0;}
		
	}
	
	private String nextColor(){
		
		String ret = colors[cno];
		
		cinc();
		
		return ret;
			
		
	}
	
	private String addUserColor(String name)throws Exception{
		
		if(userToColor.containsKey(name)){
			
			return userToColor.get(name);
			
		}else{
			
			String color= nextColor();
			userToColor.put(name, color);
			
			FileWriter		fw = new FileWriter( "pref.txt", true );
			BufferedWriter	bw = new BufferedWriter(fw);
		
			bw.write(name+space.substring(name.length())+color);
			bw.newLine();
			bw.close();
			return color;
			
		}
		
	}
	
	private void writeToHistory(String msg, String name)throws Exception{
		
		FileWriter		fw = new FileWriter( "chat.txt", true );
		BufferedWriter	bw = new BufferedWriter(fw);
		
		bw.write(name+" : "+msg);
		bw.newLine();
		bw.close();
		
	}
	
	private void readFromHistory(PrintWriter client)throws Exception{
		
		File chat = new File("chat.txt");
		Scanner scanner = new Scanner(chat);
		
		while (scanner.hasNextLine()) { 
			client.println(scanner.nextLine());   
		}
		
	}
	
	private String colorize(String msg, String name){
		
		if(name.equals("Server")){ return "\033[2;30;47m"+msg+clear;}else{
			return userToColor.get(name)+msg+clear;
		}
		
	}
	
	private void sendToPrivate(String msg, String name)throws Exception{
		
		String cmsg;
		
		cmsg = colorize(msg, name);
		
		activeUserMap.get(name).println("way up in the private sending biz");
		
		String cclient;
		
		if(userPrivateSession.get(name) == null){
					
					activeUserMap.get(name).println("why are you sending to an empty private group you dingus");
					return;
					
		}
		
		if(userPrivateSession.get(name).isEmpty()){
					
					activeUserMap.get(name).println("why are you sending to an empty private group you dingus");
					return;
					
		}
		
		for (Map.Entry<String, PrintWriter> client : activeUserMap.entrySet()){
			
				cclient = client.getKey();
				
				activeUserMap.get(name).println("checking client "+cclient);
				
				if(userPrivateSession.get(name).contains(cclient) /*&& isPrivate.get(cclient).equals(name)*/){
					client.getValue().println(name+" : "+cmsg);
				}else{
					activeUserMap.get(name).println(cclient+" is not in your private group");
				}
		}
		
		return;
		
	}
	
	private void sendToAll(String msg, String name)throws Exception{
		
		String cmsg;
		
		cmsg = colorize(msg, name);
		
		writeToHistory(cmsg, name);
		
		System.out.println(name+" : "+cmsg);
			
		for (Map.Entry<String, PrintWriter> client : activeUserMap.entrySet()){
				
				
				client.getValue().println(name+" : "+cmsg);
		
		}
			
	}
	
	private List<String> listAll(){
		
		List l = new ArrayList(activeUserMap.keySet());
		return l;
		
	}
	
	private boolean addUser(String user, PrintWriter client)throws Exception{
		
		if(activeUserMap.containsKey(user)){
			
			return false;
			
		}else{
			
			addUserColor(user);
			activeUserMap.put(user, client);
			isPrivate.put(user, null);
			userPrivateSession.put(user, new ArrayList<String>());
			return true;
		}
		
	}
	
	private boolean addPrivateUser(String user, String target){
		
		activeUserMap.get(user).println("really adding soon");
		if(!(isPrivate.get(target) == null)){
			activeUserMap.get(user).println("user is busy, shouldnt be here");
			return false;
		}else {
			activeUserMap.get(user).println("way up in there");
			isPrivate.put(target, user);
			userPrivateSession.get(user).add(target);
			activeUserMap.get(user).println("Added "+target+" to private session");
			activeUserMap.get(target).println("You have been added to a private session by "+user);
			
			return true;
		}
		
	}
	
	private boolean removePrivateUser(String target){
		
		if(isPrivate.get(target) == null){
			return false;
		}else{
			
			userPrivateSession.get(isPrivate.get(target)).remove(target);
			isPrivate.put(target, null);
			return true;
			
		}
		
	}
	
	private boolean removeUser(String user)throws Exception{
		
		if(user != null && user != ""){
			
			activeUserMap.remove(user);
			sendToAll(user+" disconnected from server", "Server");
			return true;
		
		}else{
			
			return false;
			
		}
		
	}
	
	public static class Session extends Thread{
	
		private Socket	socket;
		private Server	server;
		private String	username = null;
		private boolean isNamed =  false;
		private BufferedReader	fromClient;
		private PrintWriter 	toClient;
		
		
		public Session( Socket sock, Server serv ) {
			
			socket = sock;
			server = serv;
			
		}

		
		
		public void run(){
			
			
			String s;
		
			try{
				
				fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				toClient = new PrintWriter( new OutputStreamWriter(socket.getOutputStream()), true );

				while ( (s = fromClient.readLine()) != null ) {
					

					if(s.startsWith("@")){
						
						toClient.println("docommand");
						
						doCommand(s);
						
					}else if (isNamed){
						
						//server.sendToAll(s, username);
							
							if(server.userPrivateSession.get(username).isEmpty()){
								toClient.println("sendtoall");
								server.sendToAll(s, username);
								
							}else{
								toClient.println("privatesend");
								server.sendToPrivate(s, username);
								
							}	
							
						
						
					}	
				}
				
				server.removeUser(username);
				
				socket.close();
				
			}catch(Exception e){}
		} 
		
		private void doCommand(String cmd)throws Exception{
			
			String s = null;
			
			toClient.println("docommanding");
			
			if(!cmd.startsWith("@")){//
			
				toClient.println("bad command");
				
			}
			
			if(cmd.startsWith("@name") && isNamed == false){//
				
				toClient.println("@nameing");
				username = cmd.substring(6); //add prompt for user name
						
				if(!server.addUser(username, toClient)){
					toClient.println("Name already taken, try a different one");
					username = null;
					return;
				}
				
				toClient.println("named");
				isNamed = true;
				toClient.println("history?");
				server.readFromHistory(toClient);
				server.sendToAll(username+" entered the chat room", "Server");
				
			}else if(cmd.startsWith("@who")){//
				
				toClient.println("@whoing");
				
				for (String n : server.listAll()) {
					
					if(server.isPrivate.get(n) == null){
						toClient.println(n);
					}else{
						toClient.println(red+n+clear);
					}
				}
				
			}else if(cmd.startsWith("@private")){//
			
				
				
				s = cmd.substring(8).trim();
				toClient.println("@privateing"+s);
				
				if(!server.activeUserMap.containsKey(s)){
					
					toClient.println("That user isn't active");
					return;
					
				}
				
				toClient.println("@privateing STILL");
				if(server.isPrivate.get(s) == null){
					toClient.println("adding private user");
					server.addPrivateUser(username, s);	
					
				}
				
			}else if(cmd.startsWith("@end")){//
			
				toClient.println("@ending");
				
				s = cmd.substring(8).trim();
				
				/*if(!server.activeUserMap.containsKey(s)){
					
					toClient.println("That user isn't active");
					return;
					
				}*/
				
				toClient.println(s);
				
				if(server.isPrivate.get(s) != null){
					
					server.removePrivateUser(s);	
					
				}
				
			}else if(cmd.startsWith("@exit")){//
				
				toClient.println("Exiting...");
				
				socket.close();
				
			}else{//
				
				toClient.println("Bad command");
				
			}
			
			return;
			
		}
		
	}
	
}
