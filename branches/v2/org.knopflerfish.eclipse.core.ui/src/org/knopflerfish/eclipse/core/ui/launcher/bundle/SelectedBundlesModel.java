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

package org.knopflerfish.eclipse.core.ui.launcher.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class SelectedBundlesModel {
  
  private Map bundles = new HashMap();
  private Map bundleProjects = new HashMap();
  private ArrayList elements = new ArrayList();
  
  public void clear() {
    bundles.clear();
    bundleProjects.clear();
    elements.clear();
  }

  public void addBundles(Map m) {
    if (m == null) return;

    for(Iterator i=m.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry entry = (Map.Entry) i.next();
        String path = (String) entry.getKey();
        OsgiBundle bundle = new OsgiBundle(new File(path));
        BundleLaunchInfo info = new BundleLaunchInfo((String) entry.getValue());
        SelectedBundleElement element = new SelectedBundleElement(bundle, info);
        elements.add(element);
        bundles.put(entry.getKey(), entry.getValue());
      } catch (Exception e) {
        // Something went wrong, skip this element
      }
    }
  }
  
  public Map getBundles() {
    return bundles;
  }
  
  public void addBundleProjects(Map m) {
    if (m == null) return;

    for(Iterator i=m.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry entry = (Map.Entry) i.next();
        String name = (String) entry.getKey();
        BundleLaunchInfo info = new BundleLaunchInfo((String) entry.getValue());
        SelectedBundleElement element = new SelectedBundleElement(new BundleProject(name), info);
        elements.add(element);
        bundleProjects.put(entry.getKey(), entry.getValue());
      } catch (Exception e) {
        // Something went wrong, skip this element
      }
    }
  }
  
  public Map getBundleProjects() {
    return bundleProjects;
  }
  
  public boolean contains(SelectedBundleElement element) {
    return bundles.containsKey(element.getPath()) || bundleProjects.containsKey(element.getPath());
  }
  
  public void add(TableViewer viewer, SelectedBundleElement element) {
    if (element == null) return;
    
    // Check if element already exists
    if (contains(element)) return;
    
    // Add element to model
    if (element.getType() == SelectedBundleElement.TYPE_BUNDLE) {
      bundles.put(element.getPath(), element.getLaunchInfo().toString());
    } else if (element.getType() == SelectedBundleElement.TYPE_BUNDLE_PROJECT) {
      bundleProjects.put(element.getPath(), element.getLaunchInfo().toString());
    }

    // Add element to viewer
    viewer.add(element);
    elements.add(element);
  }

  public void remove(TableViewer viewer, SelectedBundleElement element) {
    if (element == null) return;
    
    // Check if element already exists
    if (!contains(element)) return;
    
    // Remove element from model
    if (element.getType() == SelectedBundleElement.TYPE_BUNDLE) {
      bundles.remove(element.getPath());
    } else if (element.getType() == SelectedBundleElement.TYPE_BUNDLE_PROJECT) {
      bundleProjects.remove(element.getPath());
    }
    
    // Remove element from viewer
    viewer.remove(element);
    elements.remove(element);
  }

  public SelectedBundleElement [] getElements() {
    return (SelectedBundleElement[]) elements.toArray(new SelectedBundleElement[elements.size()]);
  }
  
  public void update(TableViewer viewer, SelectedBundleElement element, String prop) {
    if (element == null) return;
    
    // Check if element already exists
    if (!contains(element)) return;
    
    // Update element in model
    if (element.getType() == SelectedBundleElement.TYPE_BUNDLE) {
      bundles.put(element.getPath(), element.getLaunchInfo().toString());
    } else if (element.getType() == SelectedBundleElement.TYPE_BUNDLE_PROJECT) {
      bundleProjects.put(element.getPath(), element.getLaunchInfo().toString());
    }
    
    // Update element in viewer
    viewer.refresh(element);
    //viewer.update(element, new String[] {prop});
    
    UiUtils.packTableColumns(viewer.getTable());
  }
}
