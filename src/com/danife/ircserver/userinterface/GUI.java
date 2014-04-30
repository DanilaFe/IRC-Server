package com.danife.ircserver.userinterface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
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
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.danife.ircserver.server.Startup;

public class GUI implements ActionListener{
	Container data = new Container();
	Container lists = new Container();
	private JScrollPane userpane;
	private JScrollPane channelpane;
	private JScrollPane areapane;
	private JList<String> users;
	private JList<String> channels;
	private JFrame frame = new JFrame("DanilaFe's IRC Server");
	private JTextArea area = new JTextArea();
	private JTextField field = new JTextField();
	private PrintWriter pwriter;
	private Startup s;
	private Thread updatelists = new Thread(){
		public void run(){
			while(true){
				try {
					Thread.sleep(500);
					users.setListData(s.getUserNames());
					channels.setListData(s.getChannelNames());
					frame.revalidate();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	
	public GUI(Startup startup){
		s = startup;
		users = new JList<String>(s.getUserNames());
		channels = new JList<String>(s.getChannelNames());
		userpane = new JScrollPane(users);
		channelpane = new JScrollPane(channels);
		areapane = new JScrollPane(area);
		
		lists.setLayout(new GridLayout(0,1));
		lists.add(userpane);
		lists.add(channelpane);
		
		data.setLayout(new BorderLayout());
		data.add(areapane, BorderLayout.EAST);
		data.add(lists, BorderLayout.CENTER);
		
		
		try {
			File folder = new File("logs");
			if(!folder.exists()){
				folder.mkdir();
			}
			
			String[] simpletime = this.getSimpleTime();
			String time = "";
			for(String s: simpletime){
				time += "_" + s;
			}
			pwriter = new PrintWriter("logs" + File.separator + "log" + time + ".txt", "UTF-8");

		
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getCause());
		}
		
		
		frame.setSize(500,500);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		frame.add(data, BorderLayout.CENTER);
		frame.add(field, BorderLayout.SOUTH);
		
		area.setEditable(false);
		field.addActionListener(this);
		
		frame.setVisible(true);
		updatelists.start();
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
	
	public String[] getSimpleTime(){
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		int second = Calendar.getInstance().get(Calendar.SECOND);
		int montht = Calendar.getInstance().get(Calendar.MONTH);
		String month = null;
		switch(montht){
		case 1:
			month = "January";
			break;
		case 2:
			month = "February";
			break;
		case 3:
			month = "March";
			break;
		case 4:
			month = "April";
			break;
		case 5:
			month = "May";
			break;
		case 6:
			month = "June";
			break;
		case 7:
			month = "July";
			break;
		case 8:
			month = "August";
			break;
		case 9:
			month = "September";
			break;
		case 10:
			month = "October";
			break;
		case 11:
			month = "November";
			break;
		case 12:
			month = "December";
			break;
		}
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return new String[] {Integer.toString(hour),Integer.toString(minute),Integer.toString(second),month, Integer.toString(year)};
	}
}
