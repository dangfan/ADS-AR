/**
 * Project: ADS_Article Recommendation
 * Comments: csv数据集论文信息提取程序
 * JDK version used: JDK 1.6
 * Author: TipsyBuff
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-29
 * Version: 1.0
 * 
 * Modified By: Dang Fan on 2012-5-29
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


import com.csvreader.CsvReader;

public class TextData {
	private String filePath;
	private ArrayList<Article> articalList;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public ArrayList<Article> getArticalList() {
		return articalList;
	}

	public void setArticalList(ArrayList<Article> articalList) {
		this.articalList = articalList;
	}
	
	public TextData(String file) {
		this.filePath = file;
		this.articalList = new ArrayList<Article>();
	}
	
	public void ParseTextDataFile() throws Exception {
		int count = 0;
		try {
			(new File("./data/articles/")).mkdirs();
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(
				    new FileInputStream(this.filePath), "UTF-8")));
			while(reader.readRecord()) {
				String row[] = reader.getValues();
				Article article = new Article(Integer.parseInt(row[0]), row[1], row[2]);
				article.WriteToFile("./data/articles/" + article.getIndex() +".txt");
				this.articalList.add(article);
				++count;
			}
			reader.close();
		} catch (Exception e) {
			throw new Exception(count + ":" + e.getMessage());
		}
	}
}
