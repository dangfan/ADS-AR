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
 * Created on Dec 3, 2004
 * Modified by Dang Fan on May 28, 2012
 */
package org.knowceans.lda;

import java.io.*;

/**
 * wrapper for an LDA model.
 */
public class LdaModel {

    private double alpha;
    private double[][] classWord;
    private double[] classTotal;
    private int numTopics;
    private int numTerms;

    /**
     * create an empty lda model with parameters:
     */
    LdaModel(int numTerms, int numTopics) {
        this.numTopics = numTopics;
        this.numTerms = numTerms;
        this.alpha = 1;

        initArrays(numTerms, numTopics);
    }

    /**
     * initialise data array in the model.
     */
    private void initArrays(int numTerms, int numTopics) {

        classTotal = new double[numTopics];
        classWord = new double[numTopics][numTerms];
        for (int i = 0; i < numTopics; i++) {
            this.classTotal[i] = 0;
            for (int j = 0; j < numTerms; j++) {
                this.classWord[i][j] = 0;
            }
        }
    }

    /**
     * save an lda model
     */
    public void save(String modelRoot) {
        String filename = modelRoot + ".beta";
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < this.numTopics; i++) {
                for (int j = 0; j < this.numTerms; j++) {
                    if (j > 0)
                        bw.write(' ');
                    bw.write(Utils.formatDouble(this.classWord[i][j]
                        / this.classTotal[i]));
                }
                bw.newLine();
            }
            bw.newLine();
            bw.close();
            filename = modelRoot + ".other";
            bw = new BufferedWriter(new FileWriter(filename));
            bw.write("num_topics " + numTopics + "\n");
            bw.write("num_terms " + numTerms + "\n");
            bw.write("alpha " + alpha + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getAlpha() {
        return alpha;
    }

    public double[] getClassTotal() {
        return classTotal;
    }

    public double getClassTotal(int cls) {
        return classTotal[cls];
    }

    public void setClassTotal(int cls, double total) {
        classTotal[cls] = total;
    }

    public void addClassTotal(int cls, double total) {
        classTotal[cls] += total;
    }

    public double[][] getClassWord() {
        return classWord;
    }

    public double getClassWord(int cls, int word) {
        return classWord[cls][word];
    }

    public void setClassWord(int cls, int word, double value) {
        classWord[cls][word] = value;
    }

    public void addClassWord(int cls, int word, double value) {
        classWord[cls][word] += value;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public int getNumTopics() {
        return numTopics;
    }

    public void setAlpha(double d) {
        alpha = d;
    }

    public void setClassTotal(double[] ds) {
        classTotal = ds;
    }

    public void setClassWord(double[][] ds) {
        classWord = ds;
    }

    public void setNumTerms(int i) {
        numTerms = i;
    }

    public void setNumTopics(int i) {
        numTopics = i;
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Model {numTerms=" + numTerms + " numTopics=" + numTopics
            + " alpha=" + alpha + "}");
        return b.toString();
    }

}
