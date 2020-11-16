package com.delmesoft.agario.utils;

public class Vec2 {
	
	public float x, y;
	
	public Vec2() {}
	
	public Vec2(Vec2 v) {
		this(v.x, v.y);
	}

	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2 set(Vec2 v) {
		return set(v.x, v.y);
	}
	
	public Vec2 set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vec2 add(Vec2 v) {
		return add(v.x, v.y);
	}
	
	public Vec2 add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vec2 addScl(Vec2 v, float scl) {
		this.x += v.x * scl;
		this.y += v.y * scl;
		return this;
	}
	
	public Vec2 subScl(Vec2 v, float scl) {
		this.x -= v.x * scl;
		this.y -= v.y * scl;
		return this;
	}
	
	public Vec2 sub(Vec2 v) {
		return sub(v.x, v.y);
	}
	
	public Vec2 sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	public Vec2 scl(float v) {
		this.x *= v;
		this.y *= v;
		return this;
	}
	
	public Vec2 setZero() {
		x = y = 0;
		return this;
	}
	
	public float dot(Vec2 v) {
		return v.x * x + v.y * y;
	}
	
	public float len() {
		return (float) Math.sqrt(x * x + y * y);
	}
	public float len2() {
		return x * x + y * y;
	}

	public Vec2 nor() {		
		float len = len();	
		if(len != 0F) {						
			x /= len;
			y /= len;
		}		
		return this;
	}
	
	public Vec2 rotate(float radians) {
		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);
		float newX = x * cos - y * sin;
		float newY = x * sin + y * cos;
		x = newX;
		y = newY;
		return this;
	}
	
	public Vec2 setRandom() {
		x = Utils.random(Float.MIN_VALUE, Float.MAX_VALUE);
		y = Utils.random(Float.MIN_VALUE, Float.MAX_VALUE);
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Vec2 [x=");
		builder.append(x);
		builder.append(", y=");
		builder.append(y);
		builder.append("]");
		return builder.toString();
	}

}
