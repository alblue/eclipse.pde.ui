/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 31, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.pde.internal.ui.wizards.imports;



import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.util.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.elements.*;
import org.eclipse.pde.internal.ui.util.*;
import org.eclipse.pde.internal.ui.wizards.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;

/**
 * @author Wassim Melhem
 */
public class PluginImportWizardDetailedPage extends BaseImportWizardSecondPage {

	
	class ContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object element) {
			return models;
		}
	}

	private Label countLabel;
	private TableViewer availableListViewer;
	private Text filterText;
	
	public PluginImportWizardDetailedPage(String pageName, PluginImportWizardFirstPage firstPage) {
		super(pageName, firstPage);
		setTitle(PDEPlugin.getResourceString("ImportWizard.DetailedPage.title")); //$NON-NLS-1$
		setMessage(PDEPlugin.getResourceString("ImportWizard.DetailedPage.desc")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 10;
		container.setLayout(layout);
				
		createScrollArea(container);
		createAvailableList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
		createButtonArea(container);
		createImportList(container).setLayoutData(new GridData(GridData.FILL_BOTH));
		updateCount();
		Composite options = createComputationsOption(container);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		options.setLayoutData(gd);
		
		addViewerListeners();
		
		initialize();
		setControl(container);
		Dialog.applyDialogFont(container);
		WorkbenchHelp.setHelp(container, IHelpContextIds.PLUGIN_IMPORT_SECOND_PAGE);
	}
	
	private void initialize(){
		setPageComplete(false);
	}
	
	private void addViewerListeners() {
		availableListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleAdd();
			}
		});
				
		importListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleRemove();
			}
		});
		
		filterText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				handleFilter();
			}
		});
		
	}

	private Composite createAvailableList(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData());

		Label label = new Label(container, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.availableList")); //$NON-NLS-1$

		Table table = new Table(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 225;
		gd.heightHint = 200;
		table.setLayoutData(gd);

		availableListViewer = new TableViewer(table);
		availableListViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
		availableListViewer.setContentProvider(new ContentProvider());
		availableListViewer.setInput(PDECore.getDefault().getExternalModelManager());
		availableListViewer.setSorter(ListUtil.PLUGIN_SORTER);

		return container;
	}
	
	
	private Composite createButtonArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		Composite container = new Composite(comp, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 30;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.existing")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleExistingProjects();
			}
		});
		SWTUtil.setButtonDimensionHint(button);

		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.existingUnshared")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleExistingUnshared();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		new Label(container, SWT.NONE);
		//new Label(container, SWT.NONE);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.add")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAdd();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.addAll")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddAll();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.remove")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.removeAll")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveAll();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.swap")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSwap();
			}
		});
		SWTUtil.setButtonDimensionHint(button);
		
		button = new Button(container, SWT.PUSH);
		button.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.addRequired")); //$NON-NLS-1$
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddRequiredPlugins();
			}

		});
		SWTUtil.setButtonDimensionHint(button);
		
		countLabel = new Label(comp, SWT.NONE);
		countLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));		
		return container;
	}
	
	private Composite createScrollArea(Composite parent){
		Group container = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2,false);
		layout.marginWidth = layout.marginHeight = 6;
		container.setLayout(layout);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=3;
		container.setLayoutData(gd);
		container.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.locate")); //$NON-NLS-1$
	
		Label filterLabel = new Label(container, SWT.NONE);
		filterLabel.setText(PDEPlugin.getResourceString("ImportWizard.DetailedPage.search")); //$NON-NLS-1$
		
		filterText = new Text(container, SWT.BORDER);
		filterText.setText(""); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(gd);
			
		return container;
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		setPageComplete(visible && importListViewer.getTable().getItemCount() > 0);
		
	}
	protected void refreshPage() {
		availableListViewer.refresh();
		importListViewer.getTable().removeAll();		
		pageChanged();
	}
	protected void pageChanged() {
		updateCount();
		handleFilter();
		setPageComplete(importListViewer.getTable().getItemCount() > 0);
	}
	private void updateCount() {
		countLabel.setText(
			PDEPlugin.getFormattedMessage(
				"ImportWizard.DetailedPage.count", //$NON-NLS-1$
				new String[] {
					new Integer(importListViewer.getTable().getItemCount()).toString(),
					new Integer(models.length).toString()}));
		countLabel.getParent().layout();
	}
	
	private void handleAdd() {
		IStructuredSelection ssel = (IStructuredSelection)availableListViewer.getSelection();
		if (ssel.size() > 0) {
			Table table = availableListViewer.getTable();
			int index = table.getSelectionIndices()[0];
			availableListViewer.remove(ssel.toArray());
			importListViewer.add(ssel.toArray());
			table.setSelection(index < table.getItemCount() ? index : table.getItemCount() -1);
			pageChanged();
		}		
	}

	private void handleAddAll() {
		TableItem[] items = availableListViewer.getTable().getItems();

		ArrayList data = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			data.add(items[i].getData());
		}
		if (data.size() > 0) {
			importListViewer.add(data.toArray());
			availableListViewer.remove(data.toArray());
			pageChanged();
		}
	}
	
	private void handleFilter() {
		if (filterText == null ||filterText.getText().trim().length() == 0)
			return;

		String text = filterText.getText().trim();
		if (text.indexOf('*') == -1 && text.indexOf('?') == -1)
			text += "*"; //$NON-NLS-1$
		
		StringMatcher stringMatcher = new StringMatcher(text, false, false);
		TableItem[] tableItems = availableListViewer.getTable().getItems();
		ArrayList results = new ArrayList();
		for (int i = 0; i<tableItems.length; i++){
			Object data = tableItems[i].getData();
			if (data instanceof IPluginModelBase){
				IPluginModelBase model = (IPluginModelBase)data;
				if (stringMatcher.match(model.getPluginBase().getId()))
					results.add(tableItems[i]);
			}
		}
		if (results.size()>0){
			TableItem[] selectionList = (TableItem[])results.toArray(new TableItem[results.size()]);
			availableListViewer.getTable().setSelection(selectionList);
		} else {
			availableListViewer.setSelection(null);
		}
	}
	
	private void handleRemove() {
		IStructuredSelection ssel = (IStructuredSelection)importListViewer.getSelection();
		if (ssel.size() > 0) {
			Table table = importListViewer.getTable();
			int index = table.getSelectionIndices()[0];
			importListViewer.remove(ssel.toArray());
			availableListViewer.add(ssel.toArray());
			table.setSelection(index < table.getItemCount() ? index : table.getItemCount() -1);
			pageChanged();
		}		
	}
	
	private void handleRemoveAll() {
		handleRemoveAll(true);
	}
	
	private void handleRemoveAll(boolean refresh) {
		TableItem[] items = importListViewer.getTable().getItems();
		
		ArrayList data = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			data.add(items[i].getData());
		}
		if (data.size() > 0) {
			availableListViewer.add(data.toArray());
			importListViewer.remove(data.toArray());
			pageChanged();
		}		
	}
	
	private void handleSwap() {
		TableItem[] aItems = availableListViewer.getTable().getItems();
		TableItem[] iItems = importListViewer.getTable().getItems();
		
		ArrayList data = new ArrayList();
		for (int i = 0; i < iItems.length; i++) {
			data.add(iItems[i].getData());
		}
		if (data.size() > 0) {
			availableListViewer.add(data.toArray());
			importListViewer.remove(data.toArray());
		}
		
		data.clear();
		for (int i = 0; i < aItems.length; i++) {
			data.add(aItems[i].getData());
		}
		if (data.size() > 0) {
			importListViewer.add(data.toArray());
			availableListViewer.remove(data.toArray());
		}
		pageChanged();		
	}
	
	private void handleExistingProjects() {
		handleRemoveAll(false);
		ArrayList result = new ArrayList();
		for (int i = 0; i < models.length; i++) {
			String id = models[i].getPluginBase().getId();
			IProject project =
				(IProject) PDEPlugin.getWorkspace().getRoot().findMember(id);
			if (project != null
				&& project.isOpen()
				&& WorkspaceModelManager.isPluginProject(project)) {
				result.add(models[i]);
			}
		}
		if (result.size() > 0) {
			importListViewer.add(result.toArray());
			availableListViewer.remove(result.toArray());
		}
		pageChanged();		
	}
	
	private void handleExistingUnshared() {
		handleRemoveAll(false);
		ArrayList result = new ArrayList();
		for (int i = 0; i < models.length; i++) {
			String id = models[i].getPluginBase().getId();
			IProject project =
				(IProject) PDEPlugin.getWorkspace().getRoot().findMember(id);
			if (project != null
				&& project.isOpen()
				&& WorkspaceModelManager.isUnsharedPluginProject(project)) {
				result.add(models[i]);
			}
		}
		if (result.size() > 0) {
			importListViewer.add(result.toArray());
			availableListViewer.remove(result.toArray());
		}
		pageChanged();		
	}
	
	private void handleAddRequiredPlugins() {
		TableItem[] items = importListViewer.getTable().getItems();
		if (items.length == 0)
			return;
		if (items.length == 1) {
			IPluginModelBase model = (IPluginModelBase) items[0].getData();
			if (model.getPluginBase().getId().equals("org.eclipse.core.boot")) { //$NON-NLS-1$
				return;
			}
		}
						
		ArrayList result = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			addPluginAndDependencies((IPluginModelBase) items[i].getData(), result, addFragmentsButton.getSelection());
		}

		handleRemoveAll(false);
		importListViewer.add(result.toArray());
		availableListViewer.remove(result.toArray());
		pageChanged();		
	}
	

}
