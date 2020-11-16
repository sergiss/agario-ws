package com.delmesoft.agario.utils;

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
public class Loop {
	
	private int fps = 60;
	private int _fps;
	
	private LoopListener listener;
	
	private Thread thread;
	
	public Loop() {}
	
	public synchronized void start() {
		if (!isRunning()) {
			thread = new Thread(Loop.class.getName()) {
				public void run() {
					try {
						long desiredTime = 1_000_000_000 / fps;
						long start = System.nanoTime();
						long last = start;
						long fpsTime = 0;
						int frames = 0;
						while(!isInterrupted()) {
							
							last = start;
							start = System.nanoTime();
							long sl = (start - last);
														
							frames++;
							fpsTime += sl;
							if(fpsTime > 1_000_000_000) {
								_fps = frames;
								frames = 0;
								fpsTime = 0;
							}
							
							float dt = sl * 0.000000001F;
							listener.update(dt);
							listener.render();
							
							long diff = (long) ((desiredTime - (System.nanoTime() - start)) * 0.000001F);
							if(diff > 0) {
								sleep(diff);
							}
							
						}
					} catch (Exception ignore) {
						ignore.printStackTrace();
					} finally {
						Loop.this.stop();
					}
				}
			};
			thread.start();
		}
	}

	public synchronized boolean isRunning() {
		return thread != null && !thread.isInterrupted();
	}

	public synchronized void stop() {
		if (isRunning()) {
			try {
				thread.interrupt();
			} finally {
				thread = null;
			}
		}
	}
	
	public int getCurrentFps() {
		return _fps;
	}
		
	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		if(fps < 1) throw new RuntimeException("Fps must be greater than 0, fps: " + fps);
		this.fps = fps;
	}

	public LoopListener getListener() {
		return listener;
	}

	public void setListener(LoopListener listener) {
		this.listener = listener;
	}

	public static interface LoopListener {
		
		void update(float dt);
		
		void render();
		
	}

}
