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
	 * ������ MySQL�� �����մϴ�. Ŭ���� ���ο��� ���� ���� �۾��� �Ͼ�ϴ�.
	 * @return 
	 * boolean - ������ �Ǿ����� ����
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
	 * ������ MySQL�� ������ ����
	 * @return
	 * boolean - ������ ���������� ������� ����
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
