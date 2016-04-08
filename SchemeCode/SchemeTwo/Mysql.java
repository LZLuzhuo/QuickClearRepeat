import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

/**
 * 数据库管理类
 * @author Luzhuo
 */
public class Mysql {
	private Connection conn;
	private PreparedStatement stat;
	
	public Mysql(String sqlName, String user,String password) {
		try{
			// 获取数据库的连接
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql:///".concat(sqlName).concat("?user=").concat(user).concat("&password=").concat(password));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭连接
	 */
	public void closeConnection(){
		if (conn != null) { try { conn.close(); } catch (SQLException e) { conn = null; } }
	}
	
	/**
	 * 创建表.删除再创建
	 */
	public void inspectTable(String tableName) {
		Statement createStatement = null;
		try{
			// 删掉表,再进行创建
			createStatement = conn.createStatement();
			createStatement.execute("DROP TABLE ".concat(tableName).concat(";"));
			createStatement.execute("CREATE TABLE ".concat(tableName).concat(" ( _id int primary key auto_increment, content varchar(255) binary, unique index(content) )CHARACTER SET utf8;"));
		}catch(SQLException sqle){
			try{
				createStatement.execute("CREATE TABLE ".concat(tableName).concat(" ( _id int primary key auto_increment, content varchar(255) binary, unique index(content) )CHARACTER SET utf8;"));
			}catch(Exception e){ e.printStackTrace(); }
		}
		
		if (createStatement != null) { try { createStatement.close(); } catch (SQLException e) { createStatement = null; } }
	}

	/**
	 * 添加到数据库
	 */
	public void add(String tableName, String content) {
		try{
			stat = conn.prepareStatement("insert into ".concat(tableName).concat(" values(null,?);"));
			
			stat.setString(1, content);
			stat.executeUpdate();
			
		}catch(MySQLIntegrityConstraintViolationException a){
			System.out.println(content.concat(":重复"));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}finally{
			try{
				if(stat != null) stat.close();
			}catch(Exception e){ stat = null; }
		}
	}
	
	/**
	 * 查询数据库
	 * @param tableName
	 * @return
	 */
	public ResultSet query(String tableName){
		try{
			stat = conn.prepareStatement("select * from ".concat(tableName).concat(" ;"));
			ResultSet executeQuery = stat.executeQuery();
			return executeQuery;
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	};

}
