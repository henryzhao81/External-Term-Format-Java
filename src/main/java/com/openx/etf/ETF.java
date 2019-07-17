package com.openx.etf;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

public class ETF {
  
  public static final ETFConfig Config = new ETFConfig()
      .setIncludeHeader(true)
      .setCompression(true)
      .setIncludeDistributionHeader(true)
      .setBert(false)
      .setVersion(TermTypes.VERSION)
      .setLoqui(false);
  
  public static Object binary_to_term(byte[] input) {
    return Config.createParser(input).next();
  }
  
  public static byte[] term_to_binary(Object input) {
    return Config.createWriter(true).write(input).toBytes(true);
  }
  
  /**
   * Example of erlang 
   * > uuid:string_to_uuid ("68d09203-21ad-4dd0-2229-de689e61dffc").                      
   * <<104,208,146,3,33,173,77,208,34,41,222,104,158,97,223,252>>
   * @return
   */
  public static byte[] string_to_uuid (String strUuid) throws Exception {
    if(strUuid.length() != 36)
      throw new Exception("wrong format of uuid : " + strUuid);
    char[] charArr = strUuid.toCharArray();
    if(charArr[8] != '-' || charArr[13] != '-' || charArr[18] != '-' || charArr[23] != '-')
      throw new Exception("wrong format of uuid : " + strUuid);
    byte[] res = new byte[16];
    int offset = 0;
    for (int i = 0; i < 16; i++) {
      if(offset == 8 || offset == 13 || offset == 18 || offset == 23)
        offset++;
      res[i] = (byte)(Character.digit(charArr[offset++], 16) * 16 + Character.digit(charArr[offset++], 16));
    }
    return res;
  }

}
