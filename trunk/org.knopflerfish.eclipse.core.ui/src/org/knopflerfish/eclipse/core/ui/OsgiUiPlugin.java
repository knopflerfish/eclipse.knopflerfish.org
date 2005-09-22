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

package org.knopflerfish.eclipse.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OsgiUiPlugin extends AbstractUIPlugin {
  private static final String IMAGE_BUNDLE_WIZ = "icons/wizban/bundle_wiz.gif";
  
  //The shared instance.
  private static OsgiUiPlugin plugin;
  //Resource bundle.
  private ResourceBundle resourceBundle;
  
  // Image descriptors
  public static ImageDescriptor BUNDLE_WIZARD_BANNER = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_WIZ);
  
  private SharedImages sharedImages;
  
  
  /**
   * The constructor.
   */
  public OsgiUiPlugin() {
    super();
    plugin = this;
    try {
      resourceBundle = ResourceBundle.getBundle("org.knopflerfish.eclipse.core.ui.UiPluginResources");
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }
  
  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception {
    sharedImages = new SharedImages();

    super.start(context);
  }
  
  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception {
    sharedImages.dispose();

    super.stop(context);
  }
  
  /**
   * Returns the shared instance.
   */
  public static OsgiUiPlugin getDefault() {
    return plugin;
  }
  
  /**
   * Returns the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = OsgiUiPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }
  
  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }
  
  public static SharedImages getSharedImages() {
    
    return getDefault().sharedImages;
  }
}
