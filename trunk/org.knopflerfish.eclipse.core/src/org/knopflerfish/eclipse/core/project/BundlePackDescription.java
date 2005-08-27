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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundlePackDescription {
  
  private static final String SEPARATOR = "/";
  
  // Tags
  private static String TAG_BUNDLEJAR = "bundlejar";
  private static String TAG_RESOURCE = "resource";
  
  private TreeSet resources = new TreeSet();
  private final IProject project;

  public BundlePackDescription(IProject project) {
    this.project = project;
  }
  
  public BundlePackDescription(IProject project, InputStream is) throws IOException, ParserConfigurationException, SAXException {
    this.project = project;
    load(project, is);
  }
  
  public BundleResource[] getResources() {
    return (BundleResource[]) resources.toArray(new BundleResource[resources.size()]);
  }
  
  public boolean addResource(BundleResource resource) {
    return resources.add(resource);
  }
  
  public boolean removeResource(BundleResource resource) {
    return resources.remove(resource);
  }
  
  public void removeResource(int type) {
    for(Iterator i=resources.iterator();i.hasNext();) {
      BundleResource resource = (BundleResource) i.next();
      if (resource.getType() == type) {
        i.remove();
      }
    }
  }

  public void removeResource(IPath src) {
    if (src == null)  return;
    for(Iterator i=resources.iterator();i.hasNext();) {
      BundleResource resource = (BundleResource) i.next();
      if (src.equals(resource.getSource())) {
        i.remove();
      }
    }
  }
  
  public void updateResource(BundleResource resource) {
    if (resources.contains(resource)) { 
      resources.remove(resource);
    }
    resources.add(resource);
  }
  
  private void load(IProject project, InputStream is) throws ParserConfigurationException, SAXException, IOException {
    // Parse xml file
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(is);
    
    // Read bundlejar tag
    NodeList list = doc.getElementsByTagName(TAG_BUNDLEJAR);
    if (list.getLength()== 0) return;
    
    Node bundleJarNode = list.item(0);
    
    // Resources
    resources.clear();
    NodeList resourceNodes = bundleJarNode.getChildNodes();
    for (int i=0; i<resourceNodes.getLength(); i++) {
      Node resourceNode = resourceNodes.item(i);
      if (resourceNode.getNodeType() != Node.ELEMENT_NODE) continue;
      
      if (TAG_RESOURCE.equals(resourceNode.getNodeName())) {
        try {
          BundleResource resource = new BundleResource(project, resourceNode);
          resources.add(resource);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public Element createElement(Document doc) {
    // Create bundlejar tag
    Element elem = doc.createElement(TAG_BUNDLEJAR);
    doc.appendChild(elem);
    
    // Create resource tags
    BundleResource[] resources = getResources();
    for (int i=0; i<resources.length; i++) {
      elem.appendChild(resources[i].createElement(doc));
    }   

    return elem;
  }
  
  public void save(OutputStream os) throws TransformerException, ParserConfigurationException {
    // Parse xml file
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    
    // Create bundlejar tag
    createElement(doc);
    
    // Create DOM source
    DOMSource source = new DOMSource();
    // Create xml node 
    source.setNode(doc);
    
    // Create result
    StreamResult result = new StreamResult(os);
    
    // Transform
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, result);
  }
 
  public Map getContentsMap(boolean invert) {
    // Add contents
    BundleResource[] resources = getResources();
    HashMap map = new HashMap();
    for(int i=0; i<resources.length; i++) {
      IPath src = resources[i].getSource();
      IResource resource = project.findMember(src.removeFirstSegments(1));
      if (resource == null) continue;
      if (resource.getType() == IResource.FILE) {
        try {
          addFileToMap(map, (IFile) resource, resources[i].getDestination(), resources[i].getPattern(), invert);
        } catch (Throwable t) {} 
      } else if(resource.getType() == IResource.FOLDER) {
        try {
          addDirToMap(map, (IFolder) resource, resources[i].getDestination(), resources[i].getPattern(), invert);
        } catch (Throwable t) {} 
      }
    }
    return map;
  }
  
  public File export(IBundleProject bundleProject, String path) throws IOException {
    JarOutputStream jos = null;
    InputStream is = null;
    File jarFile = null;

    try {
      // Create jar file
      jarFile = new File(path);
      if (jarFile.exists()) jarFile.delete();

      // Create manifest output stream
      BundleManifest manifest = new BundleManifest(bundleProject.getBundleManifest());
      // Put build attributes
      Date date = new Date();
      manifest.getMainAttributes().putValue(BundleManifest.BUILD_DATE, date.toString());

      jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);
      
      // Add contents
      Map contents = getContentsMap(true);
      for(Iterator i=contents.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        IPath src = (IPath) entry.getKey();
        IResource resource = project.findMember(src.removeFirstSegments(1));
        if (resource == null) {
          continue;
        } else if (resource.getType() == IResource.FILE) {
          try {
            File file = new File(resource.getRawLocation().toString());
            addFileToJar(jos, file, (String) entry.getValue(), null);
          } catch (Throwable t) {}
        }
      }
    } finally {
      if (is != null) {
        is.close();
      }
      if (jos != null) {
        jos.close();
      }
    }
    
    return jarFile;
    
  }
  
  private void addFileToJar(JarOutputStream jos, File file, String path, Pattern pattern) throws IOException { 
    if (file == null || !file.isFile()) return;
    
    // Check pattern
    if (pattern != null) {
      String name = file.getName();
      if (!pattern.matcher(name).matches()) return;
    }

    if (path == null || path.trim().length() == 0) {
      path = file.getName();
    }
    
    JarEntry entry = new JarEntry(path);
    jos.putNextEntry(entry);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      byte [] buf = new byte[2048];
      int numRead = 0;
      while ( (numRead = fis.read(buf)) != -1) {
        jos.write(buf, 0, numRead);
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
      //jos.closeEntry();
    }
  }

  private void addDirToMap(Map map, IFolder folder, String path, Pattern pattern, boolean invert) throws CoreException {
    if (folder == null) return;
    IResource[] resources = folder.members();
    
    if (resources  == null) return;
    if (path == null) path = "";
    if (path.length() > 0 && !path.endsWith(SEPARATOR)) {
      path += SEPARATOR;
    }
    
    StringBuffer buf = new StringBuffer(path);
    int pathLen = path.length();
    for (int i=0; i<resources.length; i++) {
      buf.setLength(pathLen);
      if (resources[i] instanceof IFolder) {
        // Skip subversion and CVS directories
        String folderName = resources[i].getName();
        if(!".svn".equalsIgnoreCase(folderName) && !"CVS".equalsIgnoreCase(folderName)) {
          buf.append(resources[i].getName());
          buf.append(SEPARATOR);
          addDirToMap(map, (IFolder) resources[i], buf.toString(), pattern, invert);
        }
      } else if (resources[i] instanceof IFile) {
        buf.append(resources[i].getName());
        addFileToMap(map, (IFile) resources[i], buf.toString(), pattern, invert);
      }
    }
  }

  private void addFileToMap(Map map, IFile file, String path, Pattern pattern, boolean invert) { 
    if (file == null) return;
    
    // Check pattern
    if (pattern != null) {
      String name = file.getName();
      if (!pattern.matcher(name).matches()) return;
    }

    if (path == null || path.trim().length() == 0) {
      path = file.getName();
    }
    
    if (path.startsWith(SEPARATOR)){
      path = path.substring(1);
    }
    if (invert) {
      map.put(file.getFullPath(), path);
    } else {
      map.put(path, file.getFullPath());
    }
  }
}
