package com.openx.etf.erlang;

import com.openx.etf.TermTypes;

public class DistributionHeader implements ErlangObject {

  private final byte[] data;

  public DistributionHeader(byte[] data) {
      this.data = data;
  }

  @Override
  public byte type() {
      return TermTypes.DISTRIBUTION_HEADER;
  }
}
