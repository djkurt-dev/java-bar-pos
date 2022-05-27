package barPOS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
	public static Connection connect() {
		//declare connection string
		Connection con = null;
		try {
			//import JDBC
			Class.forName("org.sqlite.JDBC");
			
			//connect to the database
			con = DriverManager.getConnection("jdbc:sqlite:db-bordsbar.db"); 
			System.out.println("Database connection success!");
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println(e + "");			
		}
		return con;
	}
}
