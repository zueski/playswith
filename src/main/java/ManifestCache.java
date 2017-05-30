import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import org.apache.commons.dbcp2.BasicDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ManifestCache
{
	public static final String[] _validTablesArray = new String[] {"DestinyActivityBundleDefinition",
		"DestinyActivityCategoryDefinition",
		"DestinyActivityDefinition",
		"DestinyActivityModeDefinition",
		"DestinyActivityTypeDefinition",
		"DestinyBondDefinition",
		"DestinyClassDefinition",
		"DestinyCombatantDefinition",
		"DestinyDamageTypeDefinition",
		"DestinyDestinationDefinition",
		"DestinyDirectorBookDefinition",
		"DestinyEnemyRaceDefinition",
		"DestinyFactionDefinition",
		"DestinyGenderDefinition",
		"DestinyGrimoireCardDefinition",
		"DestinyGrimoireDefinition",
		"DestinyHistoricalStatsDefinition",
		"DestinyInventoryBucketDefinition",
		"DestinyInventoryItemDefinition",
		"DestinyItemCategoryDefinition",
		"DestinyLocationDefinition",
		"DestinyMedalTierDefinition",
		"DestinyObjectiveDefinition",
		"DestinyPlaceDefinition",
		"DestinyProgressionDefinition",
		"DestinyRaceDefinition",
		"DestinyRecordBookDefinition",
		"DestinyRecordDefinition",
		"DestinyRewardSourceDefinition",
		"DestinySandboxPerkDefinition",
		"DestinyScriptedSkullDefinition",
		"DestinySpecialEventDefinition",
		"DestinyStatDefinition",
		"DestinyStatGroupDefinition",
		"DestinyTalentGridDefinition",
		"DestinyTriumphSetDefinition",
		"DestinyUnlockFlagDefinition",
		"DestinyVendorCategoryDefinition",
		"DestinyVendorDefinition"
	};
	
	static private HashMap<String,BasicDataSource> _datasourceMap = new HashMap<String,BasicDataSource>();
	static private HashSet<String> _validTables = new HashSet(Arrays.asList(_validTablesArray));
	
	
	public static String getValue(String databasePath, String table, String id)
	{
		if(!validate(table))
		{	return null; }
		Connection conn = null;
		String val = null;
		try
		{
			BasicDataSource ds = getDataSource(databasePath);
			if(ds == null)
			{	return val; }
			conn = ds.getConnection();
			PreparedStatement s = conn.prepareStatement("select json from " + table + " where id=?");
			s.setString(1, id);
			ResultSet rs = s.executeQuery();
			if(rs.next())
			{	val = rs.getString(1); }
			rs.close();
			s.close();
		} catch(Exception e) {
			// log?
		} finally {
			if(conn != null)
			{	try { conn.close(); } catch(SQLException sqle) { } }
		} return val;
	}
	
	public static String getValue(String databasePath, String table, long id)
	{
		if(!validate(table))
		{	return null; }
		Connection conn = null;
		String val = null;
		try
		{
			BasicDataSource ds = getDataSource(databasePath);
			if(ds == null)
			{	return val; }
			conn = ds.getConnection();
			PreparedStatement s = conn.prepareStatement("select json from " + table + " where id=?");
			s.setLong(1, id);
			ResultSet rs = s.executeQuery();
			if(rs.next())
			{	val = rs.getString(1); }
			rs.close();
			s.close();
		} catch(Exception e) {
			// log?
		} finally {
			if(conn != null)
			{	try { conn.close(); } catch(SQLException sqle) { } }
		} return val;
	}
	
	public static String getValue(String databasePath, String table)
	{
		if(!validate(table))
		{	return null; }
		Connection conn = null;
		String val = null;
		try
		{
			BasicDataSource ds = getDataSource(databasePath);
			if(ds == null)
			{	return val; }
			conn = ds.getConnection();
			PreparedStatement s = conn.prepareStatement("select id,json from " + table);
			ResultSet rs = s.executeQuery();
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			while(rs.next())
			{
				if(isFirst)
				{
					sb.append("{");
					isFirst = false;
				} else {
					sb.append(",");
				}
				sb.append("\"" + rs.getString(1) + "\":"+rs.getString(2));
			}
			rs.close();
			s.close();
			val =  sb.toString() + "}";
		} catch(Exception e) {
			// log?
		} finally {
			if(conn != null)
			{	try { conn.close(); } catch(SQLException sqle) { } }
		} return val;
	}

	private static BasicDataSource getDataSource(String databasePath)
	{
		BasicDataSource ds = _datasourceMap.get(databasePath);
		if(ds == null)
		{
			synchronized(_datasourceMap)
			{
				ds = _datasourceMap.get(databasePath);
				if(ds == null)
				{
					try { Class.forName("org.sqlite.JDBC"); } catch(ClassNotFoundException cnfe) { }
					ds = new BasicDataSource();
					ds.setDriverClassName("org.sqlite.JDBC");
					ds.setUrl("jdbc:sqlite:" + databasePath);
					ds.setMinIdle(0);
					ds.setMaxIdle(1);
					ds.setMaxTotal(1);
					ds.setMaxWaitMillis(30000L);
					ds.setMaxOpenPreparedStatements(180);
					_datasourceMap.put(databasePath, ds);
				}
			}
		}
		return ds;
	}
	
	private static boolean validate(String tablename)
	{	return _validTables.contains(tablename); }
}