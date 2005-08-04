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

package org.knopflerfish.eclipse.core.ui.editors.manifest.form;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

abstract public class ManifestSectionTextPart extends SectionPart {

  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";
  
  // Widgets
  public Text [] wAttributesText = null;

  private Manifest manifest = null;


  public ManifestSectionTextPart(Composite parent, FormToolkit toolkit, int style, int numAttributes) {
    super(parent, toolkit, style);
    wAttributesText = new Text[numAttributes];
    
    Section section = getSection();
    createClient(section, toolkit);
  }

  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
   */
  public void commit(boolean onSave) {
    super.commit(onSave);
    
    // Flush values to document
    IManagedForm managedForm = getManagedForm();
    IDocument doc = (IDocument) managedForm.getInput();

    for(int i=0; wAttributesText != null && i<wAttributesText.length; i++) {
      if (wAttributesText[i] == null) continue;
      
      Boolean dirty = (Boolean) wAttributesText[i].getData(PROP_DIRTY);
      if (dirty != null && dirty.booleanValue()) {
        try {
          String attribute = (String) wAttributesText[i].getData(PROP_NAME);
          if (attribute != null) {
            ManifestUtil.setManifestAttribute(doc, attribute, wAttributesText[i].getText());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        wAttributesText[i].setData(PROP_DIRTY, new Boolean(false));
      }
    }
    
    // Update manifest from document
    //ManifestUtil.createManifest(doc);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();

    // Refresh values from document
    manifest = ManifestUtil.createManifest((IDocument) getManagedForm().getInput());
    updateValues();
  }
  
  /****************************************************************************
   * Abstract methods that shall be overridden.
   ***************************************************************************/
  
  abstract public void createClient(Section section, FormToolkit toolkit); 

  /****************************************************************************
   * Public utility methods
   ***************************************************************************/

  public void textChanged(Text w) {
    // Check if dirty
    String newValue = w.getText();
    Boolean dirty = new Boolean(true);
    if (manifest != null) {
      Attributes attributes = manifest.getMainAttributes();
      String attribute = (String) w.getData(PROP_NAME);
      if (attribute != null) {
        String oldValue = attributes.getValue((String) w.getData(PROP_NAME));
        if (oldValue == null) oldValue = "";
        if (newValue.equals(oldValue)) {
          dirty = new Boolean(false);
        }
      } else {
        dirty = new Boolean(false);
      }
    }
    w.setData(PROP_DIRTY, dirty);
    updateDirtyState();
  }

  public void updateValues() {
    if (manifest == null) return;
    
    Attributes attributes = manifest.getMainAttributes();
    
    for (int i = 0; wAttributesText != null && i<wAttributesText.length ; i++) {
      if (wAttributesText[i] == null) continue;
      
      String attribute = (String) wAttributesText[i].getData(PROP_NAME);
      if (attribute != null) {
        String value = attributes.getValue(attribute);
        if (value != null) {
          wAttributesText[i].setText(value);
        } else {
          wAttributesText[i].setText("");
        }
      }
    }
  }

  public void updateDirtyState() {
    // Loop through components and check dirty state
    boolean dirty = false;
    for(int i=0; wAttributesText != null && i<wAttributesText.length; i++) {
      if (wAttributesText[i] == null) continue;
      
      Boolean attribute = (Boolean) wAttributesText[i].getData(PROP_DIRTY);
      if (attribute != null) {
        dirty = dirty || attribute.booleanValue();
      }
    }    
    
    if (dirty) {
      markDirty();
    }
  }

}
