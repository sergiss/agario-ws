package com.delmesoft.agario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.delmesoft.agario.physics.World;
import com.delmesoft.agario.physics.broadphase.AABB;
import com.delmesoft.agario.physics.entity.Entity;
import com.delmesoft.agario.physics.entity.Food;
import com.delmesoft.agario.physics.entity.Player;
import com.delmesoft.agario.physics.entity.Virus;
import com.delmesoft.agario.utils.Loop;
import com.delmesoft.agario.utils.Loop.LoopListener;
import com.delmesoft.agario.utils.Utils;
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
public abstract class Agario extends Loop implements LoopListener {
	
	public static final int FPS = 60;
	
	public static final int WORLD_WIDTH  = 4000;
	public static final int WORLD_HEIGHT = 4000;
	
	public static final int COLOR_COUNT = 10;

	public static final int DEFAULT_FOOD_COUNT  = 3000;
	public static final int DEFAULT_SPIKE_COUNT = 60;
	
	private AABB aabb = new AABB();
	private Vec2 tmp1 = new Vec2();
	private Vec2 tmp2 = new Vec2();
	
	private float accumulator = 0;
	private World world;

	private final List<Runnable> postRunnables;
	private final Map<Long, Player> playerMap;
	
	public Agario() {
		postRunnables = new ArrayList<>();
		playerMap = new HashMap<>();
		setFps(FPS);
		setListener(this);
		create();		
	}

	private void create() {
		
		final Random random = Utils.random;

		final int hw = WORLD_WIDTH  >> 1;
		final int hh = WORLD_HEIGHT >> 1;

		final int w = WORLD_WIDTH;
		final int h = WORLD_HEIGHT;		

		world = new World(WORLD_WIDTH, WORLD_HEIGHT) {
			@Override
			public void remove(Entity entity) {
				super.remove(entity);
				if (entity.getType() == Entity.FOOD) {
					Entity _entity = new Food(random.nextInt(COLOR_COUNT));
					_entity.position.set(random.nextInt(w) - hw, random.nextInt(h) - hh);
					world.add(_entity);
				} else if (entity.getType() == Entity.VIRUS) {
					Entity _entity = new Virus(0);
					_entity.position.set(random.nextInt(w) - hw, random.nextInt(h) - hh);
					world.add(_entity);
				}
			}
		};

		Entity entity;
		int i;
		for (i = 0; i < DEFAULT_FOOD_COUNT; i++) {
			entity = new Food(random.nextInt(COLOR_COUNT));
			entity.position.set(random.nextInt(w) - hw, random.nextInt(h) - hh);
			world.add(entity);
		}
		for (i = 0; i < DEFAULT_SPIKE_COUNT; i++) {
			entity = new Virus(0);
			entity.position.set(random.nextInt(w) - hw, random.nextInt(h) - hh);
			world.add(entity);
		}
	}

	@Override
	public void update(float dt) {
		float frameTime = Math.max(dt, 0.05f);
		accumulator += frameTime;
		while (accumulator >= World.TIME_STEP) {
			world.step();
			accumulator -= World.TIME_STEP;
		}
		
		// POST RUNNNABLES ********************************************		
		synchronized (postRunnables) {
			for(Runnable runnable : postRunnables) {
				runnable.run();
			}
			postRunnables.clear();
		}
	}

	public void render() {

		// UPDATE CLIENTS *********************************************
		List<Entity> entities = new ArrayList<Entity>();
		final long currentTimeMillis = System.currentTimeMillis();
		float size;
		for(Player player : playerMap.values()) {
			if(currentTimeMillis - player.getLastUpdate() > Player.UPDATE_TIME) {
				player.setLastUpdate(currentTimeMillis);
				player.update();
				player.getCenter(tmp1);
				aabb.set(player);
				size = 200 + aabb.getArea() * 0.0001F;
				aabb.min.sub(size, size);
				aabb.max.add(size, size);
				world.getAABBTree().query(aabb, entities);
				if (entities.size() > 0) {
					render(tmp1, aabb.getWidth(), aabb.getHeight(), entities, player.session);
					entities.clear();
				}
			}
			player.follow(tmp2.set(player.getScreenPoint().x, player.getScreenPoint().y).add(tmp1));	
		}
		
	}
	
	protected abstract void render(Vec2 center, float width, float height, List<Entity> entities, Session session);

	public void addPostRunnable(Runnable runnable) {
		synchronized (postRunnables) {
			postRunnables.add(runnable);
		}
	}
	
	public void joinPlayer(Session session) {
		int hw = (int) world.max.x;
		int hh = (int) world.max.y;

		int w = hw << 1;
		int h = hh << 1;

		Random random = Utils.random;

		float x = random.nextInt(w) - hw;
		float y = random.nextInt(h) - hh;

		int color = random.nextInt(COLOR_COUNT);

		addPostRunnable(new Runnable() {
			@Override
			public void run() {
				Player player = new Player(x, y, color, world);
				player.session = session;
				session.userData = player;
				playerMap.put(session.getId(), player);
			}
		});
	}

	public void unjoin(Session session) {
		session.userData = null;
		addPostRunnable(new Runnable() {
			@Override
			public void run() {
				Player player = playerMap.remove(session.getId());
				if (player != null) {
					player.session = null;
				}
			}
		});
	}

	public void onKeyPressed(int keyCode, boolean down, Session session) {
		if (down) {
			switch (keyCode) {
			case 87: // w
				addPostRunnable(new Runnable() {
					@Override
					public void run() {
						Player player = ((Player) session.userData);
						if (player != null) {
							Vec2 center = player.getCenter();
							player.share(center.add(player.getScreenPoint()));
						}
					}
				});
				break;
			case 32: // space
				addPostRunnable(new Runnable() {
					@Override
					public void run() {
						Player player = ((Player) session.userData);
						if (player != null) {
							player.split();
						}
					}
				});
				break;
			}
		}
	}

	public void onMouseMoved(int x, int y, Session session) {
		Player player = ((Player) session.userData);
		if(player != null) {
			player.getScreenPoint().set(x, y);
		}
	}
	
}
