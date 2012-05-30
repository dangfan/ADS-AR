/**
 * Project: ADS_Article Recommendation
 * Comments: 数据集处理结果格式化
 * JDK version used: JDK 1.6
 * Author: Picry Mashu
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-28
 * Version: 1.0
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Formalization {
	private String encoding;
	private String infilefolder;
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public String getInfilefolder() {
		return infilefolder;
	}
	
	public void setInfilefolder(String infilefolder) {
		this.infilefolder = infilefolder;
	}
	
	public Formalization(String encoding, String infilefolder) {
		this.setEncoding(encoding);
		if(!infilefolder.endsWith("/")) {
			infilefolder = infilefolder + "/";
		}
		this.setInfilefolder(infilefolder);
	}
	
	public void formalize() throws Exception {
		System.out.print("Formalizing...");
		InputStreamReader inFile = new InputStreamReader(new FileInputStream(this.infilefolder + "wv.txt"), this.encoding);
		BufferedReader reader = new BufferedReader(inFile);
		OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream("./data/mult.dat"), this.encoding);
		BufferedWriter writer = new BufferedWriter(outFile);
		
		String line = "";
		while((line = reader.readLine()) != null) {
			String[] sep = line.split(" ");
			int wordcount = sep.length - 1;
			writer.write(wordcount + "");
			for(int i = 1; i < sep.length; ++i) {
				String[] num = sep[i].split(":");
				int key = Integer.parseInt(num[0]);
				int value = (int)Double.parseDouble(num[1]);
				writer.write(" " + key + ":" + value);
			}
			writer.newLine();
		}
		reader.close();
		writer.close();
		inFile.close();
		outFile.close();
		System.out.println("Done!");
	}
}
