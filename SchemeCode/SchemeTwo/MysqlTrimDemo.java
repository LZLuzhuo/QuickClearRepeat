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
 * �����ݿ�Ψһ�����ķ�ʽ�Դ��ı����ݽ���ȥ��
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
		
		// ������,ɾ���ڴ���
		mysql.inspectTable(tableName);
		
		// 1.��ȡһ��,�ı����ȴ���3����ӵ����ݿ�,������
		addWordList();
		
		// 2.��ȡ���ݿ�,д���ļ�,1G/��
		writeFile();

		mysql.closeConnection();
		mysql = null;
		System.out.println("trim�������...");
	}


	/**
	 * 1.��ȡһ��,�ı����ȴ���3����ӵ����ݿ�,������
	 */
	private void addWordList() throws IOException {
		// ��ȡָ��Ŀ¼�µ������ļ�
		List<File> fileList = getFileList(dataFile);
		
		BufferedReader reader;
		for (File file : fileList) {
			// ��ȡһ���ַ�
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.ֱ����ӵ����ݿ�
					mysql.add(tableName, line);
				}else{
					System.out.println("���Ȳ���: ".concat(line));
				}
			}
			reader.close();
		}
		reader = null;

		fileList.clear();
		fileList = null;
		System.out.println("��ӵ����ݿ����...");
	}
	
	private int tempFile = 0;
	private int temp = 0;
	private static final int max = 1 * 1024 * 1024 * 1024;
	/**
	 * 2.��ȡ���ݿ�,д���ļ�,1G/��
	 * @throws IOException 
	 * @throws SQLException 
	 */
	private void writeFile() throws IOException{
		try{
			// ��ɸѡ�������������л����ļ�
			BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
			
			// ��ѯ���ݿ�
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
			System.out.println("��ӵ��ļ����...");
		}catch(SQLException e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * д������
	 */
	private void writerData(BufferedWriter writer,String data) throws IOException {
		writer.write(data);
		writer.newLine();
		writer.flush();
	}
	
	/**
	 * ��ȡָ��·���µ������ļ��б�
	 * @param dir Ҫ���ҵ�Ŀ¼
	 */
	private List<File> getFileList(File dirFile) {
	    List<File> listFile = new ArrayList<>();
	    //�������Ŀ¼�ļ�����ֱ�ӷ���
	    if (dirFile.isDirectory()) {
	        //����ļ����µ��ļ��б�Ȼ������ļ����ͷֱ���
	        File[] files = dirFile.listFiles();
	        if (null != files && files.length > 0) {
	            for (File file : files) {
	                //�������Ŀ¼��ֱ�����
	                if (!file.isDirectory()) {
	                    listFile.add(file);
	                } else {
	                    //����Ŀ¼�ļ����ݹ����
	                    listFile.addAll(getFileList(file));
	                }
	            }
	        }
	    }
	    return listFile;
	}

}
