package tools.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import tools.db.query.QueryInsertMultiple;
import tools.db.query.QueryInsertOrUpdate;

public class DBProxy
{
	private Connection connection;

	public DBProxy()
	{
	}

	public boolean connectCS(String fileLocation)
	{
		String connectionString = "jdbc:db2j:" + fileLocation;
		return connect("com.ibm.db2j.jdbc.DB2jDriver", connectionString);
	}

	public boolean connectDerby(String fileLocation)
	{
		String connectionString = "jdbc:derby:" + fileLocation;
		return connect("org.apache.derby.jdbc.EmbeddedDriver", connectionString);
	}

	private boolean connect(String driverName, String connectionString)
	{

		// start by using this weird ass driver load call
		try
		{
			Class.forName(driverName);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// now make a connection for all time
		//		String home = System.getProperty("user.dir");
		// use for profiling
		//home = "C:\\Java\\workspace\\PyjamaWorld";

		try
		{
			connection = DriverManager.getConnection(connectionString);
		}
		catch (SQLException e)
		{
			System.out.println("Unable to connect to " + connectionString);

			// have we got a either a "does not exist" or "already connected" exception
			if (e.getErrorCode() == 40000)
			{
				SQLException next = e.getNextException();

				if (next != null && next.getErrorCode() == 45000)
				{
					// we got a "already connected"		
					System.out.println("There is another app already connected, cloudscape is single user");
					System.out.println("Remember that Websphere in Data Perspective can make connections");
				}
				else
				{
					// we got a "does not exist"		
					System.out.println("The DB doesn't exist (use " + connectionString + "create=true;)");
				}
			}
			else
			{
				//otherwise just print it out
				e.printStackTrace();
			}
			return false;
		}
		return true;

	}

	protected void finalize() throws Throwable
	{
		shutDown();
	}

	public void shutDown()
	{
		try
		{
			if (connection != null)
			{
				connection.close();
			}
		}
		catch (SQLException e)
		{
			//ignore, as we are shutting down
		}
	}

	public void runQuery(QueryInsertOrUpdate tq)
	{
		int rowsUpdated = execute(tq.createUpdateQuery());
		if (rowsUpdated == 0)
		{
			rowsUpdated = execute(tq.createInsertQuery());
		}
		// lets make sure exactly one row got affected
		if (rowsUpdated != 1)
		{
			new Exception("Bad number of rows affected! " + rowsUpdated + "for query " + tq.createInsertQuery()).printStackTrace();
		}
	}

	public void runQuery(QueryInsertMultiple qim)
	{
		qim.runInsertQuery(connection);
	}

	public int execute(String executeStatement)
	{
		try
		{
			CallableStatement cs = connection.prepareCall(executeStatement);
			cs.execute();
			return cs.getUpdateCount();
		}
		catch (SQLException e)
		{
			System.out.println("Error running SQL " + executeStatement);
			e.printStackTrace();
		}
		return -1;
	}

	public ResultSet getResults(String query)
	{
		try
		{
			CallableStatement cs = connection.prepareCall(query);
			cs.executeQuery();
			return cs.getResultSet();
		}
		catch (SQLException e)
		{
			System.out.println("Error running SQL " + query);
			e.printStackTrace();
		}
		return null;
	}

	public DatabaseMetaData getMetaData()
	{
		try
		{
			return connection.getMetaData();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return
	 */
	public Connection getConnection()
	{
		return connection;
	}

}
