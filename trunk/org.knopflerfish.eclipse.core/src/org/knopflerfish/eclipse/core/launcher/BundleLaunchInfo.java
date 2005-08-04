/*
 * Copyright (c) 2003-2005, KNOPFLERFISH project
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

package org.knopflerfish.eclipse.core.launcher;

import java.util.StringTokenizer;

public class BundleLaunchInfo {
  public static String [] MODES = new String[] {"Install", "Start"};
  public static int MODE_INSTALL  = 0; 
  public static int MODE_START    = 1; 

  private int startLevel;
  private int mode;
  private String src;
  
  public BundleLaunchInfo() {
  }
  
  public BundleLaunchInfo(String s) {
    StringTokenizer st = new StringTokenizer(s, ",");
    if (st.hasMoreTokens()) {
      startLevel = Integer.parseInt(st.nextToken());
    }
    if (st.hasMoreTokens()) {
      mode = Integer.parseInt(st.nextToken());
    }
    if (st.hasMoreTokens()) {
      src = st.nextToken();
    }
  }
  
  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    this.mode = mode;
  }

  public int getStartLevel() {
    return startLevel;
  }

  public void setStartLevel(int startLevel) {
    this.startLevel = startLevel;
  }

  public String getSource() {
    return src;
  }

  public void setSource(String src) {
    this.src = src;
  }

  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(startLevel);
    buf.append(",");
    buf.append(mode);
    if (src != null) {
      buf.append(",");
      buf.append(src);
    }
    
    return buf.toString();
  }
}
