package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SocketManager {

	public ArrayList<SocketThread> socketList;
	public SocketManager(){
		socketList = new ArrayList<>();
	}
	
	public void addSocket(Socket soc){
		SocketThread socketThread = new SocketThread(soc, this);
		try {
			socketThread.ruuThread();
			socketList.add(socketThread);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void processeInput(SocketThread socketThread, String input){
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(input);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
