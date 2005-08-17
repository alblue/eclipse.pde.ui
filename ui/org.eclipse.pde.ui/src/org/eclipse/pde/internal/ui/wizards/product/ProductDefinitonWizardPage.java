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
package org.eclipse.pde.internal.ui.wizards.product;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.*;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.ui.*;
import org.eclipse.pde.internal.ui.search.*;
import org.eclipse.pde.internal.ui.util.*;
import org.eclipse.pde.internal.ui.wizards.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;

public class ProductDefinitonWizardPage extends WizardPage implements IHyperlinkListener {

	private Text fProductName;
	private Text fPluginText;
	private Text fProductText;
	private Set fProductSet;
	private Combo fApplicationCombo;
	private IProduct fProduct;

	private ModifyListener fListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validatePage();
		}
	};
	
	public ProductDefinitonWizardPage(String pageName, IProduct product) {
		super(pageName);
		setTitle(PDEUIMessages.ProductDefinitonWizardPage_title); 
		setDescription(PDEUIMessages.ProductDefinitonWizardPage_desc); 
		fProduct = product;
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 20;
		comp.setLayout(layout);
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());	
		createProductGroup(toolkit, comp);		
		createApplicationGroup(toolkit, comp);
		toolkit.dispose();
		setControl(comp);
		setPageComplete(false);
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IHelpContextIds.PRODUCT_DEFINITIONS_WIZARD);
	}

	private void createFormText(FormToolkit toolkit, Composite parent, String content, int span) {
		FormText text = toolkit.createFormText(parent, false);
		text.setText(content, true, false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		gd.widthHint = 400;
		text.setLayoutData(gd);
		text.setBackground(null);
		text.addHyperlinkListener(this);
	}

	private void createProductGroup(FormToolkit toolkit, Composite comp) {
		Group group = new Group(comp, SWT.NONE);
		group.setText(PDEUIMessages.ProductDefinitonWizardPage_productGroup); 
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createFormText(toolkit, group, PDEUIMessages.ProductDefinitonWizardPage_productDefinition, 3); 
		
		Label label;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		
		if (fProduct.getName() == null || fProduct.getName().equals("")) { //$NON-NLS-1$
			label = new Label(group, SWT.NONE);
			label.setText(PDEUIMessages.ProductDefinitonWizardPage_productName); 
			
			fProductName = new Text(group, SWT.SINGLE|SWT.BORDER);
			fProductName.setLayoutData(gd);
			fProductName.addModifyListener(fListener);
		}
		
		label = new Label(group, SWT.NONE);
		label.setText(PDEUIMessages.ProductDefinitonWizardPage_plugin); 
		
		fPluginText = new Text(group, SWT.SINGLE|SWT.BORDER);
		fPluginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fPluginText.addModifyListener(fListener);
		
		Button button = new Button(group, SWT.PUSH);
		button.setText(PDEUIMessages.ProductDefinitonWizardPage_browse); 
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		label = new Label(group, SWT.NONE);
		label.setText(PDEUIMessages.ProductDefinitonWizardPage_productId); 
		
		fProductText = new Text(group, SWT.SINGLE|SWT.BORDER);
		fProductText.setLayoutData(gd);
		fProductText.addModifyListener(fListener);
		
	}
	
	private void createApplicationGroup(FormToolkit toolkit, Composite comp) {
		Group group = new Group(comp, SWT.NONE);
		group.setText(PDEUIMessages.ProductDefinitonWizardPage_applicationGroup); 
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createFormText(toolkit, group, PDEUIMessages.ProductDefinitonWizardPage_applicationDefinition, 2); 

		Label label = new Label(group, SWT.NONE);
		label.setText(PDEUIMessages.ProductDefinitonWizardPage_application); 
		
		fApplicationCombo = new Combo(group, SWT.SINGLE|SWT.READ_ONLY);
		fApplicationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fApplicationCombo.setItems(TargetPlatform.getApplicationNames());
		if (fApplicationCombo.getItemCount() > 0)
			fApplicationCombo.setText(fApplicationCombo.getItem(0));	
	}
	
	public void setVisible(boolean visible) {
		if (visible) {
			if (fProductName != null)
				fProductName.setFocus();
			else
				fPluginText.setFocus();
		}
		super.setVisible(visible);
	}

	private void validatePage() {
		String error = null;
		String productName = getProductName();
		String pluginId = getDefiningPlugin();
		IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(pluginId);
		if (productName != null && productName.length() == 0) {
			error = PDEUIMessages.ProductDefinitonWizardPage_noProductName;
		} else if (model == null){ 
			error = PDEUIMessages.ProductDefinitonWizardPage_noPlugin; 
		} else if (model.getUnderlyingResource() == null) {
			error = PDEUIMessages.ProductDefinitonWizardPage_notInWorkspace; 
		} else if (pluginId.length() == 0) {
			error = PDEUIMessages.ProductDefinitonWizardPage_noPluginId; 
		}
		if (error == null)
			error = validateId();
		if (error == null && getProductNameSet().contains(pluginId + "." + fProductText.getText().trim())) { //$NON-NLS-1$
			error = PDEUIMessages.ProductDefinitonWizardPage_productExists; 
		}
		setErrorMessage(error);
		setPageComplete(error == null);
	}
	
	private String validateId() {
		String id = fProductText.getText().trim();
		if (id.length() == 0)
			return PDEUIMessages.ProductDefinitonWizardPage_noProductID; 

		for (int i = 0; i<id.length(); i++){
			if (!id.substring(i,i+1).matches("[a-zA-Z0-9_]")) //$NON-NLS-1$
				return PDEUIMessages.ProductDefinitonWizardPage_invalidId; 
		}
		return null;
	}

	public void linkEntered(HyperlinkEvent e) {
	}

	public void linkExited(HyperlinkEvent e) {
	}

	public void linkActivated(HyperlinkEvent e) {
		String extPoint = Platform.PI_RUNTIME + "." + e.getHref().toString(); //$NON-NLS-1$
		IPluginExtensionPoint point = PDECore.getDefault().findExtensionPoint(extPoint);
		if (point != null)
			new ShowDescriptionAction(point, true).run();
	}

	private void handleBrowse() {
		PluginSelectionDialog dialog = new PluginSelectionDialog(getShell(), PDECore.getDefault().getModelManager().getWorkspaceModels(), false);
		if (dialog.open() == PluginSelectionDialog.OK) {
			IPluginModelBase model = (IPluginModelBase)dialog.getFirstResult();
			fPluginText.setText(model.getPluginBase().getId());
		}
	}
	
	private Set getProductNameSet() {
		if (fProductSet == null)
			fProductSet = TargetPlatform.getProductNameSet();
		return fProductSet;
	}
	
	public String getDefiningPlugin() {
		return fPluginText.getText().trim();
	}
	
	public String getProductId() {
		return fProductText.getText().trim();
	}
	
	public String getApplication() {
		return fApplicationCombo.getText();
	}
	
	public String getProductName() {
		return (fProductName == null) ? null : fProductName.getText().trim();
	}
}
