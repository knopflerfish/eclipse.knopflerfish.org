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

package org.knopflerfish.eclipse.core.project;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BundleJarResource {
  
  // Attributes
  private static String TAG_RESOURCE = "resource";
  private static String ATTR_SRC     = "src";
  private static String ATTR_DST     = "dst";
  private static String ATTR_PATTERN = "pattern";
  
  private IPath src;
  private String dst;
  private Pattern pattern;
  
  public BundleJarResource() {
  }
  
  public BundleJarResource(IPath src, String dst, Pattern pattern) {
    this.src = src;
    this.dst = dst;
    this.pattern = pattern;
  }

  public BundleJarResource(IProject project, Node node) throws IOException {
    //this.project = project;
    
    NamedNodeMap attributes = node.getAttributes();
    
    // Source attribute
    Node n = attributes.getNamedItem(ATTR_SRC);
    if (n == null) throw new IOException("Source not specified");
    String value = n.getNodeValue();
    src = new Path(value);
   
    // Destination attribute
    n = attributes.getNamedItem(ATTR_DST);
    if (n != null) {
      dst = n.getNodeValue();
    }

    // Pattern attribute
    n = attributes.getNamedItem(ATTR_PATTERN);
    if (n != null) {
      String p = n.getNodeValue();
      pattern = Pattern.compile(p);
    }
  }
  
  public Element createElement(Document doc) {
    Element elem = doc.createElement(TAG_RESOURCE);
    if (getSource() != null) {
      elem.setAttribute(ATTR_SRC, getSource().toString());
    }
    
    if (getDestination() != null) {
      elem.setAttribute(ATTR_DST, getDestination());
    }
    
    if (getPattern() != null) {
      elem.setAttribute(ATTR_PATTERN, getPattern().pattern());
    }
    
    return elem;
  }
  
  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }
  
  public IPath getSource() {
    return src;
  }

  public void setSource(IPath src) {
    this.src = src;
  }
  
  public String getDestination() {
    return dst;
  }

  public void setDestination(String dst) {
    this.dst = dst;
  }
}
