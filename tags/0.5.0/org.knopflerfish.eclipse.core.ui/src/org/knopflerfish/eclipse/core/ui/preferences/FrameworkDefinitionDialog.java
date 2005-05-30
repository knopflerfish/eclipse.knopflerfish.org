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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiInstall;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author ar
 */
public class FrameworkDefinitionDialog extends Dialog {
  private static String [] PATH_FRAMEWORK_LIB = new String [] {
      "knopflerfish.org/osgi/framework.jar",
      "osgi/framework.jar",
      "framework.jar"
  };
  
  // Pathes relative knopflerfish osgi directory
  private static String PATH_FRAMEWORK_SRC = "framework/src";
  private static String PATH_JAR_DIR ="jars";
  private static String PATH_BUNDLE_DIR ="bundles";
  
  private static String DEFAULT_MAINCLASS = "org.knopflerfish.framework.Main";
  private static String MANIFEST_MAIN_CLASS_ATTRIBUTE = "Main-class";

  private static String TITLE_ADD = "Add framework definition";
  private static String TITLE_EDIT = "Edit framework definition";

  private static String TITLE_ADD_LIBRARY = "Add library";
  private static String TITLE_EDIT_LIBRARY = "Edit library";
  private static String TITLE_ADD_BUNDLE = "Add bundle";
  private static String TITLE_EDIT_BUNDLE = "Edit bundle";
  
  private final static int NUM_ROWS_BUNDLE_TABLE = 6;
  private final static int NUM_CHARS_PATH = 60;
  
  private final static int STATE_OK       = 0;
  private final static int STATE_ERROR    = 1;
  private final static int STATE_INFO     = 2;
  
  private int MARGIN = 5;
  
  // Widgets
  private Text    wNameText;
  private Text    wLocationText;
  private Text    wMainClassText;
  private Text    wSpecificationVersionText;
  private Table   wLibrariesTable;
  private Button  wDefaultButton;
  private Label   wErrorMsgLabel;
  private Label   wErrorImgLabel;
  private Button  wLibraryUpButton;
  private Button  wLibraryDownButton;
  private Button  wLibraryRemoveButton;
  private Button  wLibraryAddButton;
  private Table   wBundlesTable;
  private Button  wBundleRemoveButton;
  private Button  wBundleAddButton;
  private Group   wBundleInfoGroup;
  private Text    wBundleInfoNameText;
  private Text    wBundleInfoVersionText;
  private Text    wBundleInfoPathText;
  private Text    wBundleInfoSourceText;
  
  // Images
  private Image libImage = null;
  private Image libSrcImage = null;
  private Image bundleImage = null;
  private Image bundleSrcImage = null;
  
  private ArrayList osgiInstallNames;
  private OsgiInstall osgiInstall;
  private ArrayList libraries  = new ArrayList();
  private ArrayList bundles    = new ArrayList();
  private ArrayList properties = new ArrayList();
  
