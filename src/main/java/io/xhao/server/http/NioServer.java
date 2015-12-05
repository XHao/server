package io.xhao.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.xhao.server.config.ServerConfig;
import lombok.RequiredArgsConstructor;

public class NioServer {

	@SuppressWarnings("unused")
	public void main(ServerConfig config) {

		ExecutorService execPool = null;
		ExecutorService ioPool = null;
		ServerSocketChannel server = null;

		try {
			Selector selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(config.getPort()));
			server.register(selector, SelectionKey.OP_ACCEPT);

			ioPool = new ThreadPoolExecutor(config.getIoThreads(), config.getIoThreads(), 0L, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(config.getQueue()));
			execPool = new ThreadPoolExecutor(config.getCoreThreadSize(), config.getMaxThreadSize(), 0L,
					TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(config.getQueue()));

			// main thread
			new Acceptor(selector, ioPool, execPool).run();
		} catch (IOException e) {
			System.err.println(e.getMessage());

			if (server != null) {
				try {
					server.close();
				} catch (IOException e1) {
					System.err.println(e1.getMessage());
				}
			}

			if (ioPool != null) {
				ioPool.shutdown();
			}

			if (execPool != null) {
				execPool.shutdown();
			}

			// 等待资源回收
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException ee) {
			}
			System.exit(0);
		}
	}

	@RequiredArgsConstructor
	private static class Acceptor implements Runnable {
		private final Selector selector;
		private final ExecutorService io;
		private final ExecutorService exec;

		@Override
		public void run() {
			while (true) {
				try {
					// Wait for an event
					int readys = selector.select();
					if (readys == 0)
						continue;

					// Get list of selection keys with pending events
					Set<SelectionKey> selected = selector.selectedKeys();
					Iterator<SelectionKey> it = selected.iterator();

					while (it.hasNext()) {
						SelectionKey selKey = it.next();

						if (!selKey.isValid())
							continue;

						if (selKey.isReadable()) {
							selKey.interestOps(0);
							io.execute(new NioReadHandler(selKey, exec));
						} else if (selKey.isAcceptable()) {
							SocketChannel channel = ((ServerSocketChannel) selKey.channel()).accept();
							if (channel != null) {

								channel.configureBlocking(false);
								channel.register(selector, SelectionKey.OP_READ, new HashMap<String, Object>());
								channel.socket().setTcpNoDelay(true);
								channel.socket().setSoLinger(false, 0);
								channel.socket().setKeepAlive(true);
							} else {
								System.err.println("No Connection");
							}
						} else if (selKey.isWritable()) {
							selKey.interestOps(0);
							io.execute(new NioWriteHandler(selKey));
						}
					}
					selected.clear();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}