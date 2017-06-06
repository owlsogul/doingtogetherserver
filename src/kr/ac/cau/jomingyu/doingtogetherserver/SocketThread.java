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
	 * Ŭ���̾�Ʈ�κ��� �Է°��� ���޹޾� SocketManager�� processInput ���� ����
	 * @throws IOException - I/O ������ �߻����� ���
	 */
	public void readData() throws IOException{
		String input = br.readLine();
		Log.info(this.getClass(), String.format("From %s:%d, receive: %s", soc.getInetAddress().toString(), soc.getPort(), input));
		socketManager.processeInput(this, input);
	}
	/**
	 * public void sendData(String data)<br><br>
	 *
	 * Ŭ���̾�Ʈ�� ����ϴ� �޼ҵ�
	 * @param
	 * data - Ŭ���̾�Ʈ�� ���޵� ��Ʈ��. �ݵ�� JSON �������� ���ڵ��Ǿ��־���Ѵ�.
	 */
	public void sendData(String data){
		pw.println(data);
		Log.info(this.getClass(), String.format("To %s:%d, send: %s", soc.getInetAddress().toString(), soc.getPort(), data));
		pw.flush();
		return;
	}
	
	
}
