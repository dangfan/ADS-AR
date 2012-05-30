/**
 * Project: ADS_Article Recommendation
 * Comments: 词库载入程序
 * JDK version used: JDK 1.6
 * Author: Picry Mashu
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-28
 * Version: 1.0
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

public class InitialWords {
	private String encoding;
	private String infilepath;
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public String getInfilepath() {
		return infilepath;
	}
	
	public void setInfilepath(String infilepath) {
		this.infilepath = infilepath;
	}
	
	public InitialWords(String encoding, String infilepath) {
		this.setEncoding(encoding);
		this.setInfilepath(infilepath);
	}
	
	public List<String> initialize() throws Exception {
		List<String> list = new Vector<String>();
		InputStreamReader inFile = new InputStreamReader(new FileInputStream(this.infilepath), this.encoding);
		BufferedReader reader = new BufferedReader(inFile);
		String line = "";
		while((line = reader.readLine()) != null) {
			String[] sep = line.split(" ");
			list.add(sep[1]);
		}
		reader.close();
		inFile.close();
		return list;
	}
}
