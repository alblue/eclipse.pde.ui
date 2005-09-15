/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.ui.launcher.AbstractLauncherTab;
import org.eclipse.pde.ui.launcher.IPDELauncherConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class WorkspaceDataBlock extends BaseBlock {

	private Button fClearWorkspaceCheck;
	private Button fAskClearCheck;
	
	public WorkspaceDataBlock(AbstractLauncherTab tab) {
		super(tab);
	}
	
	public void createControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(PDEUIMessages.WorkspaceDataBlock_workspace); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createText(group, PDEUIMessages.WorkspaceDataBlock_location, 0);
		
		Composite buttons = new Composite(group, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginHeight = layout.marginWidth = 0;
		buttons.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		buttons.setLayoutData(gd);
		
		fClearWorkspaceCheck = new Button(buttons, SWT.CHECK);
		fClearWorkspaceCheck.setText(PDEUIMessages.WorkspaceDataBlock_clear);	
		fClearWorkspaceCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fClearWorkspaceCheck.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fAskClearCheck.setEnabled(fClearWorkspaceCheck.getSelection());
				fTab.updateLaunchConfigurationDialog();
			}
		});
		
		createButtons(buttons);
		
		fAskClearCheck = new Button(group, SWT.CHECK);
		fAskClearCheck.setText(PDEUIMessages.WorkspaceDataBlock_askClear);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fAskClearCheck.setLayoutData(gd);
		fAskClearCheck.addSelectionListener(fListener);
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IPDELauncherConstants.LOCATION, getLocation());
		config.setAttribute(IPDELauncherConstants.DOCLEAR, fClearWorkspaceCheck.getSelection());
		config.setAttribute(IPDELauncherConstants.ASKCLEAR, fAskClearCheck.getSelection());
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) throws CoreException {
		String location = configuration.getAttribute(IPDELauncherConstants.LOCATION, (String)null);

		// backward compatibility
		if (location == null)	
			location = configuration.getAttribute(IPDELauncherConstants.LOCATION + "0",  //$NON-NLS-1$
									    LauncherUtils.getDefaultWorkspace());
		
		fLocationText.setText(location);
		fClearWorkspaceCheck.setSelection(configuration.getAttribute(IPDELauncherConstants.DOCLEAR, false));
		fAskClearCheck.setSelection(configuration.getAttribute(IPDELauncherConstants.ASKCLEAR, true));
		fAskClearCheck.setEnabled(fClearWorkspaceCheck.getSelection());
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {		
		configuration.setAttribute(IPDELauncherConstants.LOCATION, LauncherUtils.getDefaultWorkspace()); //$NON-NLS-1$
		configuration.setAttribute(IPDELauncherConstants.DOCLEAR, false);
		configuration.setAttribute(IPDELauncherConstants.ASKCLEAR, true);
	}

	protected String getName() {
		return "workspace location";
	}
	
	protected void handleBrowseWorkspace() {
		super.handleBrowseWorkspace();
		if (fClearWorkspaceCheck.getSelection())
			fClearWorkspaceCheck.setSelection(false);
	}
	
	protected void handleBrowseFileSystem() {
		super.handleBrowseFileSystem();
		if (fClearWorkspaceCheck.getSelection())
			fClearWorkspaceCheck.setSelection(false);
	}
	
}
