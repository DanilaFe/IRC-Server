package com.danife.ircserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Startup {
	
	
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
					System.out.println("Client connected.");
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
			System.out.println("Cause:" + e.getCause());
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
		switch(argument1){
		case "USER":
			if(pieces.length >= 5){
				
				if(this.checkForSimilarName(pieces[1])){
					String s = "Client " + client.getName() + " changed name to ";
					client.setName(pieces[1]);
					System.out.println(s + client.getName());
					client.setIP(pieces[2]);
					System.out.println(client.getName() + " has changed hostip to " + pieces[2]);
					
					client.setHost(pieces[3]);
					System.out.println(client.getName() + "'s server IP is now " + pieces[2]);
					
					String realname = "";
					for(int i = 4; i < pieces.length; i ++){
						realname += pieces[i];
					}
					client.setRname(realname);
					System.out.println("Client " + client.getName() + " set their real name to " + realname);
					
					client.ping();
				} else {
					client.sendMessage("Nick taken."); //TODO Make this a proper nice message XD
					System.out.println("Client attempted to connect with taken username. Client username: " + pieces[1]);
				}
				
				

				
				
				
			}
			break;
		case "NICK":
			if(pieces.length > 1){
				if(!(pieces[1].equalsIgnoreCase(client.getName()))){
					if(this.checkForSimilarName(pieces[1])){
						String s = "Client " + client.getName() + " changed name to ";
						client.setName(pieces[1]);
						System.out.println(s + client.getName());
					} else {
						client.sendMessage("Nick taken."); //TODO Make this a proper nice message XD
						System.out.println("Client attempted to connect with taken username. Client username: " + pieces[1]);
					}

				}

				
			}
			break;
		case "PONG":
			
			if(client.checkPing(pieces[1])){
				System.out.println("Ping has succeeded");
			} else {
				System.out.println("Ping has failed.");
			}
			
			break;
		//All further commands require client to be pinged. If not, send them a message.
		case "JOIN": //TODO this might look nice, but I feel like this is slightly incomplete. Pls improve.
			System.out.println("Client Attempted to Join IRC channel.");
			if(pieces.length > 1){
				System.out.println("   There are enough arguments.");
				if(client.getPinged() == true){
					System.out.println("   The client is pinged.");
					while(modifyingclients == true){
						System.out.println("Waiting for client modifications to finish...");
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String cname = pieces[1].replace("#", "");
					if(checkForChannel(cname)){
						Channel c = getChannelByName(cname);
						try{
							clients.remove(client);	
						}catch(Exception e){
							e.printStackTrace();
						}
						if(c.checkUser(client) == false){
							c.addUser(client);
							System.out.println("Added user " + client.getName() + " to channel " +"#" + c.getName());	
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
							channels.add(c);
							System.out.println("Created new channel.");
							System.out.println("Added user " + client.getName() + " to channel " +"#" + c.getName());	
						}
						
					}

				}
			}
			break;
		case "PRIVMSG":
			if(pieces.length > 2){
				if(client.getPinged()){
					if(pieces[1].contains("#")){
						System.out.println("Received message to channel");
						if(this.checkForChannel(pieces[1].replace("#", ""))){
							String message = "";
							for(int i = 2; i < pieces.length; i ++){
								message += " " + pieces[i];
							}
							System.out.println("Sending " + message + " to channel " + pieces[1].replace("#", ""));
							this.getChannelByName(pieces[1].replace("#", "")).sendChannelMSG(message);
						}
					}
				}
			}
			break;
		case "MOTD":
			String[] motd = this.getMOTD();
			for(String s: motd){
				getClientChannel(client).sendChannelMSG(s);
			}
			break;
		case "PING":
			String pong = command.replace("PING ", "");
			client.sendMessage("PONG");
			System.out.println("PONG");
			System.out.println("Returned "  + client.getName() + "'s ping with " + pong);
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
	  * Checks for the channel with the following client.
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
	  * Checks for the channel with the following client.
	  * @param nick the name to check for.
	  * @return
	  */
	Channel getClientChannel(Client client){
		for(Channel c: channels){
			if(c.checkUser(client)){
				return c;
			}
		}
		
		return null;
	}
}
