package kr.ac.cau.jomingyu.doingtogetherserver;

public class Log {
	public static void info(String log){
		System.out.println("|LOG|"+log);
	}
	public static void info(@SuppressWarnings("rawtypes") Class c, String log){
		System.out.println(c.getSimpleName()+": " + log);
	}
}
