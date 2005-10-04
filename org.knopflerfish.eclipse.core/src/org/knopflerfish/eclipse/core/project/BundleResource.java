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

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleResource implements Comparable {
  public static final int TYPE_CLASSES    = 0;
  public static final int TYPE_CLASSPATH  = 1;
  public static final int TYPE_USER       = 2;
  
  
  // Attributes
  private static String TAG_RESOURCE = "resource";
  private static String ATTR_TYPE    = "type";
  private static String ATTR_SRC     = "src";
  private static String ATTR_DST     = "dst";
  private static String ATTR_PATTERN = "pattern";
  
  private IPath src;
  private String dst;
  private Pattern pattern;
  private int type = TYPE_USER;
  
  public BundleResource() {
  }
  
  public BundleResource(int type, IPath src, String dst, Pattern pattern) {
    this.type = type;
    this.src = src;
    this.dst = dst;
    this.pattern = pattern;
  }

  public BundleResource(IProject project, Node node) throws IOException {
    //this.project = project;
    
    NamedNodeMap attributes = node.getAttributes();
    
    // Source attribute
    Node n = attributes.getNamedItem(ATTR_SRC);
    if (n == null) throw new IOException("Source not specified");
    String value = n.getNodeValue();
    src = new Path(value);
   
    // Type attribute
    n = attributes.getNamedItem(ATTR_TYPE);
    if (n != null) {
      try {
        type = Integer.parseInt(n.getNodeValue());
      } catch (Throwable t) {}
    }
    
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
    
    elem.setAttribute(ATTR_TYPE, Integer.toString(type));
    
    if (getDestination() != null) {
      elem.setAttribute(ATTR_DST, getDestination());
    }
    
    if (getPattern() != null) {
      elem.setAttribute(ATTR_PATTERN, getPattern().pattern());
    }
    
    return elem;
  }
  
  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
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

  /*
   * 
   */
  public int compareTo(Object o) {
    if (!(o instanceof BundleResource)) return 0;
    
    BundleResource resource = (BundleResource) o;
    
    // Check type, therafter source
    if (type == resource.getType()) {
      return src.toString().compareTo(resource.getSource().toString());
    }
    return type-resource.getType();
  }
}
