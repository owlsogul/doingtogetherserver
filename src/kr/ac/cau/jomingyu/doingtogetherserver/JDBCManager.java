package kr.ac.cau.jomingyu.doingtogetherserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


public class JDBCManager {

	public String ip, port, db;
	public Connection conn;
	private String url;
	private String id;
	private String pw;
	public JDBCManager(String ip, String port, String db, String id, String pw){
		this.ip = ip;
		this.port = port;
		this.db = db;
		url = String.format("jdbc:mysql://%s:%s/%s", ip, port, db);
		this.id = id;
		this.pw = pw;
	}
	
	
	public ResultSet sendQuery(String query){
		try {
			java.sql.Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			rs = st.getResultSet();
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
 	}
	
	
	
	
	
	/**
	 * 서버와 MySQL을 연결합니다. 클래스 내부에서 연결 관련 작업이 일어납니다.
	 * @return 
	 * boolean - 연결이 되었는지 리턴
	 * */
	public boolean connectMySQL(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, id, pw);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 서버와 MySQL의 연결을 끊음
	 * @return
	 * boolean - 연결이 정상적으로 끊겼는지 리턴
	 */
	public boolean disconnectMySQL(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
