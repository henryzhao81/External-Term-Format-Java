package com.openx.etf.erlang;

import com.openx.etf.TermTypes;

public class Fun implements ErlangObject {

  private final byte[] data;

  public Fun(byte[] data) {
      this.data = data;
  }

  @Override
  public byte type() {
      return TermTypes.FUN_EXT; //TODO differentiate between new, old fun & exports
  }
}
