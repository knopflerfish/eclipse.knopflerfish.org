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

package org.knopflerfish.eclipse.core.ui.launcher.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.IOsgiVendor;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiVendor;
import org.knopflerfish.eclipse.core.PackageDescription;
import org.knopflerfish.eclipse.core.launcher.BundleLaunchInfo;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.IBundleProject;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.LibraryDialog;

/**
 * @author Anders Rimén
 */
public class BundleTab extends AbstractLaunchConfigurationTab {
  private static String TITLE_ADD_LIBRARY = "Add external bundle";
  
  // Properties
  public static String PROP_NAME       = "name";
  public static String PROP_VERSION    = "version";
  public static String PROP_STARTLEVEL = "startlevel";
  public static String PROP_MODE       = "mode";
  public static String PROP_ERROR      = "error";
  
  private static String IMAGE_BUNDLE = "icons/obj16/jar_b_obj.gif";
  private static String IMAGE_FISH = "icons/obj16/knopflerfish_obj.gif";
  
  private int MARGIN = 5;

  private static final int DEFAULT_STARTLEVEL_BUNDLE = 1;
  private static final int DEFAULT_STARTLEVEL_BUNDLE_PROJECT = 2;

  // SWT Widgets
  private Composite wPageComposite;
  private Button    wBundleAddButton;
  private Button    wBundleRemoveButton;
  private Group     wBundleInfoGroup;
  private Text      wBundleInfoNameText;
  private Text      wBundleInfoVersionText;
  private Text      wBundleInfoPathText;

  // jFace Widgets 
  private TreeViewer    wAvailableBundleTreeViewer;
  private TableViewer   wSelectedBundleTableViewer;

  // Models
  private SelectedBundlesModel selectedBundlesModel = new SelectedBundlesModel();
  private IOsgiInstall osgiInstall;

  // Images, fonts
  private Image imageBundle = null;
  private Image imageFish = null;
  
