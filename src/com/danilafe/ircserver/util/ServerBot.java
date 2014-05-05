package com.danilafe.ircserver.util;

import java.util.ArrayList;

import com.danilafe.ircserver.server.Client;
import com.danilafe.ircserver.server.Startup;

public class ServerBot {
	
	private Config config;
	private String name;
	private Startup parent;
	
	public ServerBot(Startup parent, String botname){
		name = botname;
		config = new Config(botname, "ServerBot");
		this.parent = parent;
	}
	
	/**
	 * Reads the command that starts with a !. 
	 * @param command the command
	 * @param c the client executing the command
	 * @param channelname the name of the channel in which the command was executed.
	 */
	public void read_command(String command, Client c, String channelname){
		
		ArrayList<String> lists = config.getProperties();
		for(String com: lists){
			String[] pieces = com.split(" ");
			if(pieces.length > 1){
				String reply = pieces[1];
				for(int i = 2; i < pieces.length; i ++){
					reply += " " + pieces[i];
				}
				parent.sendBotMessage(c, reply, this, channelname);
				
			}
		}
		
	}
	
	/**
	 * Returns the name of the bot
	 * @return the name of the bot
	 */
	public String getName(){
		return name;
	}

}
