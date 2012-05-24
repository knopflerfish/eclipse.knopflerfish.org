/*
 * Copyright (c) 2003-2010, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.eclipse.core.manifest;

import java.util.ArrayList;
import java.util.List;

import org.knopflerfish.eclipse.core.Util;
import org.knopflerfish.eclipse.core.VersionRange;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class PackageDescription {
  public static final String SEPARATOR = ";";
  public static final String SPECIFICATION_VERSION = "specification-version";
  public static final String VERSION = "version";
  public static final String RESOLUTION = "resolution";
  public static final String OPTIONAL = "optional";

  private static final int EXPORT = 0;
  private static final int IMPORT = 1;

  private final int type;
  private final String packageName;
  private boolean optional;
  private Version version;
  private VersionRange versionRange;

  public PackageDescription(String name, Version version)
  {
    this.type = EXPORT;
    this.packageName = name;
    this.version = version;
    if (version == null) {
      this.version = Version.emptyVersion;
    }
  }

  public PackageDescription(String name)
  {
    this(name, Version.emptyVersion);
  }

  public PackageDescription(String name, VersionRange versionRange,
      boolean optional)
  {
    this.type = IMPORT;
    this.packageName = name;
    this.optional = optional;
    this.versionRange = versionRange;
    if (versionRange == null) {
      this.versionRange = new VersionRange();
    }
  }

  public String getPackageName()
  {
    return packageName;
  }

  public Version getVersion()
  {
    return version;
  }

  public void setVersion(Version version)
  {
    this.version = version;
    if (version == null) {
      this.version = Version.emptyVersion;
    }
  }

  public VersionRange getVersionRange()
  {
    return versionRange;
  }

  public void setVersionRange(VersionRange versionRange)
  {
    this.versionRange = versionRange;
    if (versionRange == null) {
      this.versionRange = new VersionRange();
    }
  }

  public boolean isOptional()
  {
    return optional;
  }

  public boolean isCompatible(PackageDescription pd)
  {
    // Check package name
    if (pd == null || pd.getPackageName() == null)
      return false;
    if (!packageName.equals(pd.getPackageName()))
      return false;

    if (type == IMPORT) {
      return false;
    }
    // Package name the same, check specification version
    if (pd.versionRange != null) {
      return pd.versionRange.contains(version);
    } else if (pd.version != null) {
      return (version.compareTo(pd.version) >= 0);
    } else {
      return true;
    }
  }

  /**
   * Parse import/export package header.
   * 
   * @param s
   *          The value of the header to parse.
   * @param range
   *          if versions shall be parsed as ranges or not.
   * @param optionals
   *          Optional list to add packages that are marked with the directive
   *          resolution=optional.
   * @return Mapping from package name to version/version range.
   */
  static List<PackageDescription> parseNames(String s, boolean range)
  {
    final List<PackageDescription> packages = new ArrayList<PackageDescription>();

    if (s != null) {
      s = s.trim();
      final String[] lines = Util.splitwords(s, ",", '\"');
      for (int i = 0; i < lines.length; i++) {
        final String[] words = Util.splitwords(lines[i].trim(), ";", '\"');
        if (words.length < 1) {
          throw new RuntimeException("bad package spec '" + s + "'");
        }

        String spec = "0";
        String name = words[0].trim();
        boolean optional = false;

        for (int j = 1; j < words.length; j++) {
          final String[] info = Util.splitwords(words[j], "=", '\"');

          if (info.length == 2) {
            if ("specification-version".equals(info[0].trim())) {
              spec = info[1].trim();
            } else if ("version".equals(info[0].trim())) {
              spec = info[1].trim();
            } else if (info[0].endsWith(":")) {
              final String directive = info[0].substring(0,
                  info[0].length() - 1).trim();
              if (RESOLUTION.equals(directive)
                  && OPTIONAL.equals(info[1].trim())) {
                optional = true;
              }
            }
          }
        }
        if (range) {
          packages.add(new PackageDescription(name, new VersionRange(spec),
              optional));
        } else {
          packages.add(new PackageDescription(name, new Version(spec)));
        }
      }
    }
    return packages;
  }

  // ***************************************************************************
  // java.lang.Object methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof PackageDescription)) {
      return false;
    }

    PackageDescription pd = (PackageDescription) obj;

    // Check package name
    if (!packageName.equals(pd.getPackageName()))
      return false;

    // Package name the same, check version
    if ((version == null && pd.version != null) 
        || (versionRange == null && pd.versionRange != null)) {
        return false;
    }
    if ((version != null && !version.equals(pd.version))
        || (versionRange != null && !versionRange.equals(pd.versionRange))) {
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    final StringBuffer buf = new StringBuffer(packageName);

    if (version != null && !version.equals(Version.emptyVersion)) {
      buf.append(SEPARATOR);
      buf.append(VERSION);
      buf.append("=");
      buf.append(version.toString());
    } else if (versionRange != null && !versionRange.isEmpty()) {
      buf.append(SEPARATOR);
      buf.append(VERSION);
      buf.append("=");
      buf.append(versionRange.toString());
    }

    if (optional) {
      buf.append(SEPARATOR);
      buf.append(RESOLUTION);
      buf.append(":=");
      buf.append(OPTIONAL);
    }

    return buf.toString();
  }
}
