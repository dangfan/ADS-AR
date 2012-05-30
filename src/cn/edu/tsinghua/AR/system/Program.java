package cn.edu.tsinghua.AR.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Vector;

import org.knowceans.lda.Corpus;
import org.knowceans.lda.LdaEstimate;

import Jama.Matrix;

import cn.edu.tsinghua.AR.ctr.CTREstimate;
import cn.edu.tsinghua.AR.preprocessor.Preprocess;

class Pair implements Comparable<Pair> {
	int id;
	double rate;
	
	public Pair(int i, double r) {
		id = i; rate = r;
	}
	
	@Override
	public int compareTo(Pair p) {
		return Double.compare(p.rate, rate);
	}
}

public class Program {

	public static void main(String[] args) {
		String[] t = {"./data/raw-data.csv"};
		Preprocess.main(t);
		Corpus corpus = LdaEstimate.lda(0.25f, 200, "data/mult.dat", "data");
		CTREstimate.ctr(corpus, 1.0, 0.01, 0.01, 100, 200, 200, 5551, 16980, "data/user-info-train.csv");
		DoTest("data/user-info-test.csv");
	}

	private static void DoTest(String fileName) {
		try {
			Matrix users = Matrix.read(new BufferedReader(new FileReader("data/final-U.dat")));
			Matrix items = Matrix.read(new BufferedReader(new FileReader("data/final-V.dat")));
			int lastu = -1;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			FileWriter fw = new FileWriter("data/result.txt");
			String row;
			Vector<Pair> t = new Vector<Pair>();
			while ((row = br.readLine()) != null) {
				String[] fields = row.split(",");
				int u = Integer.parseInt(fields[0]) - 1;
				int i = Integer.parseInt(fields[1]) - 1;
				if (u != lastu) {
					if (lastu != -1) {
						Collections.sort(t);
						StringBuilder sb = new StringBuilder();
						sb.append((lastu + 1) + ",");
						for (int j = 0; j != 5; ++j)
							sb.append(t.elementAt(j).id + ";");
						sb.setCharAt(sb.length() - 1, '\n');
						fw.write(sb.toString());
					}
					lastu = u;
					t.clear();
				}
				Matrix user = users.getMatrix(u, u, 0, 199);
				Matrix item = items.getMatrix(i, i, 0, 199);
				double rate = user.times(item.transpose()).get(0, 0);
				t.add(new Pair(i + 1, rate));
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
