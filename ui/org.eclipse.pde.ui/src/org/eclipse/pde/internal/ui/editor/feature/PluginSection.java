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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.pde.core.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.feature.*;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.editor.*;
import org.eclipse.pde.internal.ui.editor.ModelDataTransfer;
import org.eclipse.pde.internal.ui.elements.*;
import org.eclipse.pde.internal.ui.parts.*;
import org.eclipse.pde.internal.ui.wizards.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.widgets.Section;

public class PluginSection
	extends TableSection
	implements IModelProviderListener {
	private static final String PLUGIN_TITLE =
		"FeatureEditor.PluginSection.pluginTitle"; //$NON-NLS-1$
	private static final String PLUGIN_DESC =
		"FeatureEditor.PluginSection.pluginDesc"; //$NON-NLS-1$
	private static final String KEY_NEW = "FeatureEditor.PluginSection.new"; //$NON-NLS-1$
	public static final String POPUP_NEW = "Menus.new.label"; //$NON-NLS-1$
	public static final String POPUP_OPEN = "Actions.open.label"; //$NON-NLS-1$
	public static final String POPUP_DELETE = "Actions.delete.label"; //$NON-NLS-1$
	private OpenReferenceAction openAction;
	private PropertiesAction propertiesAction;
	private TableViewer pluginViewer;
	private Action newAction;
	private Action deleteAction;

	class PluginContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			if (parent instanceof IFeature) {
				return ((IFeature) parent).getPlugins();
			}
			return new Object[0];
		}
	}

	public PluginSection(FeatureReferencePage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION, new String[] { PDEPlugin.getResourceString(KEY_NEW)});
		getSection().setText(PDEPlugin.getResourceString(PLUGIN_TITLE));
		getSection().setDescription(PDEPlugin.getResourceString(PLUGIN_DESC));
		getTablePart().setEditable(false);
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
		pluginViewer = tablePart.getTableViewer();
		pluginViewer.setContentProvider(new PluginContentProvider());
		pluginViewer.setLabelProvider(PDEPlugin.getDefault().getLabelProvider());
		pluginViewer.setSorter(ListUtil.NAME_SORTER);
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
		if (object instanceof IFeaturePlugin) {
			pluginViewer.setSelection(new StructuredSelection(object), true);
			return true;
		}
		return false;
	}
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(openAction);
		// add new
		manager.add(new Separator());
		manager.add(newAction);
		manager.add(deleteAction);
		// add delete

		getPage().getPDEEditor().getContributor().contextMenuAboutToShow(manager);
		manager.add(new Separator());
		manager.add(propertiesAction);
	}

	private void handleNew() {
		final IFeatureModel model = (IFeatureModel) getPage().getModel();
		BusyIndicator.showWhile(pluginViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				NewFeaturePluginWizardPage page = new NewFeaturePluginWizardPage(model);
				ReferenceWizard wizard = new ReferenceWizard(model, page);
				WizardDialog dialog =
					new WizardDialog(PDEPlugin.getActiveWorkbenchShell(), wizard);
				dialog.create();
				dialog.open();
			}
		});
	}
	private void handleSelectAll() {
		IStructuredContentProvider provider =
			(IStructuredContentProvider) pluginViewer.getContentProvider();
		Object[] elements = provider.getElements(pluginViewer.getInput());
		StructuredSelection ssel = new StructuredSelection(elements);
		pluginViewer.setSelection(ssel);
	}
	private void handleDelete() {
		IStructuredSelection ssel = (IStructuredSelection) pluginViewer.getSelection();

		if (ssel.isEmpty())
			return;
		IFeatureModel model = (IFeatureModel) getPage().getModel();
		IFeature feature = model.getFeature();

		try {
			IFeaturePlugin[] removed = new IFeaturePlugin[ssel.size()];
			int i = 0;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				IFeaturePlugin iobj = (IFeaturePlugin) iter.next();
				removed[i++] = iobj;
			}
			feature.removePlugins(removed);
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}
	public boolean doGlobalAction(String actionId) {
		if (actionId.equals(ActionFactory.DELETE.getId())) {
			BusyIndicator.showWhile(pluginViewer.getTable().getDisplay(), new Runnable() {
				public void run() {
					handleDelete();
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
		if (actionId.equals(ActionFactory.SELECT_ALL.getId())) {
			BusyIndicator.showWhile(pluginViewer.getTable().getDisplay(), new Runnable() {
				public void run() {
					handleSelectAll();
				}
			});
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
		if (obj instanceof IFeaturePlugin) {
			if (e.getChangeType() == IModelChangedEvent.CHANGE) {
				pluginViewer.update(obj, null);
			} else if (e.getChangeType() == IModelChangedEvent.INSERT) {
				pluginViewer.add(e.getChangedObjects());
			} else if (e.getChangeType() == IModelChangedEvent.REMOVE) {
				pluginViewer.remove(e.getChangedObjects());
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
				BusyIndicator.showWhile(pluginViewer.getTable().getDisplay(), new Runnable() {
					public void run() {
						handleDelete();
					}
				});
			}
		};
		deleteAction.setText(PDEPlugin.getResourceString(POPUP_DELETE));
		deleteAction.setEnabled(model.isEditable());
		openAction = new OpenReferenceAction(pluginViewer);
		propertiesAction = new PropertiesAction(getPage().getPDEEditor());
	}

	public void modelsChanged(IModelProviderEvent event) {
		IModel [] added = event.getAddedModels();
		IModel [] removed = event.getRemovedModels();
		IModel [] changed = event.getChangedModels();
		if (hasPluginModels(added)||hasPluginModels(removed)||hasPluginModels(changed))		
			markStale();
	}
	private boolean hasPluginModels(IModel [] models) {
		if (models==null) return false;
		if (models.length==0) return false;
		for (int i=0; i<models.length; i++) {
			if (models[i] instanceof IPluginModelBase)
				return true;
		}
		return false;
	}

	public void setFocus() {
		if (pluginViewer != null)
			pluginViewer.getTable().setFocus();
	}

	public void refresh() {
		IFeatureModel model = (IFeatureModel)getPage().getModel();
		IFeature feature = model.getFeature();
		pluginViewer.setInput(feature);
		super.refresh();
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#canPaste(Clipboard)
	 */
	public boolean canPaste(Clipboard clipboard) {
		Object [] objects = (Object[])clipboard.getContents(ModelDataTransfer.getInstance());
		if (objects != null && objects.length > 0) {
			return canPaste(null, objects);
		}
		return false;
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#canPaste(Object, Object[])
	 */
	protected boolean canPaste(Object target, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (!(objects[i] instanceof FeaturePlugin))
				return false;
		}
		return true;
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste()
	 */
	protected void doPaste() {
		Clipboard clipboard = getPage().getPDEEditor().getClipboard();
		Object [] objects = (Object[])clipboard.getContents(ModelDataTransfer.getInstance());
		if (objects != null && canPaste(null,objects))
			doPaste(null, objects);
	}
	/**
	 * @see org.eclipse.pde.internal.ui.editor.StructuredViewerSection#doPaste(Object, Object[])
	 */
	protected void doPaste(Object target, Object[] objects) {
		IFeatureModel model = (IFeatureModel) getPage().getModel();
		IFeature feature = model.getFeature();
		FeaturePlugin[] fPlugins = new FeaturePlugin[objects.length];
		try {
			for (int i = 0; i < objects.length; i++) {
				FeaturePlugin fPlugin = (FeaturePlugin) objects[i];
				fPlugin.setModel(model);
				fPlugin.setParent(feature);
				fPlugin.hookWithWorkspace();
				fPlugins[i] = fPlugin;
			}
			feature.addPlugins(fPlugins);
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}
}