  /**
   * @param parentShell
   */
  protected FrameworkDefinitionDialog(Shell parentShell, ArrayList osgiInstallNames, OsgiInstall osgiInstall) {
    super(parentShell);
    this.osgiInstallNames = osgiInstallNames;
    this.osgiInstall = osgiInstall;
    
    // Create images
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", "icons/obj16/lib_obj.gif");
    if (id != null) {
      libImage = id.createImage();
    }

    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", "icons/obj16/bundle_obj.gif");
    if (id != null) {
      bundleImage = id.createImage();
    }
    
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", "icons/ovr16/src_ovr2.gif");
    if (id != null) {
      Image srcOvrImage = id.createImage();
      if (libImage != null) { 
        libSrcImage = new Image(null, libImage.getBounds());
        GC gc = new GC(libSrcImage);
        gc.drawImage(libImage, 0, 0);
        gc.drawImage(srcOvrImage, 
            libSrcImage.getBounds().width-srcOvrImage.getBounds().width, 
            libSrcImage.getBounds().height-srcOvrImage.getBounds().height);
        gc.dispose();      
      }
      if (bundleImage != null) { 
        bundleSrcImage = new Image(null, bundleImage.getBounds());
        GC gc = new GC(bundleSrcImage);
        gc.drawImage(bundleImage, 0, 0);
        gc.drawImage(srcOvrImage, 
            bundleSrcImage.getBounds().width-srcOvrImage.getBounds().width, 
            bundleSrcImage.getBounds().height-srcOvrImage.getBounds().height);
        gc.dispose();      
      }
      srcOvrImage.dispose();
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
    
    // Dispose images
    if (libImage != null) {
      libImage.dispose();
      libImage = null;
    }
    if (libSrcImage != null) {
      libSrcImage.dispose();
      libSrcImage = null;
    }
    if (bundleImage != null) {
      bundleImage.dispose();
      bundleImage = null;
    }
    if (bundleSrcImage != null) {
      bundleSrcImage.dispose();
      bundleSrcImage = null;
    }
    
    return closed;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (osgiInstall != null) {
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
    
    setValues(osgiInstall);
    
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
    if (osgiInstall == null) {
      // Create definition
      osgiInstall = new OsgiInstall();
    }

    osgiInstall.setName(wNameText.getText());
    osgiInstall.setLocation(wLocationText.getText());
    osgiInstall.setDefaultSettings(wDefaultButton.getSelection());
    osgiInstall.setMainClass(wMainClassText.getText());
    osgiInstall.setSpecificationVersion(wSpecificationVersionText.getText());
    osgiInstall.setLibraries(libraries);
    osgiInstall.setBundles(bundles);
    osgiInstall.setSystemProperties(properties);
    
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
    FormLayout layout = new FormLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
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
    
    // Main class
    Label wMainClassLabel = new Label(composite, SWT.LEFT);
    wMainClassLabel.setText("Framework main class:");
    wMainClassText = new Text(composite, SWT.SINGLE | SWT.BORDER);

    // Specification Version
    Label wSpecificationVersionLabel = new Label(composite, SWT.LEFT);
    wSpecificationVersionLabel.setText("Specification Version:");
    wSpecificationVersionText = new Text(composite, SWT.SINGLE);
    wSpecificationVersionText.setEditable(false);
    
    // Libraries
    Label wLibrariesLabel = new Label(composite, SWT.LEFT);
    wLibrariesLabel.setText("Framework libraries:");
    wLibrariesTable = new Table(composite, SWT.BORDER);
    new TableColumn(wLibrariesTable, SWT.LEFT);
    wLibrariesTable.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {
        updateLibraryButtons();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        int idx = wLibrariesTable.getSelectionIndex();
        if (idx != -1) {
          LibraryDialog dialog = 
            new LibraryDialog(((Table) e.widget).getShell(), 
                (IOsgiLibrary) libraries.get(idx),
                TITLE_EDIT_LIBRARY); 
          if (dialog.open() == Window.OK) {
            updateLibrary(idx, dialog.getLibrary());
          }
        }
      }
    });
    
    // Library Buttons
    wLibraryUpButton = new Button(composite, SWT.CENTER);
    wLibraryUpButton.setText("Up");
    wLibraryUpButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wLibrariesTable.getSelectionIndex();
        if (idx != -1) {
          moveLibrary(idx, true);
          updateLibraryButtons();
        }
      }
    });

    wLibraryDownButton = new Button(composite, SWT.CENTER);
    wLibraryDownButton.setText("Down");
    wLibraryDownButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wLibrariesTable.getSelectionIndex();
        if (idx != -1) {
          moveLibrary(idx, false);
          updateLibraryButtons();
        }
      }
    });
    
    wLibraryRemoveButton = new Button(composite, SWT.CENTER);
    wLibraryRemoveButton.setText("Remove");
    wLibraryRemoveButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wLibrariesTable.getSelectionIndex();
        if (idx != -1) {
          removeLibrary(idx);
          wLibrariesTable.getColumn(0).pack();
          updateLibraryButtons();
        }
      }
    });

    wLibraryAddButton = new Button(composite, SWT.CENTER);
    wLibraryAddButton.setText("Add...");
    wLibraryAddButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), 
              null,
              TITLE_ADD_LIBRARY); 
        if (dialog.open() == Window.OK) {
          addLibrary(dialog.getLibrary());
          wLibrariesTable.getColumn(0).pack();
          updateLibraryButtons();
        }
      }
    });

    // Bundles
    Label lBundles = new Label(composite, SWT.LEFT);
    lBundles.setText("Bundles:");
    wBundlesTable = new Table(composite, SWT.BORDER | SWT.V_SCROLL);
    new TableColumn(wBundlesTable, SWT.LEFT);
    wBundlesTable.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {
        updateBundleButtons();
        updateBundleInfo();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        int idx = wBundlesTable.getSelectionIndex();
        if (idx != -1) {
          LibraryDialog dialog = 
            new LibraryDialog(((Table) e.widget).getShell(),
                (IOsgiLibrary) bundles.get(idx),
                TITLE_EDIT_BUNDLE); 
          if (dialog.open() == Window.OK) {
            try {
              OsgiBundle bundle = new OsgiBundle(new File(dialog.getLibrary().getPath()));
              bundle.setSourceDirectory(dialog.getLibrary().getSourceDirectory());
              updateBundle(idx, bundle);
              updateBundleInfo();
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
          }
        }
      }
    });
    
    // Bundle Buttons
    wBundleRemoveButton = new Button(composite, SWT.CENTER);
    wBundleRemoveButton.setText("Remove");
    wBundleRemoveButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        int idx = wBundlesTable.getSelectionIndex();
        if (idx != -1) {
          removeBundle(idx);
          wBundlesTable.getColumn(0).pack();
          updateBundleButtons();
          updateBundleInfo();
        }
      }
    });

    wBundleAddButton = new Button(composite, SWT.CENTER);
    wBundleAddButton.setText("Add...");
    wBundleAddButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), 
              null,
              TITLE_ADD_BUNDLE); 
        if (dialog.open() == Window.OK) {
          try {
            OsgiBundle bundle = new OsgiBundle(new File(dialog.getLibrary().getPath()));
            bundle.setSourceDirectory(dialog.getLibrary().getSourceDirectory());
            addBundle(bundle);
            wBundlesTable.getColumn(0).pack();
            updateBundleButtons();
            updateBundleInfo();
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }
    });
    // Bundle Info Group

    wBundleInfoGroup = new Group(composite, SWT.NONE);
    wBundleInfoGroup.setText("Bundle Info");
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    wBundleInfoGroup.setLayout(gridLayout);
    
    Label wBundleInfoNameLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoNameLabel.setText("Name:");
    wBundleInfoNameText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoNameText.setEditable(false);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoNameText.setLayoutData(gd);
    
    Label wBundleInfoVersionLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoVersionLabel.setText("Version:");
    wBundleInfoVersionText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoVersionText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoVersionText.setLayoutData(gd);

    Label wBundleInfoPathLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoPathLabel.setText("Path:");
    wBundleInfoPathText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoPathText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoPathText.setLayoutData(gd);

    Label wBundleInfoSourceLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoSourceLabel.setText("Source:");
    wBundleInfoSourceText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoSourceText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoSourceText.setLayoutData(gd);

    wBundleInfoGroup.setEnabled(false);

    // Error label
    wErrorMsgLabel = new Label(composite, SWT.LEFT);
    wErrorImgLabel = new Label(composite, SWT.LEFT);
    wErrorImgLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));

    // Layout Name
    FormData data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wLocationLabel,0,SWT.RIGHT);
    data.top = new FormAttachment(wNameText,0,SWT.CENTER);
    wNameLabel.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wNameLabel,0);
    data.right = new FormAttachment(100,0);
    data.top =  new FormAttachment(0,0);
    wNameText.setLayoutData(data);

    // Layout Location
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wLocationText,0,SWT.CENTER);
    
    wLocationLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(wNameText,0, SWT.LEFT);
    data.right = new FormAttachment(wBrowseLocationButton, -5, SWT.LEFT);
    data.top =  new FormAttachment(wNameText,5,SWT.BOTTOM);
    data.width = convertWidthInCharsToPixels(NUM_CHARS_PATH);
    wLocationText.setLayoutData(data);

    data = new FormData();
    data.right = new FormAttachment(wNameText,0, SWT.RIGHT);
    data.top =  new FormAttachment(wLocationText,0,SWT.CENTER);
    wBrowseLocationButton.setLayoutData(data);
    
    // Layout Default Settings
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wLocationText,5,SWT.BOTTOM);
    wDefaultButton.setLayoutData(data);
    
    
    // Layout Main Class
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wNameLabel,0,SWT.RIGHT);
    data.top = new FormAttachment(wMainClassText,0,SWT.CENTER);
    wMainClassLabel.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wNameText,0, SWT.LEFT);
    data.right = new FormAttachment(wNameText,0, SWT.RIGHT);
    data.top =  new FormAttachment(wDefaultButton,5,SWT.BOTTOM);
    wMainClassText.setLayoutData(data);
    
    // Layout Specification Version
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wNameLabel,0,SWT.RIGHT);
    data.top = new FormAttachment(wSpecificationVersionText,0,SWT.CENTER);
    wSpecificationVersionLabel.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wNameText,0, SWT.LEFT);
    data.right = new FormAttachment(wNameText,0, SWT.RIGHT);
    data.top =  new FormAttachment(wMainClassText,5,SWT.BOTTOM);
    wSpecificationVersionText.setLayoutData(data);
    
    // Layout libraries
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wSpecificationVersionText,15,SWT.BOTTOM);
    wLibrariesLabel.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wLibraryUpButton,-5, SWT.LEFT);
    data.top = new FormAttachment(wLibrariesLabel,5,SWT.BOTTOM);
    data.bottom = new FormAttachment(wLibraryAddButton,0,SWT.BOTTOM);
    wLibrariesTable.setLayoutData(data);
    
    // Layout Buttons
    data = new FormData();
    data.left = new FormAttachment(wLibraryRemoveButton, 0, SWT.LEFT);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wLibrariesTable,0,SWT.TOP);
    wLibraryUpButton.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wLibraryUpButton, 0, SWT.LEFT);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wLibraryUpButton,5,SWT.BOTTOM);
    wLibraryDownButton.setLayoutData(data);
    
    data = new FormData();
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wLibraryDownButton,5,SWT.BOTTOM);
    wLibraryRemoveButton.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wLibraryUpButton, 0, SWT.LEFT);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wLibraryRemoveButton,5,SWT.BOTTOM);
    wLibraryAddButton.setLayoutData(data);
    
    // Layout bundles
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wLibrariesTable,5,SWT.BOTTOM);
    lBundles.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wBundleRemoveButton,-5, SWT.LEFT);
    data.top = new FormAttachment(lBundles,5,SWT.BOTTOM);
    //data.bottom = new FormAttachment(wBundleAttachButton,0,SWT.BOTTOM);
    data.height = bundleImage.getBounds().height*NUM_ROWS_BUNDLE_TABLE;
    wBundlesTable.setLayoutData(data);
    
    // Layout Buttons
    data = new FormData();
    //data.left = new FormAttachment(wBundleAttachButton, 0, SWT.LEFT);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wBundlesTable,0,SWT.TOP);
    wBundleRemoveButton.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(wBundleRemoveButton, 0, SWT.LEFT);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wBundleRemoveButton,5,SWT.BOTTOM);
    wBundleAddButton.setLayoutData(data);
    
    // Layout bundle info group
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.top = new FormAttachment(wBundlesTable,5,SWT.BOTTOM);
    wBundleInfoGroup.setLayoutData(data);
    
    // Layout error
    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.top = new FormAttachment(wBundleInfoGroup,5,SWT.BOTTOM);
    wErrorImgLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(wErrorImgLabel, 5, SWT.RIGHT);
    data.top = new FormAttachment(wErrorImgLabel,0,SWT.CENTER);
    data.right = new FormAttachment(100,0);
    wErrorMsgLabel.setLayoutData(data);

    return composite;
  }

  /**
   * @return
   */
  public OsgiInstall getOsgiInstall() {
    return osgiInstall;
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
    
    if (osgiInstall != null && name.equals(osgiInstall.getName())) {
      setState(null, STATE_OK);
      return true;
    }
    
    // Check that name is not already used
    if (osgiInstallNames.contains(name)) {
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
    
    // Check that home directory contains framework.jar
    boolean libFound = false;
    for (int i=0; i<PATH_FRAMEWORK_LIB.length; i++) {
      File libFile = new File(homeDir, PATH_FRAMEWORK_LIB[i]);
      if ( libFile.exists() && libFile.isFile()) {
        libFound=true;
      }
    }
    
    if (!libFound) {
      setState("The location is not a valid root, framework.jar not found.", STATE_ERROR);
      return false;
    }
    
    setState(null, STATE_OK);
    return true;
  }

  /****************************************************************************
   * Private Utility Methods
   ***************************************************************************/
  private void enableCustomSettings(boolean enable) {
    wMainClassText.setEditable(enable);
    updateLibraryButtons();
    updateBundleButtons();
    updateBundleInfo();
  }

  private void setDefaultSettings() {

    // Set default values
    if (verifyLocation()) {
      // Add framework library
      wLibrariesTable.removeAll();
      libraries.clear();
      File lib = null;
      for (int i=0; i<PATH_FRAMEWORK_LIB.length; i++) {
        lib = new File(new File(wLocationText.getText()), PATH_FRAMEWORK_LIB[i]);
        if ( lib.exists() && lib.isFile()) {
          break;
        }
      }
      try {
        OsgiLibrary library = new OsgiLibrary(lib);
        File srcDir = new File(lib.getParentFile(), PATH_FRAMEWORK_SRC);
        if (srcDir.exists() && srcDir.isDirectory()) {
          library.setSourceDirectory(srcDir.getAbsolutePath());
        }
        addLibrary(library);
        wLibrariesTable.getColumn(0).pack();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
  
      String mainClass = DEFAULT_MAINCLASS;
      String specificationVersion = "";
      try {
        // Set main class
        JarFile jarFile = new JarFile(lib);
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
          Attributes attributes = manifest.getMainAttributes();
         
          // Main class
          String attr = attributes.getValue(Attributes.Name.MAIN_CLASS);
          if (attr != null) {
            mainClass = attr;
          }
          // Specification Version
          attr = attributes.getValue(Attributes.Name.SPECIFICATION_VERSION);
          if (attr != null) {
            specificationVersion = attr;
          }
        }
      } catch (IOException e) {
      }
      wMainClassText.setText(mainClass);
      wSpecificationVersionText.setText(specificationVersion);
      
      // Add bundles
      wBundlesTable.removeAll();
      bundles.clear();
      File root = lib.getParentFile();

      // Find jars
      File jarDir = new File(root, PATH_JAR_DIR);
      ArrayList jars = getBundleJars(jarDir);
      for (int i=0 ; i<jars.size(); i++) {
        try {
          OsgiBundle b = new OsgiBundle((File) jars.get(i));
          // Find source
          String builtFrom = b.getBuiltFrom();
          if (builtFrom != null) {
            int idx = builtFrom.lastIndexOf(PATH_BUNDLE_DIR);
            if (idx != -1) {
              File dir = new File(root, builtFrom.substring(idx));
              File srcDir = new File(dir, "src");
              if (srcDir.exists() && srcDir.isDirectory()) {
                b.setSourceDirectory(srcDir.getAbsolutePath());
              }
            }
          }
          addBundle(b);
        } catch(IOException e) {
          // Failed to create bundle from file
        }
      }
      wBundlesTable.getColumn(0).pack();
    
      // Load and set properties
      InputStream is = null;
      properties.clear();
      try {
        try {
          is = OsgiUiPlugin.getDefault().openStream(new Path("resources/framework.props"));
          Properties props = new Properties();
          props.load(is);
          
          int numProps = Integer.parseInt(props.getProperty("framework.property.num", "0"));
          for (int i=0; i<numProps; i++) {
            String propBase = "framework.property."+i;
            String name = props.getProperty(propBase+".name");
            if (name != null) {
              SystemProperty property = new SystemProperty(name);
              String description = props.getProperty(propBase+".description");
              if (description != null) {
                property.setDescription(description);
              }
              String group = props.getProperty(propBase+".group");
              if (group != null) {
                property.setGroup(group);
              }
              String defaultValue = props.getProperty(propBase+".default");
              if (defaultValue != null) {
                property.setDefaultValue(defaultValue);
              }
              String allowedValues = props.getProperty(propBase+".allowed");
              if (allowedValues != null) {
                ArrayList values = new ArrayList();
                StringTokenizer st = new StringTokenizer(allowedValues, ",");
                while(st.hasMoreTokens()) {
                  String token = st.nextToken();
                  values.add(token);
                }
                property.setAllowedValues(values);
              }
              properties.add(property);
            }
          }
        } finally {
          if (is != null) is.close();
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
  
  private ArrayList getBundleJars(File f) {
    ArrayList jars = new ArrayList();
    if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
      jars.add(f); 
    } else if (f.isDirectory()) {
      File [] list = f.listFiles();
      if (list != null) {
        for(int i=0; i<list.length; i++) {
          jars.addAll(getBundleJars(list[i]));
        }
      }
    }
    return jars;
  }
  
  private void setValues(OsgiInstall settings) {
    
    // Name
    if (settings != null) { 
      wNameText.setText(osgiInstall.getName());
    } else {
      wNameText.setText("");
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

    // Libraries
    wLibrariesTable.removeAll();
    libraries.clear();
    if (settings != null) {
      IOsgiLibrary [] osgiLibraries = settings.getLibraries();
      for (int i=0; i<osgiLibraries.length; i++) {
        addLibrary( osgiLibraries[i]);
      }
      wLibrariesTable.getColumn(0).pack();
    }
    
    // Bundles
    wBundlesTable.removeAll();
    bundles.clear();
    if (settings != null) {
      IOsgiBundle [] osgiBundles = settings.getBundles();
      for (int i=0; i<osgiBundles.length; i++) {
        addBundle( osgiBundles[i]);
      }
      wBundlesTable.getColumn(0).pack();
    }
    
    // Properties 
    properties.clear();
    if (settings != null) {
      SystemProperty [] systemProperties = settings.getSystemProperties();
      for (int i=0; i<systemProperties.length; i++) {
        properties.add(systemProperties[i]);
      }
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

  /****************************************************************************
   * Library Methods
   ***************************************************************************/
  private void updateLibraryButtons() {
    int idx = wLibrariesTable.getSelectionIndex();
    boolean enable = !wDefaultButton.getSelection();
    wLibraryUpButton.setEnabled(enable && idx > 0);
    wLibraryDownButton.setEnabled(enable && idx != -1 && idx < wLibrariesTable.getItemCount()-1);
    wLibraryRemoveButton.setEnabled(enable && idx != -1);
    wLibraryAddButton.setEnabled(enable);
  }
  
  private void updateLibrary(int idx, IOsgiLibrary library) {
    TableItem item = wLibrariesTable.getItem(idx);
    item.setText(0, library.getPath());
    if (library.getSourceDirectory() != null) {
      item.setImage(0, libSrcImage);
    } else {
      item.setImage(0, libImage);
    }
    
    libraries.set(idx, library);
  }
  
  private void addLibrary(IOsgiLibrary library) {
    TableItem item = new TableItem(wLibrariesTable, 0);
    item.setText(0, library.getPath());
    if (library.getSourceDirectory() != null) {
      item.setImage(0, libSrcImage);
    } else {
      item.setImage(0, libImage);
    }
    
    libraries.add(library);
  }
  
  private void insertLibrary(IOsgiLibrary library, int idx) {
    if (idx < 0) idx = 0;
    
    if (idx >= libraries.size()) {
      addLibrary(library);
    } else {
      TableItem item = new TableItem(wLibrariesTable, 0, idx);
      item.setText(0, library.getPath());
      if (library.getSourceDirectory() != null) {
        item.setImage(0, libSrcImage);
      } else {
        item.setImage(0, libImage);
      }
      libraries.add(idx, library);
    }
  }
  
  private IOsgiLibrary removeLibrary(int idx) {
    wLibrariesTable.remove(idx);
    return (IOsgiLibrary) libraries.remove(idx);
  }
  
  
  private void moveLibrary(int idx, boolean up) {
    IOsgiLibrary library = removeLibrary(idx);
    int newIdx = (up ? idx-1 : idx+1);
    insertLibrary(library, newIdx);
    wLibrariesTable.setSelection(newIdx);
  }

  /****************************************************************************
   * Bundle Methods
   ***************************************************************************/
  private void updateBundleButtons() {
    int idx = wBundlesTable.getSelectionIndex();
    boolean enable = !wDefaultButton.getSelection();
    wBundleRemoveButton.setEnabled(enable && idx != -1);
    wBundleAddButton.setEnabled(enable);
  }

  private void updateBundleInfo() {
    int idx = wBundlesTable.getSelectionIndex();
    if (idx != -1) {
      wBundleInfoGroup.setEnabled(true);
      // Enable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(true);
      }

      // Check what type of bundle has been selected
      IOsgiBundle bundle = (IOsgiBundle) bundles.get(idx);
      wBundleInfoNameText.setText(bundle.getName());
      if (bundle.getVersion() != null) {
        wBundleInfoVersionText.setText(bundle.getVersion());
      } else {
        wBundleInfoVersionText.setText("");
      }
      wBundleInfoPathText.setText(bundle.getPath());
      if (bundle.getSourceDirectory() != null) {
        wBundleInfoSourceText.setText(bundle.getSourceDirectory());
      } else {
        wBundleInfoSourceText.setText("");
      }
    } else {
      wBundleInfoGroup.setEnabled(false);
      // Disable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(false);
      }
      wBundleInfoNameText.setText("");
      wBundleInfoVersionText.setText("");
      wBundleInfoPathText.setText("");
      wBundleInfoSourceText.setText("");
    }
    boolean enable = !wDefaultButton.getSelection();
    wBundleRemoveButton.setEnabled(enable && idx != -1);
    wBundleAddButton.setEnabled(enable);
  }
  
  private void updateBundle(int idx, IOsgiBundle bundle) {
    TableItem item = wBundlesTable.getItem(idx);
    item.setText(0, bundle.getName());
    if (bundle.getSourceDirectory() != null) {
      item.setImage(0, bundleSrcImage);
    } else {
      item.setImage(0, bundleImage);
    }
    
    bundles.set(idx, bundle);
  }
  
  private void addBundle(IOsgiBundle bundle) {
    // Find index to insert this bundle to keep list sorted
    int idx = 0;
    boolean insert = false;
    String name = bundle.getName().toLowerCase();
    for (int i=0; i<bundles.size(); i++) {
      IOsgiBundle b = (IOsgiBundle) bundles.get(i);
      if (name.compareTo(b.getName().toLowerCase()) <= 0) {
        insert = true;
        break;
      }
      idx++;
    }

    TableItem item;
    if (!insert) {
      item = new TableItem(wBundlesTable, 0);
      bundles.add(bundle);
    } else {
      item = new TableItem(wBundlesTable, 0, idx);
      bundles.add(idx, bundle);
    }
    
    item.setText(0, bundle.getName());
    if (bundle.getSourceDirectory() != null) {
      item.setImage(0, bundleSrcImage);
    } else {
      item.setImage(0, bundleImage);
    }
  }
  
  private IOsgiBundle removeBundle(int idx) {
    wBundlesTable.remove(idx);
    return (IOsgiBundle) bundles.remove(idx);
  }
  
}