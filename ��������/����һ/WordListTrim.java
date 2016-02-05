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
 * wifi字典文件去重整理,主要功能是去掉小于3个字节的词组,重复的词组.
 * @author Luzhuo
 */
public class WordListTrim {
	private File dataFile ;
	private File saveFile ;
	private ArrayList<File> temFileBlock; // 缓存文件块文件
	private File temFile; // 重复数据文件
	private File temDir; //缓存目录

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
		if(!temDir.exists()){ // 临时文件夹不存在
			temDir.mkdirs();
		}
		
		temFileBlock = new ArrayList<File>();
		
		// 1.检查HashCode+内容长度是否相同,不相同写入saveFile,相同的则写入temFile
		inspectHashCode();
		
		// 2.temFile文件进行自我过滤
		temFileFilter();
		
		// 3.temFile文件进行与saveFile进行去重过滤
		temFileAndsaveFileFilter();
		
		// 4.清理缓存文件
		delTemDir();
		
		Print.print("trim任务完成...");
	}
	
	/**
	 * 4.清理缓存文件
	 */
	private void delTemDir() {
		for (File file : temFileBlock) {
			file.delete();
		}
		temDir.delete();
		temFileBlock.clear();
		temFileBlock = null;
		Print.print("清理缓存文件完成...");
	}

	/**
	 * 3.temFile文件进行与saveFile进行去重过滤
	 */
	private void temFileAndsaveFileFilter() throws Exception {
		ArrayList<String> temData = new ArrayList<String>(); // 存放temFile数据
		ArrayList<String> dataData = new ArrayList<String>(); // 存放temFile过滤后的数据
		BufferedReader temDataReader;
		BufferedReader dataReader;
		BufferedWriter dataWriter;
		// 对每个temFile进行去重过滤,非重复就写入到saveFile
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
				// 进行HashCode和每个单词的比较
				for (String tem : temData) {
					// HashCode相同,内容不同
					if(dataline.hashCode() == tem.hashCode() && !dataline.equals(tem)){
						dataData.add(tem);
						// Print.print("HashCode+length相同,内容不同: ".concat(dataline.concat(" = ").concat(tem)));
					}
				}
			}
			dataReader.close();
			
			// 将过滤后的数据写入saveFile
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
		Print.print("重过滤完成...");
	}

	/**
	 * 2.temFile文件进行自我过滤
	 * @throws Exception 
	 */
	private void temFileFilter() throws Exception {
		HashSet<String> temData = new HashSet<String>();// 存放不重复的数据
		BufferedReader temFilereader;
		BufferedWriter temFileWriter;
		// 对每个缓存文件进行自我过滤
		for (File file : temFileBlock) {
			temFilereader = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = temFilereader.readLine()) != null) {
				boolean isAdd = temData.add(line);
				
				 // if(!isAdd) Print.print("重复: ".concat(line));
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
		Print.print("自我过滤完成...");
	}

	/**
	 * 1.比较HashCode值
	 */
	private void inspectHashCode() throws IOException {
		int temData = 0; // 缓存数据
		int temDataPiece = 0; // 缓存数据块编码
		final int StageTemData = 10 * 1024 * 1024;
		
		// 将不重复的HashCode记录到集合中
		HashSet<Long> hashCode = new HashSet<Long>();
		
		// 获取指定目录下的所有文件
		List<File> fileList = getFileList(dataFile);
		
		// 第一个缓存文件
		temFile = new File(temDir, "temFile".concat(String.valueOf(temDataPiece)).concat(".txt"));
		temFileBlock.add(temFile);
		
		// 将筛选出来的数据序列化到文件
		BufferedWriter saveWriter = new BufferedWriter(new FileWriter(saveFile));
		BufferedWriter temWriter = new BufferedWriter(new FileWriter(temFile));
		
		BufferedReader reader;
		for (File file : fileList) {
			// 读取一行字符
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.length() > 3){
					// 1.1 比较HashCode+内容长度,相同则添加HashCode到集合中,并写入saveFile否则写入temFile
					long dataHashCode = Long.decode(String.valueOf(line.hashCode()).concat(String.valueOf(line.length())));
					boolean isAdd = hashCode.add(dataHashCode);
					if(!isAdd){ // 存在,写入temFile
						writerData(temWriter, line);
					
						// 限制缓存文件的大小,超过指定大小,新建缓存文件
						temData += line.length();
						if(temData >= StageTemData){
							temData = 0;
							temDataPiece ++;
							temWriter.close();
							temFile = new File(temDir, "temFile".concat(String.valueOf(temDataPiece)).concat(".txt"));
							temFileBlock.add(temFile);
							temWriter = new BufferedWriter(new FileWriter(temFile));
						}
					}else{ // 不存在,写入saveFile
						writerData(saveWriter, line);
					}
					
				}else{
					 // Print.print("长度不够: ".concat(line));
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
		Print.print("HashCode比较完成...");
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

