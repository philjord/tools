package tools.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryInsertMultiple
{
	private String idColName = "ID";

	private int ID = 0;

	private String table;

	private String[] fields;

	private ArrayList<String[]> strings = new ArrayList<String[]>();

	private ArrayList<float[]> floats = new ArrayList<float[]>();

	private ArrayList<int[]> ints = new ArrayList<int[]>();

	private ArrayList<double[]> doubles = new ArrayList<double[]>();

	public QueryInsertMultiple(int ID, String table)
	{
		this.ID = ID;
		this.table = table;
	}

	public QueryInsertMultiple(int ID, String table, String idColName)
	{
		this.ID = ID;
		this.table = table;
		this.idColName = idColName;
	}

	public void setFields(String[] fields)
	{
		this.fields = fields;
	}

	public void add(int[] row)
	{
		ints.add(row);
	}

	public void add(float[] row)
	{
		floats.add(row);
	}

	public void add(double[] row)
	{
		doubles.add(row);
	}

	public void add(String[] row)
	{
		// note we do not double up single quotes as we are not using a "string" update query
		strings.add(row);
	}

	public void runInsertQuery(Connection connection)
	{
		String query = "INSERT INTO " + table + " ( " + idColName + ", ";
		for (int i = 0; i < fields.length; i++)
		{
			query += fields[i];
			query += (i < fields.length - 1) ? ", " : " )";
		}
		query += " VALUES ( " + ID + ", ";
		for (int i = 0; i < fields.length; i++)
		{
			query += (i < fields.length - 1) ? "?, " : "? )";
		}

		try
		{
			// turn off auto commit for speed
			connection.setAutoCommit(false);

			PreparedStatement ps = connection.prepareStatement(query);

			if (strings.size() > 0)
			{
				// strings
				for (int i = 0; i < strings.size(); i++)
				{
					String[] row = strings.get(i);
					for (int j = 0; j < row.length; j++)
					{
						ps.setString(j + 1, row[j]);
					}
					ps.execute();
				}
			}
			else if (ints.size() > 0)
			{
				// ints
				for (int i = 0; i < ints.size(); i++)
				{
					if (i % (ints.size() / 5) == 0)
					{
						System.out.println("i = " + i + " float rows, out of " + ints.size());
					}
					int[] row = ints.get(i);
					for (int j = 0; j < row.length; j++)
					{
						ps.setInt(j + 1, row[j]);
					}
					ps.execute();
				}
			}
			else if (doubles.size() > 0)
			{
				// doubles
				for (int i = 0; i < doubles.size(); i++)
				{
					if (i % (doubles.size() / 5) == 0)
					{
						System.out.println("i = " + i + " float rows, out of " + doubles.size());
					}
					double[] row = doubles.get(i);
					for (int j = 0; j < row.length; j++)
					{
						ps.setDouble(j + 1, row[j]);
					}
					ps.execute();
				}
			}
			else if (floats.size() > 0)
			{
				// floats
				for (int i = 0; i < floats.size(); i++)
				{
					if (i % (floats.size() / 5) == 0)
					{
						System.out.println("i = " + i + " float rows, out of " + floats.size());
					}
					float[] row = floats.get(i);
					for (int j = 0; j < row.length; j++)
					{
						ps.setFloat(j + 1, row[j]);
					}
					ps.execute();
				}
			}

			connection.commit();
			// turn on auto commit again
			connection.setAutoCommit(true);

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
