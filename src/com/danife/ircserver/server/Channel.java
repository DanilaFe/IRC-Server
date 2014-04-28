package com.danife.ircserver.server;

import java.util.ArrayList;

public class Channel {
	
	private String topic;
	private String name;
	private ArrayList<Client> usershere = new ArrayList<Client>(); 
	//This is daithi testing commenting and uploading shit.
	
	public Channel(String name){
		this.name = name;
	}

	/**
	 * Requests channel's name.
	 * @return The channel's name.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Attempts to get the client with the name that is in this channel.
	 * @param name The name of the requested player
	 * @return The player, or null if one with that name isn't there.
	 */
	public Client getClientWithName(String name){
		for(Client c: usershere){
			if(c.getName().equals(name)){
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks wether the client with the name is there
	 * @param name the name for which to check.
	 * @return true or false, true if client with the name is found.
	 */
	public boolean checkClient(String name){
		boolean is_here = false;
		for(Client c: usershere){
			if(c.getName().equals(name)){
				is_here = true;
			}
		}
		
		return is_here;
	}
	
	/**
	 * Adds a user to this channel. Please do this after removing said user from the main lobby.
	 * @param c the player to be added.
	 */
	public void addUser(Client c){
		usershere.add(c);
	}
	
	/**
	 * Requests the list of users on this server.
	 * @return the list of users on the channel.
	 */
	public ArrayList<Client> getUsers(){
		return usershere;
	}
	
	/**
	 * Checks wether the client is on the channel.
	 * @param c the client to be checked for.
	 * @return true if the client IS there.
	 */
	public boolean checkUser(Client c){
		for(Client tempc: usershere){
			if(tempc.equals(c)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Sends message to every client on the channel.
	 * @param s the message to send.
	 */
	public void sendChannelMSG(String s){
		sendChannelMSGExclude(null, s);
	}
	
	/**
	 * Sends a message to every client except for the one passed down to it.
	 * @param excludeme the client to be excluded
	 * @param message the message to be sent.
	 */
	public void sendChannelMSGExclude(Client excludeme, String message){
		if(excludeme != null){
			for(Client c: usershere){
				if(!(excludeme.equals(c))){
					c.sendMessage(message);
				}
			}
		} else {
			for(Client c: usershere){
				c.sendMessage(message);
			}
		}

	}
	
	/**
	 * Makes a user leave the channel.
	 * @param c
	 */
	public void partUser(Client c){
		for(int i = 0; i < usershere.size(); i ++){
			if(c.equals(usershere.get(i))){
				usershere.remove(usershere.get(i));
			}
		}
	}
	
	/**
	 * Request a string with a list of all users on the channel. Used for NAMES command.
	 * @return the string containing the users ont the channel.
	 */
	public String returnUsers(){
		String returnme = "";
		if(usershere.size() > 0){
			returnme += "@" + usershere.get(0).getName();
		}
		if(usershere.size() > 1){
			for(int i = 1; i < usershere.size(); i ++){
				returnme += " " + usershere.get(i).getName();
			}
		}
		
		return returnme;

	}
	
	/**
	 * Get the number of users on the channels
	 * @return the number of users on the channel
	 */
	public int getUserCount(){
		return usershere.size();
	}
	
	/**
	 * Returns the channel's topic.
	 * @return the topic of the channel.
	 */
	public String getTopic(){
		return topic;
	}
	
			

}
