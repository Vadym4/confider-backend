import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * name -> ISO Code
 * 
 * 
 * DONE:
 * - Tunnel zu localhost
 * - setMitarbeiter
 * - getAlleMitarbeiterMitFilter
 * - changeMitarbeiter
 * 
 * 
 * TODO Liste
 * - ID zurückgeben in der Response bei setMitarbeiter -> TESTEN
 * - getMitarbeiter response -> TESTEN
 * 
 * - API um FlaggenURL zurückzugeben (aus Sprache -> iso code -> Aufruf der API) 
 * - tabelle für projekte
 *
 *  10) Machine Learning model
 *  
 * 
 *  
 */

public class BackendMain {
	public static void main (String[] args) {
		//verbindung zu sqlite driver
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
    HttpServer projektserver = HttpServer.create((new InetSocketAddress(9980)), 0);	
    HttpContext test = projektserver.createContext("/test");
    HttpContext setMitarbeiter = projektserver.createContext("/setMitarbeiter");
    HttpContext changeMitarbeiter = projektserver.createContext("/changeMitarbeiter");
    HttpContext getMitarbeiterFiltered = projektserver.createContext("/getMitarbeiterFiltered");
    
    test.setHandler(BackendMain::handleRequest);
    setMitarbeiter.setHandler(BackendMain::setMitarbeiter);
    getMitarbeiterFiltered.setHandler(BackendMain::getMitarbeiterFiltered);
    changeMitarbeiter.setHandler(BackendMain::changeMitarbeiter);

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
        String htmlResponse = "hallo"; //hier die Response rein 
        httpExchange.sendResponseHeaders(200, htmlResponse.getBytes().length);
        outputStream.write(htmlResponse.getBytes());
        outputStream.close();
	}
	

/** DEPENDENDY: gleicher Code bei change Mitarbeiter */ 
	
