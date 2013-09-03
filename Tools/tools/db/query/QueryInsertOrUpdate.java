package tools.db.query;

import java.util.ArrayList;

public class QueryInsertOrUpdate
{
	private String idColName = "ID";

	private int ID = 0;

	private String table;

	private ArrayList<String> fields = new ArrayList<String>();

	private ArrayList<String> values = new ArrayList<String>();

	public QueryInsertOrUpdate(int ID, String table)
	{
		this.ID = ID;
		this.table = table;
	}

	public QueryInsertOrUpdate(int ID, String table, String idColName)
	{
		this.ID = ID;
		this.table = table;
		this.idColName = idColName;
	}

	public void add(String field, int value)
	{
		fields.add(field);
		values.add(new Integer(value).toString());
	}

	public void add(String field, double value)
	{
		fields.add(field);
		values.add(new Double(value).toString());
	}

	public void add(String field, String value)
	{
		fields.add(field);
		values.add("'" + value.replaceAll("'", "''") + "'");
	}

	public String createInsertQuery()
	{
		if (fields.size() < 1)
		{
			System.out.println("No fields added to InsertOrUpdateQuery");
		}

		String query = "INSERT INTO " + table + " ( " + idColName + ", ";
		for (int i = 0; i < fields.size(); i++)
		{
			query += fields.get(i);
			query += (i < fields.size() - 1) ? ", " : " ) ";
		}
		query += " VALUES ( " + ID + ", ";
		for (int i = 0; i < values.size(); i++)
		{
			query += values.get(i);
			query += (i < values.size() - 1) ? ", " : " ) ";
		}
		return query;
	}

	public String createUpdateQuery()
	{
		if (fields.size() < 1)
		{
			System.out.println("No fields added to InsertOrUpdateQuery");
		}

		String query = "UPDATE " + table + " SET ";
		for (int i = 0; i < fields.size(); i++)
		{
			String field = fields.get(i);
			String value = values.get(i);

			query += field + " = ";
			query += value;
			query += (i < fields.size() - 1) ? ", " : "";
		}
		query += " WHERE " + idColName + " = " + ID;
		return query;
	}
}
