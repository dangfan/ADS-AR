/**
 * Project: ADS_Article Recommendation
 * Comments: 封装每篇论文信息的类
 * JDK version used: JDK 1.6
 * Author: TipsyBuff
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-29
 * Version: 1.0
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Article {
	private int index;
	private String title;
	private String abstractContent;
	
	public String getAbstractContent() {
		return abstractContent;
	}
	
	public void setAbstractContent(String abstractContent) {
		this.abstractContent = abstractContent;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Article(int index, String title, String abstractContent) {
		this.index = index;
		this.title = title;
		this.abstractContent = abstractContent;
	}
	
	public void WriteToFile(String file) throws Exception {
		try {
			//System.out.println("log: write " + file);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(file), "UTF-8"));
			writer.write(this.title + "\r\n" + this.abstractContent);
			writer.close();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}	
	}
}