package wifi;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import string.Print;

/**
 * wifi�ֵ��ļ�ȥ������,��Ҫ������ȥ��С��3���ֽڵĴ���,�ظ��Ĵ���.
 * @author Luzhuo
 */
public class WordListTrim {
	private File dataFile ;
	private File saveFile ;
	private ArrayList<File> temFileBlock; // �����ļ����ļ�
	private File temFile; // �ظ������ļ�
	private File temDir; //����Ŀ¼

	public WordListTrim(File dataFile, File saveFile) {
		this.dataFile = dataFile;
		this.saveFile = saveFile;
	}

	public void startTrim() throws Exception {
		if(dataFile == null) return;
		if(saveFile == null)
			saveFile = new File(dataFile.getParentFile(),"wordlist_01.txt");
		else
			saveFile = new File(saveFile,"wordlist_01.txt");
		
		temDir = new File(dataFile.getParentFile(), "tem");
		if(!temDir.exists()){ // ��ʱ�ļ��в�����
			temDir.mkdirs();
		}
		
		temFileBlock = new ArrayList<File>();
		
		// 1.���HashCode+���ݳ����Ƿ���ͬ,����ͬд��saveFile,��ͬ����д��temFile
		inspectHashCode();
		
		// 2.temFile�ļ��������ҹ���
		temFileFilter();
		
		// 3.temFile�ļ�������saveFile����ȥ�ع���
		temFileAndsaveFileFilter();
		
		// 4.�������ļ�
		delTemDir();
		
		Print.print("trim�������...");
	}
	
	/**
	 * 4.�������ļ�
	 */
	private void delTemDir() {
		for (File file : temFileBlock) {
			file.delete();
		}
		temDir.delete();
		temFileBlock.clear();
		temFileBlock = null;
		Print.print("�������ļ����...");
	}

	/**
	 * 3.temFile�ļ�������saveFile����ȥ�ع���
	 */
	private void temFileAndsaveFileFilter() throws Exception {
		ArrayList<String> temData = new ArrayList<String>(); // ���temFile����
		ArrayList<String> dataData = new ArrayList<String>(); // ���temFile���˺������
		BufferedReader temDataReader;
		BufferedReader dataReader;
		BufferedWriter dataWriter;
		// ��ÿ��temFile����ȥ�ع���,���ظ���д�뵽saveFile
		for (File file : temFileBlock) {
			temData.clear();
			temDataReader = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = temDataReader.readLine()) != null) {
				 temData.add(line);
			}
			temDataReader.close();
			
			dataReader = new BufferedReader(new FileReader(saveFile));
			
			String dataline = null;
			while ((dataline = dataReader.readLine()) != null) {
				// ����HashCode��ÿ�����ʵıȽ�
				for (String tem : temData) {
					// HashCode��ͬ,���ݲ�ͬ
					if(dataline.hashCode() == tem.hashCode() && !dataline.equals(tem)){
						dataData.add(tem);
						// Print.print("HashCode+length��ͬ,���ݲ�ͬ: ".concat(dataline.concat(" = ").concat(tem)));
					}
				}
			}
			dataReader.close();
			
			// �����˺������д��saveFile
			dataWriter = new BufferedWriter(new FileWriter(saveFile, true));
			for (String tem : dataData) {
				writerData(dataWriter, tem);
			}
			dataWriter.close();
		}
		
		temData.clear();
		temData = null;
		temDataReader = null;
		dataReader = null;
		dataWriter = null;
		Print.print("�ع������...");
	}

	/**
	 * 2.temFile�ļ��������ҹ���
	 * @throws Exception 
	 */
	private void temFileFilter() throws Exception {
		HashSet<String> temData = new HashSet<String>();// ��Ų��ظ�������
		BufferedReader temFilereader;
		BufferedWriter temFileWriter;
		// ��ÿ�������ļ��������ҹ���
		for (File file : temFileBlock) {
			temFilereader = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = temFilereader.readLine()) != null) {
				boolean isAdd = temData.add(line);
				
				 // if(!isAdd) Print.print("�ظ�: ".concat(line));
			}
			temFilereader.close();
			
			temFileWriter = new BufferedWriter(new FileWriter(file));
			for (String string : temData) {
				writerData(temFileWriter, string);
			}
			temFileWriter.close();
		}
		temData.clear();
		temData = null;
		temFilereader = null;
		temFileWriter = null;
		Print.print("���ҹ������...");
	}

	/**
	 * 1.�Ƚ�HashCodeֵ
	 */
	private void inspectHashCode() throws IOException {
		int temData = 0; // ��������
		int temDataPiece = 0; // �������ݿ����
		final int StageTemData = 10 * 1024 * 1024;
		
		// �����ظ���HashCode��¼��������
		HashSet<Long> hashCode = new HashSet<Long>();
		
		// ��ȡָ��Ŀ¼�µ������ļ�
		List<File> fileList = getFileList(dataFile);
		
		// ��һ�������ļ�
		temFile = new File(temDir, "temFile".concat(String.valueOf(temDataPiece)).concat(".txt"));
		temFileBlock.add(temFile);
		
		// ��ɸѡ�������������л����ļ�
		BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
		BufferedWriter temWriter = new BufferedWriter(new FileWriter(temFile));
		
		BufferedReader reader;
		for (File file : fileList) {
			// ��ȡһ���ַ�
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.1 �Ƚ�HashCode+���ݳ���,��ͬ�����HashCode��������,��д��saveFile����д��temFile
					long dataHashCode = Long.decode(String.valueOf(line.hashCode()).concat(String.valueOf(line.length())));
					boolean isAdd = hashCode.add(dataHashCode);
					if(!isAdd){ // ����,д��temFile
						writerData(temWriter, line);
					
						// ���ƻ����ļ��Ĵ�С,����ָ����С,�½������ļ�
						temData += line.length();
						if(temData >= StageTemData){
							temData = 0;
							temDataPiece ++;
							temWriter.close();
							temFile = new File(temDir, "temFile".concat(String.valueOf(temDataPiece)).concat(".txt"));
							temFileBlock.add(temFile);
							temWriter = new BufferedWriter(new FileWriter(temFile));
						}
					}else{ // ������,д��saveFile
						writerData(saveWriter, line);
					}
					
				}else{
					 // Print.print("���Ȳ���: ".concat(line));
				}
			}
			reader.close();
		}
		reader = null;

		saveWriter.close();
		temWriter.close();
		fileList.clear();
		fileList = null;
		hashCode.clear();
		hashCode = null;
		Print.print("HashCode�Ƚ����...");
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

