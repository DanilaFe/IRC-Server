package com.danilafe.ircserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class IP {
	
	private String myip;
	
	public IP() {
		try {
			URL ipget = new URL("http://icanhazip.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(ipget.openStream()));
			myip = in.readLine();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the received ip.
	 * @return
	 */
	public String getIP(){
		return myip;
	}

}
