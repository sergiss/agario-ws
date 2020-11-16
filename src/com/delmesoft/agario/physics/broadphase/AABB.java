package com.delmesoft.agario.physics.broadphase;

import com.delmesoft.agario.physics.broadphase.DynamicTree.Node;
import com.delmesoft.agario.utils.Vec2;

/*
 * Copyright (c) 2020, Sergio S.- sergi.ss4@gmail.com http://sergiosoriano.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *    	
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
public class AABB {
	
	Node node;
	
	public final Vec2 min, max;

	public AABB() {
		min = new Vec2();
		max = new Vec2();
	}

	public AABB(float minX, float minY, float maxX, float maxY) {
		min = new Vec2(minX, minY);
		max = new Vec2(maxX, maxY);
	}

	public AABB set(AABB aabb) {
		this.min.set(aabb.min);
		this.max.set(aabb.max);
		return this;
	}

	public AABB combine(AABB a, AABB b) {
	    min.x = a.min.x < b.min.x ? a.min.x : b.min.x;
	    min.y = a.min.y < b.min.y ? a.min.y : b.min.y;
	    max.x = a.max.x > b.max.x ? a.max.x : b.max.x;
	    max.y = a.max.y > b.max.y ? a.max.y : b.max.y;
		return this;
	}
	
	public float getArea() {
		return getWidth() * getHeight();
	}

	public float getPerimeter() {
		return (getWidth() + getHeight()) * 2.0F;
	}

	public float getWidth() {
		return max.x - min.x;
	}

	public float getHeight() {
		return max.y - min.y;
	}
	
	public Vec2 getCenter() {
		return getCenter(new Vec2());
	}

	public Vec2 getCenter(Vec2 result) {
		return result.set(getWidth(), getHeight()).scl(0.5F).add(min);
	}
	
	public boolean contains(AABB aabb) {
		return min.x < aabb.min.x &&
			   min.y < aabb.min.y &&
			   max.x > aabb.max.x &&
			   max.y > aabb.max.y;
	}
	
	public boolean overlap(AABB aabb) {
		return min.x < aabb.max.x &&
			   min.y < aabb.max.y &&
			   max.x > aabb.min.x &&
			   max.y > aabb.min.y;		
	}
	
	public static boolean overlap(AABB a, AABB b) {
		return a.min.x < b.max.x && 
			   a.min.y < b.max.y && 
			   a.max.x > b.min.x && 
			   a.max.y > b.min.y;
	}
	
	
	@Override
	public String toString() {
		return "AABB [min=" + min + ", max=" + max + "]";
	}
		
}
