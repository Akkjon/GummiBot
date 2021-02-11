package de.akkjon.pr.mbrm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger extends PrintStream {
	
	private static final PrintStream standardOut = System.out;
	private static final PrintStream standardErr = System.err;
	private static boolean isRunning = false;
	
	private static String filePath = null;	
	
	public static void init() {
		if(!isRunning) {
			isRunning = true;
			filePath = getFilePath();
			System.setOut(new Logger(standardOut, true, "Out"));
			System.setErr(new Logger(standardErr, true, "Error"));
		}
	}
	
	public static String getFilePath() {
		return Storage.jarFolder + File.separator + "logs" + File.separator + "log_" + new SimpleDateFormat("dd.MM.yyyy_HH_mm_ss").format(new Date()) + ".log";
	}
	
	public static String getTimePrefix() {
		return "[" + new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(new Date()) + "] ";
	}
	
	private final PrintStream defaultStream;
	private boolean printTimePrefix = true;
	private final boolean isLoggingToChannel;
	private StringBuilder cache ;
	private long lastCache = 0;
	private final String name;

	private Logger(PrintStream defaultStream, boolean isLoggingToChannel, String name) {
		super(new OutputStream() {
			
			@Override
			public void write(int b) {
				// not used
			}
		});
		this.defaultStream = defaultStream;

		this.isLoggingToChannel = isLoggingToChannel;
		this.name = name + "\n";
		cache = new StringBuilder(this.name);
	}
	
	@Override
	public void println() {
		println("");
	}
	
	@Override
	public void println(boolean x) {
		println(String.valueOf(x));
	}
	
	@Override
	public void println(char x) {
		println(String.valueOf(x));
	}
	
	@Override
	public void println(char[] x) {
		println(new String(x));
	}
	
	@Override
	public void println(double x) {
		println(Double.toString(x));
	}
	
	@Override
	public void println(float x) {
		println(Float.toString(x));
	}
	
	@Override
	public void println(int x) {
		println(Integer.toString(x));
	}
	
	@Override
	public void println(long x) {
		println(Long.toString(x));
	}
	
	@Override
	public void println(Object x) {
		println(String.valueOf(x));
	}
	
	@Override
	public void println(String x) {
		log(x + "\n");
		printTimePrefix = true;
	}
	
	
	
	
	@Override
	public void print(boolean b) {
		print(String.valueOf(b));
	}
	
	@Override
	public void print(char c) {
		print(String.valueOf(c));
	}
	
	@Override
	public void print(char[] c) {
		print(new String(c));
	}
	
	@Override
	public void print(double d) {
		print(Double.toString(d));
	}
	
	@Override
	public void print(float f) {
		print(Float.toString(f));
	}
	
	@Override
	public void print(int i) {
		print(Integer.toString(i));
	}
	
	@Override
	public void print(long l) {
		print(Long.toString(l));
	}
	
	@Override
	public void print(Object o) {
		print(String.valueOf(o));
	}
	
	@Override
	public void print(String s) {
		log(s);
		printTimePrefix = false;
	}
	
	private void log(String s) {
		if(isLoggingToChannel && Main.isEnabled) {
			long stamp = System.currentTimeMillis();
			this.lastCache = stamp;
			cache.append(s);
			new Thread(() -> {
				try {
					Thread.sleep(500);
				} catch (InterruptedException err) {return;}
				if(stamp != Logger.this.lastCache) return;
				ServerWatcher.logError(cache.toString());
				cache = new StringBuilder(this.name);
			});
		}

		s = (printTimePrefix ? getTimePrefix() : "") + s;
		defaultStream.print(s);


		try {
			writeToLogFile(s);
		} catch (IOException ignored) {}
	}
	
	
	private boolean isMillisToday(long millis) {	
		
		Calendar today = Calendar.getInstance();

		Calendar other = Calendar.getInstance();
		other.setTimeInMillis(millis);
		
		return ((today.get(Calendar.YEAR) == other.get(Calendar.YEAR)) 
				&& (today.get(Calendar.MONTH) == other.get(Calendar.MONTH)) 
				&& (today.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)));
	}
	
	private void prepareNewFile() throws IOException {
		createFile();
		
		if(!isMillisToday(Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class).creationTime().toMillis())) {
			filePath = getFilePath();
			createFile();
		}
	}
	
	private void createFile() throws IOException {
		File file = new File(filePath);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
	}
	
	private void writeToLogFile(String s) throws IOException {
		prepareNewFile();
		
		Files.write(Paths.get(filePath), s.getBytes(), StandardOpenOption.APPEND);
	}
}
