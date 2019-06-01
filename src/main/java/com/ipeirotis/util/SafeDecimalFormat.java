package com.ipeirotis.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class SafeDecimalFormat {

	private static final ThreadLocal<Map<String, NumberFormat>> threadLocal = new ThreadLocal<Map<String, NumberFormat>>() {
		@Override
		protected Map<String, NumberFormat> initialValue() {
			return new HashMap<String, NumberFormat>();
		}
	};

	public static NumberFormat forPattern(String pattern) {
		Map<String, NumberFormat> cache = threadLocal.get();
		NumberFormat df = cache.get(pattern);
		if (df == null) {
			df = new DecimalFormat(pattern);
			cache.put(pattern, df);
		}
		return df;
	}
}
