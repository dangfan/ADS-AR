/**
 * Project: ADS_Article Recommendation
 * Comments: 数据集降维程序
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DimensionalityReduction {
	private String encoding;
	private String wvfilepath;	//wv.txt
	private String wlfilepath;	//wordlist.txt
	private String outfilepath;	//newwordlist.txt
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public String getWvfilepath() {
		return wvfilepath;
	}
	
	public void setWvfilepath(String wvfilepath) {
		this.wvfilepath = wvfilepath;
	}
	
	public String getWlfilepath() {
		return wlfilepath;
	}
	
	public void setWlfilepath(String wlfilepath) {
		this.wlfilepath = wlfilepath;
	}
	
	public String getOutfilepath() {
		return outfilepath;
	}
	
	
	public void setOutfilepath(String outfilepath) {
		this.outfilepath = outfilepath;
	}
	
	public DimensionalityReduction(String encoding, String wvfilepath, 
			String wlfilepath, String outfilepath) throws Exception {
		this.setEncoding(encoding);
		this.setWvfilepath(wvfilepath);
		this.setWlfilepath(wlfilepath);
		this.setOutfilepath(outfilepath);
		(new File(this.outfilepath.substring(0, this.outfilepath.lastIndexOf("/")))).mkdirs();
	}
	
	public Map<Integer, Double> wv2map(String wv) throws Exception {
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		String[] sep = wv.split(" ");
		for(int i = 1; i < sep.length; ++i) {
			String[] num = sep[i].split(":");
			int key = Integer.parseInt(num[0]);
			double value = Double.parseDouble(num[1]);
			map.put(key, value);
		}
		return map;
	}
	
	public void reduce() throws Exception {
		System.out.println("Start: reducing dimensionality...");
		
		System.out.print("Preparing...");
		InputStreamReader wvinFile = new InputStreamReader(new FileInputStream(this.wvfilepath), this.encoding);
		BufferedReader wvreader = new BufferedReader(wvinFile);
		InputStreamReader wlinFile = new InputStreamReader(new FileInputStream(this.wlfilepath), this.encoding);
		BufferedReader wlreader = new BufferedReader(wlinFile);
		OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream(this.outfilepath), this.encoding);
		BufferedWriter writer = new BufferedWriter(outFile);
		ArrayList<Map<Integer, Double>> maplist = new ArrayList<Map<Integer, Double>>();
		ArrayList<String> wordlist = new ArrayList<String>();
		Map<Integer, Double> totalmap = new HashMap<Integer, Double>();
		String line = "";
		System.out.println("Done!");
		
		System.out.print("Step 1: maping article vectors...");
		while((line = wvreader.readLine()) != null) {
			maplist.add(this.wv2map(line));
		}
		System.out.println("Done!");
		
		System.out.print("Step 2: adding all dimensions...");
		for(Map<Integer, Double> m : maplist) {
			Iterator<Entry<Integer, Double>> it = m.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)it.next();
				int key = (int)entry.getKey();
				double value = (double)entry.getValue();
				if(totalmap.containsKey(key)) {
					double ori_value = totalmap.get(key);
					double new_value = ori_value + value;
					totalmap.remove(key);
					totalmap.put(key, new_value);
				} else {
					totalmap.put(key, value);
				}
			}
		}
		System.out.println("Done!");
		
		System.out.print("Step 3: sorting dimensions...");
		List<Map.Entry<Integer, Double>> infoIds = new ArrayList<Map.Entry<Integer, Double>>(totalmap.entrySet());
		Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
				if(o2.getValue() > o1.getValue()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		System.out.println("Done!");
		
		System.out.print("Step 4: loading word list...");
		while((line = wlreader.readLine()) != null) {
			if(!line.equals("")) {
				wordlist.add(line);
			}
		}
		System.out.println("Done!");
		
		System.out.print("Step 5: writing to file...");
		for(int i = 0; i < infoIds.size() && i < 8000; ++i) {
			int key = infoIds.get(i).getKey();
			double value = infoIds.get(i).getValue();
			String word = wordlist.get(key);
			writer.write(key + " " + word + " " + value);
			writer.newLine();
		}
		System.out.println("Done!");
		
		wvreader.close();
		wlreader.close();
		writer.close();
		wvinFile.close();
		wlinFile.close();
		outFile.close();
		
		System.out.println("End: dimensionality reduced!");
	}
}
