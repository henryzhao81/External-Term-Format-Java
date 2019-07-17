package com.openx.etf.erlang;

import com.openx.etf.TermTypes;

public class Port implements ErlangObject {

  private final String atom;
  private final int atomCacheRef;
  private final int id;
  private final byte creation;

  public Port(String atom, int id, byte creation) {
      this.atom = atom;
      this.id = id;
      this.creation = creation;
      this.atomCacheRef = -1;
  }

  public Port(int atomCacheRef, int id, byte creation) {
      this.atomCacheRef = atomCacheRef;
      this.id = id;
      this.creation = creation;
      this.atom = null;
  }

  @Override
  public byte type() {
      return TermTypes.PORT_EXT;
  }
}