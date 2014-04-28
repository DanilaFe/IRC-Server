package com.danife.ircserver.userinterface;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.danife.ircserver.server.Startup;

public class GUI implements ActionListener{
	private JFrame frame = new JFrame("DanilaFe's IRC Server");
	private JTextArea area = new JTextArea();
	private JTextField field = new JTextField();
	private PrintWriter pwriter;
	private Startup s;
	
	public GUI(Startup startup){
		s = startup;
		
		try {
			pwriter = new PrintWriter("logs" + File.separator + "log-" + Calendar.getInstance().getTime().toString(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		frame.setSize(500,500);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(area, BorderLayout.CENTER);
		frame.add(field, BorderLayout.SOUTH);
		
		area.setEditable(false);
		field.addActionListener(this);
		
		frame.setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource().equals(field)){
			String text = field.getText();
			if(text.startsWith("/")){
				//It's a command.
				String command = text.replace("/", "");
				String[] pieces = command.split(" ");
				switch(pieces[0].toLowerCase()){
				case "quit":
					WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
					break;
				}
			}
			else {
				//It's text.
				s.sendServerMessage(text);
			}
			field.setText("");
		}
		
	}
	
	/**
	 * Adds a new line to the log. It is automatically time-stamped.
	 * @param line the line to add
	 */
	public void addLogLine(String line){
		Calendar c = Calendar.getInstance();
		area.append("[" + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)+ "] " + line + "\n");
		pwriter.println("[" + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)+ "] " + line);
	}

	/**
	 * Shuts down the gui.
	 */
	public void close(){
		pwriter.close();
	}
}
