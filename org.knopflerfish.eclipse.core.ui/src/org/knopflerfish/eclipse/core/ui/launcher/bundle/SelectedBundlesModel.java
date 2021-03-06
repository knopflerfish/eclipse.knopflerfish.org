/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
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
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.Util;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rim�n, Makewave
 * @see http://www.makewave.com/
 */
public class SelectedBundlesModel {
  
  private Map<String, String> bundles = new HashMap<String, String>();
  private Map<String, String> bundleProjects = new HashMap<String, String>();
  private List<SelectedBundleElement> elements = new ArrayList<SelectedBundleElement>();
  
  public void clear() {
    bundles.clear();
    bundleProjects.clear();
    elements.clear();
  }

  public boolean addBundles(Map<String, String>m, final AvailableElementRoot availableElementRoot, TableViewer viewer) {
    if (m == null) return false;
    
    boolean changed = false;
    for(Iterator<Map.Entry<String, String>> i=m.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry<String, String> entry = i.next();
        String path = entry.getKey();
        // Check if path exists, otherwise try paths from repositories
        BundleLaunchInfo info = new BundleLaunchInfo(entry.getValue());
        IOsgiBundle bundle = null;
        File f = new File(path);
        if (f.exists() && f.isFile()) {
          bundle = new OsgiBundle(f);
        } else {
          changed = true;
          if (availableElementRoot != null) {
            AvailableElementBundle element = availableElementRoot.findBundle(Util.getFileName(path));
            if (element != null) {
              bundle = element.getBundle();
              info.setSource(bundle.getSource());
            }
          }
        }
        if (bundle != null) {
          SelectedBundleElement element = new SelectedBundleElement(bundle, info);
          add(viewer, element);
          //elements.add(element);
          //bundles.put(entry.getKey(), entry.getValue());
        }
      } catch (Exception e) {
        // Something went wrong, skip this element
      }
    }
    return changed;
  }
  
  public Map<String, String> getBundles() {
    return bundles;
  }
  
  public void addBundleProjects(Map<String, String> m) {
    if (m == null) return;

    for(Iterator<Map.Entry<String, String>> i=m.entrySet().iterator();i.hasNext();) {
      try {
        Map.Entry<String, String> entry = i.next();
        String name = entry.getKey();
        BundleLaunchInfo info = new BundleLaunchInfo(entry.getValue());
        SelectedBundleElement element = new SelectedBundleElement(new BundleProject(name), info);
        elements.add(element);
        bundleProjects.put(entry.getKey(), entry.getValue());
      } catch (Exception e) {
        // Something went wrong, skip this element
      }
    }
  }
  
  public Map<String, String> getBundleProjects() {
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
  
  public void removeAll(TableViewer viewer) {
    for (Iterator<SelectedBundleElement> i=elements.iterator(); i.hasNext();) {
      SelectedBundleElement element = i.next();
    
      // Remove element from model
      if (element.getType() == SelectedBundleElement.TYPE_BUNDLE) {
        bundles.remove(element.getPath());
      } else if (element.getType() == SelectedBundleElement.TYPE_BUNDLE_PROJECT) {
        bundleProjects.remove(element.getPath());
      }
    
      // Remove element from viewer
      viewer.remove(element);
      i.remove();
    }
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
