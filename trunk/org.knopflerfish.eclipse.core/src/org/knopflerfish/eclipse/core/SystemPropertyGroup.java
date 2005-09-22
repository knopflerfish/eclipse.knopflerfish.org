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

package org.knopflerfish.eclipse.core;

import java.util.ArrayList;

import org.knopflerfish.eclipse.core.preferences.FrameworkDistribution;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SystemPropertyGroup {

  private final String name;
  private FrameworkDistribution distribution;
  private ArrayList properties = new ArrayList();
  
  public SystemPropertyGroup(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public FrameworkDistribution getFrameworkDistribution() {
    return distribution;
  }

  public void setFrameworkDistribution(FrameworkDistribution distribution) {
    this.distribution = distribution;
  }
  
  public SystemProperty[] getProperties() {
    return (SystemProperty[]) properties.toArray(new SystemProperty[properties.size()]);
  }

  public boolean contains(SystemProperty property) {
    return properties.contains(property);
  }
  
  public void clear() {
    properties.clear();
  }
  public SystemProperty findSystemProperty(String name) {
    int idx = properties.indexOf(new SystemProperty(name));
    if (idx != -1) {
      return (SystemProperty) properties.get(idx);
    } else {
      return null;
    }
  }

  public void addSystemProperty(SystemProperty property) {
    if (property != null && !properties.contains(property)) {
      properties.add(property);
      property.setSystemPropertyGroup(this);
    }
  }

  public void removeSystemProperty(SystemProperty property) {
    if (property != null && properties.contains(property)) {
      properties.remove(property);
      property.setSystemPropertyGroup(null);
    }
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof SystemPropertyGroup)) return false;
    
    return ((SystemPropertyGroup) o).getName().equals(getName());
  }
}
