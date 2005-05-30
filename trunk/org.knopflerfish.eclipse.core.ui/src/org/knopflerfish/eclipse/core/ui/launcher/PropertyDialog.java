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

package org.knopflerfish.eclipse.core.ui.launcher;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.SystemProperty;

/**
 * @author ar
 */
public class PropertyDialog extends Dialog {
  private final static int STATE_OK       = 0;
  private final static int STATE_ERROR    = 1;
  private final static int STATE_INFO     = 2;

  private static final int NUM_CHARS_WIDTH = 60;
  private static final int NUM_ROWS_DESCRIPTION = 5;
  
  private SystemProperty property;
  private final String title;
  
  // Widgets
  private Text    wNameText;
  private Combo   wValueCombo;
  private Text    wValueText;
  //private Text    wDescriptionText;
  private Button  wDefaultButton;
  private Label   wErrorMsgLabel;
  private Label   wErrorImgLabel;
  
  
  protected PropertyDialog(Shell parentShell, SystemProperty property, String title) {
    super(parentShell);
    
    this.property = property;
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
    
    setValues(property);
    
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
    if (property == null) {
      property = new SystemProperty(wNameText.getText());
      property.setGroup(MainTab.USER_GROUP);
    }
    
    // Value
    String value = null;
    if (wValueText != null) {
      value = wValueText.getText();
    } else if (wValueCombo != null){
      value = wValueCombo.getText();
    }
    property.setValue(value);
    
    // Description
    /*
    String description = null;
    if (wDescriptionText.getText().trim().length() > 0) {
      description = wDescriptionText.getText().trim();
    }
    property.setDescription(description);
    */
    
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
    layout.numColumns = 3;
    composite.setLayout(layout);
    
    // Property Name
    Label wNameLabel = new Label(composite, SWT.LEFT);
    wNameLabel.setText("Name:");
    wNameText = new Text(composite, SWT.BORDER);
    wNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyAll();
      }
    });
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wNameText.setLayoutData(data);
    
    // Property Value
    Label wValueLabel = new Label(composite, SWT.LEFT);
    wValueLabel.setText("Value:");
    List values = null;
    if (property != null) values = property.getAllowedValues();
    if (values != null && values.size() > 0) {
      wValueCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
      wValueCombo.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          updateDefaultButton();
        }        
      });
      for(int i=0; i<values.size(); i++) {
        wValueCombo.add((String) values.get(i));
      }
    } else {
      wValueText = new Text(composite, SWT.BORDER);
      wValueText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          updateDefaultButton();
        }
      });
    }
    
    data = new GridData(GridData.FILL_HORIZONTAL);
    if (property != null && property.getDefaultValue() != null) {
      wDefaultButton = new Button(composite, SWT.NONE);
      wDefaultButton.setText("Restore Default");
      wDefaultButton.addSelectionListener(new SelectionAdapter(){
        public void widgetSelected(SelectionEvent e) {
          if (property != null && property.getDefaultValue() != null) {
            if (wValueCombo != null) {
              wValueCombo.setText(property.getDefaultValue());
            } else {
              wValueText.setText(property.getDefaultValue());
            }
          }
          updateDefaultButton();
        }
      });
    } else {
      data.horizontalSpan = 2;
    }
    if (wValueCombo != null) {
      wValueCombo.setLayoutData(data);
    } else {
      wValueText.setLayoutData(data);
    }
    // Property Description
    /*
    Label wDescriptionLabel = new Label(composite, SWT.LEFT);
    wDescriptionLabel.setText("Description:");
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    wDescriptionLabel.setLayoutData(data);
    if (property != null && !MainTab.USER_GROUP.equals(property.getGroup())) {
      wDescriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    } else {
      wDescriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    }
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    data.heightHint = convertHeightInCharsToPixels(NUM_ROWS_DESCRIPTION);
    data.widthHint = convertWidthInCharsToPixels(NUM_CHARS_WIDTH);
    wDescriptionText.setLayoutData(data);
    */
    
    // Error label
    Composite wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wErrorComposite.setLayout(layout);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    wErrorComposite.setLayoutData(data);
    
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    data = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(data);

    return composite;
  }
    
  /****************************************************************************
   * Verify Methods
   ***************************************************************************/
  public boolean verifyAll() {

    if (!verifyName()) {
      return false;
    }
    
    /*
    if (!verifyValue()) {
      return false;
    }
    */
    
    return true;
  }
  
  public boolean verifyName() {
    String name = wNameText.getText();
    
    // Check if name is valid
    if (name == null || name.length() == 0) {
      setState("Enter a property name.", STATE_ERROR);
      return false;
    }
    
    // If this is a new property check that name is not already used.
    if (property == null) {
      
    }
    
    setState(null, STATE_OK);
    return true;
  }
  
  public boolean verifyValue() {
    String value = null;
    if (wValueText != null) {
      value = wValueText.getText();
    } else {
      value = wValueCombo.getText();
    }
    
    // Check if location is valid
    if (value == null || value.length() == 0) {
      setState("Enter a property value.", STATE_INFO);
      return false;
    }
    
    setState(null, STATE_OK);
    return true;
  }
  
  /****************************************************************************
   * Private Worker Methods
   ***************************************************************************/
  private void updateDefaultButton() {
    if (wDefaultButton == null) return;
    
    if (property != null && property.getDefaultValue() != null) {
      if (wValueText != null) {
        wDefaultButton.setEnabled(!property.getDefaultValue().equals(wValueText.getText()));
      } else {
        wDefaultButton.setEnabled(!property.getDefaultValue().equals(wValueCombo.getText()));
      }
    } else {
      wDefaultButton.setEnabled(false);
    }
  }
  
  private void setState(String msg, int state) {
    switch (state) {
      case STATE_OK:
        getButton(IDialogConstants.OK_ID).setEnabled(true);
        wErrorMsgLabel.setVisible(false);
        wErrorImgLabel.setVisible(false);
        break;
      case STATE_ERROR:
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        wErrorMsgLabel.setText(msg);
        wErrorMsgLabel.setVisible(true);
        wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
        wErrorImgLabel.setVisible(true);
        break;
      case STATE_INFO:
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        wErrorMsgLabel.setText(msg);
        wErrorMsgLabel.setVisible(true);
        wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
        wErrorImgLabel.setVisible(true);
        break;
    }
  }
  
  private void setValues(SystemProperty prop) {
    
    if (prop == null) {
      wNameText.setText("");
      wNameText.setEditable(true);
      //wDescriptionText.setEditable(true);
    } else {
      wNameText.setText(prop.getName());
      wNameText.setEditable(false);

      // Value
      String value = prop.getValue();
      if (value == null) {
        value = prop.getDefaultValue();
      }
      
      if (wValueCombo != null) {
        if (value != null) {
          wValueCombo.setText(value);
        } else {
          wValueCombo.select(0);
        }
      } else if (wValueText != null && value != null) {
        wValueText.setText(value);
      }

      // Description
      /*
      String description = prop.getDescription();
      if (description != null) {
        wDescriptionText.setText(description);
      }
      if (MainTab.USER_GROUP.equals(prop.getGroup())) {
        wDescriptionText.setEditable(true);
      } else {
        wDescriptionText.setEditable(false);
      }
      */
    }

    verifyAll();
    updateDefaultButton();
  }
  
  public SystemProperty getSystemProperty() {
    return property;
  }
}
