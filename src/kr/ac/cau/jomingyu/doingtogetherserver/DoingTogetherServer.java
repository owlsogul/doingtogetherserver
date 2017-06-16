package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DoingTogetherServer {

	public static DoingTogetherServer Server;
	public static int Port = 8765;
	public static void main(String[] data){
		Server = new DoingTogetherServer(Port);
		Server.open();
		Server.close();
		
	}
	
	public ServerSocket ssoc;
	public int port;
	
	public SocketManager socketManager;
	public JDBCManager jdbcManager;
	public String dbName = "DoingTogether";
	public String id = "root";
	public String pw = "jo1203";
	
	public File dataFolder;
	public String dataFolderName = "data";
	
	public DoingTogetherServer(int port){
		this.port = port;
		this.socketManager = new SocketManager(this);
		this.jdbcManager = new JDBCManager("localhost", "3306", dbName, id, pw);
		this.dataFolder = new File(dataFolderName);
	}
	
	public void open(){
		if (!dataFolder.exists()){
			dataFolder.mkdirs();
		}
		if (jdbcManager.connectMySQL()){
			Log.info(this.getClass(), "MySQL과 연결되었습니다");
		}
		try {
			ssoc = new ServerSocket(port);
			Log.info(this.getClass(), "IP : "+ssoc.getInetAddress().toString() + " 에서 서버가 열렸습니다. PORT : " + port);
			while (true){
				Socket soc = ssoc.accept();
				socketManager.addSocket(soc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		if (jdbcManager.disconnectMySQL()){
			Log.info(this.getClass(), "MySQL과 연결을 끊었습니다.");
		}
		try {
			ssoc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
