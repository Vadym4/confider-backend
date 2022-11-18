import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


public class BackendMain {
	public static void main (String[] args) {
	    try {
    HttpServer projektserver = HttpServer.create((new InetSocketAddress(9998)), 0);	
    HttpContext test = projektserver.createContext("/test");
    HttpContext setMitarbeiter = projektserver.createContext("/setMitarbeiter");
    HttpContext changeMitarbeiter = projektserver.createContext("changeMitarbeiter");
    HttpContext getMitarbeiter = projektserver.createContext("/getMitarbeiter");
    
    test.setHandler(BackendMain::handleRequest);

    projektserver.start();
	  } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void handleRequest(HttpExchange httpExchange) throws IOException {			
		//Ausgabe vom Requestbody
		InputStreamReader isr =  new InputStreamReader(httpExchange.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
		  sb.append(output);
		}
		System.out.println(sb.toString());
		
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "test123";
        httpExchange.sendResponseHeaders(200, htmlResponse.getBytes().length);
        outputStream.write(htmlResponse.getBytes());
        outputStream.close();
        
	}
	

	/**
	 * erstes TODO morgen: testen!
	 */
	private static void setMitarbeiter(HttpExchange input) throws IOException {
	//konvertiert input in String
		InputStreamReader isr =  new InputStreamReader(input.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
		  sb.append(output);
		}
				
		//Annahme: String kommt so rein wie bei Post request, d.h. key1=value1&key2=value2&...
		//--> INSERT (key1, key2, key3, ...) VALUES (value1, value2, ...)
		String inputAsString = sb.toString();
		String [] entries = inputAsString.split(",");
		
		String UPDATEDB = "INSERT INTO mitarbeiter(";
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split(":")[0]);
		}
		UPDATEDB = UPDATEDB.concat(") VALUES (");
		
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split("=")[1]);
		}	
		UPDATEDB.concat(")");
	}
	
	private static void getMitarbeiterFiltered(HttpExchange input) throws IOException {
		InputStreamReader isr =  new InputStreamReader(input.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
		  sb.append(output);
		}
				
		//Annahme: String kommt so rein wie bei Post request, d.h. key1=value1&key2=value2&...
		//--> INSERT (key1, key2, key3, ...) VALUES (value1, value2, ...)
		String inputAsString = sb.toString();
		String [] entries = inputAsString.split(",");
		
		//TODO: select name --> hier alle relvante attribute machen!
		
		
		String UPDATEDB = "SELECT name FROM mitarbeiter WHERE "; //WHERE
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split(":")[0]); //attribut
			UPDATEDB = UPDATEDB.concat("='");                 //=
			UPDATEDB = UPDATEDB.concat(entry.split(":")[1]);  //wert
			UPDATEDB = UPDATEDB.concat("' AND");
		}
	
	}
	

}
