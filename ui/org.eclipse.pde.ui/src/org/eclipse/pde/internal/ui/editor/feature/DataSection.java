/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.feature;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.core.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.feature.*;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.pde.internal.ui.editor.ModelDataTransfer;
import org.eclipse.pde.internal.ui.elements.*;
import org.eclipse.pde.internal.ui.parts.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.widgets.Section;

public class DataSection
	extends TableSection
	implements IModelProviderListener {
	private static final String SECTION_TITLE = "FeatureEditor.DataSection.title"; //$NON-NLS-1$
	private static final String SECTION_DESC = "FeatureEditor.DataSection.desc"; //$NON-NLS-1$
	private static final String KEY_NEW = "FeatureEditor.DataSection.new"; //$NON-NLS-1$
	private static final String POPUP_NEW = "Menus.new.label"; //$NON-NLS-1$
	private static final String POPUP_DELETE = "Actions.delete.label"; //$NON-NLS-1$
	private PropertiesAction propertiesAction;
	private TableViewer dataViewer;
	private Action newAction;
	private Action openAction;
	private Action deleteAction;

	class PluginContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			if (parent instanceof IFeature) {
				return ((IFeature) parent).getData();
			}
			return new Object[0];
		}
	}

	public DataSection(FeatureAdvancedPage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION, new String[] { PDEPlugin.getResourceString(KEY_NEW)});
		getSection().setText(PDEPlugin.getResourceString(SECTION_TITLE));
		getSection().setDescription(PDEPlugin.getResourceString(SECTION_DESC));
		getTablePart().setEditable(false);
		//setCollapsable(true);
		//IFeatureModel model = (IFeatureModel)page.getModel();
		//IFeature feature = model.getFeature();
		//setCollapsed(feature.getData().length==0);
	}

	public void commit(boolean onSave) {
		super.commit(onSave);
	}

	public void createClient(Section section, FormToolkit toolkit) {
		Composite container = createClientContainer(section, 2, toolkit);
		GridLayout layout = (GridLayout) container.getLayout();
		layout.verticalSpacing = 9;

		createViewerPartControl(container, SWT.MULTI, 2, toolkit);
		TablePart tablePart = getTablePart();
		dataViewer = tablePart.getTableViewer();
		dataViewer.setContentProvider(new PluginContentProvider());
		dataViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
		toolkit.paintBordersFor(container);
		makeActions();
		section.setClient(container);
		initialize();
	}

	protected void handleDoubleClick(IStructuredSelection selection) {
		openAction.run();
	}

	protected void buttonSelected(int index) {
		if (index == 0)
			handleNew();
	}

	public void dispose() {
		IFeatureModel model = (IFeatureModel) getPage().getModel();
		if (model!=null)
			model.removeModelChangedListener(this);
		WorkspaceModelManager mng = PDECore.getDefault().getWorkspaceModelManager();
		mng.removeModelProviderListener(this);
		super.dispose();
	}
	public boolean setFormInput(Object object) {
		if (object instanceof IFeatureData) {
			dataViewer.setSelection(new StructuredSelection(object), true);
			return true;
		}
		return false;
	}
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(openAction);
		manager.add(new Separator());
		manager.add(newAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		getPage().getPDEEditor().getContributor().contextMenuAboutToShow(manager);
		manager.add(new Separator());
		manager.add(propertiesAction);
	}

	private void handleNew() {
		final IFeatureModel model = (IFeatureModel) getPage().getModel();
		IResource resource = model.getUnderlyingResource();
		final IContainer folder = resource.getParent();

		BusyIndicator.showWhile(dataViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				ResourceSelectionDialog dialog =
					new ResourceSelectionDialog(dataViewer.getTable().getShell(), folder, null);
				dialog.open();
				Object[] result = dialog.getResult();
				processNewResult(model, folder, result);
			}
		});
	}
	private void processNewResult(
		IFeatureModel model,
		IContainer folder,
		Object[] result) {
		if (result==null || result.length==0) return;
		IPath folderPath = folder.getProjectRelativePath();
		ArrayList entries = new ArrayList();
		for (int i = 0; i < result.length; i++) {
			Object item = result[i];
			if (item instanceof IFile) {
				IFile file = (IFile) item;
				IPath filePath = file.getProjectRelativePath();
				int matching = filePath.matchingFirstSegments(folderPath);
				IPath relativePath = filePath.removeFirstSegments(matching);
				entries.add(relativePath);
			}
		}
		if (entries.size() > 0) {
			try {
				IFeatureData[] array = new IFeatureData[entries.size()];
				for (int i = 0; i < array.length; i++) {
					IFeatureData data = model.getFactory().createData();
					IPath path = (IPath) entries.get(i);
					data.setId(path.toString());
					array[i] = data;
				}
				model.getFeature().addData(array);
			} catch (CoreException e) {
				PDEPlugin.logException(e);
			}
		}
	}
	private void handleSelectAll() {
		IStructuredContentProvider provider =
			(IStructuredContentProvider) dataViewer.getContentProvider();
		Object[] elements = provider.getElements(dataViewer.getInput());
		StructuredSelection ssel = new StructuredSelection(elements);
		dataViewer.setSelection(ssel);
	}
	private void handleDelete() {
		IStructuredSelection ssel = (IStructuredSelection) dataViewer.getSelection();

		if (ssel.isEmpty())
			return;
		IFeatureModel model = (IFeatureModel) getPage().getModel();
		IFeature feature = model.getFeature();

		try {
			IFeatureData[] removed = new IFeatureData[ssel.size()];
			int i = 0;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				IFeatureData iobj = (IFeatureData) iter.next();
				removed[i++] = iobj;
			}
			feature.removeData(removed);
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}
	public boolean doGlobalAction(String actionId) {
		if (actionId.equals(ActionFactory.DELETE.getId())) {
			BusyIndicator.showWhile(dataViewer.getTable().getDisplay(), new Runnable() {
				public void run() {
					handleDelete();
				}
			});
			return true;
		}
		if (actionId.equals(ActionFactory.SELECT_ALL.getId())) {
			BusyIndicator.showWhile(dataViewer.getTable().getDisplay(), new Runnable() {
				public void run() {
					handleSelectAll();
				}
			});
			return true;
		}
		if (actionId.equals(ActionFactory.CUT.getId())) {
			// delete here and let the editor transfer
			// the selection to the clipboard
			handleDelete();
			return false;
		}
		if (actionId.equals(ActionFactory.PASTE.getId())) {
			doPaste();
			return true;
		}
		return false;
	}
	protected void selectionChanged(IStructuredSelection selection) {
		getPage().getPDEEditor().setSelection(selection);
	}
	public void initialize() {
		IFeatureModel model = (IFeatureModel)getPage().getModel();
		refresh();
		getTablePart().setButtonEnabled(0, model.isEditable());
		model.addModelChangedListener(this);
		WorkspaceModelManager mng = PDECore.getDefault().getWorkspaceModelManager();
		mng.addModelProviderListener(this);
	}

	public void modelChanged(IModelChangedEvent e) {
		if (e.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
			markStale();
			return;
		}
		Object obj = e.getChangedObjects()[0];
		if (obj instanceof IFeatureData && !(obj instanceof IFeaturePlugin)) {
			if (e.getChangeType() == IModelChangedEvent.CHANGE) {
				dataViewer.update(obj, null);
			} else if (e.getChangeType() == IModelChangedEvent.INSERT) {
				dataViewer.add(e.getChangedObjects());
			} else if (e.getChangeType() == IModelChangedEvent.REMOVE) {
				dataViewer.remove(e.getChangedObjects());
			}
		}
	}
	private void makeActions() {
		IModel model = (IModel)getPage().getModel();
		newAction = new Action() {
			public void run() {
				handleNew();
			}
		};
		newAction.setText(PDEPlugin.getResourceString(POPUP_NEW));
		newAction.setEnabled(model.isEditable());

		deleteAction = new Action() {
			public void run() {
				BusyIndicator.showWhile(dataViewer.getTable().getDisplay(), new Runnable() {
					public void run() {
						handleDelete();
					}
				});
			}
		};
		deleteAction.setEnabled(model.isEditable());
		deleteAction.setText(PDEPlugin.getResourceString(POPUP_DELETE));
		openAction = new OpenReferenceAction(dataViewer);
		propertiesAction = new PropertiesAction(getPage().getPDEEditor());
	}

	public void modelsChanged(IModelProviderEvent event) {
		markStale();
	}

	public void setFocus() {
		if (dataViewer != null)
			dataViewer.getTable().setFocus();
	}

	public void refresh() {
		IFeatureModel model = (IFeatureModel)getPage().getModel();
		IFeature feature = model.getFeature();
		dataViewer.setInput(feature);
		super.refresh();
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#canPaste(Object, Object[])
	 */
	protected boolean canPaste(Object target, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof FeaturePlugin || !(objects[i] instanceof FeatureData))
				return false;
		}
		return true;
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste()
	 */
	protected void doPaste() {
		Clipboard clipboard = getPage().getPDEEditor().getClipboard();
		ModelDataTransfer modelTransfer = ModelDataTransfer.getInstance();
		Object [] objects = (Object[])clipboard.getContents(modelTransfer);
		if (objects != null) {
			doPaste(null, objects);
		}
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste(Object, Object[])
	 */
	protected void doPaste(Object target, Object[] objects) {
		IFeatureModel model = (IFeatureModel) getPage().getModel();
		IFeature feature = model.getFeature();
		FeatureData[] fData = new FeatureData[objects.length];
		try {
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof FeatureData && !(objects[i] instanceof FeaturePlugin)) {
					FeatureData fd = (FeatureData) objects[i];
					fd.setModel(model);
					fd.setParent(feature);
					fData[i] = fd;
				}
			}
			feature.addData(fData);
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}
}
