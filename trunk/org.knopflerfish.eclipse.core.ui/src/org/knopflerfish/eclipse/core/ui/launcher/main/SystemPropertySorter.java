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

package org.knopflerfish.eclipse.core.ui.launcher.main;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;
import org.knopflerfish.eclipse.core.preferences.FrameworkDistribution;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SystemPropertySorter extends ViewerSorter {
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public int compare(Viewer viewer, Object o1, Object o2) {
    int c1 = category(o1);
    int c2 = category(o2);
    if (c1 != c2) return c1-c2;
    
    String n1 = null;
    String n2 = null;
    if (o1 instanceof SystemPropertyGroup) {
      n1 = ((SystemPropertyGroup) o1).getName();
      n2 = ((SystemPropertyGroup) o2).getName();
    } else if (o1 instanceof SystemProperty) {
      n1 = ((SystemProperty) o1).getName();
      n2 = ((SystemProperty) o2).getName();
    }
    if (n1 == null) n1 = "";
    if (n2 == null) n2 = "";
    return n1.toLowerCase().compareTo(n2.toLowerCase());
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
   */
  public int category(Object o) {
    if (o instanceof FrameworkDistribution) {
      return 0;
    } else if (o instanceof SystemPropertyGroup) {
      // Check if user defined group
      SystemPropertyGroup group = (SystemPropertyGroup) o;
      if (MainTab.USER_GROUP.equals(group.getName())) {
        return 11;
      } else {
        return 10;
      }
    } else if (o instanceof SystemProperty) {
      return 100;
    } else {
      return -1;
    }
  }
}
