package io.xhao.server.http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Parser {

	public static Request parse(ByteBuffer buffer) {
		Request r = new Request();

		StringBuilder method = new StringBuilder(8);
		Step1: while (buffer.hasRemaining()) {
			// utf-8
			char b = (char) buffer.get();
			switch (b) {
			case ' ': {
				break Step1;
			}
			default: {
				method.append(b);
			}
			}
		}

		r.setMethod(method.toString());

		StringBuilder url = new StringBuilder(32);

		Step2: while (buffer.hasRemaining()) {
			// utf-8
			char b = (char) buffer.get();
			switch (b) {
			case ' ': {
				break Step2;
			}
			default: {
				url.append(b);
			}
			}
		}

		r.setUrl(url.toString());

		StringBuilder version = new StringBuilder(8);
		Step3: while (buffer.hasRemaining()) {
			// utf-8
			char b = (char) buffer.get();
			switch (b) {
			case '\n': {
				break Step3;
			}
			default: {
				version.append(b);
			}
			}
		}

		r.setVersion(version.toString());

		Map<String, String> params = new HashMap<>();
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean mark = false;
		char[] prev = new char[2];

		while (buffer.hasRemaining()) {
			// utf-8
			char b = (char) buffer.get();
			switch (b) {
			case '\r': {
				break;
			}
			case '\n': {
				if (prev[0] == '\n' && prev[1] == '\r') {
					// content start
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					r.setContent(bytes);
				} else {
					mark = false;
					params.put(key.toString(), value.toString());
					key = new StringBuilder();
					value = new StringBuilder();
				}
				break;
			}
			case ':': {
				mark = true;
				break;
			}
			default: {
				if (mark == false) {
					key.append(b);
				} else {
					if (b == ' ' && prev[1] == ':') {
					} else {
						value.append(b);
					}
				}
			}
			}
			prev[0] = prev[1];
			prev[1] = b;
		}

		r.setParams(params);

		int content_len = Integer.valueOf(r.getParams().get("Content-Length"));
		if (content_len != r.getContent().length) {
			r.setPendingRead(true);
		}
		return r;

	}
}