  public BundleTab() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_FISH);
    if (id != null) {
      imageFish = id.createImage();
    }
  }
    
  /****************************************************************************
   * org.eclipse.debug.ui.ILaunchConfigurationTab Methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Bundles";
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  public Image getImage() {
    return imageBundle;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  public void dispose() {
    if (imageBundle != null) {
      imageBundle.dispose();
      imageBundle = null;
    }
    if (imageFish != null) {
      imageFish.dispose();
      imageFish = null;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    
    wPageComposite = new Composite(parent, 0);
    setControl(wPageComposite);
    FormLayout layout = new FormLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    wPageComposite.setLayout(layout);
    
    // All bundle tree
    Label wBundleAllLabel = new Label(wPageComposite, SWT.LEFT);
    wBundleAllLabel.setText("Available bundles:");
    wBundleAddButton = new Button(wPageComposite, SWT.CENTER);
    wBundleAddButton.setText("Add");
    wBundleAddButton.setEnabled(false);
    wBundleAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        selectBundle();
      }
    });
    Tree wAvailableBundleTree =  new Tree(wPageComposite, SWT.MULTI | SWT.BORDER);
    wAvailableBundleTreeViewer = new TreeViewer(wAvailableBundleTree);
    wAvailableBundleTreeViewer.setContentProvider(new AvailableTreeContentProvider());
    wAvailableBundleTreeViewer.setLabelProvider(new AvailableBundlesLabelProvider());
    wAvailableBundleTreeViewer.setSorter(new SorterName());
    wAvailableBundleTreeViewer.addFilter(new AvailableTreeFilter());
    wAvailableBundleTreeViewer.setInput(new AvailableElementRoot());
    wAvailableBundleTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wAvailableBundleTreeViewer.getSelection();
        updateInfoWidgets(selection);
        // Enable/disable add button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          // Check that bundle or bundle project is selcted
          for (Iterator i = selection.iterator(); i.hasNext() && !enable; ) {
            IAvailableTreeElement element = (IAvailableTreeElement) i.next();
            int type = element.getType();
            enable |= 
              type == IAvailableTreeElement.TYPE_BUNDLE || 
              type == IAvailableTreeElement.TYPE_PROJECT;
          }
        }
        wBundleAddButton.setEnabled(enable);
      }
    });
    
    // Bundle Info Group
    wBundleInfoGroup = new Group(wPageComposite, SWT.NONE);
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

    wBundleInfoGroup.setEnabled(false);
    
    // Selected bundle tree
    Label wBundleSelectedLabel = new Label(wPageComposite, SWT.LEFT);
    wBundleSelectedLabel.setText("Selected bundles:");
    Table wSelectedBundleTable = new Table(wPageComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
    wSelectedBundleTableViewer = new TableViewer(wSelectedBundleTable);
    wSelectedBundleTableViewer.setContentProvider(new SelectedBundlesContentProvider());
    wSelectedBundleTableViewer.setLabelProvider(new SelectedBundlesLabelProvider());
    wSelectedBundleTableViewer.setSorter(new SorterStartLevel());
    wSelectedBundleTableViewer.setColumnProperties(
        new String[] {PROP_NAME, PROP_VERSION, PROP_STARTLEVEL, PROP_MODE, PROP_ERROR});
    wSelectedBundleTableViewer.setCellModifier(new CellModifier());
    TextCellEditor startLevelEditor = new TextCellEditor(wSelectedBundleTable, SWT.NONE);
    startLevelEditor.setValidator(new StartLevelValidator());
    ComboBoxCellEditor modeEditor = new ComboBoxCellEditor(wSelectedBundleTable, 
        BundleLaunchInfo.MODES, SWT.DROP_DOWN | SWT.READ_ONLY);
    wSelectedBundleTableViewer.setCellEditors(
        new CellEditor[] {null, null, startLevelEditor, modeEditor, null});
    wSelectedBundleTable.setHeaderVisible(true);
    wSelectedBundleTable.setLinesVisible(true);
    wSelectedBundleTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wSelectedBundleTableViewer.getSelection();
        updateInfoWidgets(selection);
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wBundleRemoveButton.setEnabled(enable);
      }
    });

    // Table columns
    TableColumn colName = new TableColumn(wSelectedBundleTable, SWT.LEFT);
    colName.setText("Name");
    colName.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        wSelectedBundleTableViewer.setSorter(new SorterName());
      }
    });
    TableColumn colVersion = new TableColumn(wSelectedBundleTable, SWT.LEFT);
    colVersion.setText("Version");
    TableColumn colLocation = new TableColumn(wSelectedBundleTable, SWT.LEFT);
    colLocation.setText("Start Level");
    colLocation.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        wSelectedBundleTableViewer.setSorter(new SorterStartLevel());
      }
    });
    TableColumn colMode = new TableColumn(wSelectedBundleTable, SWT.LEFT);
    colMode.setText("Mode");
    TableColumn colError = new TableColumn(wSelectedBundleTable, SWT.LEFT);
    colError.setText("Missing packages");
    
    wBundleRemoveButton = new Button(wPageComposite, SWT.CENTER);
    wBundleRemoveButton.setText("Remove");
    wBundleRemoveButton.setEnabled(false);
    wBundleRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        unselectBundle();
      }
    });

    Button wAddExternalBundleButton = new Button(wPageComposite, SWT.CENTER);
    wAddExternalBundleButton.setText("Add external...");
    wAddExternalBundleButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        LibraryDialog dialog = 
          new LibraryDialog(((Button) e.widget).getShell(), 
              null,
              TITLE_ADD_LIBRARY); 
        if (dialog.open() == Window.OK) {
          try {
            OsgiBundle bundle = new OsgiBundle(new File(dialog.getLibrary().getPath()));
            bundle.setSource(dialog.getLibrary().getSource());
            BundleLaunchInfo info = new BundleLaunchInfo();
            info.setStartLevel(DEFAULT_STARTLEVEL_BUNDLE);
            String activator = null;
            if (bundle.getBundleManifest() != null){
              activator = bundle.getBundleManifest().getActivator();
            }

            info.setMode(activator != null ? 
                BundleLaunchInfo.MODE_START :
                BundleLaunchInfo.MODE_INSTALL); 
            SelectedBundleElement selectedElement = new SelectedBundleElement(bundle, info);
            selectedBundlesModel.add(wSelectedBundleTableViewer, selectedElement);
    
            // Check package dependencies
            updatePackages();
            
            // Refilter available bundle tree 
            wAvailableBundleTreeViewer.refresh();
            
            // Notify that configuration is changed
            updateLaunchConfigurationDialog();
            
            // Resize columns in selected table 
            UiUtils.packTableColumns(wSelectedBundleTableViewer.getTable());
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }
    });
    
    
    // Layout All Bundles Tree
    FormData data = new FormData();
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(0,0);
    wBundleAllLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleAllLabel,5, SWT.BOTTOM);
    data.right = new FormAttachment(wBundleAddButton,-5,SWT.LEFT);
    data.bottom = new FormAttachment(50,0);
    wAvailableBundleTree.setLayoutData(data);
    
    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.left = new FormAttachment(wAddExternalBundleButton, 0, SWT.LEFT);
    data.top = new FormAttachment(wAvailableBundleTree,0, SWT.TOP);
    wBundleAddButton.setLayoutData(data);
    
    // Layout Bundle Info Group
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wAvailableBundleTree,0, SWT.RIGHT);
    data.top = new FormAttachment(wAvailableBundleTree,5, SWT.BOTTOM);
    wBundleInfoGroup.setLayoutData(data);

    // Layout Selected Bundles Table
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleInfoGroup,5, SWT.BOTTOM);
    wBundleSelectedLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleSelectedLabel,5, SWT.BOTTOM);
    data.right = new FormAttachment(wAddExternalBundleButton,-5,SWT.LEFT);
    data.bottom = new FormAttachment(100,0);
    wSelectedBundleTable.setLayoutData(data);

    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.left = new FormAttachment(wAddExternalBundleButton, 0, SWT.LEFT);
    data.top = new FormAttachment(wSelectedBundleTable,0, SWT.TOP);
    wBundleRemoveButton.setLayoutData(data);

    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.top = new FormAttachment(wBundleRemoveButton,5, SWT.BOTTOM);
    wAddExternalBundleButton.setLayoutData(data);
    
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    // New configuration created, set default values

    // Set default bundles
    HashMap defaultBundles = new HashMap();
    // TODO: Calculate default bundles
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, defaultBundles);   

    // Set default bundle projects
    HashMap defaultBundleProjects = new HashMap();
    // TODO: Calculate default bundles
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, defaultBundleProjects);   
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    // Set values to GUI widgets
    IOsgiVendor osgiVendor = Osgi.getVendor(OsgiVendor.VENDOR_NAME);
    try {
      String name = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_OSGI_INSTALL_NAME, (String) null);
      if (name != null) {
        osgiInstall = osgiVendor.getOsgiInstall(name);
      } else {
        osgiInstall = osgiVendor.getDefaultOsgiInstall();
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }

    try {
      selectedBundlesModel.clear();
      // Bundles
      selectedBundlesModel.addBundles(configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, (Map) null));
      // Bundle Projects
      selectedBundlesModel.addBundleProjects(configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, (Map) null));
      wSelectedBundleTableViewer.setInput(selectedBundlesModel);
    } catch (CoreException e) {
      e.printStackTrace();
    }

    // Update packages
    updatePackages();

    
    wAvailableBundleTreeViewer.refresh();
    UiUtils.packTableColumns(wSelectedBundleTableViewer.getTable());
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    // Read values from GUI widgets

    // Bundles
    HashMap bundles = new HashMap();
    if (selectedBundlesModel.getBundles() != null) {
      bundles.putAll(selectedBundlesModel.getBundles());
    }
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, bundles);
    
    // Bundle Projects
    HashMap bundleProjects = new HashMap();
    if (selectedBundlesModel.getBundleProjects() != null) {
      bundleProjects.putAll(selectedBundlesModel.getBundleProjects());
    }
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, bundleProjects);   
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void updateInfoWidgets(IStructuredSelection selection) {
    if (selection == null || selection.size() != 1) {
      // Disable bundle info group
      wBundleInfoGroup.setEnabled(false);
      // Disable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(false);
      }
      wBundleInfoNameText.setText("");
      wBundleInfoVersionText.setText("");
      wBundleInfoPathText.setText("");
    } else {
      wBundleInfoGroup.setEnabled(true);
      // Enable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(true);
      }
      Object o = selection.getFirstElement();
      if (o instanceof IAvailableTreeElement) {
        IAvailableTreeElement element = (IAvailableTreeElement) o;
        wBundleInfoNameText.setText(element.getName() == null ? "" : element.getName());
        wBundleInfoVersionText.setText(element.getVersion() == null ? "" : element.getVersion());
        wBundleInfoPathText.setText(element.getPath() == null ? "" : element.getPath());
      } else if (o instanceof SelectedBundleElement) {
        SelectedBundleElement element = (SelectedBundleElement) o;
        wBundleInfoNameText.setText(element.getName() == null ? "" : element.getName());
        wBundleInfoVersionText.setText(element.getVersion() == null ? "" : element.getVersion());
        wBundleInfoPathText.setText(element.getPath() == null ? "" : element.getPath());
      }
    }
  }
  
  private void selectBundle() {
    IStructuredSelection selection = 
      (IStructuredSelection) wAvailableBundleTreeViewer.getSelection();
    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      IAvailableTreeElement element = (IAvailableTreeElement) i.next();

      SelectedBundleElement selectedElement = null;
      if (element.getType() == IAvailableTreeElement.TYPE_BUNDLE) {
        IOsgiBundle bundle = (IOsgiBundle) element.getData();
        
        BundleLaunchInfo info = new BundleLaunchInfo();
        info.setStartLevel(DEFAULT_STARTLEVEL_BUNDLE);
        String activator = null;
        if (bundle.getBundleManifest() != null){
          activator = bundle.getBundleManifest().getActivator();
        }

        info.setMode(activator != null ? 
            BundleLaunchInfo.MODE_START :
            BundleLaunchInfo.MODE_INSTALL); 
        selectedElement = new SelectedBundleElement(bundle, info);
      } else if(element.getType() == IAvailableTreeElement.TYPE_PROJECT) {
        IBundleProject project = (IBundleProject) element.getData();

        BundleLaunchInfo info = new BundleLaunchInfo();
        info.setStartLevel(DEFAULT_STARTLEVEL_BUNDLE_PROJECT);
        info.setMode(project.getBundleManifest().getActivator() != null ? 
            BundleLaunchInfo.MODE_START :
            BundleLaunchInfo.MODE_INSTALL); 
        selectedElement = new SelectedBundleElement(project, info);
      }
      
      if (selectedElement != null) {
        selectedBundlesModel.add(wSelectedBundleTableViewer, selectedElement);
      }
    }

    // Check package dependencies
    updatePackages();
    
    // Refilter available bundle tree 
    wAvailableBundleTreeViewer.refresh();
    
    // Notify that configuration is changed
    updateLaunchConfigurationDialog();
    
    // Resize columns in selected table 
    UiUtils.packTableColumns(wSelectedBundleTableViewer.getTable());
  }
  
  private void unselectBundle() {
    IStructuredSelection selection = 
      (IStructuredSelection) wSelectedBundleTableViewer.getSelection();
    
    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      SelectedBundleElement element = (SelectedBundleElement) i.next();
      selectedBundlesModel.remove(wSelectedBundleTableViewer, element);
    }

    // Check package dependencies
    updatePackages();
    
    // Refilter available bundle tree 
    wAvailableBundleTreeViewer.refresh();
    
    // Notify that configuration is changed
    updateLaunchConfigurationDialog();
    
    // Resize columns in selected table 
    UiUtils.packTableColumns(wSelectedBundleTableViewer.getTable());
  }
  
  public void updatePackages() {
    HashMap exportedPackages = new HashMap();

    // Get exported packages by runtime
    // TODO: Fix this better, should check the property exported packages as well
    IOsgiLibrary [] libraries = osgiInstall.getRuntimeLibraries();
    if (libraries != null) {
      for (int i=0; i<libraries.length; i++) {
        try {
          OsgiBundle bundle = new OsgiBundle(new File(libraries[i].getPath()));
          
          PackageDescription [] packages = null;
          if (bundle.getBundleManifest() != null) {
            packages = bundle.getBundleManifest().getExportedPackages();
          }
          if (packages != null) {
            for (int j=0; j<packages.length; j++) {
              ArrayList descriptions = (ArrayList) exportedPackages.get(packages[j].getPackageName());
              if (descriptions == null) {
                descriptions = new ArrayList();
              }
              descriptions.add(packages[j]);
              exportedPackages.put(packages[j].getPackageName(), descriptions);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    
    // Loop through elements and find exported packages
    SelectedBundleElement[] elements = selectedBundlesModel.getElements(); 
    for (int i=0; i<elements.length; i++) {
      PackageDescription [] packages = elements[i].getExportedPackages();
      if (packages != null) {
        for (int j=0; j<packages.length; j++) {
          ArrayList descriptions = (ArrayList) exportedPackages.get(packages[j].getPackageName());
          if (descriptions == null) {
            descriptions = new ArrayList();
          }
          descriptions.add(packages[j]);
          exportedPackages.put(packages[j].getPackageName(), descriptions);
        }
      }
    }
    
    // Loop through elements and update import packages 
    TableItem[] items = wSelectedBundleTableViewer.getTable().getItems();
    StringBuffer error = new StringBuffer();
    for (int i=0; i<items.length; i++) {
      error.setLength(0);
      Object data = items[i].getData();
      if (data != null && data instanceof SelectedBundleElement) {
        SelectedBundleElement element = (SelectedBundleElement) data; 
        PackageDescription [] packages = element.getImportedPackages();
        if (packages != null) {
          for (int j=0; j<packages.length; j++) {
            ArrayList descriptions = (ArrayList) exportedPackages.get(packages[j].getPackageName());
            String version = packages[j].getAttribute("specification-version");
            if (descriptions == null) {
              if (error.length() > 0) error.append(", ");
              error.append(packages[j].getPackageName());
            } else if (version != null) {
              // TODO: Check version
              
            }
          }          
        }
        
        element.setError(error.length() > 0 ? error.toString() : null);
        wSelectedBundleTableViewer.update(element, null);
      }
    }
  }
  
  
  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  class StartLevelValidator implements ICellEditorValidator {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
     */
    public String isValid(Object value) {
      String errorMsg = null;
      try {
        int i = Integer.parseInt((String) value);
        if (i < 1) {
          errorMsg = "Start level must be >= 1";
        }
      } catch (Exception e) {
        errorMsg = "Value must be an integer";
      }
      return errorMsg;
    }
  }
  
  class CellModifier implements ICellModifier {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
      if (PROP_STARTLEVEL.equals(property) || PROP_MODE.equals(property)) {
        return true;
      } else {
        return false;
      }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object o, String property) {
      SelectedBundleElement element = (SelectedBundleElement) o;
      if (PROP_STARTLEVEL.equals(property)) {
        return Integer.toString(element.getLaunchInfo().getStartLevel());
      } else if (PROP_MODE.equals(property)) {
        return new Integer(element.getLaunchInfo().getMode());
      } else {
        return null;
      }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object o, String property, Object value) {
      SelectedBundleElement element = null;
      if (o instanceof SelectedBundleElement) {
        element = (SelectedBundleElement) o;
      } else if (o instanceof TableItem) {
        element = (SelectedBundleElement) (((TableItem) o).getData());
      }
      if (element == null) return;
      if (PROP_STARTLEVEL.equals(property)) {
        try {
          int i = Integer.parseInt((String) value);
          if (i >= 1) {
            element.getLaunchInfo().setStartLevel(i);
            selectedBundlesModel.update(wSelectedBundleTableViewer, element);
            updateLaunchConfigurationDialog();
          }
        } catch (Exception e) {
          // Failed to set start level
        }
      } else if (PROP_MODE.equals(property)) {
        element.getLaunchInfo().setMode( (((Integer) value)).intValue());
        selectedBundlesModel.update(wSelectedBundleTableViewer, element);
        updateLaunchConfigurationDialog();
      }
    }
  }
  
  class AvailableTreeFilter extends ViewerFilter {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object o) {
      if (!(o instanceof IAvailableTreeElement)) return true;
      
      IAvailableTreeElement e = (IAvailableTreeElement) o;
      
      if (e.getType() == IAvailableTreeElement.TYPE_BUNDLE) {
        IOsgiBundle bundle = (IOsgiBundle) e.getData();
        SelectedBundleElement sbe = new SelectedBundleElement(bundle, null);
        return !selectedBundlesModel.contains(sbe);
      } else if (e.getType() == IAvailableTreeElement.TYPE_PROJECT) {
        BundleProject project = (BundleProject) e.getData();
        SelectedBundleElement sbe = new SelectedBundleElement(project, null);
        return !selectedBundlesModel.contains(sbe);
      } else {
        return true;
      }
    }
  }
  
  class SorterName extends ViewerSorter {
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public int compare(Viewer viewer, Object o1, Object o2) {
      int c1 = category(o1);
      int c2 = category(o2);
      if (c1 != c2) return c1-c2;

      String n1 = null;
      String n2 = null;
      if (o1 instanceof SelectedBundleElement) {
        SelectedBundleElement e1 = (SelectedBundleElement) o1;
        SelectedBundleElement e2 = (SelectedBundleElement) o2;
        n1 = e1.getName();
        n2 = e2.getName();
      } else if (o1 instanceof IAvailableTreeElement) {
        IAvailableTreeElement e1 = (IAvailableTreeElement) o1;
        IAvailableTreeElement e2 = (IAvailableTreeElement) o2;
        n1 = e1.getName();
        n2 = e2.getName();
      }
      if (n1 == null) n1 = "";
      if (n2 == null) n2 = "";
      return n1.toLowerCase().compareTo(n2.toLowerCase());
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
     */
    public int category(Object o) {
      if (o instanceof IAvailableTreeElement) {
        IAvailableTreeElement e = (IAvailableTreeElement) o;
        switch (e.getType()) {
          case IAvailableTreeElement.TYPE_ROOT:
            return 0;
          case IAvailableTreeElement.TYPE_WORKSPACE:
            return 1;
          case IAvailableTreeElement.TYPE_OSGI_INSTALL:
            return 2;
          case IAvailableTreeElement.TYPE_BUNDLE:
            return 3;
          case IAvailableTreeElement.TYPE_PROJECT:
            return 3;
          default:
            return 0;
        }
      } else {
        return 0;
      }
    }
  }

  class SorterStartLevel extends ViewerSorter {
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public int compare(Viewer viewer, Object o1, Object o2) {
      SelectedBundleElement e1 = (SelectedBundleElement) o1;
      SelectedBundleElement e2 = (SelectedBundleElement) o2;
      int sl = e1.getLaunchInfo().getStartLevel()-e2.getLaunchInfo().getStartLevel();
      if (sl != 0) {
        return sl;
      } else {
        String n1 = e1.getName();
        String n2 = e2.getName();
        if (n1 == null) n1 = "";
        if (n2 == null) n2 = "";
        return n1.toLowerCase().compareTo(n2.toLowerCase());
      }
    }
  }
}
