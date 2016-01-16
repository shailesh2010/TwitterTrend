package Model;
import java.sql.*;

public class connect {
	public Connection DatabaseConnection = null;
	public Statement Query = null;
	public boolean ErrorFlag = true;
	private static final String DatabaseName = "CloudTwitterMap";
	private static final String DatabaseServer = "localhost:1433";//"cloudtwittermap.cfjqyessvfk4.us-west-2.rds.amazonaws.com:1433";
	private static final String JDBCConnection = "jdbc:sqlserver://" + DatabaseServer + ";databaseName=" + DatabaseName;
	private static final String DatabaseLoginUser = "user";
	private static final String DatabaseLoginPassword = "password";

	public connect() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			DatabaseConnection = DriverManager.getConnection(JDBCConnection,
					DatabaseLoginUser, DatabaseLoginPassword);			
			Query = DatabaseConnection.createStatement();
		} catch (Exception e) {
			ErrorFlag = false;			
		}
	}
	
	public Statement getQuery(){		
		return Query; //same as DatabaseConnection.createStatement()		
	}

	public void closeConnection() {
		try {
			Query.close();
			DatabaseConnection.close();
		} catch (SQLException e) {
			ErrorFlag = false;
		}

	}
}
