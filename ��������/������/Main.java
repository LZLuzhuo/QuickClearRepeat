import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		String[] tips = new String[]{"输入数据库名: ", "输入用户名: ","输入密码: ","目录名: "};
		String[] data = new String[4];
		
		Scanner scanner = new Scanner(System.in);
		for (int i = 0; i < tips.length; i++) {
			System.out.print(tips[i]);
			data[i] = scanner.nextLine().trim();
		}
		
		MysqlTrimDemo mysqlTrimDemo = new MysqlTrimDemo();
		mysqlTrimDemo.startTrim(data[0],data[1],data[2],new File(data[3]), null);
	}

}
