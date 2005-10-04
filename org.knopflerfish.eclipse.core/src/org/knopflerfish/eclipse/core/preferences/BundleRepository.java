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

package org.knopflerfish.eclipse.core.preferences;

import org.osgi.service.prefs.Preferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleRepository {

  // Preferences keys
  protected static final String PREF_NAME             = "Name";
  private static final String PREF_TYPE               = "Type";
  private static final String PREF_CONFIG             = "Config";
  private static final String PREF_ACTIVE             = "Active";
  
  private String name;
  private String type;
  private String config;
  private boolean active;
  
  public BundleRepository() {
  }
  
  public BundleRepository(Preferences node) {
    load(node);
  }
  
  private void load(Preferences node) {
    // Name
    name = node.get(PREF_NAME, "");
    
    // Type
    type = node.get(PREF_TYPE, "");
    
    // Config
    config = node.get(PREF_CONFIG, "");
    
    // Active 
    active = "true".equalsIgnoreCase(node.get(PREF_ACTIVE, "false"));
  }

  protected void save(Preferences node) {
    
    // Name
    if (name != null) {
      node.put(PREF_NAME, name);
    } else {
      node.remove(PREF_NAME);
    }

    // Type
    if (type != null) {
      node.put(PREF_TYPE, type);
    } else {
      node.remove(PREF_TYPE);
    }

    // Config
    if (config != null) {
      node.put(PREF_CONFIG, config);
    } else {
      node.remove(PREF_CONFIG);
    }
    
    // Active 
    node.putBoolean(PREF_ACTIVE, active);
    
  }
  
  /****************************************************************************
   * Getters and Setters
   ***************************************************************************/
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  public String getConfig() {
    return config;
  }
  
  public void setConfig(String config) {
    this.config = config;
  }
  
  public boolean isActive() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }

  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BundleRepository)) {
      return false;
    }
    BundleRepository repository = (BundleRepository) o;
    
    if (name == null) {
      return false;
    }
    
    return name.equals(repository.getName());
  }
}
