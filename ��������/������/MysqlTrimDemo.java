import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 以数据库的方式对大文本内容进行去重,这速度也太慢了吧...
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
			this.saveFile = new File(dataFile.getParentFile(),"wordlist_01.txt");
		else
			this.saveFile = new File(saveFile,"wordlist_01.txt");
		
		if(user == null || password == null) return;
		mysql = new Mysql(sqlName, user, password);
		
		// 创建表,删除在创建
		mysql.inspectTable(tableName);
		
		// 1.读取一行,比较HashCode+Length和内容,不同写入数据库,相同丢弃
		inspectHashCode();
		
		mysql.closeConnection();
		mysql = null;
		System.out.println("trim任务完成...");
	}


	/**
	 * 1.读取一行,比较HashCode+Length和内容,不同写入数据库,相同丢弃
	 */
	private void inspectHashCode() throws IOException {
		// 获取指定目录下的所有文件
		List<File> fileList = getFileList(dataFile);
		
		// 将筛选出来的数据序列化到文件
		BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
		
		BufferedReader reader;
		for (File file : fileList) {
			// 读取一行字符
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.1 比较HashCode+内容长度和内容.不同则写入文件,并写入数据库,相同则丢弃
					long dataHashCode = Long.decode(String.valueOf(line.hashCode()).concat(String.valueOf(line.length())));
					boolean isexit = mysql.dataIsexit(tableName, dataHashCode, line);
					if(!isexit){ // 存在
						writerData(saveWriter, line);
						mysql.add(tableName, dataHashCode, line);
					}
				}else{
					System.out.println("长度不够: ".concat(line));
				}
			}
			reader.close();
		}
		reader = null;

		saveWriter.close();
		fileList.clear();
		fileList = null;
		System.out.println("HashCode比较完成...");
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
