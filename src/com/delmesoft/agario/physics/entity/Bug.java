package com.delmesoft.agario.physics.entity;

import com.delmesoft.agario.physics.World;
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
public class Bug extends Entity {

	public static final float DEFAULT_MASS = 10F;
	public static final float SHARED_MASS  = 10F;
	public static final float MIN_MASS_TO_SPLIT = 35F;
	
	public static final float FORCE      = 4.0f;
	public static final float EMACIATION = 0.003F;
		
	public static final float DEFAULT_TIME = 30F;

	private static Vec2 tmp = new Vec2();

	private int tick;

	private float accumEmaciation; 

	public float splitTime;

	public Player parent;
	
	public Bug(int color) {
		super(color, DEFAULT_MASS);
	}

	@Override
	public void update() {
		super.update();

		if(mass > DEFAULT_MASS) {
			tick++;
			if(tick > 60) {
				tick = 0;
				accumEmaciation += mass * EMACIATION;
			}
			if(accumEmaciation - 1f > 0f) {
				accumEmaciation--;
				mass--;
			}			
		}

		if(splitTime > 0f) {
			splitTime -= World.TIME_STEP;
		}

	}

	@Override
	public void collideWith(Entity entity) { // Overlaps
		
		if(!remove) {			
			Bug bug = (Bug) entity;
			if (bug.parent == parent) { // Same Player
				mass += entity.mass;
				splitTime = getSplitTime(splitTime, entity.mass);
				parent.childs.remove(entity);
				parent.canEatSpikes = false;
				entity.remove = true;
			} else if (parent != null) {
				float ratio = mass / entity.mass;
				if (ratio > RATIO) {
					mass += entity.mass;
					splitTime = getSplitTime(splitTime, entity.mass);
					if (bug.parent != null) {
						bug.parent.childs.remove(entity);
						//bug.parent.canEatSpikes = false;
						//parent.canEatSpikes = false;
					}
					entity.remove = true;
				}
			}
		} // remove check

	}
	
	public boolean split(float nx, float ny) {
		return split(nx, ny, mass * 0.5F);
	}

	public boolean split(float nx, float ny, float newMass){
		if(!remove) {
			float diff = mass - newMass;
			if(newMass >= Bug.DEFAULT_MASS && diff >= Bug.DEFAULT_MASS) {
				Bug bug = new Bug(color);
				mass = diff;
				splitTime = getSplitTime(mass);
				bug.mass = newMass;				
				bug.splitTime = getSplitTime(bug.mass);
				bug.force.set(nx, ny).scl((float) (1.0 / (8 * Math.pow(bug.mass, -0.439)) * 380));
				//radius = massToRadius(mass, DENSITY);
				//bug.radius = massToRadius(bug.mass, DENSITY);
				//float r2 = radius + bug.radius;
				bug.position.x = position.x + nx;
				bug.position.y = position.y + ny;
				parent.childs.add(bug);
				bug.parent = parent;
				world.add(bug);
				return true;
			}
		}
		return false;
	}

	public void share(float x, float y) {
		if(mass - SHARED_MASS >= MIN_MASS_TO_SPLIT) {
			Bug food = new Bug(color);
			mass -= (food.mass = SHARED_MASS);
			float rnd = Utils.random(-0.2F, 0.2F);
			float dx = x - position.x;
			float dy = y - position.y;
			tmp.set(dx, dy).nor();
			food.force.set(tmp).rotate(rnd).scl((float) (1.0 / (8.0 * Math.pow(food.mass, -0.439)) * 380));
			//radius = massToRadius(mass, DENSITY);
			//food.radius = massToRadius(food.mass, DENSITY);
			tmp.scl(massToRadius(mass, DENSITY) + massToRadius(food.mass, DENSITY));
			food.position.set(position).add(tmp);
			world.add(food);
		}
	}

	public void follow(Vec2 worldPoint) {
		float dx = worldPoint.x - position.x;
		float dy = worldPoint.y - position.y;
		float len2 = dx * dx + dy * dy;
		if(len2 != 0) {
			float invDst = (float) (1f / StrictMath.sqrt(len2));
			dx *= invDst;
			dy *= invDst;
		}
		force.add(dx * FORCE, dy * FORCE);
	}

	@Override
	public int getType() {
		return Entity.BUG;
	}
	
	public static float getSplitTime(float mass) {
		return getSplitTime(DEFAULT_TIME, mass);
	}

	public static float getSplitTime(float defaultTime, float mass) {
		return defaultTime + mass * 0.02333f;
	}

}
