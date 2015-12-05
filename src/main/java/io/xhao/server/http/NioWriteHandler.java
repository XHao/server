package io.xhao.server.http;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NioWriteHandler implements Runnable {
	private SelectionKey key;

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Map<String, Object> map = (Map<String, Object>) key.attachment();
		Response response = (Response) map.get("response");

		SocketChannel channel = (SocketChannel) key.channel();
		try {
			if (channel.isOpen()) {
				channel.write(response.buffer());
				key.interestOps(SelectionKey.OP_READ);
				key.selector().wakeup();
			}
		} catch (IOException e) {
			System.err.println("write encounter io exception");
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
