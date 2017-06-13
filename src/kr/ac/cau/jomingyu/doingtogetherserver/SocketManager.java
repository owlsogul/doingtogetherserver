package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SocketManager {

	public DoingTogetherServer server;
	public ArrayList<SocketThread> socketList;
	public SocketManager(DoingTogetherServer server){
		this.socketList = new ArrayList<>();
		this.server = server;
	}
	
	public void addSocket(Socket soc){
		SocketThread socketThread = new SocketThread(soc, this);
		Log.info(this.getClass(), soc.getInetAddress().toString() + " 가 연결되었습니다.");
		try {
			socketThread.ruuThread();
			socketList.add(socketThread);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void removeSocket(SocketThread socketThread){
		int before = socketList.size();
		socketList.remove(socketThread);
		int after = socketList.size();
		Log.info(this.getClass(), String.format("Socket is removed. %d -> %d", before, after));
	}
	
	public void processeInput(SocketThread socketThread, String input){
		// Json string to map
		LinkedHashMap<String, String> map = JSONUtility.translateJsonToMap(input);
		String dataType = map.get(Constants.KEY_DATATYPE);
		String result = null;

		if (dataType.equalsIgnoreCase(Constants.KEY_REGISTER)){
			result = processRegister(map);
		}
		else if (dataType.equalsIgnoreCase(Constants.KEY_LOGIN)){
			result = processLogin(map);
		}
		
		
		Log.info(this.getClass(), "message which will be sended is " + result);
		socketThread.sendData(result);
	}
	
	public String processLogin(LinkedHashMap<String, String> map) {
		
		String id = map.get(Constants.KEY_LOGIN_ID);
		String pw = map.get(Constants.KEY_LOGIN_PW);
		
		String query = String.format("select * from %s where %s=\"%s\" and %s=\"%s\"", Constants.TABLE_USER_INFO, Constants.COLUMN_USER_ID, id, Constants.COLUMN_USER_PW, pw);
		ResultSet result = server.jdbcManager.executeQuery(query);
		int row = 0;
		
		// 로그인으로 다시 넣기
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_LOGIN);
		map.put(Constants.KEY_LOGIN_ID, id);
		
		try {
			while(result.next()){
				row++;
			}
			System.out.println("row is "+row);
		} catch (SQLException e) {
			e.printStackTrace();
			map.put(Constants.KEY_LOGIN_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		
		if (row >= 1){
			map.put(Constants.KEY_LOGIN_RESULT, "777");
		}
		else {
			map.put(Constants.KEY_LOGIN_RESULT, "400");
		}
		return JSONUtility.translateMapToJson(map);
	}

	public String processRegister(LinkedHashMap<String, String> map){
		
		// get data
		String id = map.get(Constants.KEY_REGISTER_ID);
		String pw = map.get(Constants.KEY_REGISTER_PW);
		String name = map.get(Constants.KEY_REGISTER_NAME);
		String email = map.get(Constants.KEY_REGISTER_EMAIL);
		String phone = map.get(Constants.KEY_REGISTER_PHONE);
		
		String query = String.format("select * from %s where id = \"%s\"", Constants.TABLE_USER_INFO, id);
		ResultSet result = server.jdbcManager.executeQuery(query);
		int row = 0;
		
		// register로  다시 넣기
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
		
		// 같은 게 있는지 확인하기
		try {
			while(result.next()){
				row++;
			}
			System.out.println("row is "+row);
		} catch (SQLException e) { // 오류나면 501 전송
			e.printStackTrace();
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "401");
			return JSONUtility.translateMapToJson(map);
		}

		if (row > 0){ // 이미 있는 계정일 경우
			Log.info(this.getClass(), id + " 는 이미 있는 계정입니다.");
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		// 아닐경우
		System.out.println(String.format("%s %s %s %s %s", id, pw, name, email, phone));
		query = String.format("insert into %s (%s, %s, %s, %s, %s) values(\"%s\", \"%s\", \"%s\", \"%s\", \"%s\")", Constants.TABLE_USER_INFO,
				Constants.COLUMN_USER_ID, Constants.COLUMN_USER_PW, Constants.COLUMN_USER_NAME, Constants.COLUMN_USER_EMAIL, Constants.COLUMN_USER_PHONE,
				id, pw, name, email, phone
				);
		System.out.println(query);
		int r = server.jdbcManager.updateQuery(query);
		if (r > 0){
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "777");
			return JSONUtility.translateMapToJson(map);	
		}
		else {
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
	}
	
}
