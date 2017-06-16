package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		else if (dataType.equalsIgnoreCase(Constants.KEY_UPLOAD)){
			result = processUpload(map);
		}
		else if (dataType.equalsIgnoreCase(Constants.KEY_DOWNLOAD)){
			result = processDownload(map);
		}
		else if (dataType.equalsIgnoreCase(Constants.KEY_SHARE)){
			result = processShare(map);
		}
		else if (dataType.equalsIgnoreCase(Constants.KEY_TIMELINE)){
			result = processTimeline(map);
		}
		
		
		Log.info(this.getClass(), "message which will be sended is " + result);
		socketThread.sendData(result);
	}
	
	@SuppressWarnings("unchecked")
	public String processTimeline(LinkedHashMap<String, String> map) {
		
		String query = String.format("select * from %s limit 100", Constants.TABLE_SHARING_INFO);
		ResultSet result = server.jdbcManager.executeQuery(query);
		

		JSONObject jObj = new JSONObject();
		jObj.put(Constants.KEY_DATATYPE, Constants.KEY_TIMELINE);
		JSONArray jArr = new JSONArray();		
		try {
			while (result.next()){
				JSONObject content = new JSONObject();
				content.put(Constants.KEY_TIMELINE_WRITER_ID, result.getString(Constants.COLUMN_SHARING_WRITER_ID));
				content.put(Constants.KEY_TIMElINE_WRITE_TIME, result.getString(Constants.COLUMN_SHARING_WRITE_TIME));
				content.put(Constants.KEY_TIMELINE_TITLE, result.getString(Constants.COLUMN_SHARING_TITLE));
				content.put(Constants.KEY_TIMELINE_PRIORITY, String.valueOf(result.getInt(Constants.COLUMN_SHARING_PRIORITY)));
				content.put(Constants.KEY_TIMELINE_PEOPLE, result.getString(Constants.COLUMN_SHARING_PEOPLE));
				content.put(Constants.KEY_TIMELINE_DUE_DATE, result.getString(Constants.COLUMN_SHARING_DUEDATE));
				content.put(Constants.KEY_TIMELINE_MEMO, result.getString(Constants.COLUMN_SHARING_MEMO));
				jArr.add(content);
			}
		} catch (SQLException e) {
			Log.info(this.getClass(), "SQL ERROR. return ERROR CODE 400");
			jObj.put(Constants.KEY_TIMELINE_RESULT, "400");
			return jObj.toJSONString();
		}
		
		jObj.put(Constants.KEY_TIMELINE_CONTENTS, jArr);
		jObj.put(Constants.KEY_TIMELINE_RESULT, "777"
				);
		return jObj.toJSONString();
	}

	public String processShare(LinkedHashMap<String, String> map) {

		
		int writerKey = Integer.parseInt(map.get(Constants.KEY_SHARE_WRITER_KEY));
		String writerId = map.get(Constants.KEY_SHARE_WRITER_ID);
		String writeTime = map.get(Constants.KEY_SHARE_WRITE_TIME);
		String title = map.get(Constants.KEY_SHARE_TITLE);
		int priority = Integer.parseInt(map.get(Constants.KEY_SHARE_PRIORITY));
		String people = map.get(Constants.KEY_SHARE_PEOPLE);
		String dueDate = map.get(Constants.KEY_SHARE_DUEDATE);
		String memo = map.get(Constants.KEY_SHARE_MEMO);
		String images = map.get(Constants.KEY_SHARE_IMAGES);
				
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_SHARE);
		String query = 
			String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " +
						  "values(%d, \"%s\", \"%s\", \"%s\", %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\")", 
			Constants.TABLE_SHARING_INFO,
			Constants.COLUMN_SHARING_WRITER_KEY, 
			Constants.COLUMN_SHARING_WRITER_ID, 
			Constants.COLUMN_SHARING_WRITE_TIME, 
			Constants.COLUMN_SHARING_TITLE, 
			Constants.COLUMN_SHARING_PRIORITY,
			Constants.COLUMN_SHARING_PEOPLE,
			Constants.COLUMN_SHARING_DUEDATE,
			Constants.COLUMN_SHARING_MEMO,
			Constants.COLUMN_SHARING_IMAGES,
			Constants.COLUMN_SHARING_CHEERUP_KEY,
			writerKey, writerId, writeTime, title, priority, people, dueDate, memo, images, ""
			);
		Log.info(this.getClass(), "Query is " + query);
		int result = server.jdbcManager.updateQuery(query);
		
		
		try {
			Log.info(this.getClass(), ""+result);
		} catch (NullPointerException e){
			Log.info(this.getClass(), "글 작성에 실패하였습니다. " + e.getCause());
			map.put(Constants.KEY_SHARE_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		
		if (result == 0){
			Log.info(this.getClass(), "글 작성에 실패하였습니다. ");
			map.put(Constants.KEY_SHARE_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		
		map.put(Constants.KEY_SHARE_RESULT, "777");
		return JSONUtility.translateMapToJson(map);
	}

	public String processDownload(LinkedHashMap<String, String> map) {
		
		String id = map.get(Constants.KEY_DOWNLOAD_ID);
		
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_DOWNLOAD);
		File file = new File(server.dataFolder, id+".todo.json");
		
		if (!file.exists()){ // file check
			map.put(Constants.KEY_DOWNLOAD_RESULT, "400");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return JSONUtility.translateMapToJson(map);
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String content = "";
			while (true){
				String str = reader.readLine();
				if (str == null) break;
				content += str;
			}
			map.put(Constants.KEY_DOWNLOAD_CONTENT, content);
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			map.put(Constants.KEY_DOWNLOAD_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			map.put(Constants.KEY_DOWNLOAD_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		} catch (IOException e) {
			e.printStackTrace();
			map.put(Constants.KEY_DOWNLOAD_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
			
		}
		map.put(Constants.KEY_DOWNLOAD_RESULT, "700");
		return JSONUtility.translateMapToJson(map);
	}

	public String processUpload(LinkedHashMap<String, String> map) {
		
		String id = map.get(Constants.KEY_UPLOAD_ID);
		String content = map.get(Constants.KEY_UPLOAD_CONTENT);
		
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_UPLOAD);
		map.put(Constants.KEY_UPLOAD_ID, id);
		
		File dataFile = new File(server.dataFolder, id+".todo.json");
		if (!dataFile.exists()){
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				map.put(Constants.KEY_UPLOAD_RESULT, "400");
				return JSONUtility.translateMapToJson(map);
			}
		}
		
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile),"UTF-8"));
			output.write(content);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
			map.put(Constants.KEY_UPLOAD_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		map.put(Constants.KEY_UPLOAD_RESULT, "777");
		return JSONUtility.translateMapToJson(map);
		
	}
	
	public String processLogin(LinkedHashMap<String, String> map) {
		
		String id = map.get(Constants.KEY_LOGIN_ID);
		String pw = map.get(Constants.KEY_LOGIN_PW);
		
		String query = String.format("select * from %s where %s=\"%s\" and %s=\"%s\"", Constants.TABLE_USER_INFO, Constants.COLUMN_USER_ID, id, Constants.COLUMN_USER_PW, pw);
		ResultSet result = server.jdbcManager.executeQuery(query);
		int row = 0;
		int rowNum = 0;
		
		// 로그인으로 다시 넣기
		map.clear();
		map.put(Constants.KEY_DATATYPE, Constants.KEY_LOGIN);
		map.put(Constants.KEY_LOGIN_ID, id);
		
		try {
			while(result.next()){
				row++;
				rowNum = result.getInt("user_key");
			}
			System.out.println("row is "+ row + " row num is " + rowNum);
		} catch (SQLException e) {
			e.printStackTrace();
			map.put(Constants.KEY_LOGIN_RESULT, "400");
			return JSONUtility.translateMapToJson(map);
		}
		
		if (row >= 1){
			map.put(Constants.KEY_LOGIN_KEY, String.valueOf(rowNum));
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
