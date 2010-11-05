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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class SymbolicName {

  public static String SEPARATOR = ";";
  public static String HEADER_SINGELTON = "singelton";
  public static String HEADER_FRAGMENT_ATTACHMENT = "fragment-attachment";

  private String symbolicName;
  private Map<String, String> attributes = new HashMap<String, String>();

  public SymbolicName(String name)
  {
    StringTokenizer st = new StringTokenizer(name, SEPARATOR);

    // First token is symbolic name
    symbolicName = st.nextToken().trim();

    // Attributes
    while (st.hasMoreTokens()) {
      String parameter = st.nextToken();
      int idx = parameter.indexOf('=');
      if (idx != -1) {
        String attr = parameter.substring(0, idx).trim();
        String value = parameter.substring(idx + 1).trim();
        // Removes qoutes if qouted
        if (value.startsWith("\"") && value.endsWith("\"")) {
          value = value.substring(1, value.length() - 1);
        }
        attributes.put(attr, value);
      }
    }
  }

  // ***************************************************************************
  // Getters and setters
  // ***************************************************************************

  public String getSymbolicName()
  {
    return symbolicName;
  }

  public boolean isSingelton()
  {
    return Boolean.valueOf(getAttribute(HEADER_SINGELTON)).booleanValue();
  }

  public String getFragmentAttachment()
  {
    return getAttribute(HEADER_FRAGMENT_ATTACHMENT);
  }

  public String getAttribute(String attr)
  {
    return (String) attributes.get(attr);
  }

  public void setAttribute(String attr, String value)
  {
    if (attr == null)
      return;

    if (value == null || value.trim().length() == 0) {
      attributes.remove(attr);
    } else {
      attributes.put(attr, value);
    }
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
    if (obj == null || !(obj instanceof SymbolicName)) {
      return false;
    }

    SymbolicName sn = (SymbolicName) obj;

    return symbolicName.equals(sn.symbolicName);
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer(symbolicName);

    for (Iterator<Map.Entry<String, String>> i = attributes.entrySet()
        .iterator(); i.hasNext();) {
      Map.Entry<String, String> entry = i.next();
      buf.append(SEPARATOR);
      buf.append(entry.getKey());
      buf.append("=");
      String value = entry.getValue();
      boolean qoute = value.indexOf(" ") != -1;
      if (qoute) {
        buf.append("\"");
      }
      buf.append(value);
      if (qoute) {
        buf.append("\"");
      }
    }

    return buf.toString();
  }
}
