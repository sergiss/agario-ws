package com.delmesoft.agario.physics.entity;

import java.util.LinkedList;

import com.delmesoft.agario.physics.World;
import com.delmesoft.agario.physics.broadphase.AABB;
import com.delmesoft.agario.utils.Vec2;
import com.delmesoft.httpserver.Session;

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
public class Player extends AABB {

	public static final String DEFAULT_NAME = "An unnamed bug";
	
	public static final long UPDATE_TIME = 100;

	public static final int MAX_SPLIT = 16;

	public World world;

	public String name = DEFAULT_NAME;

	public final LinkedList<Bug> childs;

	public float maxMass;

	public float mass;

	public boolean canEatSpikes;

	public Session session;
	public long lastUpdate;
	public Vec2 screenPoint = new Vec2();

	public Player(float x, float y, int color, World world) {

		this.world = world;

		childs = new LinkedList<Bug>();

		Bug bug = new Bug(color);
		bug.parent = this;		

		childs.add(bug);

		bug.position.set(x, y);

		world.add(bug);

	}

	public void update() {

		// Update AABB (camera)

		Vec2 min = this.min;
		Vec2 max = this.max;		

		if (childs.size() > 1) {

			min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
			max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

			mass = 0;
			float x, y;

			for (Bug bug : childs) { // nullPointerException

				x = bug.position.x;
				y = bug.position.y;

				if (x < min.x) {
					min.x = x;
				}

				if (y < min.y) {
					min.y = y;
				}

				if (x > max.x) {
					max.x = x;
				}

				if (y > max.y) {
					max.y = y;
				}

				mass += bug.mass;

			}

			if(mass > maxMass) {
				maxMass = mass;
			}

		} else if(childs.size() > 0) {

			Bug bug = childs.getFirst();

			min.set(bug.position.x - bug.radius, bug.position.y - bug.radius);
			max.set(bug.position.x + bug.radius, bug.position.y + bug.radius);

			mass = bug.mass;

			if(mass > maxMass) {
				maxMass = mass;
			}

		}

	}

	public void share(Vec2 vec2) {		
		for(Bug bug : childs) {
			bug.share(vec2.x, vec2.y);			
		}		
	}

	public void follow(Vec2 worldPoint) {		
		for(Bug bug : childs) {
			bug.follow(worldPoint);			
		}		
	}
	
	public void split(float nx, float ny) {
		int size = childs.size();
		for(int i = 0; i < size; i++) {
			childs.get(i).split(nx, ny);			
		}
	}

	public void split() {		
		
		int size = childs.size();
		if(size < MAX_SPLIT) {
			Vec2 tmp = new Vec2(childs.getFirst().velocity).nor();		
			for(int i = 0; i < size; i++) {
				childs.get(i).split(tmp.x, tmp.y);			
			}
			if(childs.size() >= MAX_SPLIT ) {
				canEatSpikes = true;
			}
		}
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Vec2 getScreenPoint() {
		return screenPoint;
	}

	public void setScreenPoint(Vec2 screenPoint) {
		this.screenPoint = screenPoint;
	}
	
}

