import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * �����ݿ�ķ�ʽ�Դ��ı����ݽ���ȥ��,���ٶ�Ҳ̫���˰�...
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
		
		// ������,ɾ���ڴ���
		mysql.inspectTable(tableName);
		
		// 1.��ȡһ��,�Ƚ�HashCode+Length������,��ͬд�����ݿ�,��ͬ����
		inspectHashCode();
		
		mysql.closeConnection();
		mysql = null;
		System.out.println("trim�������...");
	}


	/**
	 * 1.��ȡһ��,�Ƚ�HashCode+Length������,��ͬд�����ݿ�,��ͬ����
	 */
	private void inspectHashCode() throws IOException {
		// ��ȡָ��Ŀ¼�µ������ļ�
		List<File> fileList = getFileList(dataFile);
		
		// ��ɸѡ�������������л����ļ�
		BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
		
		BufferedReader reader;
		for (File file : fileList) {
			// ��ȡһ���ַ�
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.1 �Ƚ�HashCode+���ݳ��Ⱥ�����.��ͬ��д���ļ�,��д�����ݿ�,��ͬ����
					long dataHashCode = Long.decode(String.valueOf(line.hashCode()).concat(String.valueOf(line.length())));
					boolean isexit = mysql.dataIsexit(tableName, dataHashCode, line);
					if(!isexit){ // ����
						writerData(saveWriter, line);
						mysql.add(tableName, dataHashCode, line);
					}
				}else{
					System.out.println("���Ȳ���: ".concat(line));
				}
			}
			reader.close();
		}
		reader = null;

		saveWriter.close();
		fileList.clear();
		fileList = null;
		System.out.println("HashCode�Ƚ����...");
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
