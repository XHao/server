package io.xhao.server.http;

import java.nio.ByteBuffer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimpleResponse implements Response {
	protected static final String ENTER = "\n";
	protected static final String version = "HTTP/1.1 200 OK";
	protected static final String Common1 = "Content-Type: application/json; charset=UTF-8";
	protected static final String Common2 = "Content-Length: ";
	protected static final String Common3 = "Connection: keep-alive";

	protected String content;

	public ByteBuffer buffer() {
		StringBuilder sb = new StringBuilder(512);
		sb.append(version).append(ENTER).append(Common1).append(ENTER).append(Common2).append(content.getBytes().length)
				.append(ENTER).append(Common3).append(ENTER).append(ENTER).append(content);

		byte[] bytes = sb.toString().getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}