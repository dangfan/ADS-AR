/******************************************************************************

Modified and rewritten by Dang Fan, dangf09@gmail.com

*******************************************************************************

(C) Copyright 2011, Chong Wang and David Blei

written by Chong Wang, chongw@cs.princeton.edu.

CTR is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

CTR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 59 Temple
Place, Suite 330, Boston, MA 02111-1307 USA

***************************************************************************/

package cn.edu.tsinghua.AR.ctr;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;

import org.knowceans.lda.Corpus;
import org.knowceans.lda.Document;

import Jama.Matrix;

public class CTREstimate {
	static String DIR = "data/";
	
	static Vector<Vector<Integer>> users;
	static Vector<Vector<Integer>> items;
	
	static double a;
	static double b;
	static double lambda_u;
	static double lambda_v;
	static int max_iter;
	static int min_iter = 15;
	
	static int num_factors;
	static int num_items;
	static int num_users;
	
	static Matrix beta;
	static Matrix theta;
	static Matrix U;
	static Matrix V;

	public static void ctr(Corpus corpus,
			double aa, double bb, double lu, double lv,
			int mi, int nf, int nu, int ni, String datafile) {
		a = aa; b = bb;
		lambda_u = lu; lambda_v = lv;
		max_iter = mi; num_users = nu; num_items = ni;
		num_factors = nf;
		users = new Vector<Vector<Integer>>(num_users);
		items = new Vector<Vector<Integer>>(num_items);
		for (int i = 0; i != num_users; ++i)
			users.add(new Vector<Integer>());
		for (int i = 0; i != num_items; ++i)
			items.add(new Vector<Integer>());
		
		readData(datafile, users, items);
		readLDAinfo();
		
		learnMapEstimate(corpus);
	}
	
