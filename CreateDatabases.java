

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDatabases {
		public static void main (String [] args) {  
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			/** 
			 * PFAD ZUR DB
			 */
			String url = "jdbc:sqlite:C:/Users/Vadym/Documents/mitarbeiter.db";  
	        try {  
	            Connection conn = DriverManager.getConnection(url);  
	            if (conn != null) {  
	                DatabaseMetaData meta = conn.getMetaData();  
	                System.out.println("The driver name is " + meta.getDriverName());  
	                System.out.println("A new database has been created.");  
	            }  
	            /**
	             * SPALTEN DER DB
	             */
	            String sql = "CREATE TABLE IF NOT EXISTS mitarbeiter ("  
	                    + " Id integer PRIMARY KEY,"  
	                    + " Name text NOT NULL, " 
	                    + " Vorname TEXT, " 
	                   
	                    + " age TEXT, " 
	                    + " Email TEXT, "

	                    + " Reisebereitschaft TEXT, " //TODO: evt. integer
	                    + " Arbeitsdauer TEXT, " 
	                    + " Standort TEXT, " 
	                    
	                    + " Fachrichtung TEXT,"
	                    + " Abteilung TEXT,"
	                    + " Funktion TEXT,"
	      
	                    + " Sprachen TEXT, " 
	                    + " Stundenzahl INTEGER, " 
	                    + " Berufserfahrung INTEGER"
	                   + ")";  

	                    /**
	                     * Bescheinigungen
	                     * Zertifikate
	                     * Fortbildungen
	                     * 
	                     * Kentnisse
	                     *  -> Programmiersprachen
						 *
	                     */
	                    
	                    //TODO
	            Statement stmt = conn.createStatement();  
	            stmt.execute(sql);  
	        } catch (SQLException e) {  
	            System.out.println(e.getMessage());  
	        }  
	        
		    }  
}
		