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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.project.BundleManifest;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class VendorSection extends ManifestSectionTextPart {

  // Section title and description
  private static final String TITLE = "Vendor Information";
  private static final String DESCRIPTION = "This section describes vendor information for this bundle.";

  // Initialize attributes, use array to keep order of attributes
  static private String[][] widgetAttributes = new String[][] {
    new String[] {BundleManifest.BUNDLE_VENDOR,    "Vendor Name:",
        "A text description of the vendor."},  
    new String[] {BundleManifest.BUNDLE_CONTACT,   "Contact Address:",
        "Contact address if it is necessary to contact the vendor."},  
    new String[] {BundleManifest.BUNDLE_COPYRIGHT, "Copyright:",
        "Copyright specification for this bundle."}  
  };
  
  public VendorSection(Composite parent, FormToolkit toolkit, int style) {
    super(parent, toolkit, style, widgetAttributes.length);
    
    Section section = getSection();
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }

  /****************************************************************************
   * org.gstproject.eclipse.osgi.ui.editors.ManifestSectionPart methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.ui.editors.ManifestSectionPart#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public void createClient(Section section, FormToolkit toolkit) {

    // Set section layout
    ColumnLayoutData data = new ColumnLayoutData();
    section.setLayoutData(data);
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        getManagedForm().getForm().reflow(true);
      }
    });

    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    container.setLayout(layout);
    
    // Create widgets for attributes
    for (int i = 0 ; i<widgetAttributes.length; i++) {
      String attr = widgetAttributes[i][0];
      String label = widgetAttributes[i][1];
      String tooltip = widgetAttributes[i][2];
      
      Label wLabel = toolkit.createLabel(container, label);
      wLabel.setToolTipText(tooltip);
      wAttributeControls[i] = toolkit.createText(container, "");
      wAttributeControls[i].setData(PROP_DIRTY, new Boolean(false));
      wAttributeControls[i].setData(PROP_NAME, attr);
      ((Text) wAttributeControls[i]).addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          textChanged((Text) e.widget);
        }
      });
      TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
      wAttributeControls[i].setLayoutData(td);
    }
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
    
    updateValues();
  }

}
