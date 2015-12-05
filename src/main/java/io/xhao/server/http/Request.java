package io.xhao.server.http;

import java.util.Map;

import lombok.Data;

@Data
public class Request {
	private String method;
	private String url;
	private String version;
	private Map<String, String> params;
	private byte[] content;

	private boolean pendingRead;
}
