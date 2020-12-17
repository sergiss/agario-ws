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
var COLORS1=['#07FF42', '#FF0757', '#FFF007', '#FF5407', '#07FFF6', '#9CFF07', '#FFD907', '#0774FF', '#1A07FF', '#F007FF'];
var COLORS2=[];

var PI2=2.0 * Math.PI;

var canvas;
window.onload = function() {

	canvas = document.getElementById('canvas');
	document.body.style.overflow = 'hidden';
	document.body.scroll = "no"; // ie only

	for(var i = 0; i < 10; i++) {
		COLORS2[i] = shadeColor(COLORS1[i], -10);
	}

}

function onKey(keyCode, down) {
	var message = {
			type : 0,
			args : [ keyCode, down ]
	};
	sendMessage(message);
}

function onMouseMove(x, y, rect) {
	var message = {
			type : 2,
			args : [ (x - rect.left) - rect.width * 0.5, (y - rect.top) - rect.height * 0.5 ]
	};
	sendMessage(message);
}

function sendMessage(message) {
	webSocket.send(JSON.stringify(message));
}

var host = prompt("Please enter host ip:", location.hostname ? location.hostname : 'localhost');

var webSocket = new WebSocket('ws://' + host + ':8081/bugsws/websocketendpoint');
webSocket.binaryType = "arraybuffer";

webSocket.onopen = function (event) {

	document.addEventListener('keydown', function(event) {
		onKey(event.keyCode, true);
	}, false);

	document.addEventListener('keyup', function(event) {
		onKey(event.keyCode, false);
	}, false);

	document.addEventListener('mousemove', function(event) {
		var rect = canvas.getBoundingClientRect();
		onMouseMove(event.clientX, event.clientY, rect);
	}, false);

	document.addEventListener('click', function(event) {
		var rect = canvas.getBoundingClientRect();
		onMouseMove(event.clientX, event.clientY, rect);
	}, false);

}

webSocket.onmessage = function(event) {

	var data = pako.inflate(event.data);

	let offset = 0;
	var x = readInt(data, offset);
	var y = readInt(data, offset + 4);

	var ctx = canvas.getContext('2d');

	var w = window.innerWidth;
	var h = window.innerHeight;

	ctx.canvas.width  = w;
	ctx.canvas.height = h;

	var ar = w > h ? w / readInt(data, offset + 8) : h / readInt(data, offset + 12);
	
	ctx.fillStyle="#f2fbff";
	ctx.fillRect(0, 0, w, h);

	ctx.translate(w * 0.5, h * 0.5);	

	drawGrid(x, y, w, h, ctx, ar);

	ctx.textAlign="center";
	
	offset += 16;
	let info, id, entities = [];
	for(; offset < data.length;) {
		id   = readInt(data, offset);
		info = readInt(data, offset + 4);
		entities.push({ 
				id: id,
				ci: info >> 1, // color index
				r : readInt(data, offset +  8), // radius
				x : readInt(data, offset + 12), // x
				y : readInt(data, offset + 16)  // y
			});
		offset += 20; // TODO : id, name
	}
	
	entities.sort(function(a, b) {		
		return a.r - b.r;
	});
	
	let r, lw, cx, cy;
	entities.forEach(function(entity) {
		r = entity.r * ar;
		lw = r * 0.1;
		cx = (entity.x - x) * ar;
		cy = (entity.y - y) * ar;
		ctx.lineWidth = lw;		
		ctx.fillStyle = COLORS1[entity.ci];
		drawCircle(ctx, cx, cy, r - lw * 0.5);
		ctx.fill();
		ctx.strokeStyle = COLORS2[entity.ci];
		ctx.stroke();
	});
	
}

function readInt(data, off) {
	return (data[off    ] & 0xFF) << 24 
		 | (data[off + 1] & 0xFF) << 16 
		 | (data[off + 2] & 0xFF) << 8 
		 | (data[off + 3] & 0xFF);
}

function drawGrid(x, y, w, h, ctx, ar) {

	var size = 12;

	var cols = w / size;
	var rows = h / size;

	var hw = w * 0.5;
	var hh = h * 0.5;

	x = (x % size) + hw;
	y = (y % size) + hh;

	hw *= ar;
	hh *= ar;

	ctx.strokeStyle = "#dae2e6";
	ctx.lineWidth = 0.4 * ar;	
	ctx.beginPath();
	var i, j;
	for(i = 0; i < cols; i++) {
		j = (i * size - x) * ar;		
		ctx.moveTo(j,-hh);
		ctx.lineTo(j, hh);
	}

	for(i = 0; i < rows; i++) {
		j = (i * size - y) * ar;
		ctx.moveTo(-hw, j);
		ctx.lineTo( hw, j);		
	}

	ctx.stroke();

}

function drawCircle(ctx, x, y, r) {

	ctx.beginPath();

	var n = Math.max(1, Math.floor(6 * Math.cbrt(r))) - 1;
	var angle = PI2 / (n + 1);

	var cos = Math.cos(angle);
	var sin = Math.sin(angle);

	var cx = r, cy = 0;
	ctx.lineTo(r + x, y);
	for(var i = 0; i < n; i++) {		
		var temp = cx;
		cx = cos * cx   - sin * cy;
		cy = sin * temp + cos * cy
		ctx.lineTo(cx + x, cy + y);
	}
	ctx.lineTo(r + x, y);
	ctx.closePath();

}

function shadeColor(color, percent) {
    var num = parseInt(color.slice(1), 16),
        amt = Math.round(2.55 * percent),
        R = (num >> 16) + amt,
        G = (num >> 8 & 0x00FF) + amt,
        B = (num & 0x0000FF) + amt;

    return "#" + (0x1000000 + clamp(R, 0, 255) * 0x10000 + clamp(G, 0, 255) * 0x100 + clamp(B, 0, 255)).toString(16).slice(1);
}

function clamp(v, min, max) {
	if(v < min) return min;
	if(v > max) return max;
	return v;
}