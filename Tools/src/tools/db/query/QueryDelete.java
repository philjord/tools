package tools.db.query;

import java.util.Vector;

public class QueryDelete
{
	private int ID = 0;

	private Vector<String> tables = new Vector<String>();

	public QueryDelete(int ID)
	{
		this.ID = ID;
	}

	public void addTable(String table)
	{
		tables.add(table);
	}

	public String createDeleteQuery()
	{
		String query = "";
		String whereClause = "WHERE ID =  " + ID + ":";
		for (int i = 0; i < tables.size(); i++)
		{
			query += "DELETE FROM ";
			query += tables.elementAt(i);
			query += whereClause;
		}
		return query;
	}
}
