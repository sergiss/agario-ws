package com.delmesoft.agario.physics.entity;

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
public class Virus extends Entity {

	public static final float DEFAULT_MASS = 100F;	

	private static Vec2 tmp = new Vec2();

	public Virus(int color) {
		super(color, DEFAULT_MASS);
	}

	@Override
	public void collideWith(Entity entity) {
		
		Bug bug = ((Bug) entity);
		float ratio = entity.mass / mass;
		if (ratio > RATIO) {
			remove = true;
			bug.mass += mass;
			if (!bug.parent.canEatSpikes) {
				bug.parent.canEatSpikes = true;
				int n = (int) Math.min(16, (int) (bug.mass / (Bug.MIN_MASS_TO_SPLIT - 15)));
				float iter = Utils.PI2 / n;
				for (int i = 0; i < n; i++) {
					tmp.set((float) Math.cos(i * iter), (float) Math.sin(i * iter)).nor();
					bug.split(tmp.x, tmp.y, Bug.MIN_MASS_TO_SPLIT - 15);
				}
				tmp.setRandom().nor();
				for (int i = 0; i < 3; i++) {
					if(bug.split(tmp.x, tmp.y)) {
						bug = bug.parent.childs.getLast();
					} else {
						break;
					}
				}
			}
		} else if(bug.parent == null) { // shared bug
			entity.remove = true;
			mass += entity.mass;
			if(mass > 170F) {					
				mass = DEFAULT_MASS;					
				Virus spike = new Virus(0);					
				spike.position.set(position);					
				spike.force.set(entity.force).add(entity.velocity).nor().scl(DEFAULT_MASS * 4F);
				world.add(spike);									
			}			
		}

	}

	@Override
	public int getType() {
		return Entity.VIRUS;
	}

}
