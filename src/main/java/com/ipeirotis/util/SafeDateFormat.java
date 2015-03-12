package com.ipeirotis.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SafeDateFormat {

  private static final ThreadLocal<Map<String, DateFormat>> threadLocal = new ThreadLocal<Map<String, DateFormat>>() {
    @Override
    protected Map<String, DateFormat> initialValue() {
      return new HashMap<String, DateFormat>();
    }
  };

  public static DateFormat forPattern(String pattern) {
    Map<String, DateFormat> cache = threadLocal.get();
    DateFormat df = cache.get(pattern);
    if (df == null) {
      df = new SimpleDateFormat(pattern, Locale.ENGLISH);
      cache.put(pattern, df);
    }
    return df;
  }
}
