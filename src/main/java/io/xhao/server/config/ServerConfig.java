package io.xhao.server.config;

import lombok.Data;

@Data
public class ServerConfig {
	private int port = 8080;
	private int ioThreads = 100;
	private int coreThreadSize = 500;
	private int maxThreadSize = 500;
	private int queue = 128;

}
