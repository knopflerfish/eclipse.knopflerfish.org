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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;

/**
 * Knopflerfish implementation of IOsgiConfiguration
 */
public class OsgiConfiguration implements IOsgiConfiguration {

  private static final int DEFAULT_STARTLEVEL = 7;
  
  private static String PROPERTY_FRAMEWORK_DIR = "org.osgi.framework.dir";
  
  private final File instanceDir;
  private final Map attributes;
  private final TreeMap bundles = new TreeMap(); // Startlevel (Integer), List of IOsgiBundle

  
  public OsgiConfiguration(File dir, Map attributes) {
    this.instanceDir = dir;
    this.attributes = attributes;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiConfiguration#create()
   */
  public String[] create() throws IOException {
    ArrayList args = new ArrayList();
    
    // Create xargs file
    File initFile = new File(instanceDir, "init.xargs");
    File restartFile = new File(instanceDir, "restart.xargs");
    
    // Start empty framework
    if (attributes.containsKey(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_INIT) && 
        ((Boolean) attributes.get(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTANCE_INIT)).booleanValue()) {
      args.add("-init");
    }
    
    // Set framework dir
    writeProperty(initFile, PROPERTY_FRAMEWORK_DIR, instanceDir.getAbsolutePath()+"/fwdir", false);
    writeProperty(restartFile, PROPERTY_FRAMEWORK_DIR, instanceDir.getAbsolutePath()+"/fwdir", false);
    
    // System properties
    Map properties = (Map) attributes.get(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES);
    if (properties != null) {
      for(Iterator i=properties.entrySet().iterator();i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        writeProperty(initFile, (String) entry.getKey(), (String) entry.getValue(), true);
        writeProperty(restartFile, (String) entry.getKey(), (String) entry.getValue(), true);
      }
    }

    writeCommand(initFile, "-init", "", true);
    
    // Add install entries
    int currentLevel = -1;
    for (Iterator i=bundles.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      Integer initLevel = (Integer) entry.getKey();
      
      // Set initial start level
      if (currentLevel != initLevel.intValue()) {
        writeCommand(initFile, "-initlevel", initLevel.toString(), true);
        currentLevel = initLevel.intValue();
      }
      
      // Add bundle install entries for this start level
      ArrayList l = (ArrayList) entry.getValue();
      for (Iterator j = l.iterator() ; j.hasNext() ;) {
        BundleElement e = (BundleElement) j.next();
        writeCommand(initFile, "-install", "file:"+e.getBundle().getPath(), true);
      }
    }
    // Set start level and launch
    writeCommand(initFile, "-startlevel", Integer.toString(DEFAULT_STARTLEVEL), true);
    writeCommand(initFile, "-launch", "", true);
    
    
    // Add start entries
    for (Iterator i=bundles.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      
      // Add bundle install entries for this start level
      ArrayList l = (ArrayList) entry.getValue();
      for (Iterator j = l.iterator() ; j.hasNext() ;) {
        BundleElement e = (BundleElement) j.next();
        if (e.getLaunchInfo().getMode() == BundleLaunchInfo.MODE_START) {
          writeCommand(initFile, "-start", "file:"+e.getBundle().getPath(), true);
        }
      }
    }
    
    return (String[]) args.toArray(new String[args.size()]);
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiConfiguration#addBundle(org.gstproject.eclipse.osgi.IOsgiBundle, java.lang.Integer)
   */
  public void addBundle(IOsgiBundle bundle, BundleLaunchInfo info) {
    Integer startLevel = new Integer(info.getStartLevel());
    ArrayList l = (ArrayList) bundles.get(startLevel);
    if (l == null) {
      l = new ArrayList();
    }
    l.add(new BundleElement(bundle, info));
    bundles.put(startLevel, l);
  }

  private void writeCommand(File f, String cmd, String value, boolean append) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      buf.append(cmd);
      buf.append(" ");
      buf.append(value);
      buf.append("\n");
      
      writer.write(buf.toString());
      
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }
  
  private void writeProperty(File f, String name, String value, boolean append) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(f, append);
      StringBuffer buf = new StringBuffer();
      buf.append("-D");
      buf.append(name);
      buf.append("=");
      buf.append(value);
      buf.append("\n");
      
      writer.write(buf.toString());
      
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  
  class BundleElement {
    private final IOsgiBundle bundle;
    private final BundleLaunchInfo launchInfo;
    
    BundleElement(IOsgiBundle bundle, BundleLaunchInfo info) {
      this.bundle = bundle;
      this.launchInfo = info;
    }
    
    public IOsgiBundle getBundle() {
      return bundle;
    }

    public BundleLaunchInfo getLaunchInfo() {
      return launchInfo;
    }
  }
}