	private static void learnMapEstimate(Corpus corpus) {
		U = new Matrix(num_users, num_factors);
		V = theta.copy();
		
		int iter = 0;
		int vocab = corpus.getNumTerms();
		double likelihood = -Math.exp(50), likelihood_old;
		double converge = 1.0;
		Matrix XX = new Matrix(num_factors, num_factors);
		Matrix A = new Matrix(num_factors, num_factors);
		Matrix B = new Matrix(num_factors, num_factors);
		Matrix x = new Matrix(1, num_factors);
		
		Matrix empty = new Matrix(num_factors, num_factors);
		Matrix ex = new Matrix(1, num_factors);
		Matrix emptyss = new Matrix(num_factors, vocab);
		Matrix identity = Matrix.identity(num_factors, num_factors);
		
		Matrix phi = new Matrix(corpus.getMaxCopusLength(), num_factors);
		Matrix word_ss = new Matrix(num_factors, vocab);
		Matrix log_beta = beta.copy();
		Utils.mlog(log_beta);
		Matrix gamma = ex.copy();
		
		double a_minus_b = a - b; // Confidence
		while (iter < max_iter && converge > 1e-6 || iter < min_iter) {
			likelihood_old = likelihood;
			likelihood = 0.;
			
			// update U
			// gsl_matrix_set_zero(XX);
			XX.setMatrix(0, num_factors-1, 0, num_factors-1, empty);
			for (int i = 0; i != num_items; ++i) {
				if (!items.elementAt(i).isEmpty()) {
					Matrix v = V.getMatrix(i, i, 0, num_factors-1);
					XX.plusEquals(v.transpose().times(v));
				}
			}
			XX.timesEquals(b).plusEquals(identity.times(lambda_u));
			
			for (int i = 0; i != num_users; ++i) {
				int n = users.elementAt(i).size();
				if (n > 0) {
					// this user has rated some articles
					A.setMatrix(0, num_factors-1, 0, num_factors-1, XX);
					x.setMatrix(0, 0, 0, num_factors-1, ex);
					for (int l = 0; l != n; ++l) {
						int j = users.elementAt(i).elementAt(l);
						Matrix v = V.getMatrix(j, j, 0, num_factors-1);
						A.plusEquals(v.transpose().times(v).timesEquals(a_minus_b));
						x.plusEquals(v.times(a));
					}
					
					Matrix u = A.lu().solve(x.transpose()).transpose();
					U.setMatrix(i, i, 0, num_factors-1, u);
					
					// update likelihood
					likelihood -= 0.5 * lambda_u * u.times(u.transpose()).get(0, 0);
				}
			}
			
			// update V
			word_ss.setMatrix(0, num_factors-1, 0, vocab-1, emptyss);
			XX.setMatrix(0, num_factors-1, 0, num_factors-1, empty);
			for (int i = 0; i != num_users; ++i) {
				if (!users.elementAt(i).isEmpty()) {
					Matrix u = U.getMatrix(i, i, 0, num_factors-1);
					XX.plusEquals(u.transpose().times(u));
				}
			}
			XX.timesEquals(b);
			
			for (int j = 0; j != num_items; ++j) {
				Matrix theta_v = theta.getMatrix(j, j, 0, num_factors-1);
				int m = items.elementAt(j).size();
				if (m > 0) {
					// m > 0, some users have rated this article
					A.setMatrix(0, num_factors-1, 0, num_factors-1, XX);
					x.setMatrix(0, 0, 0, num_factors-1, ex);
					for (int l = 0; l != m; ++l) {
						int i = items.elementAt(j).elementAt(l);
						Matrix u = U.getMatrix(i, i, 0, num_factors-1);
						A.plusEquals(u.transpose().times(u).timesEquals(a_minus_b));
						x.plusEquals(u.times(a));
					}
					
					// adding the topic vector
					x.plusEquals(theta_v.times(lambda_v));
					
					// save for computing likelihood
					B.setMatrix(0, num_factors-1, 0, num_factors-1, A);
					
					A.plusEquals(identity.times(lambda_v));
					Matrix v = A.lu().solve(x.transpose()).transpose();
					V.setMatrix(j, j, 0, num_factors-1, v);
					
					// update the likelihood
					likelihood -= 0.5 * m * a;
					for (int l = 0; l != m; ++l) {
						int i = items.elementAt(j).elementAt(l);
						Matrix u = U.getMatrix(i, i, 0, num_factors-1);
						likelihood += a * u.times(v.transpose()).get(0, 0);
					}
					likelihood -= 0.5 * v.times(B.times(v.transpose())).get(0, 0);
					
					// likelihood part of theta
					x.setMatrix(0, 0, 0, num_factors-1, v);
					x.minusEquals(theta_v);
					likelihood -= 0.5 * lambda_v * x.times(x.transpose()).get(0, 0);
					
					likelihood += doc_inference(corpus.getDoc(j), theta_v, log_beta, phi, gamma, word_ss, true);
					optimize_simplex(gamma, v, lambda_v, theta_v);
					theta.setMatrix(j, j, 0, num_factors-1, theta_v);
				} else {
					// m=0, this article has never been rated
			        doc_inference(corpus.getDoc(j), theta_v, log_beta, phi, gamma, word_ss, false); 
			        Utils.normalise(gamma);
					theta.setMatrix(j, j, 0, num_factors-1, gamma);
				}
			}
			
			// update beta
			beta.setMatrix(0, num_factors-1, 0, vocab-1, word_ss);
			Utils.normalise(beta);
			log_beta.setMatrix(0, num_factors-1, 0, vocab-1, beta);
			Utils.mlog(log_beta);
			
			++iter;
			converge = Math.abs((likelihood-likelihood_old)/likelihood_old);
			System.out.println(String.format("iter=%04d, likelihood=%.5f, converge=%.10f", iter, likelihood, converge));
		}
		try {
			PrintWriter bw = new PrintWriter(DIR + "final-U.dat");
			U.print(bw, 25, 15);
			bw.close();
			bw = new PrintWriter(DIR + "final-V.dat");
			V.print(bw, 25, 15);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// projection gradient algorithm
	private static void optimize_simplex(Matrix gamma, Matrix vv,
			double lambda, Matrix opt_x) {
		Matrix opt_x_old = opt_x.copy();
		Matrix g = opt_x.copy();
		double f_old = f_simplex(gamma, vv, lambda, opt_x);
		
		double ab_sum = df_simplex(gamma, vv, lambda, opt_x, g);
		if (ab_sum > 1.0) g.timesEquals(1.0 / ab_sum);
		
		opt_x.minusEquals(g);
		Matrix x_bar = opt_x.copy();
		simplex_projection(opt_x, x_bar, 1.0);
		x_bar.minusEquals(opt_x_old);
		double r = g.times(x_bar.transpose()).get(0, 0) * 0.5;
		
		double beta = 0.5;
		double t = beta;
		int iter = 0;
		while (++iter < 100) {
			opt_x.setMatrix(0, 0, 0, num_factors-1, opt_x_old);
			opt_x.plusEquals(x_bar.times(t));
			double f_new = f_simplex(gamma, vv, lambda, opt_x);
			if (f_new > f_old + r * t) t = t * beta;
			else break;
		}
		
		if (!is_feasible(opt_x))
			System.out.println("Something is wrong!");
	}

	private static boolean is_feasible(Matrix opt_x) {
		double sum = 0;
		for (int i = 0; i != opt_x.getColumnDimension() - 1; ++i) {
			double val = opt_x.get(0, i);
			if (val < 0 || val > 1) return false;
			sum += val;
			if (sum > 1) return false;
		}
		return true;
	}

	private static void simplex_projection(Matrix xx, Matrix x_proj, double z) {
		double[] xp = x_proj.getArray()[0];
		Arrays.sort(xp);
		double cumsum = -z;
		int j = 0;
		for (int i = xx.getColumnDimension()-1; i >= 0; --i) {
			double u = xp[i];
			cumsum += u;
			if (u > cumsum/(j+1)) ++j;
			else break;
		}
		double the = cumsum / j;
		for (int i = 0; i != xx.getColumnDimension(); ++i) {
			double u = xx.get(0, i) - the;
			if (u <= 0) u = 0;
			xp[i] = u;
		}
		Utils.normalise(x_proj);
	}

	private static double df_simplex(Matrix gamma, Matrix vv, double lambda,
			Matrix opt_x, Matrix g) {
		g.minusEquals(vv).timesEquals(-lambda);
		Matrix y = gamma.copy();
		for (int i = 0; i != y.getColumnDimension(); ++i)
			y.set(0, i, y.get(0, i) / opt_x.get(0, i));
		g.plusEquals(y).timesEquals(-1);
		double result = 0;
		for (int i = 0; i != g.getColumnDimension(); ++i)
			result += Math.abs(g.get(0, i));
		return result;
	}

	private static double f_simplex(Matrix gamma, Matrix vv, double lambda,
			Matrix opt_x) {
		Matrix y = opt_x.copy();
		Utils.mlog(y);
		double f = y.times(gamma.transpose()).get(0, 0);
		y.setMatrix(0, 0, 0, num_factors-1, vv);
		y.minusEquals(opt_x);
		return 0.5 * lambda * y.times(y.transpose()).get(0, 0) - f;
	}

	private static double doc_inference(Document doc, Matrix theta_v,
			Matrix log_beta, Matrix phi, Matrix gamma, Matrix word_ss, boolean update) {
		double likelihood = 0;
		int m = phi.getColumnDimension();
		Matrix log_theta_v = theta_v.copy();
		Utils.mlog(log_theta_v);
		for (int n = 0; n != doc.getLength(); ++n) {
			int w = doc.getWord(n);
			for (int k = 0; k != num_factors; ++k)
				phi.set(n, k, theta_v.get(0, k) * beta.get(k, w));
			
			Matrix row = phi.getMatrix(n, n, 0, m-1);
			double sum = row.normInf();
			if (sum < 0 || sum > 0) {
				phi.setMatrix(n, n, 0, m-1, row.timesEquals(1 / sum));
			}
			
			for (int k = 0; k != num_factors; ++k) {
				double x = phi.get(n, k);
				if (x > 0)
					likelihood += x * (log_theta_v.get(0, k) + log_beta.get(k, w) - Math.log(x));
			}
		}
		for (int i = 0; i != gamma.getColumnDimension(); ++i)
			gamma.set(0, i, 1.0);
		for (int n = 0; n != doc.getLength(); ++n)
			for (int k = 0; k != num_factors; ++k) {
				double x = doc.getCount(n) * phi.get(n, k);
				gamma.set(0, k, x + gamma.get(0, k));
				if (update) {
					int t = doc.getWord(n);
					word_ss.set(k, t, x + word_ss.get(k, t));
				}
			}
		return likelihood;
	}

	private static void readLDAinfo() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(DIR + "final.gamma"));
			theta = Matrix.read(br);
			Utils.normalise(theta);
			br.close();
			
			br = new BufferedReader(new FileReader(DIR + "final.beta"));
			beta = Matrix.read(br);
			// exponentiate if it's not
			if (beta.get(0, 0) < 0)
				Utils.mexp(beta);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readData(String datafile,
			Vector<Vector<Integer>> users,
			Vector<Vector<Integer>> items) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(datafile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				int uid = Integer.parseInt(fields[0]) - 1;
				int aid = Integer.parseInt(fields[1]) - 1;
				users.elementAt(uid).add(aid);
				items.elementAt(aid).add(uid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
//		ctr(null, 1.0, 0.01, 0.01, 100,
//				200, 200, 1, 50, "data/a.csv");
//		U.times(V.transpose()).print(5, 5);
	}

}
