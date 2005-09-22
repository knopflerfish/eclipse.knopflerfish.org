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

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiPreferences {

  // Preference nodes
  public static final String PREFERENCE_ROOT_NODE         = "org.knopflerfish.eclipse.core.osgi";
  public static final String PREFERENCE_FRAMEWORKS_NODE   = "frameworks";
  public static final String PREFERENCE_ENVIRONMENTS_NODE = "environments";
                        
  
  /****************************************************************************
   * Framework preferences methods
   ***************************************************************************/
  public static FrameworkDistribution[] getFrameworkDistributions() {

    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_FRAMEWORKS_NODE);
    ArrayList distributions = new ArrayList();
    
    try {
      String [] children = node.childrenNames();
      for (int i=0; i<children.length; i++) {
        distributions.add(new FrameworkDistribution(node.node(children[i])));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return (FrameworkDistribution[]) distributions.toArray(new FrameworkDistribution[distributions.size()]);
  }

  public static FrameworkDistribution getFrameworkDistribution(String name) {
    if (name == null || name.length() == 0) return null;
    
    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_FRAMEWORKS_NODE);
    try  {
      if (node.nodeExists(name)) {
        return new FrameworkDistribution(node.node(name));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static FrameworkDistribution getDefaultFrameworkDistribution() {
    FrameworkDistribution[] distributions = getFrameworkDistributions();
    for (int i=0; i<distributions.length; i++) {
      if (distributions[i].isDefaultDefinition()) {
        return distributions[i];
      }
    }
    return null;
  }

  public static void setFrameworkDistributions(FrameworkDistribution[] distributions) throws BackingStoreException {
    // Remove previous distributions
    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_FRAMEWORKS_NODE);
    String [] names = node.childrenNames();
    if (names != null) {
      for(int i=0; i<names.length; i++) {
        node.node(names[i]).removeNode();
      }
    }
    
    // Save framework distributions
    if (distributions == null) return;
    for(int i=0; i<distributions.length;i++) {
      distributions[i].save(node);
    }
    node.flush();
  }
  
  /****************************************************************************
   * Environment preferences methods
   ***************************************************************************/
  public static ExecutionEnvironment[] getExecutionEnvironments() {
    // Load all osgi install definitions from preferences
    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_ENVIRONMENTS_NODE);
    ArrayList environments = new ArrayList();
    
    try {
      String [] children = node.childrenNames();
      for (int i=0; i<children.length; i++) {
        environments.add(new ExecutionEnvironment(node.node(children[i])));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return (ExecutionEnvironment[]) environments.toArray(new ExecutionEnvironment[environments.size()]);
  }

  public static ExecutionEnvironment getExecutionEnvironment(String name) {
    if (name == null || name.length() == 0) return null;
    
    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_ENVIRONMENTS_NODE);
    try  {
      if (node.nodeExists(name)) {
        return new ExecutionEnvironment(node.node(name));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static ExecutionEnvironment getDefaultExecutionEnvironment() {
    ExecutionEnvironment[] environments = getExecutionEnvironments();
    for (int i=0; i<environments.length; i++) {
      if (environments[i].isDefaultEnvironment()) {
        return environments[i];
      }
    }
    return null;
  }

  public static void setExecutionEnvironment(ExecutionEnvironment[] environments) throws BackingStoreException {
    // Remove previous environments
    Preferences node = new InstanceScope().getNode(PREFERENCE_ROOT_NODE).node(PREFERENCE_ENVIRONMENTS_NODE);
    String [] names = node.childrenNames();
    if (names != null) {
      for(int i=0; i<names.length; i++) {
        node.node(names[i]).removeNode();
      }
    }
    
    // Save environments
    if (environments == null) return;
    for(int i=0; i<environments.length;i++) {
      environments[i].save(node);
    }
    node.flush();
  }
}
