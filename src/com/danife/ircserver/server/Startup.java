package com.danife.ircserver.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Startup {
	//TODO we still need all the reply codes.
	final String RPL_WELCOME = "001";
	final String RPL_YOURHOST = "002";
	final String RPL_CREATED = "003";
	final String RPL_MYINFO = "004";
	final String RPL_BOUNCE = "005";
	final int RPL_NAMREPLY = 353;
	final int RPL_ENDOFNAMES = 366;
	final int RPL_LIST = 322;
	final int RPL_LISTEND = 323;
	final int ERR_NICKNAMEINUSE = 433;
	final int RPL_MOTD = 372;
	final int RPL_ENDOFMOTD = 376;
	final int RPL_MOTDSTART = 375;
	
	String ip = "24.21.16.125";
	boolean modifyingclients = false;
	Startup me = this;
	ArrayList<Client> clients = new ArrayList<Client>();
	ArrayList<Channel> channels = new ArrayList<Channel>();
	ServerSocket s;
	Thread acceptthread = new Thread(){
		public void run(){
			while(true){
				
				try {
					Socket temps = s.accept();
					modifyingclients = true;
					Client c = new Client(me, temps);
					if(modifyingclients == false) Thread.sleep(100);
					clients.add(c);
					modifyingclients = false;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	};

	
	Startup(){
		//TODO we need to get our actual ip, k?
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					try {
						acceptthread.stop();
						if(s != null){
							s.close();
							for(Client c: clients){
								c.close();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			s = new ServerSocket(6667);
			acceptthread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Startup();
	}
	
	/**
	 * Handles the command given to it.
	 * @param client the client that fired the command. MUST NOT BE NULL.
	 * @param command the command to be processed.
	 */
	void handleCommand(Client client, String command){
		String[] pieces = command.split(" ");
		String argument1 = command.split(" ")[0];
		switch(argument1.toUpperCase()){
		case "USER":
			if(pieces.length >= 5){
				
				if(this.checkForSimilarName(pieces[1]) || pieces[1].equalsIgnoreCase(client.getName())){
					if(client.getName() == null){
						client.setName(pieces[1]);
					}
					
					client.setIP(pieces[2]);
					
					client.setHost(pieces[3]);
					
					String realname = "";
					for(int i = 4; i < pieces.length; i ++){
						realname += pieces[i];
					}
					client.setRname(realname);
					
					client.ping();
					client.sendMessage(":" + ip + " " + RPL_WELCOME + " " + client.getName() +  " :Welcome to Danilafe's IRC");
					this.sendMOTD(client);
				} else {
					client.sendMessage(":" + ip + " " + ERR_NICKNAMEINUSE + " " + client.getName());
				}
				
				

				
				
				
			}
			break;
		case "NICK":
			if(pieces.length > 1){
				if(!(pieces[1].equalsIgnoreCase(client.getName()))){
					if(this.checkForSimilarName(pieces[1])){
						String s = "Client " + client.getName() + " changed name to ";
						String oldname = client.getName();
						client.setName(pieces[1]);
						for(Channel c: getClientChannel(client)){
							c.sendChannelMSG(":" + oldname + "!" + oldname + "@" + client.getIP() +" NICK " + client.getName());
						}
					} else {
						client.sendMessage(":" + ip + " " + ERR_NICKNAMEINUSE + " " + client.getName()); 
					}

				}

				
			}
			break;
		case "PONG":
			
			if(client.checkPing(pieces[1])){
		
			} else {
			
			}
			
			break;
		//All further commands require client to be pinged. If not, send them a message.
		case "JOIN": 
			if(pieces.length > 1){
				if(client.getPinged() == true){
					if(pieces[1].split(",").length == 1){
						String cname = pieces[1].replace("#", "");
						this.joinChannel(cname, client);
						this.sendNamesToClient(client, pieces[1]);

					} else if(pieces[1].split(",").length > 1){
						String[] channels = pieces[1].split(",");
						ArrayList<String> channel= new ArrayList<String>();
						for(String c: channels){
							channel.add(c.replace("#", ""));
						}
						Object[] obj =  channel.toArray();
						String[] properchannels = Arrays.copyOf(obj, obj.length, String[].class);
						for(String s: properchannels){
							this.joinChannel(s, client);
							this.sendNamesToClient(client, "#" + s);
						}
					}
				}
			}
			break;
		case "PRIVMSG":
			if(pieces.length > 2){
				if(client.getPinged()){
					if(pieces[1].startsWith("#")){
						if(this.checkForChannel(pieces[1].replace("#", ""))){
							String message = pieces[2];
							if(message.startsWith(":")){
								message = message.substring(1);
							}
							for(int i = 3; i < pieces.length; i ++){
								message += " " + pieces[i];
							}
							this.getChannelByName(pieces[1].replace("#", "")).sendChannelMSGExclude(client,":" + client.getName() + "!" + client.getName() + "@" + client.getIP() +" PRIVMSG " + pieces[1] + " :" + message);
							}
					}
				}
			}
			break;
		case "MOTD":
			this.sendMOTD(client);
			
			break;
		case "PING":
			String pong = command.replace("PING ", "");
			client.sendMessage("PONG " + pong);
			break;
		case "NAMES":
			if(pieces.length > 1){
				this.sendNamesToClient(client, pieces[1]);
			}
			break;
		case "PART":
			ArrayList<Channel> chan = getClientChannel(client);
			if(chan.size() > 1){
				for(Channel c: chan){
					if(c.getName().equals(pieces[1].replace("#", ""))){
						c.partUser(client);
					}
				}
			} 
			if(channels.size() == 1){
				Channel c = channels.get(0);
				if(c.getName().equals(pieces[1].replace("#", ""))){
					c.partUser(client);
					clients.add(client);
				}
			}
			
			break;
		case "LIST":
			sendChannelList(client);
			break;
			
		case "QUIT":
			handleDisconnection(client);
			break;
		
		}
	}
	
	
	/**
	 * Gets the server's motd.
	 * @return the array of strings, the motd.
	 */
	String[] getMOTD() {
		try {
			BufferedReader r = new BufferedReader(new FileReader("MOTD.txt"));
			ArrayList<String> temparray = new ArrayList<String>();
			String s = null;
			while((s = r.readLine()) != null){
				temparray.add(s);
			}
			
			Object[] ret = temparray.toArray();
			String[] toret = Arrays.copyOf(ret, ret.length, String[].class);
			return toret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the channel with the given name. If there is no channel with that name,
	 * creates a new one.
	 * @param cname
	 * @return
	 */
	 Channel getChannelByName(String cname){
		boolean returned = false;
		for(Channel c: channels){
			if(c.getName().equals(cname)){
				returned = true;
				return c;
			}
		}
		
		
		Channel newc = new Channel(cname);
		channels.add(newc);
		return newc;
	}
	
	/**
	 * Checks for channel with said name.
	 * @param cname the name of the channel to check for
	 * @return boolean - false if no channel with name is found.
	 */
	 boolean checkForChannel(String cname){
		boolean returned = false;
		for(Channel c: channels){
			if(c.getName().equals(cname)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check for players on the server that already have similar names.
	 * @param name the name to check for
	 * @return boolean - true if no players are with that name.
	 */
	 boolean checkForSimilarName(String name){
		ArrayList<Client> il = new ArrayList<Client>();
		for(int i = 0; i < clients.size(); i ++){
			il.add(clients.get(i));
		}
		for(int i = 0; i < channels.size(); i ++){
			ArrayList<Client> templ = channels.get(i).getUsers();
			for(int k = 0; k < templ.size(); k++){
				il.add(templ.get(k));
			}
		} 
		for(Client cl: il){
			if(cl.getName() != null){
				if(cl.getName().equalsIgnoreCase(name)){
					return false;
				}
			}
		}
		return true;
	}
	
	 /**
	  * Gets the channel with the following client.
	  * @param nick the name to check for.
	  * @return
	  */
	Channel getClientChannel(String nick){
		for(Channel c: channels){
			if(c.checkClient(nick)){
				return c;
			}
		}
		
		return null;
	}
	
	 /**
	  * Gets the channel with the following client.
	  * @param client the client to get connection
	  * @return the array list containing all the channles this client has.
	  */
	ArrayList<Channel> getClientChannel(Client client){
		ArrayList<Channel> chan = new ArrayList<Channel>();
		for(Channel c: channels){
			if(c.checkUser(client)){
				chan.add(c);
			}
		}
		
		return chan;
	}
	
	/**
	 * Sends the client a list of all the channels on the server.
	 * @param client the client to send the list to.
	 */
	void sendChannelList(Client client){
		String channelmode; //TODO get the actual channel mode.
		for(Channel c: channels){
			client.sendMessage(":" + ip + " " + RPL_LIST + " " + client.getName() + " " + "#" + c.getName() + " " + c.getUserCount() + " " + "[+ntr]" + " " + c.getTopic()); //TODO Add function to sendchannelmode
		}
		client.sendMessage(":" + ip + " " + RPL_LISTEND + " " + client.getName() + " " + ":End of /LIST command." );
		
	}
	
	/**
	 * Handles the disconnection of a client.
	 * @param c the client that has disconnected
	 */
	void handleDisconnection(Client c){
		for(Channel dis : getClientChannel(c)){
			dis.partUser(c);
		}
		
		if(clients.contains(c)){
			clients.remove(c);
		}
		
		c.close();
		
		}
	
	/**
	 * Adds the client to the channel passed down.
	 * @param cname the name of the channel to add the user to.
	 * @param client the client to add to the channel.
	 */
	void joinChannel(String cname, Client client){
		if(checkForChannel(cname)){
			Channel c = getChannelByName(cname);
			try{
				clients.remove(client);	
			}catch(Exception e){
				e.printStackTrace();
			}
			if(c.checkUser(client) == false){
				c.addUser(client);
				c.sendChannelMSG(":" + client.getName() + "!" + client.getName() + "@" + client.getIP() +" JOIN " + "#" + cname);
				c.sendChannelMSG(":" + client.getName() + " MODE " + client.getName() + " :" +"+i");
				client.setMode("+i");
				
			}
			
		} else {
			Channel c = getChannelByName(cname);
			try{
				clients.remove(client);	
			}catch(Exception e){
				e.printStackTrace();
			}
			if(c.checkUser(client) == false){
				c.addUser(client);
				c.sendChannelMSG(":" + client.getName() + "!" + client.getName() + "@" + client.getIP() +" JOIN " + "#" + cname);
				c.sendChannelMSG(":" + client.getName() + " MODE " + client.getName() + " :" +"+i");
				client.setMode("+i");
				
			}
			
		}
	}
	
	/**
	 * Sends a list of all the clients on the channel.
	 * @param client the client to send the list to
	 * @param channel the channel whose clients to list.
	 */
	void sendNamesToClient(Client client, String channel){
		if(this.checkForChannel(channel.replace("#", ""))){

			client.sendMessage(":" + ip + " " + RPL_NAMREPLY + " " + client.getName() + " = " + channel + " :" + getChannelByName(channel.replace("#", "")).returnUsers());
			client.sendMessage(":" + ip + " " + RPL_ENDOFNAMES + " " + client.getName() + " :End of /NAMES command.");

	}
	}
	
	/**
	 * Sends the motd stored in MOTD.txt to the client
	 * @param client the client to send to.
	 */
	void sendMOTD(Client client){
		String[] motd = this.getMOTD();
		client.sendMessage(":" + ip + " " + RPL_MOTDSTART + " " + client.getName() + " :Start of /MOTD command");
		for(String s: motd){
			client.sendMessage(":" + ip + " " + RPL_MOTD + " " + client.getName() + " :" + s);
		}
		client.sendMessage(":" + ip + " " + RPL_ENDOFMOTD + " " + client.getName() + " :End of /MOTD command.");
	}
}
