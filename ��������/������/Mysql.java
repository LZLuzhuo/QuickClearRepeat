import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ���ݿ������
 * @author Luzhuo
 */
public class Mysql {
	private Connection conn;
	private PreparedStatement stat;
	
	public Mysql(String sqlName, String user,String password) {
		try{
			// ��ȡ���ݿ������
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql:///".concat(sqlName).concat("?user=").concat(user).concat("&password=").concat(password));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ر�����
	 */
	public void closeConnection(){
		if (conn != null) { try { conn.close(); } catch (SQLException e) { conn = null; } }
	}
	
	/**
	 * ������.ɾ���ٴ���
	 */
	public void inspectTable(String tableName) {
		Statement createStatement = null;
		try{
			// ɾ����,�ٽ��д���
			createStatement = conn.createStatement();
			createStatement.execute("DROP TABLE ".concat(tableName).concat(";"));
			createStatement.execute("CREATE TABLE ".concat(tableName).concat(" ( _id int primary key auto_increment, hashCode bigint, content varchar(255) )CHARACTER SET utf8;"));
		}catch(SQLException sqle){
			try{
				createStatement.execute("CREATE TABLE ".concat(tableName).concat(" ( _id int primary key auto_increment, hashCode bigint, content varchar(255) )CHARACTER SET utf8;"));
			}catch(Exception e){ e.printStackTrace(); }
		}
		
		if (createStatement != null) { try { createStatement.close(); } catch (SQLException e) { createStatement = null; } }
	}

	/**
	 * �ж����ݿ��Ƿ���ڸ�����
	 */
	public boolean dataIsexit(String tableName,long hashCode, String content) {
		ResultSet resultSet = null;
		try{
			stat = conn.prepareStatement("select count(*) as rowCount from ".concat(tableName).concat(" where hashCode = ? and content = ? ;"));
			
			stat.setLong(1, hashCode);
			stat.setString(2, content);
			resultSet = stat.executeQuery();
			
	        resultSet.next();
	        return resultSet.getInt("rowCount") == 0 ? false : true;
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}finally{
			try{
				if(resultSet != null) resultSet.close();
			}catch(Exception e){ resultSet = null; }
			try{
				if(stat != null) stat.close();
			}catch(Exception e){ stat = null; }
		}
		return false;
	}

	/**
	 * ��ӵ����ݿ�
	 */
	public void add(String tableName,long hashCode, String content) {
		try{
			stat = conn.prepareStatement("insert into ".concat(tableName).concat(" values(null,?,?);"));
			
			stat.setLong(1, hashCode);
			stat.setString(2, content);
			stat.executeUpdate();
			
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}finally{
			try{
				if(stat != null) stat.close();
			}catch(Exception e){ stat = null; }
		}
	}

}
