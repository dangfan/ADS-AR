package cn.edu.tsinghua.AR.ctr;

import java.util.Arrays;

import Jama.Matrix;

public class Utils {
	public static void normalise(Matrix a) {
		int m = a.getColumnDimension();
		int n = a.getRowDimension();
		for (int i = 0; i != n; ++i) {
			Matrix row = a.getMatrix(i, i, 0, m-1);
			double sum = row.normInf();
			if (sum < 0 || sum > 0) {
				a.setMatrix(i, i, 0, m-1, row.timesEquals(1 / sum));
			}
		}
	}

	public static void mexp(Matrix a) {
		int m = a.getColumnDimension();
		int n = a.getRowDimension();
		for (int i = 0; i != n; ++i)
			for (int j = 0; j != m; ++j)
				a.set(i, j, Math.exp(a.get(i, j)));
	}

	public static void mlog(Matrix a) {
		int m = a.getColumnDimension();
		int n = a.getRowDimension();
		for (int i = 0; i != n; ++i)
			for (int j = 0; j != m; ++j)
				a.set(i, j, safelog(a.get(i, j)));
	}
	
	public static double safelog(double x) {
		if (x <= 0)
			return (-10000);
		else
			return (Math.log(x));
	}
	
	public static void main(String[] args) {
		double[][] a = {{8,-3,2}};
		Matrix A = new Matrix(a);
		double[] xp = A.getArray()[0];
		Arrays.sort(xp);
		A.print(5, 5);
	}
}
