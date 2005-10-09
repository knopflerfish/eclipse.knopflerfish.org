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

package org.knopflerfish.eclipse.core.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.LibraryDialog;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class EnvironmentDialog extends Dialog {

  private final static String TITLE_ADD = "Add execution environment";
  private final static String TITLE_EDIT = "Edit execution environment";

  private final static String TITLE_ADD_LIBRARY = "Add library";
  private final static String TITLE_EDIT_LIBRARY = "Edit library";

  private final static int NUM_CHARS_NAME = 60;

  private final static int STATE_OK       = 0;
  private final static int STATE_ERROR    = 1;
  private final static int STATE_INFO     = 2;
  
  // Widgets
  private Text    wNameText;
  private Button  wLibraryUpButton;
  private Button  wLibraryDownButton;
  private Button  wLibraryRemoveButton;
  private Button  wLibraryAttachButton;
  private Button  wLibraryAddButton;
  private Button  wLibraryImportButton;
  private Label   wErrorMsgLabel;
  private Label   wErrorImgLabel;

  TableViewer    wLibraryTableViewer;
  
  private ArrayList usedNames;
  private EnvironmentPreference environment;
  ArrayList libraries = new ArrayList(); 
  
  protected EnvironmentDialog(Shell parentShell, ArrayList usedNames, EnvironmentPreference environment) {
    super(parentShell);

    this.usedNames = usedNames;
    this.environment = environment;
    if (environment != null && usedNames != null) {
      usedNames.remove(environment.getName());
    }
  }

  public EnvironmentPreference getExecutionEnvironment() {
    return environment;
  }

  /****************************************************************************
   * org.eclipse.jface.window.Window Methods
   ***************************************************************************/
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
    if (environment != null) {
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
    
    setValues(environment);
    
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
    if (environment == null) {
      // Create execution environment
      environment = new EnvironmentPreference();
    }

    environment.setType(EnvironmentPreference.TYPE_USER);
    environment.setName(wNameText.getText());
    environment.setLibraries( (IOsgiLibrary[]) libraries.toArray(new IOsgiLibrary[libraries.size()]));
    
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
    wNameLabel.setText("Environment name:");
    wNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    wNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyAll();
      }
    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = convertWidthInCharsToPixels(NUM_CHARS_NAME);
    wNameText.setLayoutData(gd);
    
    
    // Libraries
    Composite wLibraryComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wLibraryComposite.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wLibraryComposite.setLayoutData(gd);
    
    Label wLibrariesLabel = new Label(wLibraryComposite, SWT.LEFT);
    wLibrariesLabel.setText("Libraries :");
    gd = new GridData();
    gd.horizontalSpan = 2;
    wLibrariesLabel.setLayoutData(gd);

    Table wLibraryTable =  new Table(wLibraryComposite, SWT.MULTI | SWT.BORDER);
    new TableColumn(wLibraryTable, SWT.LEFT);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    gd.verticalSpan = 6;
    wLibraryTable.setLayoutData(gd);
    wLibraryTableViewer = new TableViewer(wLibraryTable);
    LibraryContentProvider provider = new LibraryContentProvider();
    wLibraryTableViewer.setContentProvider(provider);
    wLibraryTableViewer.setLabelProvider(provider);
    wLibraryTableViewer.setInput(libraries);
    wLibraryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });

    // Library Buttons
    wLibraryAddButton = new Button(wLibraryComposite, SWT.NONE);
    wLibraryAddButton.setText("Add Library...");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wLibraryAddButton.setLayoutData(gd);
    wLibraryAddButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(Display.getDefault().getActiveShell(), null, TITLE_ADD_LIBRARY);
        
        if (dialog.open() == Window.OK) {
          libraries.add(dialog.getLibrary());
          
          wLibraryTableViewer.refresh();
          UiUtils.packTableColumns(wLibraryTableViewer.getTable());
          updateButtons();
        }
      }
    });

    wLibraryAttachButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryAttachButton.setText("Attach Source...");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wLibraryAttachButton.setLayoutData(gd);
    wLibraryAttachButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTableViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        IOsgiLibrary lib = (IOsgiLibrary) selection.getFirstElement();

        LibraryDialog dialog = 
          new LibraryDialog(Display.getDefault().getActiveShell(), lib, TITLE_EDIT_LIBRARY); 
        if (dialog.open() == Window.OK) {
          lib.setSource(dialog.getLibrary().getSource());
        }
        
        wLibraryTableViewer.refresh();
        updateButtons();
      }
    });

    wLibraryRemoveButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryRemoveButton.setText("Remove");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wLibraryRemoveButton.setLayoutData(gd);
    wLibraryRemoveButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTableViewer.getSelection();
        if (selection == null || selection.isEmpty()) return;

        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          IOsgiLibrary lib = (IOsgiLibrary) i.next();
          libraries.remove(lib);
        }
        
        wLibraryTableViewer.refresh();
        UiUtils.packTableColumns(wLibraryTableViewer.getTable());
        updateButtons();
      }
    });
    
    wLibraryUpButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryUpButton.setText("Up");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wLibraryUpButton.setLayoutData(gd);
    wLibraryUpButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTableViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        IOsgiLibrary lib = (IOsgiLibrary) selection.getFirstElement();
        moveUp(lib);
        wLibraryTableViewer.refresh();
        updateButtons();
      }
    });
    wLibraryDownButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryDownButton.setText("Down");
    gd = new GridData();
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.TOP;
    wLibraryDownButton.setLayoutData(gd);
    wLibraryDownButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTableViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        IOsgiLibrary lib = (IOsgiLibrary) selection.getFirstElement();
        moveDown(lib);
        wLibraryTableViewer.refresh();
        updateButtons();
      }
    });
    


    wLibraryImportButton = new Button(wLibraryComposite, SWT.NONE);
    wLibraryImportButton.setText("Import Libraries...");
    wLibraryImportButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
      }
    });
    
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

  /****************************************************************************
   * Verify Methods
   ***************************************************************************/
  public boolean verifyAll() {

    if (!verifyName()) {
      return false;
    }
    
    return true;
  }
  
  public boolean verifyName() {
    String name = wNameText.getText();
    
    // Check if name is valid
    if (name == null || name.length() == 0) {
      setState("Enter a name for the environment.", STATE_INFO);
      return false;
    }
    
    // Check that name is not already used
    if (usedNames.contains(name)) {
      setState("Environment name is already used.", STATE_ERROR);
      return false;
    }
    
    setState(null, STATE_OK);
    return true;
  }
  
  /****************************************************************************
   * Private Utility Methods
   ***************************************************************************/
  void updateButtons() {
    // Get current selection
    IStructuredSelection selection = (IStructuredSelection) wLibraryTableViewer.getSelection();
    
    // Enable/disable add button
    boolean enableRemove = false;
    boolean enableAttach = false;
    boolean enableUp = false;
    boolean enableDown = false;
    if (selection != null && !selection.isEmpty()) {
      // Check if remove buttton shall be enabled
      enableRemove = true;
      
      // Check if up, down and attach buttons shall be enabled
      if (selection.size() == 1) {
        enableAttach = true;
        IOsgiLibrary lib = (IOsgiLibrary) selection.getFirstElement();
        int idx = libraries.indexOf(lib);
        enableUp = (idx > 0);
        enableDown = (idx < libraries.size()-1);
      }
    }
    wLibraryRemoveButton.setEnabled(enableRemove);
    wLibraryUpButton.setEnabled(enableUp);
    wLibraryDownButton.setEnabled(enableDown);
    wLibraryAttachButton.setEnabled(enableAttach);
  }
  
  public void moveUp(IOsgiLibrary lib) {
    int idx = libraries.indexOf(lib);
    if (idx < 1) return;
    
    if (libraries.remove(lib)) {
      libraries.add(idx-1, lib);
    }
  }

  public void moveDown(IOsgiLibrary lib) {
    int idx = libraries.indexOf(lib);
    if (idx == -1 || idx >= libraries.size()-1) return;
    
    if (libraries.remove(lib)) {
      libraries.add(idx+1, lib);
    }
  }
  
  private void setValues(EnvironmentPreference settings) {
    
    // Name
    if (settings != null) { 
      wNameText.setText(settings.getName());
    } else {
      wNameText.setText("");
    }
    
    // Libraries
    libraries.clear();
    if (settings != null) {
      IOsgiLibrary [] libs = settings.getLibraries();
      for (int i=0; i<libs.length; i++) {
        libraries.add(libs[i]);
      }
    }
    
    // Update viewer input
    wLibraryTableViewer.refresh();
    UiUtils.packTableColumns(wLibraryTableViewer.getTable());
    
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
