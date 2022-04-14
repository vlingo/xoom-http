// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

public class ExtendedCharactersFixture {
  public static String asciiWithExtendedCharacters() {
    final StringBuilder builder = new StringBuilder();

    final int asciiBegin = 0x0020;
    final int asciiEnd = 0x007E;

    for (int ascii = asciiBegin; ascii <= asciiEnd; ++ascii) {
      builder.append((char) ascii);
    }

    final int cyrillicBegin = 0x0409;
    final int cyrillicEnd = 0x04FF;

    for (int cyrillic = cyrillicBegin; cyrillic <= cyrillicEnd; ++cyrillic) {
      builder.append((char) cyrillic);
    }

    final int greekCopticBegin = 0x0370;
    final int greekCopticEnd = 0x03FF;

    for (int greekCoptic = greekCopticBegin; greekCoptic <= greekCopticEnd; ++greekCoptic) {
      builder.append((char) greekCoptic);
    }

    return builder.toString();
  }
}
