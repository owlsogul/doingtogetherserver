package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class SocketManager {

	public DoingTogetherServer server;
	public ArrayList<SocketThread> socketList;
	public SocketManager(DoingTogetherServer server){
		this.socketList = new ArrayList<>();
		this.server = server;
	}
	
	public void addSocket(Socket soc){
		SocketThread socketThread = new SocketThread(soc, this);
		Log.info(this.getClass(), soc.getInetAddress().toString() + " �� ����Ǿ����ϴ�.");
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
		LinkedHashMap<String, String> map = JSONUtility.translateJsonToMap(input);
		String dataType = map.get(Constants.KEY_DATATYPE);
		String result = null;
		Set<String> set = map.keySet();
		for (String key : set){
			Log.info(this.getClass(), key + " - " + map.get(key));
		}
		if (dataType.equalsIgnoreCase(Constants.KEY_REGISTER)){
			result = processRegister(map);
		}
		Log.info(this.getClass(), "message which will be sended is " + result);
		socketThread.sendData(result);
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
		map.clear();
		
		// ���� �� �ִ��� Ȯ���ϱ�
		try {
			while(result.next()){
				row++;
			}
			System.out.println("row is "+row);
		} catch (SQLException e) { // �������� 501 ����
			e.printStackTrace();
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "401");
			return JSONUtility.translateMapToJson(map);
		}

		if (row > 0){ // �̹� �ִ� ������ ���
			Log.info(this.getClass(), id + " �� �̹� �ִ� �����Դϴ�.");
			map.put(Constants.KEY_DATATYPE, Constants.KEY_REGISTER);
			map.put(Constants.KEY_REGISTER_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		// �ƴҰ��
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
