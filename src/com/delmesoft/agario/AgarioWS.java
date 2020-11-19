package com.delmesoft.agario;

import java.io.IOException;
import java.util.List;

import com.delmesoft.agario.utils.protocol.ProtocolDataV1;
import com.delmesoft.httpserver.Session;
import com.delmesoft.httpserver.websocket.WebSocketHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
public class AgarioWS extends WebSocketHandler {
	
	private final Agario agario;
	private final Gson gson;
	
	public AgarioWS() {
		agario = new Agario();
		agario.setProtocolData(new ProtocolDataV1(this));
		agario.start();
		gson =  new GsonBuilder().create();
		super.getIndexSet().add("/bugsws/websocketendpoint");
	}
	
	@Override
	public void onOpen(Session session) {
		System.out.println(String.format("Session opened %s", session.toString()));
		agario.joinPlayer(session);
	}

	@Override
	public void onClose(Session session) {
		agario.unjoin(session);
	}

	@Override
	public void onData(byte[] data, int len, Session session) {}

	@Override
	public void onText(String text, Session session) {
		try {
			Message message = gson.fromJson(text, Message.class);
			List<Object> args = message.getArgs();
			switch (message.getType()) {
				case Message.KEY_PRESSED: {
					int keyCode = ((Number) args.get(0)).intValue();
					boolean down = (boolean) args.get(1);
					agario.onKeyPressed(keyCode, down, session);
					break;
				}
				case Message.MOUSE_MOVED: {
					int x = ((Number) args.get(0)).intValue();
					int y = ((Number) args.get(1)).intValue();
					agario.onMouseMoved(x, y, session);
					break;
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
			closeSession(session);
		}
	}
	
	public void closeSession(Session session) {
		try {
			session.close();
		} catch (IOException e) {}
	}

}
