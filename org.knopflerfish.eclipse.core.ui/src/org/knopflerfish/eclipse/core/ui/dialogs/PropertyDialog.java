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

package org.knopflerfish.eclipse.core.ui.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Anders Rimén
 */
public class PropertyDialog extends Dialog {

  private static final String ERROR     = "error";
  private static final String WARNING   = "warning";
  
  private static final int NUM_CHARS_WIDTH = 60;
  
  private HashMap map;  // Key, Property
  private final String title;
  
  // Widgets
  private HashMap textWidgets = new HashMap();
  private Composite   wErrorComposite;
  private Label       wErrorMsgLabel;
  private Label       wErrorImgLabel;
  
  // Class
  static public class Property {
    private final String key;
    private String label;
    private String value;
    
    public Property(String key) {
      this.key = key;
    }
    
    public String getKey() {
      return key;
    }
    
    public String getLabel() {
      return label == null ? key : label;
    }
    
    public void setLabel(String label) {
      this.label = label;
    }
    
    public String getValue() {
      return value;
    }
    
    public void setValue(String value) {
      this.value = value;
    }
    
    public boolean isValid(String value) {
      return true;
    }
  }
  
  public PropertyDialog(Shell parentShell, HashMap map, String title) {
    super(parentShell);
    
    this.map = map;
    this.title = title;
  }

  /****************************************************************************
   * org.eclipse.jface.window.Window Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    
    setValues(map);
    
    return c;
  }
  
  /****************************************************************************
   * org.eclipse.jface.dialogs.Dialog Methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    for(Iterator i=textWidgets.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();

      Property p = (Property) map.get(entry.getKey());
      if (p != null) {
        String value = ((Text) entry.getValue()).getText();
        if (value == null || value.length()==0) {
          p.setValue(null);
        } else {
          p.setValue(value);
        }
      }
    }
    
    // Set return code and close window
    setReturnCode(Window.OK);
    close();
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite)super.createDialogArea(parent);
    
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    composite.setLayout(layout);
    
    for(Iterator i=map.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      
      Property p = (Property) entry.getValue();

      Label wLabel = new Label(composite, SWT.LEFT);
      wLabel.setText(p.getLabel());
      Text wText = new Text(composite, SWT.BORDER);
      wText.setData(p);
      wText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          Text w = (Text) e.widget;
          Property p = (Property) w.getData();
          if (p.isValid(w.getText())) {
            w.setData(ERROR, null);
            w.setData(WARNING, null);
          } else {
            w.setData(ERROR, "Invalid value for "+p.getKey());
          }
          updateStatus(getDialogArea());
        }
      });
      
      GridData data = new GridData(GridData.FILL_HORIZONTAL);
      data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
      wText.setLayoutData(data);
      textWidgets.put(p.getKey(), wText);
    }
    
    // Error label
    wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wErrorComposite.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wErrorComposite.setLayoutData(gd);
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(gd);
    
    setMessage(null, null);

    return composite;
  }
    
  /****************************************************************************
   * Private Worker Methods
   ***************************************************************************/
  
  private void setValues(HashMap m) {
    for(Iterator i=map.entrySet().iterator();i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();

      Property p = (Property) entry.getValue();

      Text wText = (Text) textWidgets.get(p.getKey());
      if (wText != null) {
        if (p.getValue() == null) {
          wText.setText("");
        } else {
          wText.setText(p.getValue());
        }
      }
    }
  }
  
  public HashMap getValues() {
    return map;
  }
  
  private void updateStatus(Control c) {
    
    // Loop through widgets checking for errors
    String error = (String) getData(c, ERROR);
    if (error != null) {
      getButton(IDialogConstants.OK_ID).setEnabled(false);
      setMessage(
          error, 
          PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
      return;
    }
    
    // Loop through widgets checking for warnings
    String warning = (String) getData(c, WARNING);
    if (warning != null) {
      getButton(IDialogConstants.OK_ID).setEnabled(true);
      setMessage(
          warning,
          PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
      return;
    }

    // Everything is ok
    getButton(IDialogConstants.OK_ID).setEnabled(true);
    setMessage(null, null);
  }
  
  private void setMessage(String msg, Image img) {
    if (msg == null) {
      wErrorComposite.setVisible(false);
    } else {
      wErrorComposite.setVisible(true);
      wErrorImgLabel.setImage(img);
      wErrorMsgLabel.setText(msg);
    }
    
  }
  
  private Object getData(Control c, String key) {
    if (c == null || key == null) return null;
    
    // Check if this control contains the key
    Object data = c.getData(key);
    if (data != null) return data;
    
    if (c instanceof Composite) {
      // Check if any children contain the key
      Control [] children = ((Composite) c).getChildren();
      if (children != null) {
        for(int i=0; i<children.length; i++) {
          data = getData(children[i], key);
          if (data != null) return data;
        }
      }
    }
    return null;
  }
  
}
