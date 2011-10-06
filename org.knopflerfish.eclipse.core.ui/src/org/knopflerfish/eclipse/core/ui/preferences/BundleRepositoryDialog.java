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

package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.IBundleRepositoryType;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.preferences.RepositoryPreference;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class BundleRepositoryDialog extends Dialog {

  private final static String TITLE_ADD = "Add bundle repository";
  private final static String TITLE_EDIT = "Edit bundle repository";

  private final static int NUM_CHARS_NAME = 60;

  private final static int STATE_OK       = 0;
  private final static int STATE_ERROR    = 1;
  private final static int STATE_INFO     = 2;
  
  // Widgets
  private Text    wNameText;
  Combo   wTypeCombo;
  private Combo    wConfigCombo;
  Label   wConfigLabel;

  private Label   wErrorMsgLabel;
  private Label   wErrorImgLabel;
  
  private List<String> usedNames;
  private RepositoryPreference repository;
  private Map<String, IBundleRepositoryType> repositories = new TreeMap<String, IBundleRepositoryType>();
  
  protected BundleRepositoryDialog(Shell parentShell, List<String> usedNames, RepositoryPreference repository) {
    super(parentShell);
  
    this.usedNames = usedNames;
    this.repository = repository;
    if (repository != null && usedNames != null) {
      usedNames.remove(repository.getName());
    }
    String[] names = Osgi.getBundleRepositoryTypeNames();
    for (int i=0;i<names.length; i++) {
      repositories.put(names[i], Osgi.getBundleRepositoryType(names[i]));
    }
  }

  public RepositoryPreference getBundleRespository() {
    return repository;
  }
  
  //***************************************************************************
  // org.eclipse.jface.window.Window Methods
  //***************************************************************************
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#close()
   */
  public boolean close() {
    boolean closed = super.close();
    
    return closed;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (repository != null) {
      newShell.setText(TITLE_EDIT);
    } else {
      newShell.setText(TITLE_ADD);
    }
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    
    setValues(repository);
    
    return c;
  }

  //***************************************************************************
  // org.eclipse.jface.dialogs.Dialog Methods
  //***************************************************************************
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    if (repository == null) {
      // Create bundle repository
      repository = new RepositoryPreference();
    }

    repository.setName(wNameText.getText());
    repository.setType(wTypeCombo.getText());
    repository.setConfig(wConfigCombo.getText());
    
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
    
    // Name
    Label wNameLabel = new Label(composite, SWT.LEFT);
    wNameLabel.setText("Repository name:");
    wNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    wNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyAll();
      }
    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = convertWidthInCharsToPixels(NUM_CHARS_NAME);
    wNameText.setLayoutData(gd);
    
    // Type
    Label wTypeLabel = new Label(composite, SWT.LEFT);
    wTypeLabel.setText("Repository type:");
    wTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
    wTypeCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // Update description
        updateConfigDescription();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wTypeCombo.setLayoutData(gd);
    
    // Config
    Group wConfigGroup = new Group(composite, SWT.NONE);
    wConfigGroup.setText("Repository configuration");
    layout = new GridLayout();
    layout.numColumns = 1;
    wConfigGroup.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wConfigGroup.setLayoutData(gd);
    
    wConfigLabel = new Label(wConfigGroup, SWT.LEFT);
    wConfigCombo = new Combo(wConfigGroup, SWT.DROP_DOWN);
    wConfigCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyAll();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = convertWidthInCharsToPixels(NUM_CHARS_NAME);
    wConfigCombo.setLayoutData(gd);

    // Error label
    Composite wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wErrorComposite.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wErrorComposite.setLayoutData(gd);
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(gd);

    return composite;
  }

  //***************************************************************************
  // Verify Methods
  //***************************************************************************
  public boolean verifyAll() {

    if (!verifyType()) {
      return false;
    }
    
    if (!verifyName()) {
      return false;
    }
    
    if (!verifyConfig()) {
      return false;
    }
    
    return true;
  }
  
  public boolean verifyType() {
    if (wTypeCombo.getItemCount() == 0) {
      setState("No repository type extensions are registered.", STATE_ERROR);
      return false;
    }
    
    return true;
  }
  
  public boolean verifyName() {
    String name = wNameText.getText();
    
    // Check if name is valid
    if (name == null || name.length() == 0) {
      setState("Enter a name for the repository.", STATE_INFO);
      return false;
    }
    
    // Check that name is not already used
    if (usedNames.contains(name)) {
      setState("Repository name is already used.", STATE_ERROR);
      return false;
    }
    
    setState(null, STATE_OK);
    return true;
  }
  
  public boolean verifyConfig() {
    String config = wConfigCombo.getText();

    // Check if location is a valid directory for this definition
    String type = wTypeCombo.getText();
    IBundleRepositoryType repository = (IBundleRepositoryType) repositories.get(type);
    if (repository == null) {
      setState("No repository plugin found for '"+type+"'.", STATE_ERROR);
      return false;
    }

    if (!repository.isValidConfig(config)) {
      setState("The configuration is not valid for the selected repository type.", STATE_ERROR);
      return false;
    }
    setState(null, STATE_OK);
    return true;
  }
  
  //***************************************************************************
  // Private Utility Methods
  //***************************************************************************
  void updateConfigDescription() {
    String type = wTypeCombo.getText();
    String description = Osgi.getBundleRepositoryTypeConfigDescription(type);
    if (description == null) {
      description = "";
    }
    wConfigLabel.setText(description);

    IBundleRepositoryType repository = (IBundleRepositoryType) repositories.get(type);
    wConfigCombo.removeAll();
    if (repository != null) {
      String[] suggestions = repository.getConfigSuggestions();
      if (suggestions != null) {
        for (int i=0; i<suggestions.length; i++) {
          wConfigCombo.add(suggestions[i]);
        }
      }
    }
  }
  
  private void setValues(RepositoryPreference settings) {
    
    // Name
    if (settings != null) { 
      wNameText.setText(settings.getName());
    } else {
      wNameText.setText("");
    }
    
    // Type
    wTypeCombo.removeAll();
    for(Iterator<String> i=repositories.keySet().iterator(); i.hasNext();) {
      wTypeCombo.add(i.next());
    }
    if (settings != null && settings.getType() != null && settings.getType().trim().length() > 0) {
      wTypeCombo.setText(settings.getType());
    } else {
      wTypeCombo.select(0);
    }

    // Description
    updateConfigDescription();

    // Config
    if (settings != null) { 
      wConfigCombo.setText(settings.getConfig());
    } else {
      wConfigCombo.setText("");
    }
    
    
    verifyAll();
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
}
