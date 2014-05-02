package com.danilafe.ircserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Config {
	
	BufferedReader r;
	PrintWriter p;
	File config;
	
	
	public Config(){
		config = new File("serverConfig.txt");
		if(!config.exists()){ //If it does not exists, we create a new file and add defaults
			try {
				config.createNewFile();
				r = new BufferedReader(new FileReader(config));
				p = new PrintWriter(config);
				p.println("IRCPass: " + "defaultpass");
				p.println("IRCFileFolder: " + "files");
				p.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else { //Else just make the reader.
			try {
				r = new BufferedReader(new FileReader(config));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Rewrites the config file changing only the property mentioned.
	 * @param property the property to set.
	 * @param newvalue the new value of the property
	 */
	public void setProperty(String property, String newvalue){
		try {
			r = new BufferedReader(new FileReader(config));
		} catch (IOException e1) {
	
		}
		String temp = "";
		try {
			p = new PrintWriter(config);
			while((temp = r.readLine()) != null){
				if(!(temp.startsWith(property))){
					p.println(temp);
				} else {
					p.println(property + ": " + newvalue);
					p.close();
					return;
				}
			}
			p.println(property + ": " + newvalue);
			p.close();
		} catch (IOException e) {

		}
	}
	
	/**
	 * Gets the property indicated.
	 * @param property the property to get
	 * @return the property value.
	 */
	public String getProperty(String property){
		try {
			r = new BufferedReader(new FileReader(config));
		} catch (IOException e) {
		}
		String temp;
		try {
			while((temp = r.readLine()) != null){
				if(temp.startsWith(property)){
					return temp.replace(property + ": ", "");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}	

}
