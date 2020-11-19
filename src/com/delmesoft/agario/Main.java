package com.delmesoft.agario;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import com.delmesoft.httpserver.HttpRequest;
import com.delmesoft.httpserver.HttpResponse;
import com.delmesoft.httpserver.HttpResponse.Status;
import com.delmesoft.httpserver.HttpServer;
import com.delmesoft.httpserver.HttpServerImpl;
import com.delmesoft.httpserver.webserver.WebServerHandler;

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
public class Main {

	public static void main(String[] args) throws Exception {
		
		int port = 8081;

		System.out.printf("Agario Server listening at port: %d\n", port);
		
		HttpServer webSocketServer = new HttpServerImpl(port);
		
		// set HTTP listener
		webSocketServer.setHttpListener(new AgarioWS());
		
		// connect server
		webSocketServer.connect();
		
		port = 8080;
		System.out.printf("Web Server listening at port: %d\n", port);
		HttpServer webServer = new HttpServerImpl(port);
		
		webServer.setHttpListener(new WebServerHandler() {
			@Override
			public HttpResponse handleQuery(HttpRequest httpRequest) {
				return HttpResponse.build(Status.NOT_FOUND);
			}
			@Override
			public InputStream toStream(File file) throws Exception {
				return new FileInputStream(file); // Convert content to input stream
			}
		});
		webServer.connect();
				
		try(Scanner scanner = new Scanner(System.in)){
			System.out.println("Press Enter to exit...");
			scanner.nextLine();
		}
		
		webSocketServer.disconnect();
		webServer.disconnect();
	}

}
