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

package org.eclipse.pde.internal.ui.wizards.plugin;

import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.wizards.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;

/**
 * @author cgwong
 */
public class PluginContentPage extends ContentPage {

	private ModifyListener classListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (isInitialized)
				fChangedGroups |= P_CLASS_GROUP;
			validatePage();
		}
	};
	public PluginContentPage(String pageName, IProjectProvider provider,
			NewProjectCreationPage page, AbstractFieldData data) {
		super(pageName, provider, page, data, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.plugin.ContentPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.NEW_PROJECT_REQUIRED_DATA);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.wizards.plugin.ContentPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPropertyControls(Composite container) {
		Group propertiesGroup = new Group(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 5;
		propertiesGroup.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		propertiesGroup.setLayoutData(gd);
		propertiesGroup.setText(PDEPlugin.getResourceString("ContentPage.pGroup")); //$NON-NLS-1$

		Label label = new Label(propertiesGroup, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ContentPage.pid")); //$NON-NLS-1$
		fIdText = createText(propertiesGroup, propertiesListener);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ContentPage.pversion")); //$NON-NLS-1$
		fVersionText = createText(propertiesGroup, propertiesListener);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ContentPage.pname")); //$NON-NLS-1$
		fNameText = createText(propertiesGroup, propertiesListener);

		label = new Label(propertiesGroup, SWT.NONE);
		label.setText(PDEPlugin.getResourceString("ContentPage.pprovider")); //$NON-NLS-1$
		fProviderText = createText(propertiesGroup, propertiesListener);

		fLibraryLabel = new Label(propertiesGroup, SWT.NONE);
		fLibraryLabel
				.setText(PDEPlugin.getResourceString("ProjectStructurePage.library")); //$NON-NLS-1$
		fLibraryText = createText(propertiesGroup, propertiesListener);
		addPluginSpecificControls(container);
	}

	/**
	 * @param container
	 */
	private void addPluginSpecificControls(Composite container) {
		Group classGroup = new Group(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 5;
		classGroup.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		classGroup.setLayoutData(gd);
		classGroup.setText(PDEPlugin.getResourceString("ContentPage.pClassGroup")); //$NON-NLS-1$

		fGenerateClass = new Button(classGroup, SWT.CHECK);
		fGenerateClass.setText(PDEPlugin.getResourceString("ContentPage.generate")); //$NON-NLS-1$
		fGenerateClass.setSelection(true);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fGenerateClass.setLayoutData(gd);
		fGenerateClass.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fClassLabel.setEnabled(fGenerateClass.getSelection());
				fClassText.setEnabled(fGenerateClass.getSelection());
				fUIPlugin.setEnabled(fGenerateClass.getSelection());
				validatePage();
			}
		});

		fClassLabel = new Label(classGroup, SWT.NONE);
		fClassLabel.setText(PDEPlugin.getResourceString("ContentPage.classname")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalIndent = 30;
		fClassLabel.setLayoutData(gd);
		fClassText = createText(classGroup, classListener);

		fUIPlugin = new Button(classGroup, SWT.CHECK);
		fUIPlugin.setText(PDEPlugin.getResourceString("ContentPage.uicontribution")); //$NON-NLS-1$
		fUIPlugin.setSelection(true);
		gd = new GridData();
		gd.horizontalIndent = 30;
		gd.horizontalSpan = 2;
		fUIPlugin.setLayoutData(gd);
		fUIPlugin.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}
		});
	}

	public void updateData() {
		super.updateData();
		((PluginFieldData) fData).setClassname(fClassText.getText().trim());
		((PluginFieldData) fData).setIsUIPlugin(fUIPlugin.getSelection());
		((PluginFieldData) fData).setDoGenerateClass(fGenerateClass.isEnabled() && fGenerateClass.getSelection());
	}
	
}
