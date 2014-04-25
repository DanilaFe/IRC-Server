package com.danife.ircserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Startup {
	
	
	
	Startup me = this;
	ArrayList<Client> clients = new ArrayList<Client>();
	ServerSocket s;
	Thread acceptthread = new Thread(){
		public void run(){
			while(true){
				
				try {
					Socket temps = s.accept();
					Client c = new Client(me, temps);
					System.out.println("Client connected.");
					clients.add(c);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	};
	
	public Startup(){
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
	
	public void handleCommand(Client client, String command){
		String[] pieces = command.split(" ");
		String argument1 = command.split(" ")[0];
		switch(argument1){
		case "USER":
			if(pieces.length >= 5){
				
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
				
				
				
			}
			break;
		case "NICK":
			if(pieces.length > 1){

				String s = "Client " + client.getName() + " changed name to ";
				client.setName(pieces[1]);
				System.out.println(s + client.getName());
				
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
		
		}
	}
}
