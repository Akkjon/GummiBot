package de.akkjon.pr.mbrm;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class Index implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String response = Storage.getInternalFile("index.html");
		try {
			Handler.respond(exchange, response, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

class GetHandler implements HttpHandler {
	

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		List<String> data = Handler.getData();
		StringBuilder out = new StringBuilder();
		for (String string : data) {
			out.append(", \"").append(string).append("\"");
		}
		if(out.length() > 0) {
			out = new StringBuilder(out.substring(2));
		}
		String response = "[" + out + "]";
		Handler.respond(exchange, response, true);
	}
}

class RemoveHandler implements HttpHandler {
	

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String element = exchange.getRequestURI().getPath();
		String pathName = "/removedata/";
		if(element.length()>pathName.length()) {
			element = StringEscapeUtils.unescapeHtml4(element.substring(pathName.length()));
			List<String> data = Handler.getData();
			if(!data.contains(element)) {
				Handler.respond(exchange, "errNotExists", true);
			} else {
				data.remove(element);
				Handler.saveData(data);
				Handler.respond(exchange, "succ", true);
			}
		} else {
			Handler.respond(exchange, "errNoData", true);
		}
	}
}

class AddHandler implements HttpHandler {
	

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String element = exchange.getRequestURI().getPath();
		String pathName = "/adddata/";
		
		if(element.length()>pathName.length()) {
			element = StringEscapeUtils.unescapeHtml4(element.substring(pathName.length()));
			List<String> data = Handler.getData();
			if(data.contains(element)) {
				Handler.respond(exchange, "errAlreadyExists", true);
			} else {
				data.add(element);
				Handler.saveData(data);
				Handler.respond(exchange, "succ", true);
			}
		} else {
			Handler.respond(exchange, "errNoData", true);
		}
	}
}

class ControlHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String url = exchange.getRequestURI().getPath();
		String pathName = "/control/";
		if(url.equals(pathName + "stopbot")) {
			System.out.println("Stopping because of signal");
			Main.jda.getCategoryById(802719239723024414L).getTextChannels().forEach(channel -> channel.delete().complete());
			try {
				Updater.shutdownInternals();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Handler.respond(exchange, "succ", true);
			System.exit(0);
		} else if (url.equals(pathName + "restartbot")) {
			System.out.println("Restarting because of signal");
			String jarName = Storage.getJarName();
			if(jarName==null) {
				Handler.respond(exchange, "errNoPath", true);
				return;
			}
			String path = Storage.jarFolder + File.separator + jarName;
			
			try {
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", path);
				pb.start();
				Handler.respond(exchange, "succ", true);
				System.exit(0);
			} catch(Exception e) {
				Handler.respond(exchange, "err", true);
				e.printStackTrace();
			}
			
			
		} else if (url.equals(pathName + "updatebot")) {
			Handler.respond(exchange, "succ", true);
			Updater.getUpdater().updateRoutine();
		}
	}
	
}

class Handler {
	
	public static final String filePath = Storage.jarFolder + File.separator + "messagesData";
	private static HttpServer server = null;
	
	@SuppressWarnings("unchecked")
	protected static List<String> getData() throws IOException {
		File file = new File(filePath);
		if(!file.exists()) {
			return new ArrayList<>();
		}
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<String> data;
		try {
			data = (List<String>) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			data = new ArrayList<>();
		}
		ois.close();
		return data;
	}
	
	protected static void saveData(List<String> data) throws IOException {
		File file = new File(filePath);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(data);
		oos.close();
	}
	
	protected static void respond(HttpExchange exchange, String res, boolean isJson) throws IOException {
		if(isJson) {
			exchange.getResponseHeaders().set("charset", "utf-8");
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			res = new String(res.getBytes(StandardCharsets.UTF_8));
		}
		exchange.sendResponseHeaders(200, res.length());
		OutputStream os = exchange.getResponseBody();
		os.write(res.getBytes());
		os.close();
	}
	
	public static void initWebServer() throws IOException {
		if(server != null) return;
		server = HttpServer.create(new InetSocketAddress(8088), 0);
		server.createContext("/", new Index());
		server.createContext("/adddata", new AddHandler());
		server.createContext("/removedata", new RemoveHandler());
		server.createContext("/getdata", new GetHandler());
		server.createContext("/control", new ControlHandler());
		server.setExecutor(null);
		server.start();
	}
	
	public static void stopWebServer() {
		if(server != null) server.stop(0);
	}
}