package io.xhao.server.http;

public interface InvokeHandler {

	static final InvokeHandler DEFAULT = new InvokeHandler() {

		@Override
		public Response process(Request r) {
			return new SimpleResponse("hello world!");
		}
	};

	Response process(Request r);

}
