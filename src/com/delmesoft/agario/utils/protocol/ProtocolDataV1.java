package com.delmesoft.agario.utils.protocol;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.delmesoft.agario.AgarioWS;
import com.delmesoft.agario.physics.entity.Entity;
import com.delmesoft.agario.utils.Compressor;
import com.delmesoft.agario.utils.Vec2;
import com.delmesoft.httpserver.Session;

public class ProtocolDataV1 implements ProtocolData {

	private AgarioWS agarioWS;
	
	private Compressor compressor;
	private Executor executor;

	public ProtocolDataV1(AgarioWS agarioWS) {
		this.agarioWS = agarioWS;
		this.compressor = new Compressor();
		this.executor = Executors.newFixedThreadPool(1);
	}

	@Override
	public void send(Vec2 center, float width, float height, List<Entity> entities, Session session) {
		byte[] data = new byte[1024];
		int offset = 0;
		
		offset += writeInt(data, offset, (int) center.x);
		offset += writeInt(data, offset, (int) center.y);
		offset += writeInt(data, offset, (int) width);
		offset += writeInt(data, offset, (int) height);

		//offset = writeInt(data, offset, entities.size()); // entity count
		for (Entity entity : entities) {
			data = ensureCapacity(data, offset, 20); // TODO : name
			offset += writeInt(data, offset, (int) entity.id);
			int info = (entity.color & 0xF) << 1;
			if (entity.getType() == Entity.BUG) {
				info |= 0b1;
				// TODO : name value
			}
			offset += writeInt(data, offset, info);
			offset += writeInt(data, offset, (int) Math.ceil(entity.radius));
			offset += writeInt(data, offset, (int) entity.position.x);
			offset += writeInt(data, offset, (int) entity.position.y);
		}
		sendAsync(data, offset, session);
	}

	private void sendAsync(byte[] data, int offset, Session session) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] compressed = compressor.compress(data, 0, offset);
//					 System.out.println("Compression: " + ((float)
//					 compressed.length / data.length));
					agarioWS.sendData(compressed, session);
				} catch (Throwable e) {
					// e.printStackTrace();
					agarioWS.closeSession(session);
				}
			}
		});
	}

	private byte[] ensureCapacity(byte[] data, int offset, int len) {
		while(data.length - offset < len) {
			byte[] tmp = new byte[(int) (data.length * 1.75)];
			System.arraycopy(data, 0, tmp, 0, data.length);
			data = tmp;
		}
		return data;
	}
	
	public static int writeInt(byte[] data, int offset, int value) {
		data[offset     ] = ((byte) (0xFF & (value >>> 24)));
		data[offset + 1 ] = ((byte) (0xFF & (value >>> 16)));
		data[offset + 2 ] = ((byte) (0xFF & (value >>> 8)));
		data[offset + 3 ] = ((byte) (0xFF & value));
		return 4;
	}

}
