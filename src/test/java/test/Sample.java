package test;

import io.xhao.server.config.ServerConfig;
import io.xhao.server.http.NioServer;

public class Sample {
	public static void main(String[] args) {
		NioServer server = new NioServer();
		server.main(new ServerConfig());
	}
}
