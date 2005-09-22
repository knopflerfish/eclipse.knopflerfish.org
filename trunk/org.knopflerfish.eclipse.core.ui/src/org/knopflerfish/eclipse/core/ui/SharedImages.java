package org.knopflerfish.eclipse.core.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class SharedImages {

  public static final String IMG_OVR_WARNING  = "icons/ovr16/warning_co.gif";
  public static final String IMG_OVR_ERROR    = "icons/ovr16/error_co.gif";
  
  private HashMap images = new HashMap();
  
  public SharedImages() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMG_OVR_WARNING);
    if (id != null) {
      images.put(IMG_OVR_WARNING, id.createImage());
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMG_OVR_ERROR);
    if (id != null) {
      images.put(IMG_OVR_ERROR, id.createImage());
    }
  }
  
  public void dispose() {
    for(Iterator i=images.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      
      Image img = (Image) entry.getValue();
      img.dispose();
      i.remove();
    }
  }
  
  public Image getImage(String key) {
    return (Image) images.get(key);
  }
  
}
