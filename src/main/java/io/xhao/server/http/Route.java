package io.xhao.server.http;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class Route implements Runnable {

	SelectionKey key;

	public Route(SelectionKey key) {
		this.key = key;
	}

	@Override
	public void run() {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) key.attachment();
		ByteBuffer buffer = (ByteBuffer) map.get("request");
		Request r = parse(buffer);

		if (r.isPendingRead()) {
			buffer.rewind();
			key.interestOps(SelectionKey.OP_READ);
			key.selector().wakeup();
		} else {
			Response response = getInvoker(r).process(r);
			map.put("response", response);
			key.interestOps(SelectionKey.OP_WRITE);
			key.selector().wakeup();
		}
	}

	private InvokeHandler getInvoker(Request r) {
		return InvokeHandler.DEFAULT;
	}

	private Request parse(ByteBuffer buffer) {
		buffer.flip();
		return Parser.parse(buffer);
	}

}