	private static void setMitarbeiter(HttpExchange input) throws IOException {
		System.out.println("DEBUG: SET Mitarbeiter Request recieved");
	
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
		insertMitarbeiterIntoDB(inputAsString, "INSERT");

		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Vadym/Documents/mitarbeiter.db");
			Statement stmt = conn.createStatement();
			 String getCurrentID = "SELECT MAX(Id) FROM Mitarbeiter";

			 ResultSet rs    = stmt.executeQuery(getCurrentID);
			 String maxID = "";
				while (rs.next()) {
					maxID = rs.getString(1);
				}		
			stmt.close();
			conn.close();
            OutputStream outputStream = input.getResponseBody();
            input.sendResponseHeaders(200, maxID.getBytes().length);
            outputStream.write(maxID.getBytes());
            outputStream.close();
            
		}catch(Exception e) {
			e.printStackTrace();
		}
        
        
	}
     //JSON Daten zu SQL String, dann SQLString ausführen
	// mode ist INSERT oder UPDATE
	private static String insertMitarbeiterIntoDB(String inputAsString, String mode) throws IOException {
		inputAsString = inputAsString.replace("\"", "").replace("{", "").replace("}", "");
		System.out.println(inputAsString);
		System.out.println("DEBUG_MODE "+ mode);
				
		String UPDATEDB = "";
		if (mode.equals("INSERT")) {
			String [] entries = inputAsString.split(",");
			UPDATEDB = "INSERT INTO mitarbeiter(";	
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split(":")[0]);
			UPDATEDB = UPDATEDB.concat(",");
		}
		UPDATEDB = UPDATEDB.substring(0, UPDATEDB.length()-1);
		UPDATEDB = UPDATEDB.concat(") VALUES (");		
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat("'");
			UPDATEDB = UPDATEDB.concat(entry.split(":")[1]);
			UPDATEDB = UPDATEDB.concat("'");
			UPDATEDB = UPDATEDB.concat(",");
		}	
		UPDATEDB = UPDATEDB.substring(0, UPDATEDB.length()-1);
		UPDATEDB = UPDATEDB.concat(")");
		
		//bei UPDATE ist der erste Eintrag die ID für das Projekt
		/** UPDATE mitarbeiter SET k = v, k2 = v2, ... WHERE ID = employeeID*/
		
		} else if (mode.equals("UPDATE")) {
			//erster Eintrag im Array ist die employee id --> vor dem ersten , ist employeeID, danach ist der rest
			int Index = inputAsString.indexOf(",");
			String employeeID = inputAsString.substring(0, Index).split(":")[1];
			inputAsString = inputAsString.substring(Index+1);
			String [] entries = inputAsString.split(",");
			UPDATEDB = "UPDATE mitarbeiter SET ";	
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split(":")[0]);
			UPDATEDB = UPDATEDB.concat("='");
			UPDATEDB = UPDATEDB.concat(entry.split(":")[1]);
			UPDATEDB = UPDATEDB.concat("',");
		}
		
		
		UPDATEDB = UPDATEDB.substring(0, UPDATEDB.length()-1);
		UPDATEDB = UPDATEDB.concat(" WHERE Id='"+employeeID+"'");
		}
		
		else throw new IOException("Error in MODE");

		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Vadym/Documents/mitarbeiter.db");
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(UPDATEDB);
			stmt.close();
			conn.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//TODO: ausgeben!!!
	private static void getMitarbeiterFiltered(HttpExchange input) throws IOException {
		InputStreamReader isr =  new InputStreamReader(input.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
		  sb.append(output);
		}
				
		//Annahme: Annahme: String kommt rein im JSON Format
		
		String inputAsString = sb.toString();
-		inputAsString = inputAsString.replace("\"", "").replace("{", "").replace("}", "");
		String [] entries = inputAsString.split(",");
				
		//wenn keine Filter, dann SELECT * FROM mitarbeiter
		//sonst SELECT ... WHERE <<filter erfüllt>>
		
		String UPDATEDB = "SELECT * FROM mitarbeiter "; 
		if(entries.length>0) {
			UPDATEDB = UPDATEDB.concat("WHERE ");
		}
		for (String entry: entries) {
			UPDATEDB = UPDATEDB.concat(entry.split(":")[0]); //attribut
			UPDATEDB = UPDATEDB.concat("='");                 //=
			UPDATEDB = UPDATEDB.concat(entry.split(":")[1]);  //wert
			UPDATEDB = UPDATEDB.concat("' AND ");
		}
	
		//AND am ende raus
		UPDATEDB = UPDATEDB.substring(0, UPDATEDB.length()-4);
		System.out.println(UPDATEDB);
		
		//verbinde zu Datenbank
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Vadym/Documents/mitarbeiter.db");
			if (conn!=null) {
                System.out.println("Connected to db.");
        	}
			Statement stmt = conn.createStatement();
			/** HIER IST DAS ERGEBNIS */
            ResultSet rs    = stmt.executeQuery(UPDATEDB);
            String jsonString = convertResultSetIntoJSON(rs);
            OutputStream outputStream = input.getResponseBody();
            input.sendResponseHeaders(200, jsonString.getBytes().length);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * Struktur vom JSONSTRING: 
	 * {mitarbeiter: {attribut1: wert1, attribut2:wert2}, mitarbeiter:{...}
	 * 
	 * Beispiel:
	 * {"mitarbeiter":{"id":"1","name":"test_mitarbeiter","vorname":"null","age":"null","Email":"null","Reisebereitschaft":"null","Arbeitsdauer":"null","Standort":"null","Fachrichtung":"null","Abteilung":"null","Funktion":"null","Sprachen":"null","stundenzahl":"30","Berufserfahrung":"null"},"mitarbeiter":{"id":"2","name":"test_mitarbeiter","vorname":"null","age":"null","Email":"null","Reisebereitschaft":"null","Arbeitsdauer":"null","Standort":"null","Fachrichtung":"null","Abteilung":"null","Funktion":"null","Sprachen":"null","stundenzahl":"30","Berufserfahrung":"null"},"mitarbeiter":{"id":"3","name":"test_mitarbeiter2","vorname":"null","age":"null","Email":"null","Reisebereitschaft":"null","Arbeitsdauer":"null","Standort":"null","Fachrichtung":"null","Abteilung":"null","Funktion":"null","Sprachen":"null","stundenzahl":"30","Berufserfahrung":"null"}}
	 */
	
	private static String convertResultSetIntoJSON(ResultSet rs) throws SQLException {
		StringBuilder JSONString = new StringBuilder();
		JSONString.append("{");
		
		int i = 1;
		String [] AttributeToReturn = GlobaleAttribute.alleAttributeMitarbeiter;

		while (rs.next()) { 
			JSONString.append("\"mitarbeiter\":{"); //"mitarbeiter:{"
			
			while (i <= AttributeToReturn.length) {
				JSONString.append("\""+AttributeToReturn[i-1]+"\":");JSONString.append("\""); JSONString.append(rs.getString(i)); JSONString.append("\"");  //"name":"wert"
				JSONString.append(",");
				i++;
			}
			i=1;
			String newString = JSONString.substring(0, JSONString.length()-1); 
			JSONString = new StringBuilder(newString);
			
			JSONString.append("},");  //}
		}
		//, am Ende entfernen
		String newString = JSONString.substring(0, JSONString.length()-1); //TODO: , am Ende entfernen
		JSONString = new StringBuilder(newString);		
		
		JSONString.append("}");
		return JSONString.toString();	
	}
	
	/** ERSTES DING IN DER JSON IST DIE MITARBEITER ID */
	private static void changeMitarbeiter (HttpExchange input) throws IOException {
		System.out.println("DEBUG: request recieved");
		
		InputStreamReader isr =  new InputStreamReader(input.getRequestBody(),"utf-8");
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
		  sb.append(output);
		}
		String inputAsString = sb.toString();
		insertMitarbeiterIntoDB(inputAsString, "UPDATE");
		
		//TODO: schließt das die Verbindung?
        OutputStream outputStream = input.getResponseBody();
        outputStream.close();
		
	}
}
