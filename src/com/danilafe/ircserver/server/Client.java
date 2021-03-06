package com.danilafe.ircserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

public class Client {
	//test
	private String mode;
	private String lasping;
	private String ip;
	private String host;
	private String realname;
	private String username;
	private Boolean is_pinged = false;
	private Client me = this;
	private Startup parent;
	private Socket client;
	private BufferedReader br;
	private PrintStream ps;
	private Thread pinger = new Thread(){
		public void run(){
			while(true){
				try {
					Thread.sleep(60 * 1000);
					me.ping();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private Thread listener = new Thread(){
		public void run(){
			try{
				String receive = null;
				while((receive = br.readLine()) != null){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					parent.handleCommand(me, receive);
				}	
			} catch(IOException e){
				parent.addLogLine("Client " + username + " has detected an error in the communication, probably closed socket.");
				parent.handleDisconnection(me);
			}
			
		}
	};
	
	public Client(Startup s, Socket socket){
		try {
			client = socket;
			parent = s;
			ps = new PrintStream(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			listener.start();
			pinger.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Forces to send the client a message
	 * @param m the message to send.
	 */
	public void sendMessage(String m){
		ps.println(m);
	}
	
	/**
	 * Sets the Nick for the client
	 * @param newname the new name to set.
	 */
	public void setName(String newname){
		this.username = newname;
	}
	
	/**
	 * Gets the current name of the client.
	 * @return the current Nick of the client
	 */
	public String getName(){
		return username;
	}
	
	/**
	 * Sets the host of the client. Should be us.
	 * @param host the new host to set.
	 */
	public void setHost(String host){
		this.host = host;  
	}
	
	/**
	 * Returns the client's host. Should be our IP.
	 * @return The host.
	 */
	public String getHost(){
		return host;
	}
	
	/**
	 *Sets new real name for the client
	 *@param rname the new real name to replace the old one.
	 */
	public void setRname(String rname){
		this.realname = rname;  
	}
	
	/**
	 * Returns the real name of the client.
	 * @return the current real name for the client.
	 */	
	public String getRname(){
		return realname;
	}
	
	/**
	 * I'm sorry but this is currently worthless.
	 * @return the current IP of the client.
	 */
	public String getIP(){
		return ip;
	}
	
	/**
	 * I'm sorry but this is currently worthless.
	 * @param newip sets the IP of the client.
	 */
	public void setIP(String newip){
		this.ip = newip;
	}
	/**
	 * Tests wether the client has been pinged before
	 * @return boolean, true if it has been pinged before. 
	 */
	public boolean getPinged(){
		return is_pinged;
	}
	/**
	 * Closes the Client's sockets, basically shutting it down.
	 */
	public void close(){
		try {
			client.close();
			pinger.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks wether the user returned the correct ping.
	 * If the user hasn't, set is_pinged to false, else
	 * make it true.
	 * @param ping The ping to check
	 * @return Wether the ping is correct (boolean)
	 */
	public boolean checkPing(String ping){
		if(lasping.equals(ping) || (":" + lasping).equals(ping)){
			this.is_pinged = true;
			return true;
		} else {
			this.is_pinged = false;
			return false;
		}
	}
	
	/**
	 * Sends a ping to the client. Used to make sure they're there.
	 */
	public void ping(){
		String ping = generatePing(5);
		this.sendMessage("PING :" + ping);
		parent.addLogLine("Pinged " + username + " with " + ping);
		lasping = ping;
	}
	
	/**
	 * Generates random string with only capital letters
	 * @param length the length of the desired string
	 * @return the generated string.
	 */
	private String generatePing(int length){
		Random rand = new Random();
		String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		char[] chars = new char[length];
		for(int i = 0; i < length; i ++){
			chars[i] = charset.charAt(rand.nextInt(charset.length()));
		}
		
		String ret = new String(chars);
		return ret;
	}
	
	/**
	 * Set the mode of the client
	 * @param mode the mode to set.
	 */
	public void setMode(String mode){
		this.mode = mode;
	}
	
	/**
	 * Manualy override the pinged status.
	 * @param bol
	 */
	public void setPinged(boolean bol){
		is_pinged = bol;
	}
	
	/**
	 * Test connection of client by attempting to write to its socket. If it fails, kicks the client.
	 */
	public void testConnection(){
		try {
			client.getOutputStream().write(0);
		} catch (IOException e) {
			parent.addLogLine("Client " + username + " has detected an error in the communication, probably close socket.");
		}
		
		parent.handleDisconnection(this);
	}
	
}
