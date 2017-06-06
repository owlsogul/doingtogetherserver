package kr.ac.cau.jomingyu.doingtogetherserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketThread implements Runnable{
	

	public boolean isLogin = false;
	
	private Socket soc;
	private SocketManager socketManager;
	private Thread thread;
	private BufferedReader br;
	private PrintWriter pw;
	
	public SocketThread(Socket soc, SocketManager socketManager){
		this.soc = soc;
		this.socketManager = socketManager;
		this.thread = new Thread(this);
	}
	
	public void ruuThread() throws IOException{
		thread.start();
		br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		pw = new PrintWriter(soc.getOutputStream());
		
	}
	
	public void stopThread(){
		thread.interrupt();
	}

	@Override
	public void run() {
		while (true){
			try {
				readData();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		Log.info(this.getClass(), "NULL");
	}
	
	/**
	 * public void readData() throws IOException<br><br>
	 *
	 * 클라이언트로부터 입력값을 전달받아 SocketManager의 processInput 으로 전달
	 * @throws IOException - I/O 오류가 발생했을 경우
	 */
	public void readData() throws IOException{
		String input = br.readLine();
		Log.info(this.getClass(), String.format("From %s:%d, receive: %s", soc.getInetAddress().toString(), soc.getPort(), input));
		socketManager.processeInput(this, input);
	}
	/**
	 * public void sendData(String data)<br><br>
	 *
	 * 클라이언트로 출력하는 메소드
	 * @param
	 * data - 클라이언트로 전달될 스트링. 반드시 JSON 형식으로 인코딩되어있어야한다.
	 */
	public void sendData(String data){
		pw.println(data);
		Log.info(this.getClass(), String.format("To %s:%d, send: %s", soc.getInetAddress().toString(), soc.getPort(), data));
		pw.flush();
		return;
	}
	
	
}
