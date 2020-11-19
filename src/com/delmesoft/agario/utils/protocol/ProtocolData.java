package com.delmesoft.agario.utils.protocol;

import java.util.List;

import com.delmesoft.agario.physics.entity.Entity;
import com.delmesoft.agario.utils.Vec2;
import com.delmesoft.httpserver.Session;

public interface ProtocolData {

	void send(Vec2 center, float width, float height, List<Entity> entities, Session session);

}
