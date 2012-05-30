/**
 * Project: ADS_Article Recommendation
 * Comments: 数据集处理主程序
 * JDK version used: JDK 1.6
 * Author: Picry Mashu
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-29
 * Version: 1.0
 * 
 * Modified by Dang Fan on 2012-5-29
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.util.List;
import java.util.Vector;

public class Preprocess {
	public static void main(String[] args) {
		try {
			System.out.print("Pre-treating csv...");
			TextData data = new TextData(args[0]);
			data.ParseTextDataFile();
			System.out.println("Done!");
			
			String creation = "TF-IDF";	//TO\TF-IDF\TF
			WordVector wvtool = new WordVector("UTF-8", "./data/articles/", 
					"./data/" + creation + "/wordvector/");
			wvtool.generate(creation, new Vector<String>(), false);
			
			DimensionalityReduction dr = new DimensionalityReduction("UTF-8", 
					"./data/" + creation + "/wordvector/wv.txt", 
					"./data/" + creation + "/wordvector/wordlist.txt", 
					"./data/" + creation + "/dimensionality_reduction/reduce.txt");
			dr.reduce();
			
			InitialWords iw = new InitialWords("UTF-8", 
					"./data/" + creation + "/dimensionality_reduction/reduce.txt");
			List<String> initialWords = iw.initialize();
			wvtool = new WordVector("UTF-8", "./data/articles/", "./data/TO/wordvector/" + creation);
			wvtool.generate("TO", initialWords, true);
			
			Formalization fm = new Formalization("UTF-8", 
					"./data/TO/wordvector/" + creation);
			fm.formalize();
			System.out.println("SUCCESS!!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
