package com.jingz.app.pano.math;

public class Matrix3x3d {
	public double[] m = new double[9];
	
	public static void add(Matrix3x3d m1, Matrix3x3d m2, Matrix3x3d m3) {
		m3.m[0] = m1.m[0] + m2.m[0];
		m3.m[1] = m1.m[1] + m2.m[1];
		m3.m[2] = m1.m[2] + m2.m[2];
		m3.m[3] = m1.m[3] + m2.m[3];
		m3.m[4] = m1.m[4] + m2.m[4];
		m3.m[5] = m1.m[5] + m2.m[5];
		m3.m[6] = m1.m[6] + m2.m[6];
		m3.m[7] = m1.m[7] + m2.m[7];
		m3.m[8] = m1.m[8] + m2.m[8];
	}
	
	public static void mult(Matrix3x3d m1, Matrix3x3d m2, Matrix3x3d m3) {
		m3.set(	m1.m[0] * m2.m[0] + m1.m[1] * m2.m[3] + m1.m[2] * m2.m[6], 
	    		m1.m[0] * m2.m[1] + m1.m[1] * m2.m[4] + m1.m[2] * m2.m[7], 
	    		m1.m[0] * m2.m[2] + m1.m[1] * m2.m[5] + m1.m[2] * m2.m[8], 
	    		m1.m[3] * m2.m[0] + m1.m[4] * m2.m[3] + m1.m[5] * m2.m[6], 
	    		m1.m[3] * m2.m[1] + m1.m[4] * m2.m[4] + m1.m[5] * m2.m[7], 
	    		m1.m[3] * m2.m[2] + m1.m[4] * m2.m[5] + m1.m[5] * m2.m[8], 
	    		m1.m[6] * m2.m[0] + m1.m[7] * m2.m[3] + m1.m[8] * m2.m[6], 
	    		m1.m[6] * m2.m[1] + m1.m[7] * m2.m[4] + m1.m[8] * m2.m[7], 
	    		m1.m[6] * m2.m[2] + m1.m[7] * m2.m[5] + m1.m[8] * m2.m[8]);
	}
	
	public void set(double e1, double e2, double e3, double e4, double e5, double e6, double e7, double e8, double e9) {
		m[0] = e1;
		m[1] = e2;
		m[2] = e3;
		m[3] = e4;
		m[4] = e5;
		m[5] = e6;
		m[6] = e7;
		m[7] = e8;
		m[8] = e9;
	}
	
	public void set(int row, int col, double e) {
		m[col + row * 3] = e;
	}
	
	public void set(Matrix3x3d matrix) {
		m[0] = matrix.m[0];
		m[1] = matrix.m[1];
		m[2] = matrix.m[2];
		m[3] = matrix.m[3];
		m[4] = matrix.m[4];
		m[5] = matrix.m[5];
		m[6] = matrix.m[6];
		m[7] = matrix.m[7];
		m[8] = matrix.m[8];
	}
}
