/**
 * Project: ADS_Article Recommendation
 * Comments: WVTool词频分析程序
 * JDK version used: JDK 1.6
 * Author: Picry Mashu
 * Created on: 2012-5-28
 * Modified By: Picry Mashu
 * Modified Date: 2012-5-28
 * Version: 1.0
 */

package cn.edu.tsinghua.AR.preprocessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import edu.udo.cs.wvtool.config.WVTConfiguration;
import edu.udo.cs.wvtool.config.WVTConfigurationFact;
import edu.udo.cs.wvtool.generic.output.WordVectorWriter;
import edu.udo.cs.wvtool.generic.stemmer.PorterStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.WVTStemmer;
import edu.udo.cs.wvtool.generic.tokenizer.SimpleTokenizer;
import edu.udo.cs.wvtool.generic.vectorcreation.TFIDF;
import edu.udo.cs.wvtool.generic.vectorcreation.TermFrequency;
import edu.udo.cs.wvtool.generic.vectorcreation.TermOccurrences;
import edu.udo.cs.wvtool.generic.wordfilter.StopWordsWrapper;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTFileInputList;
import edu.udo.cs.wvtool.main.WVTool;
import edu.udo.cs.wvtool.wordlist.WVTWordList;

public class WordVector {
	private String encoding;
	private String infilefolder;
	private String outfilefolder;
	private WVTWordList wordList;
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getOufilefolder() {
		return outfilefolder;
	}

	public void setOufilefolder(String oufilefolder) {
		this.outfilefolder = oufilefolder;
	}

	public String getInfilefolder() {
		return infilefolder;
	}

	public void setInfilefolder(String infilefolder) {
		this.infilefolder = infilefolder;
	}
	
	public WordVector(String encoding, String infilefolder, String outfilefolder) {
		this.setEncoding(encoding);
		if(!infilefolder.endsWith("/")) {
			infilefolder = infilefolder + "/";
		}
		if(!outfilefolder.endsWith("/")) {
			outfilefolder = outfilefolder + "/";
		}
		this.setInfilefolder(infilefolder);
		this.setOufilefolder(outfilefolder);
		(new File(this.outfilefolder)).mkdirs();
	}
	
	public void generate(String creation, List<String> initialWords, boolean initial) throws Exception {
		if(!creation.equals("TO") && !creation.equals("TF-IDF") && !creation.equals("TF")) {
			System.out.println("Wrong creation parameters!");
			return;
		}
		
		System.out.println("Start: Generating word vector...");
		
		System.out.print("Preparing...");
		WVTool wvt = new WVTool(false);
		WVTConfiguration config = new WVTConfiguration();
		WVTStemmer stemmer = new PorterStemmerWrapper();
		SimpleTokenizer tk  = new SimpleTokenizer();
		StopWordsWrapper filter = new StopWordsWrapper();
		
		config.setConfigurationRule(WVTConfiguration.STEP_TOKENIZER, 
				new WVTConfigurationFact(tk));
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, 
				new WVTConfigurationFact(stemmer));
		config.setConfigurationRule(WVTConfiguration.STEP_WORDFILTER, 
				new WVTConfigurationFact(filter));
		System.out.println("Done!");
		
		System.out.print("Step 1: loading infile texts...");
		WVTFileInputList list = new WVTFileInputList(1);
		for(int i = 1; i <= 16980; ++i) {	//16980
			list.addEntry(new WVTDocumentInfo(this.infilefolder + i + ".txt",
					"txt", this.encoding, "english", 0));
		}
		System.out.println("Done!");
		
		System.out.print("Step 2: creating word list...");
		if(initial == false) {
			this.wordList = wvt.createWordList(list, config);
		} else {
			this.wordList = wvt.createWordList(list, config, initialWords, false);
		}
		
		this.wordList.storePlain(new OutputStreamWriter(new FileOutputStream(this.outfilefolder + "wordlist.txt"), this.encoding));
		this.wordList.store(new OutputStreamWriter(new FileOutputStream(this.outfilefolder + "wordVector.txt"), this.encoding));
		System.out.println("Done!");
		
		System.out.print("Step 3: creating vectors...");
		OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream(this.outfilefolder + "wv.txt"), this.encoding);
		WordVectorWriter wvw = new WordVectorWriter(outFile, true);
		
		config.setConfigurationRule(WVTConfiguration.STEP_OUTPUT, new WVTConfigurationFact(wvw));
		if(creation.equals("TF-IDF")) {
			config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, 
					new WVTConfigurationFact(new TFIDF()));
		} else if (creation.equals("TF")) {
			config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, 
					new WVTConfigurationFact(new TermFrequency()));
		} else if (creation.equals("TO")) {
			config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, 
					new WVTConfigurationFact(new TermOccurrences()));
		}
		
		wvt.createVectors(list, config, this.wordList);
		System.out.println("Done!");
		
		wvw.close();
		outFile.close();
		System.out.println("End: word vector generated!");
	}
}
