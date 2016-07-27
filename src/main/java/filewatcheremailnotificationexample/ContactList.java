package filewatcheremailnotificationexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ContactList {

	private static StringBuilder list=new StringBuilder();


	public static void loadListFromFile(String filePath){

		Path listFile=Paths.get(filePath);
		try {
			BufferedReader bufferedReader=Files.newBufferedReader(listFile, Charset.forName("UTF-8"));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				list.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getList(){
		return list.toString();
	}

}
