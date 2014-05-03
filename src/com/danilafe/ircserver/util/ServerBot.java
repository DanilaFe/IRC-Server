package com.danilafe.ircserver.util;

import java.util.ArrayList;

import com.danife.ircserver.server.Client;
import com.danife.ircserver.server.Startup;

public class ServerBot {
	
	private Config config;
	private String name;
	private Startup parent;
	
	public ServerBot(Startup parent, String botname){
		name = botname;
		config = new Config(botname, "ServerBot");
		this.parent = parent;
	}
	
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
	
	public String getName(){
		return name;
	}

}
