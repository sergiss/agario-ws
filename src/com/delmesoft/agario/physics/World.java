package com.delmesoft.agario.physics;

import java.util.ArrayList;
import java.util.List;

import com.delmesoft.agario.Agario;
import com.delmesoft.agario.physics.broadphase.AABB;
import com.delmesoft.agario.physics.broadphase.AABBTree;
import com.delmesoft.agario.physics.broadphase.DynamicTree.Iterator;
import com.delmesoft.agario.physics.entity.Bug;
import com.delmesoft.agario.physics.entity.Entity;
import com.delmesoft.agario.utils.Vec2;

public class World extends AABB {

	public static final float TIME_STEP = 1F / Agario.FPS;

	public static final float SLOP      = 0.05f;
	public static final float PERCENT   = 0.5f; // usually 20% to 80%

	public static final float BOUNCE    = 0.15f;
	public static final float FRICTION  = 0.95f;

	private final Vec2 tmp1 = new Vec2();
	private final Vec2 tmp2 = new Vec2();	

	private final List<Entity> entities;

	private final AABBTree<Entity> aabbTree;

	private MyIterator iterator;

	public World(float width, float height) {		
		super(-width * 0.5f, -height * 0.5f, width * 0.5f, height * 0.5f);			
		entities = new ArrayList<Entity>();		
		aabbTree = new AABBTree<Entity>();
		iterator = new MyIterator();		
	}

	public void step() {

		Entity entity;

		int i, n = entities.size();

		for (i = 0; i < n; i++) {

			entity = entities.get(i);

			if (entity.getType() == Entity.BUG) {
				entity.matched = true;
				iterator.entityA = entity;
				aabbTree.iterate(entity, iterator);	
			}

			// World bounds collision detection

			float x = entity.position.x + entity.velocity.x;
			float diff = x - entity.radius;
			if (diff < min.x) {
				entity.position.x += min.x - diff;
				entity.velocity.x *= -BOUNCE;
				// entityA.force.x = 0;
			}

			diff = x + entity.radius;
			if (diff > max.x) {
				entity.position.x -= diff - max.x;
				entity.velocity.x *= -BOUNCE;
				//entityA.force.x = 0;
			}

			float y = entity.position.y + entity.velocity.y;
			diff = y - entity.radius;
			if (diff < min.y) {
				entity.position.y += min.y - diff;
				entity.velocity.y *= -BOUNCE;
				//entityA.force.y = 0;
			}

			diff = y + entity.radius;
			if (diff > max.y) {
				entity.position.y -= diff - max.y;
				entity.velocity.y *= -BOUNCE;
				//	entityA.force.y = 0;
			}

		}	
		
		for (i = 0; i < n; i++) {
			entity = entities.get(i);
			if (entity.remove) {
				remove(entity);
				i--; n--;
			} else {
				// World friction (velocity)
				entity.velocity.scl(FRICTION);
				// Apply force
				entity.velocity.addScl(entity.force, TIME_STEP);
				entity.force.setZero(); // clear force
				entity.update();				
				aabbTree.update(entity);
				entity.matched = false;				
			}
		}

	}

	public void add(Entity entity) {		
		entity.world = this;
		entities.add(entity);
		entity.updateAABB();
		aabbTree.insert(entity);		
	}

	public void remove(Entity entity) {		
		entities.remove(entity);
		aabbTree.remove(entity);
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public AABBTree<Entity> getAABBTree() {
		return aabbTree;
	}

	public void clear() {
		entities.clear();
		aabbTree.clear();
	}
	
	public class MyIterator implements Iterator<Entity> {

		public Entity entityA;

		@Override
		public boolean next(Entity entityB) {
			if(entityA.remove) return false;
			if (!entityB.matched && !entityB.remove) {
				if (entityB.overlap(entityA)) { // AABB test
					Vec2 d = tmp1.set(entityB.position).sub(entityA.position);
					final float len2 = d.len2();
					float radius = entityA.radius + entityB.radius;
					if(len2 < radius * radius) { // Collision test
						if(entityB.getType() == Entity.FOOD) {						
							entityA.mass += entityB.mass;
							entityB.remove = true;						
						} else {
							float distance = (float) StrictMath.sqrt(len2);
							float penetration = radius - distance;
							Bug bugA = (Bug) entityA;
							if(entityB.getType() == Entity.BUG) {
								Bug bugB = (Bug) entityB;
								if(bugB.parent == bugA.parent && (bugB.parent == null || (bugB.splitTime + bugA.splitTime) > 0f)) {
									float combinedMass = entityA.mass + entityB.mass;
									if(distance > 0) {
										// normals
										d.scl(1F / distance);
									} else {
										d.set(1, 0);
									}
									Vec2 rv = tmp2.set(entityB.velocity).sub(entityA.velocity);									
									float vn = rv.dot(d);
									if (vn < 0F) {
										float j = vn / combinedMass;
										entityA.velocity.addScl(d, j * entityB.mass);
										entityB.velocity.subScl(d, j * entityA.mass);
									}
									final float correction = Math.max(penetration - SLOP, 0.0f) / combinedMass * PERCENT;
									entityA.position.subScl(d, correction * entityB.mass);
									entityB.position.addScl(d, correction * entityA.mass);
								} else if(penetration > Math.min(entityA.radius, entityB.radius) ) {									
									entityA.collideWith(entityB);
									entityB.collideWith(entityA);									
								}
							} else if(penetration >= Math.min(entityA.radius, entityB.radius) * 1.25f ) { // Virus test						
								entityB.collideWith(entityA);								
							} // Virus test
						}
					} // Collision test
				} else {
					return false;
				}
			}
			return true;
		}

	}

}
