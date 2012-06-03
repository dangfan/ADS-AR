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

    public static String normalize(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }
	
	public static void main(String[] args) {
		System.out.println(normalize("bar.jsp?somepar=someval&par=/../WEB-INF/web.xml"));
//		Preprocess.start(args[0]);
//		Corpus corpus = LdaEstimate.lda(0.25f, 200, "data/mult.dat", "data");
//		CTREstimate.ctr(corpus, 1.0, 0.01, 0.01, 100, 200, 200, 5551, 16980, args[1]);
//		DoTest(args[2]);
		
	}

	private static void DoTest(String fileName) {
		try {
			Matrix users = Matrix.read(new BufferedReader(new FileReader("data/final-U.dat")));
			Matrix items = Matrix.read(new BufferedReader(new FileReader("data/final-V.dat")));
			int lastu = -1;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			FileWriter fw = new FileWriter("result.txt");
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
			Collections.sort(t);
			StringBuilder sb = new StringBuilder();
			sb.append((lastu + 1) + ",");
			for (int j = 0; j != 5; ++j)
				sb.append(t.elementAt(j).id + ";");
			sb.setCharAt(sb.length() - 1, '\n');
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
