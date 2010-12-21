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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.IFrameworkDefinition;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.ui.dialogs.ImportLibrariesDialog;
import org.knopflerfish.eclipse.core.ui.dialogs.LibraryDialog;
import org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryElementBundle;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryElementBundleRoot;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryElementRoot;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryElementRuntime;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryElementRuntimeRoot;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryTreeContentProvider;
import org.knopflerfish.eclipse.core.ui.preferences.model.LibraryTreeLabelProvider;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FrameworkDialog extends Dialog {
  
  private static String TITLE_ADD = "Add framework definition";
  private static String TITLE_EDIT = "Edit framework definition";

  static String TITLE_ADD_LIBRARY = "Add library";
  static String TITLE_EDIT_LIBRARY = "Edit library";
  static String TITLE_ADD_BUNDLE = "Add bundle";
  static String TITLE_EDIT_BUNDLE = "Edit bundle";
  static String TITLE_IMPORT_LIBRARIES = "Import libraries";
  
  private final static int NUM_CHARS_NAME = 60;
  
  private final static int STATE_OK       = 0;
  private final static int STATE_ERROR    = 1;
  private final static int STATE_INFO     = 2;
  
  // Widgets
  private Text    wNameText;
  private Combo   wTypeCombo;
  Text    wLocationText;
  Button  wDefaultButton;
  private Text    wMainClassText;
  private Text    wSpecificationVersionText;
  private Button  wLibraryUpButton;
  private Button  wLibraryDownButton;
  private Button  wLibraryRemoveButton;
  private Button  wLibraryAttachButton;
  private Button  wLibraryAddRuntimeButton;
  private Button  wLibraryAddBundleButton;
  private Button  wLibraryImportButton;
  private Label   wErrorMsgLabel;
  private Label   wErrorImgLabel;

  TreeViewer    wLibraryTreeViewer;
  
  LibraryElementRoot frameworkLibraryModel = new LibraryElementRoot();
  ArrayList usedNames;
  private FrameworkPreference distribution;
  private TreeMap distributions = new TreeMap();
  
  /**
   * @param parentShell
   */
  protected FrameworkDialog(Shell parentShell, ArrayList usedNames, FrameworkPreference distribution) {
    super(parentShell);
    this.usedNames = usedNames;
    this.distribution = distribution;
    if (distribution != null && usedNames != null) {
      usedNames.remove(distribution.getName());
    }
    String[] names = Osgi.getFrameworkDefinitionNames();
    for (int i=0;i<names.length; i++) {
      distributions.put(names[i], Osgi.getFrameworkDefinition(names[i]));
    }
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
    if (distribution != null) {
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
    
    setValues(distribution);
    
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
    if (distribution == null) {
      // Create definition
      distribution = new FrameworkPreference();
    }

    distribution.setName(wNameText.getText());
    distribution.setType(wTypeCombo.getText());
    distribution.setLocation(wLocationText.getText());
    distribution.setDefaultSettings(wDefaultButton.getSelection());
    distribution.setMainClass(wMainClassText.getText());
    distribution.setSpecificationVersion(wSpecificationVersionText.getText());
    distribution.setRuntimeLibraries(frameworkLibraryModel.getRuntimeLibraries());
    distribution.setBundles(frameworkLibraryModel.getBundles());
    distribution.clearSystemPropertyGroups();
    String type = wTypeCombo.getText();
    IFrameworkDefinition def = (IFrameworkDefinition) distributions.get(type);
    PropertyGroup[] groups = def.getSystemPropertyGroups();
    if(groups != null) {
      for (int i=0; i<groups.length; i++) {
        distribution.addSystemPropertyGroup(groups[i]);
        groups[i].setFrameworkDistribution(distribution);
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
    layout.numColumns = 3;
    composite.setLayout(layout);
    
    // Name
    Label wNameLabel = new Label(composite, SWT.LEFT);
    wNameLabel.setText("Framework name:");
    wNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    wNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        verifyAll();
      }
    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    gd.widthHint = convertWidthInCharsToPixels(NUM_CHARS_NAME);
    wNameText.setLayoutData(gd);
    
    // Type
    Label wTypeLabel = new Label(composite, SWT.LEFT);
    wTypeLabel.setText("Framework type:");
    wTypeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
    wTypeCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (wDefaultButton.getSelection()) {
          setDefaultSettings();
        }
        verifyAll();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wTypeCombo.setLayoutData(gd);
    

    // Location
    Label wLocationLabel = new Label(composite, SWT.LEFT);
    wLocationLabel.setText("Framework home directory:");
    wLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    wLocationText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (wDefaultButton.getSelection()) {
          setDefaultSettings();
        }
        verifyAll();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wLocationText.setLayoutData(gd);
    Button wBrowseLocationButton = new Button(composite, SWT.CENTER);
    wBrowseLocationButton.setText("Browse...");
    wBrowseLocationButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(((Button) e.widget).getShell());
        String path = dialog.open();
        if (path != null) {
          wLocationText.setText(path);
          if (wDefaultButton.getSelection()) {
            setDefaultSettings();
          }
          verifyAll();
        }
      }
    });
    
    // Default settings
    wDefaultButton = new Button(composite, SWT.CHECK);
    wDefaultButton.setText("Use default settings");
    wDefaultButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        if (((Button) e.widget).getSelection()) {
          setDefaultSettings();
        }
        enableCustomSettings(!((Button) e.widget).getSelection());
        verifyAll();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wDefaultButton.setLayoutData(gd);
    
    // Main class
    Label wMainClassLabel = new Label(composite, SWT.LEFT);
    wMainClassLabel.setText("Framework main class:");
    wMainClassText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wMainClassText.setLayoutData(gd);

    // Specification Version
    Label wSpecificationVersionLabel = new Label(composite, SWT.LEFT);
    wSpecificationVersionLabel.setText("Specification Version:");
    wSpecificationVersionText = new Text(composite, SWT.SINGLE);
    wSpecificationVersionText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wSpecificationVersionText.setLayoutData(gd);
    
    // Libraries
    Composite wLibraryComposite = new Composite(composite, SWT.NONE);
    FormLayout formLayout = new FormLayout();
    wLibraryComposite.setLayout(formLayout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wLibraryComposite.setLayoutData(gd);
    
    Label wLibrariesLabel = new Label(wLibraryComposite, SWT.LEFT);
    wLibrariesLabel.setText("OSGi Libraries :");

    Tree wLibraryTree =  new Tree(wLibraryComposite, SWT.MULTI | SWT.BORDER);
    wLibraryTreeViewer = new TreeViewer(wLibraryTree);
    wLibraryTreeViewer.setContentProvider(new LibraryTreeContentProvider());
    wLibraryTreeViewer.setLabelProvider(new LibraryTreeLabelProvider());
    //wLibraryTreeViewer.setSorter(new SorterName());
    wLibraryTreeViewer.setInput(frameworkLibraryModel);
    wLibraryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });
    
    wLibraryTree.setLayoutData(gd);

    // Library Buttons
    wLibraryUpButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryUpButton.setText("Up");
    wLibraryUpButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTreeViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        ILibraryTreeElement element = (ILibraryTreeElement) selection.getFirstElement();
        int type = element.getType();

        if (type == ILibraryTreeElement.TYPE_RUNTIME) {
          LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
          runtimeRoot.moveUp(element);
        }
        wLibraryTreeViewer.refresh();
        updateButtons();
      }
    });
    wLibraryDownButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryDownButton.setText("Down");
    wLibraryDownButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTreeViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        ILibraryTreeElement element = (ILibraryTreeElement) selection.getFirstElement();
        int type = element.getType();

        if (type == ILibraryTreeElement.TYPE_RUNTIME) {
          LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
          runtimeRoot.moveDown(element);
        }
        wLibraryTreeViewer.refresh();
        updateButtons();
      }
    });
    wLibraryRemoveButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryRemoveButton.setText("Remove");
    wLibraryRemoveButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTreeViewer.getSelection();
        if (selection == null || selection.isEmpty()) return;

        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          ILibraryTreeElement element = (ILibraryTreeElement) i.next();
          int type = element.getType();
          
          if (type == ILibraryTreeElement.TYPE_RUNTIME) {
            LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
            runtimeRoot.remove(element);
          } else if (type == ILibraryTreeElement.TYPE_BUNDLE) {
            LibraryElementBundleRoot bundleRoot = frameworkLibraryModel.getBundleRoot();
            bundleRoot.remove(element);
          }
        }
        
        wLibraryTreeViewer.refresh();
        updateButtons();
      }
    });
    wLibraryAttachButton = new Button(wLibraryComposite, SWT.CENTER);
    wLibraryAttachButton.setText("Attach Source...");
    wLibraryAttachButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wLibraryTreeViewer.getSelection();
        if (selection == null || selection.size() != 1) return;
        
        ILibraryTreeElement element = (ILibraryTreeElement) selection.getFirstElement();
        int type = element.getType();

        IOsgiLibrary lib = null;
        String title = TITLE_EDIT_LIBRARY;
        if (type == ILibraryTreeElement.TYPE_RUNTIME) {
          lib = ((LibraryElementRuntime) element).getLibrary();
        } else if (type == ILibraryTreeElement.TYPE_BUNDLE) {
          lib = ((LibraryElementBundle) element).getBundle();
          title = TITLE_EDIT_BUNDLE;
        }
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), lib, title); 
        if (dialog.open() == Window.OK) {
          try {
            OsgiBundle bundle = new OsgiBundle(new File(dialog.getLibrary().getPath()));
            bundle.setUserDefined(true);
            bundle.setSource(dialog.getLibrary().getSource());
            if (type == ILibraryTreeElement.TYPE_RUNTIME) {
              ((LibraryElementRuntime) element).setLibrary(bundle);
            } else if (type == ILibraryTreeElement.TYPE_BUNDLE) {
              ((LibraryElementBundle) element).setBundle(bundle);
            }
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
        
        wLibraryTreeViewer.refresh();
        updateButtons();
      }
    });

    wLibraryAddRuntimeButton = new Button(wLibraryComposite, SWT.NONE);
    wLibraryAddRuntimeButton.setText("Add Runtime Library...");
    wLibraryAddRuntimeButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), null, TITLE_ADD_LIBRARY);
        
        if (dialog.open() == Window.OK) {
          LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
          runtimeRoot.addChild(new LibraryElementRuntime(runtimeRoot, dialog.getLibrary()));
          
          wLibraryTreeViewer.refresh();
          updateButtons();
        }
      }
    });
    wLibraryAddBundleButton = new Button(wLibraryComposite, SWT.NONE);
    wLibraryAddBundleButton.setText("Add Bundle...");
    wLibraryAddBundleButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), null, TITLE_ADD_BUNDLE);
        
        if (dialog.open() == Window.OK) {
          LibraryElementBundleRoot bundleRoot = frameworkLibraryModel.getBundleRoot();
          try {
            OsgiBundle bundle = new OsgiBundle(new File(dialog.getLibrary().getPath()));
            bundle.setUserDefined(true);
            bundle.setSource(dialog.getLibrary().getSource());
            bundleRoot.addChild(new LibraryElementBundle(bundleRoot, bundle));
          } catch (IOException ioe) {
          }
          wLibraryTreeViewer.refresh();
          updateButtons();
        }
      }
    });

    wLibraryImportButton = new Button(wLibraryComposite, SWT.NONE);
    wLibraryImportButton.setText("Import Libraries...");
    wLibraryImportButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        ImportLibrariesDialog dialog = 
          new ImportLibrariesDialog(Display.getCurrent().getActiveShell(), usedNames, TITLE_IMPORT_LIBRARIES);
        
        if (dialog.open() == Window.OK) {
          String name = dialog.getFrameworkName();
          FrameworkPreference framework = OsgiPreferences.getFramework(name);
          boolean onlyUserDefined = dialog.isOnlyUserDefined();
          
          // Import runtime libraries
          if (dialog.isImportRuntimeLibraries()) {
            LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
            IOsgiLibrary[] libraries = framework.getRuntimeLibraries();
            for (int i=0; i<libraries.length; i++) {
              if (!onlyUserDefined || libraries[i].isUserDefined()) {
                runtimeRoot.addChild(new LibraryElementRuntime(runtimeRoot, libraries[i]));
              }
            }
          }
          
          // Import bundles
          if (dialog.isImportBundles()) {
            LibraryElementBundleRoot bundleRoot = frameworkLibraryModel.getBundleRoot();
            IOsgiBundle[] bundles = framework.getBundles();
            for (int i=0; i<bundles.length; i++) {
              if (!onlyUserDefined || bundles[i].isUserDefined()) {
                bundleRoot.addChild(new LibraryElementBundle(bundleRoot, bundles[i]));
              }
            }
          }
          
          wLibraryTreeViewer.refresh();
          updateButtons();
        }
      }
    });
    
    // Layout libraries composite
    FormData fd = new FormData();
    fd.left = new FormAttachment(0,0);
    fd.top = new FormAttachment(0,0);
    fd.right = new FormAttachment(100,0);
    wLibrariesLabel.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0,0);
    fd.top = new FormAttachment(wLibrariesLabel, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(wLibraryAddRuntimeButton, -5, SWT.LEFT);
    fd.bottom = new FormAttachment(100,0);
    wLibraryTree.setLayoutData(fd);
    
    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryTree, 0, SWT.TOP);
    fd.right = new FormAttachment(100, 0);
    wLibraryUpButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryUpButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryDownButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryDownButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryRemoveButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryRemoveButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryAttachButton.setLayoutData(fd);

    fd = new FormData();
    //fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryAttachButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryAddRuntimeButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryAddRuntimeButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryAddBundleButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wLibraryAddRuntimeButton, 0, SWT.LEFT);
    fd.top = new FormAttachment(wLibraryAddBundleButton, 5, SWT.BOTTOM);
    fd.right = new FormAttachment(100, 0);
    wLibraryImportButton.setLayoutData(fd);
    
    // Error label
    Composite wErrorComposite = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    wErrorComposite.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 3;
    wErrorComposite.setLayoutData(gd);
    wErrorImgLabel = new Label(wErrorComposite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
    wErrorMsgLabel = new Label(wErrorComposite, SWT.LEFT);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorMsgLabel.setLayoutData(gd);

    return composite;
  }

  /**
   * @return
   */
  public FrameworkPreference getFrameworkDistribution() {
    return distribution;
  }

  /****************************************************************************
   * Verify Methods
   ***************************************************************************/
  public boolean verifyAll() {

    if (!verifyName()) {
      return false;
    }
    
    if (!verifyLocation()) {
      return false;
    }
    
    return true;
  }
  
  public boolean verifyName() {
    String name = wNameText.getText();
    
    // Check if name is valid
    if (name == null || name.length() == 0) {
      setState("Enter a name for the framework.", STATE_INFO);
      return false;
    }
    
    // Check that name is not already used
    if (usedNames.contains(name)) {
      setState("Framework name is already used.", STATE_ERROR);
      return false;
    }
    
    setState(null, STATE_OK);
    return true;
  }
  
  public boolean verifyLocation() {
    String location = wLocationText.getText();
    
    // Check if location is valid
    if (location == null || location.length() == 0) {
      setState("Enter location of framework installation.", STATE_INFO);
      return false;
    }
    
    File homeDir = new File(location);
    
    // Check that home directory exists
    if (!homeDir.exists()) {
      setState("The location does not exist.", STATE_ERROR);
      return false;
    }
    
    // Check that home directory is a directory
    if (!homeDir.isDirectory()) {
      setState("The location is not a directory.", STATE_ERROR);
      return false;
    }
    
    // Check if location is a valid directory for this definition
    String type = wTypeCombo.getText();
    IFrameworkDefinition def = (IFrameworkDefinition) distributions.get(type);
    if (def == null) {
      setState("No framework plugin found for '"+type+"'.", STATE_ERROR);
      return false;
    }

    if (!def.isValidDir(homeDir)) {
      setState("The location is not a valid directory for the selected framework type.", STATE_ERROR);
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
    IStructuredSelection selection = (IStructuredSelection) wLibraryTreeViewer.getSelection();
    
    // Enable/disable add button
    boolean enableRemove = false;
    boolean enableAttach = false;
    boolean enableUp = false;
    boolean enableDown = false;
    if (selection != null && !selection.isEmpty()) {
      // Check if remove buttton shall be enabled
      for (Iterator i = selection.iterator(); i.hasNext() && !enableRemove; ) {
        ILibraryTreeElement element = (ILibraryTreeElement) i.next();
        int type = element.getType();
        enableRemove |= 
          type == ILibraryTreeElement.TYPE_BUNDLE || 
          type == ILibraryTreeElement.TYPE_RUNTIME;
      }
      
      // Check if up, down and attach buttons shall be enabled
      if (selection.size() == 1) {
        ILibraryTreeElement element = (ILibraryTreeElement) selection.getFirstElement();
        int type = element.getType();
        if (type == ILibraryTreeElement.TYPE_BUNDLE || 
            type == ILibraryTreeElement.TYPE_RUNTIME) {
          enableAttach = true;
        }
        if (type == ILibraryTreeElement.TYPE_RUNTIME) {
          LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
          int idx = runtimeRoot.indexOf(element);
          enableUp = (idx > 0); 
          enableDown = (idx < runtimeRoot.size()-1);
        }
      }
    }
    wLibraryRemoveButton.setEnabled(enableRemove && !wDefaultButton.getSelection());
    wLibraryUpButton.setEnabled(enableUp && !wDefaultButton.getSelection());
    wLibraryDownButton.setEnabled(enableDown && !wDefaultButton.getSelection());
    wLibraryAttachButton.setEnabled(enableAttach && !wDefaultButton.getSelection());
    wLibraryAddBundleButton.setEnabled(!wDefaultButton.getSelection());
    wLibraryAddRuntimeButton.setEnabled(!wDefaultButton.getSelection());
    wLibraryImportButton.setEnabled(!wDefaultButton.getSelection() && 
        usedNames != null && usedNames.size() > 0);
  }
  
  void enableCustomSettings(boolean enable) {
    wMainClassText.setEditable(enable);
    updateButtons();
  }

  void setDefaultSettings() {

    // Clear model
    frameworkLibraryModel.clear();
    
    if (verifyLocation()) {
      String type = wTypeCombo.getText();
      IFrameworkDefinition def = (IFrameworkDefinition) distributions.get(type);
      
      String location = wLocationText.getText();
      File dir = new File(location);
      
      // Read framework info
      IOsgiLibrary mainLib = def.getMainLibrary(dir);
      Manifest manifest = mainLib.getManifest();
      String mainClass = null;
      String specificationVersion = null;
      if (manifest != null) {
        Attributes attributes = manifest.getMainAttributes();
        mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
        specificationVersion = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
      }
      wMainClassText.setText(mainClass == null ? "":mainClass);
      wSpecificationVersionText.setText(specificationVersion == null ? "":specificationVersion);

      // Set runtime libraries
      LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
      IOsgiLibrary[] runtimeLibraries = def.getRuntimeLibraries(dir);
      if (runtimeLibraries != null) {
        for(int i=0; i<runtimeLibraries.length; i++) {
          runtimeRoot.addChild(new LibraryElementRuntime(runtimeRoot, runtimeLibraries[i]));
        }
      }
      
      // Set bundles
      LibraryElementBundleRoot bundleRoot = frameworkLibraryModel.getBundleRoot();
      IOsgiBundle[] bundles = def.getBundles(dir);
      if (bundles != null) {
        for(int i=0; i<bundles.length; i++) {
          bundleRoot.addChild(new LibraryElementBundle(bundleRoot, bundles[i]));
        }
      }
    } else {
      wMainClassText.setText("");
      wSpecificationVersionText.setText("");
    }

    // Refresh viewer
    wLibraryTreeViewer.refresh();
  }
  
  private void setValues(FrameworkPreference settings) {
    
    // Name
    if (settings != null) { 
      wNameText.setText(settings.getName());
    } else {
      wNameText.setText("");
    }
    
    // Type
    wTypeCombo.removeAll();
    for(Iterator i=distributions.keySet().iterator(); i.hasNext();) {
      wTypeCombo.add((String)i.next());
    }
    if (settings != null && settings.getType() != null && settings.getType().trim().length() > 0) {
      wTypeCombo.setText(settings.getType());
    } else {
      wTypeCombo.select(0);
    }
    
    // Location
    if (settings != null) {
      wLocationText.setText(settings.getLocation());
    } else {
      wLocationText.setText("");
    }
    
    // Default settings
    if (settings != null) {
      wDefaultButton.setSelection(settings.isDefaultSettings());
    } else {
      wDefaultButton.setSelection(true);
    }
    enableCustomSettings(!wDefaultButton.getSelection());
    
    // Main Class
    if (settings != null) {
      wMainClassText.setText(settings.getMainClass());
    } else {
      wMainClassText.setText("");
    }

    // Specification Version
    if (settings != null && settings.getSpecificationVersion() != null) {
      wSpecificationVersionText.setText(settings.getSpecificationVersion());
    } else {
      wSpecificationVersionText.setText("");
    }

    // Runtime libraries
    LibraryElementRuntimeRoot runtimeRoot = frameworkLibraryModel.getRuntimeRoot();
    runtimeRoot.clear();
    if (settings != null) {
      IOsgiLibrary [] osgiLibraries = settings.getRuntimeLibraries();
      for (int i=0; i<osgiLibraries.length; i++) {
        runtimeRoot.addChild(new LibraryElementRuntime(runtimeRoot, osgiLibraries[i]));
      }
    }
    
    // Bundles
    LibraryElementBundleRoot bundleRoot = frameworkLibraryModel.getBundleRoot();
    bundleRoot.clear();
    if (settings != null) {
      IOsgiBundle [] osgiBundles = settings.getBundles();
      for (int i=0; i<osgiBundles.length; i++) {
        bundleRoot.addChild(new LibraryElementBundle(bundleRoot, osgiBundles[i]));
      }
    }
    
    // Update viewer input
    wLibraryTreeViewer.refresh();
    
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