package com.delmesoft.agario.utils;

import java.util.Random;

public class Utils {
	
	public static final float PI  = (float) Math.PI;
	public static final float PI2 = (float) (Math.PI * 2.0);
	public static final float PI4 = (float) (Math.PI * 4.0);
	
	public static final float HALF_PI = PI * 0.5F;
	
	public static final float byteToMega = 0.0000009536743164f;
		
	public static Random random = new Random(System.nanoTime());

	public static int fastFloor(float x) {
		int xi = (int) x;
		return x < xi ? xi - 1 : xi;
	}
	
	public static int fastCeil(float x) {
		int xi = (int) x;
		return x > xi ? xi + 1 : xi;
	}
	
	// Linear interpolation
	public static float lerp (float startValue, float endValue, float scale) {
		return startValue + (endValue - startValue) * scale;
	}

	public static float random(float min, float max) {
		return random.nextFloat() * (max - min) + min;
	}

	public static float clamp (float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

}
