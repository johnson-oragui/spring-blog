package com.johnson.utilities;

import com.github.f4b6a3.uuid.UuidCreator;

public class UUIDGenerator {
  public static String generateUUIDv7() {
    return UuidCreator.getTimeOrderedEpoch().toString();
  }
}
