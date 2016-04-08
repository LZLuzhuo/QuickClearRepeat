import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 以数据库唯一索引的方式对大文本内容进行去重
 * @author Luzhuo
 */
public class MysqlTrimDemo {
	private static final String tableName = "saveData";
	private Mysql mysql;
	private File dataFile ;
	private File saveFile ;
	
	public void startTrim(String sqlName, String user, String password, File dataFile, File saveFile) throws IOException {
		if(dataFile == null) return;
		this.dataFile = dataFile;
		if(saveFile == null)
			this.saveFile = new File(dataFile.getParentFile(),"wordlist_0.txt");
		else
			this.saveFile = new File(saveFile,"wordlist_0.txt");
		
		if(user == null || password == null) return;
		mysql = new Mysql(sqlName, user, password);
		
		// 创建表,删除在创建
		mysql.inspectTable(tableName);
		
		// 1.读取一行,文本长度大于3则添加到数据库,否则丢弃
		addWordList();
		
		// 2.读取数据库,写入文件,1G/个
		writeFile();

		mysql.closeConnection();
		mysql = null;
		System.out.println("trim任务完成...");
	}


	/**
	 * 1.读取一行,文本长度大于3则添加到数据库,否则丢弃
	 */
	private void addWordList() throws IOException {
		// 获取指定目录下的所有文件
		List<File> fileList = getFileList(dataFile);
		
		BufferedReader reader;
		for (File file : fileList) {
			// 读取一行字符
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.直接添加到数据库
					mysql.add(tableName, line);
				}else{
					System.out.println("长度不够: ".concat(line));
				}
			}
			reader.close();
		}
		reader = null;

		fileList.clear();
		fileList = null;
		System.out.println("添加到数据库完成...");
	}
	
	private int tempFile = 0;
	private int temp = 0;
	private static final int max = 1 * 1024 * 1024 * 1024;
	/**
	 * 2.读取数据库,写入文件,1G/个
	 * @throws IOException 
	 * @throws SQLException 
	 */
	private void writeFile() throws IOException{
		try{
			// 将筛选出来的数据序列化到文件
			BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
			
			// 查询数据库
			ResultSet query = mysql.query(tableName);
			if(query == null) return;
			while(query.next()){
				String line = query.getString("content");
				writerData(saveWriter, line);
				temp += line.length();
				if(temp >= max) { 
					tempFile ++;
					saveWriter = new BufferedWriter(new FileWriter(new File(saveFile.getParentFile(),"wordlist_".concat(String.valueOf(tempFile)).concat(".txt"))));
					temp = 0;
				}
			}
			query.close();
			saveWriter.close();
			System.out.println("添加到文件完成...");
		}catch(SQLException e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * 写入数据
	 */
	private void writerData(BufferedWriter writer,String data) throws IOException {
		writer.write(data);
		writer.newLine();
		writer.flush();
	}
	
	/**
	 * 获取指定路径下的所有文件列表
	 * @param dir 要查找的目录
	 */
	private List<File> getFileList(File dirFile) {
	    List<File> listFile = new ArrayList<>();
	    //如果不是目录文件，则直接返回
	    if (dirFile.isDirectory()) {
	        //获得文件夹下的文件列表，然后根据文件类型分别处理
	        File[] files = dirFile.listFiles();
	        if (null != files && files.length > 0) {
	            for (File file : files) {
	                //如果不是目录，直接添加
	                if (!file.isDirectory()) {
	                    listFile.add(file);
	                } else {
	                    //对于目录文件，递归调用
	                    listFile.addAll(getFileList(file));
	                }
	            }
	        }
	    }
	    return listFile;
	}

}
