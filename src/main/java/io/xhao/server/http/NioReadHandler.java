package io.xhao.server.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NioReadHandler implements Runnable {

	private SelectionKey key;
	private ExecutorService execPool;

	private final int SCALE = 512;
	private final int INITIAL = 1024;

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		ByteBuffer buffer = null;

		Map<String, Object> map = (Map<String, Object>) key.attachment();
		ByteBuffer oldbuffer = (ByteBuffer) map.get("request");
		if (oldbuffer != null) {
			buffer = ByteBuffer.allocate(oldbuffer.capacity() + SCALE);
			buffer.put(oldbuffer);
		} else {
			buffer = ByteBuffer.allocate(INITIAL);
		}

		SocketChannel channel = (SocketChannel) key.channel();
		try {
			while (true) {
				if (channel.isOpen()) {
					if (!buffer.hasRemaining()) {
						ByteBuffer newbuffer = ByteBuffer.allocate(buffer.capacity() + SCALE);
						buffer.flip();
						newbuffer.put(buffer);
						buffer = newbuffer;
					}
					int count = channel.read(buffer);
					if (count == 0) {
						map.put("request", buffer);
						execPool.execute(new Route(key));
						break;
					} else if (count == -1) {
						key.cancel();
						channel.close();
						break;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("read encounter io exception");
			if (key != null && key.isValid()) {
				key.cancel();
				try {
					channel.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
