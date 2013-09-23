package com.jingz.app.pano.math;

public class Vector3 {
	public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
	
	public float x;
	public float y;
	public float z;
	
	public Vector3() {}

	public Vector3(float x, float y, float z) {
		set(x, y, z);
	}
	
	public final float dot(Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	public final float length() {
		return (float) Math.sqrt(length2());
	}

	public final float length2() {
		return x * x + y * y + z * z;
	}

	public final float normalize() {
		float f = length();
		if (f != 0.0f) {
			x /= f;
			y /= f;
			z /= f;
		}
		return f;
	}
	
	public final void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final float[] toFloatArray() {
		float[] array = new float[3];
		array[0] = x;
		array[1] = y;
		array[2] = z;
		return array;
	}

	@Override
	public String toString() {
		return "" + x + ", " + y + ", " + z;
	}
	
}
