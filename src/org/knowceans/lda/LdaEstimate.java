/*
 * (C) Copyright 2005, Gregor Heinrich (gregor :: arbylon : net) (This file is
 * part of the lda-j (org.knowceans.lda.*) experimental software package.)
 */
/*
 * lda-j is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 */
/*
 * lda-j is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
/*
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on Jan 4, 2005
 * Modified by Dang Fan, May 28, 2012
 */
package org.knowceans.lda;

import java.io.*;
import static java.lang.Math.*;
import java.text.*;

import org.knowceans.util.Cokus;

/**
 * lda parameter estimation
 */
public class LdaEstimate {

	static int LAG = 10;
	static int NUM_INIT = 1;
	static float EM_CONVERGED;
	static int EM_MAX_ITER;
	static double INITIAL_ALPHA;
	static double K;

	static {
		Cokus.cokusseed(4357);
	}

	static double myrand() {
		return (((long) Cokus.cokusrand()) & 0xffffffffl) / (double) 0x100000000l;
	}

	/**
	 * initializes class_word and class_total to reasonable beginnings.
	 */
	public static LdaModel initialModel(Corpus corpus,
			int numTopics, double alpha) {
		int k, d, i, n;
		LdaModel model;
		Document doc;

		model = new LdaModel(corpus.getNumTerms(), numTopics);
		model.setAlpha(alpha);
		// for each topic
		for (k = 0; k < numTopics; k++) {
			// sample NUM_INIT documents and add their term counts to the
			// class-word table
			for (i = 0; i < NUM_INIT; i++) {
				d = (int) floor(myrand() * corpus.getNumDocs());
				System.out.println("initialized with document " + d);
				doc = corpus.getDoc(d);
				for (n = 0; n < doc.getLength(); n++) {
					model.addClassWord(k, doc.getWord(n), doc.getCount(n));
				}
			}
			// add to all terms in class-word table 1/nTerms; update class
			// total accordingly
			assert model.getNumTerms() > 0;
			for (n = 0; n < model.getNumTerms(); n++) {
				model.addClassWord(k, n, 1.0 / model.getNumTerms());
				model.addClassTotal(k, model.getClassWord(k, n));
			}
		}
		return model;
	}

	/**
	 * iterate_document
	 */
	public static double docEm(Document doc, double[] gamma, LdaModel model,
			LdaModel nextModel) {
		double likelihood;
		double[][] phi;
		int n, k;

		phi = new double[doc.getLength()][model.getNumTopics()];

		likelihood = LdaInference.ldaInference(doc, model, gamma, phi);
		for (n = 0; n < doc.getLength(); n++) {
			for (k = 0; k < model.getNumTopics(); k++) {
				nextModel.addClassWord(k, doc.getWord(n), doc.getCount(n) * phi[n][k]);
				nextModel.addClassTotal(k, doc.getCount(n) * phi[n][k]);
			}
		}
		return likelihood;
	}

	/**
	 * saves the gamma parameters of the current dataset
	 */
	static void saveGamma(String filename, double[][] gamma, int numDocs,
			int numTopics) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			int d, k;
			for (d = 0; d < numDocs; d++) {
				for (k = 0; k < numTopics; k++) {
					if (k > 0)
						bw.write(' ');
					bw.write(Utils.formatDouble(gamma[d][k]));
				}
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * run_em
	 */
	public static LdaModel runEm(String directory, Corpus corpus) {
		try {
			BufferedWriter likelihoodFile;
			String filename;
			int i, d;
			double likelihood, likelihoodOld = Double.NEGATIVE_INFINITY, converged = 1;
			LdaModel model, nextModel;
			double[][] varGamma;
			filename = directory + "/" + "likelihood.dat";
			likelihoodFile = new BufferedWriter(new FileWriter(filename));
			varGamma = new double[corpus.getNumDocs()][(int) K];
			model = initialModel(corpus, (int) K, INITIAL_ALPHA);
			filename = directory + "/000";
			model.save(filename);
			i = 0;
			NumberFormat nf = new DecimalFormat("000");
			String itername = "";
			while (((converged > EM_CONVERGED) || (i <= 2))
					&& (i <= EM_MAX_ITER)) {
				i++;
				System.out.println("**** em iteration " + i + " ****");
				likelihood = 0;
				nextModel = new LdaModel(model.getNumTerms(),
						model.getNumTopics());
				nextModel.setAlpha(INITIAL_ALPHA);
				for (d = 0; d < corpus.getNumDocs(); d++) {
					if ((d % 100) == 0)
						System.out.println("document " + d);
					likelihood += docEm(corpus.getDoc(d), varGamma[d], model,
							nextModel);
				}
				LdaAlpha.maximizeAlpha(varGamma, nextModel, corpus.getNumDocs());
				model = nextModel;
				assert likelihoodOld != 0;
				converged = (likelihoodOld - likelihood) / likelihoodOld;
				likelihoodOld = likelihood;
				likelihoodFile.write(likelihood + "\t" + converged + "\n");
				likelihoodFile.flush();

				if ((i % LAG) == 0) {
					itername = nf.format(i);
					filename = directory + "/" + itername;
					model.save(filename);

					filename = directory + "/" + itername + ".gamma";
					saveGamma(filename, varGamma, corpus.getNumDocs(),
							model.getNumTopics());
				}
			}
			filename = directory + "/final";
			model.save(filename);
			filename = directory + "/final.gamma";
			saveGamma(filename, varGamma, corpus.getNumDocs(),
					model.getNumTopics());
			likelihoodFile.close();
			return model;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Corpus lda(float alpha, int k, String data, String dir) {
		Corpus corpus;

		INITIAL_ALPHA = alpha;
		K = k;
		LdaInference.VAR_MAX_ITER = 10;
		LdaInference.VAR_CONVERGED = 1e-6f;
		EM_MAX_ITER = 100;
		EM_CONVERGED = 1e-5f;
		corpus = new Corpus(data);
		new File(dir).mkdir();

		runEm(dir, corpus);
		
		return corpus;
	}

	/**
	 * main
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("usage\n: lda <initial alpha> <k> <data> <directory>");
			return;
		}
		lda(Float.parseFloat(args[0]),
				Integer.parseInt(args[1]),
				args[2],
				args[3]);
	}
}
