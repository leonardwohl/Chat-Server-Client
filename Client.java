import	java.util.*;
import	java.io.*;
import	java.net.*;

public class Client {
	
	public static void main( String args[] ) throws Exception{ //args[0] = addr, args[1]=port, args[2]=username

		Socket			socket = null;
		BufferedReader	stdIn;
		BufferedReader	fromServer;
		PrintWriter		toServer;
		String			s;
		String			msg;
		int				port;

		port = Integer.parseInt(args[1]);
		socket = new Socket( args[0], port );

		stdIn = new BufferedReader( new InputStreamReader( System.in ) );
		fromServer = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
		toServer = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ), true );

		new Client().new ClientReceiverThread( fromServer ).start();
		
		if(args.length == 3){
			
			toServer.println("@name "+args[2]);
			
		}

		while ( (s = stdIn.readLine()) != null ) { //this sends things
			if ( !s.equals("") ) {
				toServer.println( s );
			}
		}
		socket.close();
	}	
	
	private class ClientReceiverThread extends Thread { //this thread prints things

		BufferedReader	fromServer;

		public ClientReceiverThread ( BufferedReader fs ) {
			fromServer = fs;
		}

		public void run() {
			String	print;
			try{			
				while ( (print = fromServer.readLine()) != null  && !print.equals( "@exit" ) ) {
					System.out.println(print);
				}
				if ( print == null ) {
					System.out.println( "The Server has gone offline" );
					System.exit(0);
				}
			}catch(Exception e){}
			
		
		}
	}
	
}
