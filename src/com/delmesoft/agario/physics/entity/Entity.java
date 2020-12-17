package com.delmesoft.agario.physics.entity;

import java.util.concurrent.atomic.AtomicLong;

import com.delmesoft.agario.physics.World;
import com.delmesoft.agario.physics.broadphase.AABB;
import com.delmesoft.agario.utils.Utils;
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
public abstract class Entity extends AABB {
	
	public static final float RATIO = 1.3F;
	
	public static final int FOOD  = 0;
	public static final int VIRUS = 1;
	public static final int BUG   = 2;
	
	public static final float MIN_MASS = 1.0F;
	public static final float MAX_MASS = 22500F;
	
	public static final float DENSITY = 0.025F;
		
	private static AtomicLong ids = new AtomicLong();
	
	public final long id;
	
	public World world;
	
	public final Vec2 position;
	public final Vec2 velocity;
	public final Vec2 force;
		
	public final int color;	
	
	public float mass;
		
	public float radius;
	
	public boolean matched, remove;
	
	public Entity(int color, float mass) {
		
		position = new Vec2();
		velocity = new Vec2();
		force    = new Vec2();
		
		this.color = color;
		this.mass = mass;
		
		this.id = ids.getAndIncrement();
		
	}

	public void update() {
		mass = Utils.clamp(mass, MIN_MASS, MAX_MASS);
		position.addScl(velocity, (float) (8 * Math.pow(mass, -0.439)));
		//testEntity();
		updateAABB();
	}

	public void updateAABB() {
		computeRadius();
		min.x = position.x - radius;
		min.y = position.y - radius;
		max.x = position.x + radius;
		max.y = position.y + radius;
	}

	protected void computeRadius() {
		radius = Utils.lerp(radius, massToRadius(mass, DENSITY), 0.5F);
	}

	public abstract void collideWith(Entity entity);

	public abstract int getType();

	public static float massToRadius(float mass, float density) {
		return (float) (Math.sqrt(mass / (Math.PI * density)));
	}

}
