package org.eclipse.pde.internal.wizards.project;

import java.lang.reflect.*;
import org.eclipse.ui.actions.*;
import org.eclipse.jface.operation.*;
import org.eclipse.pde.internal.wizards.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.pde.internal.elements.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.pde.internal.base.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.pde.internal.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jdt.core.*;


public class ProjectCodeGeneratorsPage extends WizardListSelectionPage {
	private Button blankPageRadio;
	private Button templateRadio;
	private Control wizardList;
	private ControlEnableState wizardListEnableState;
	private boolean fragment;
	private IProjectProvider provider;
	private static final String KEY_TITLE = "NewProjectWizard.ProjectCodeGeneratorsPage.title";
	private static final String KEY_BLANK_LABEL = "NewProjectWizard.ProjectCodeGeneratorsPage.blankLabel";
	private static final String KEY_BLANK_FLABEL = "NewProjectWizard.ProjectCodeGeneratorsPage.blankFLabel";
	private static final String KEY_TEMPLATE_LABEL = "NewProjectWizard.ProjectCodeGeneratorsPage.templateLabel";
	private static final String KEY_TEMPLATE_FLABEL = "NewProjectWizard.ProjectCodeGeneratorsPage.templateFLabel";
	private static final String KEY_DESC = "NewProjectWizard.ProjectCodeGeneratorsPage.desc";
	private static final String KEY_FTITLE = "NewProjectWizard.ProjectCodeGeneratorsPage.ftitle";
	private static final String KEY_FDESC = "NewProjectWizard.ProjectCodeGeneratorsPage.fdesc";
	private ProjectStructurePage projectStructurePage;

	public static final String KEY_CODEGEN_TITLE = "NewProjectWizard.ProjectCodeGeneratorsPage.title";
	public static final String KEY_CODEGEN_DESC = "NewProjectWizard.ProjectCodeGeneratorsPage.desc";


public ProjectCodeGeneratorsPage(
	IProjectProvider provider,
	ProjectStructurePage projectStructurePage,
	ElementList wizardElements,
	String message,
	boolean fragment) {
	super(wizardElements, message);
	this.fragment = fragment;
	PDEPlugin plugin = PDEPlugin.getDefault();
	if (fragment) {
		setTitle(PDEPlugin.getResourceString(KEY_FTITLE));
		setDescription(PDEPlugin.getResourceString(KEY_FDESC));
	} else {
		setTitle(PDEPlugin.getResourceString(KEY_TITLE));
		setDescription(PDEPlugin.getResourceString(KEY_DESC));
	}
	this.provider = provider;
	this.projectStructurePage = projectStructurePage;
}
public void createControl(Composite parent) {
	Composite outerContainer = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.verticalSpacing = 9;
	outerContainer.setLayout(layout);

	blankPageRadio = new Button(outerContainer, SWT.RADIO | SWT.LEFT);
	GridData gd = new GridData();
	gd.horizontalAlignment = GridData.FILL;
	gd.verticalAlignment = GridData.BEGINNING;
	gd.grabExcessHorizontalSpace = true;
	blankPageRadio.setSelection(true);
	String labelText =
		fragment
			? PDEPlugin.getResourceString(KEY_BLANK_FLABEL)
			: PDEPlugin.getResourceString(KEY_BLANK_LABEL);
	blankPageRadio.setText(labelText);
	blankPageRadio.setLayoutData(gd);

	templateRadio = new Button(outerContainer, SWT.RADIO | SWT.LEFT);
	labelText =
		fragment
			? PDEPlugin.getResourceString(KEY_TEMPLATE_FLABEL)
			: PDEPlugin.getResourceString(KEY_TEMPLATE_LABEL);
	templateRadio.setText(labelText);
	templateRadio.setSelection(false);
	gd = new GridData();
	gd.horizontalAlignment = GridData.FILL;
	gd.verticalAlignment = GridData.BEGINNING;
	gd.grabExcessHorizontalSpace = true;
	templateRadio.setLayoutData(gd);

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			setWizardListEnabled(templateRadio.getSelection());
			updateWizardButtons();

		}
	};
	blankPageRadio.addSelectionListener(listener);

	gd = new GridData();
	gd.horizontalAlignment = GridData.FILL;
	gd.grabExcessHorizontalSpace = true;
	gd.verticalAlignment = GridData.FILL;
	gd.grabExcessVerticalSpace = true;
	super.createControl(outerContainer);
	wizardList = super.wizardSelectionViewer.getControl();
	Control control = getControl();
	control.setLayoutData(gd);
	setWizardListEnabled(false);
	setControl(outerContainer);
}
protected IWizardNode createWizardNode(WizardElement element) {
	return new WizardNode(this, element) {
		public IBasePluginWizard createWizard() throws CoreException {
			IPluginContentWizard wizard =
				(IPluginContentWizard) wizardElement.createExecutableExtension();
			wizard.init(provider, projectStructurePage.getStructureData(), fragment);
			return wizard;
		}
	};
}
public boolean finish() {
	IProject project = provider.getProject();
	if (blankPageRadio.getSelection()) {
		// we must set the Java settings here
		// because there are no wizards to run
		runJavaSettingsOperation();
	}
	return true;
}
	public IWizardPage getNextPage() {
		if (blankPageRadio.getSelection()) return null;
		return super.getNextPage();
	}
	public boolean isPageComplete() {
		if (blankPageRadio!=null && blankPageRadio.getSelection()) return true;
		return super.isPageComplete();
	}
private void runJavaSettingsOperation() {
	final IPluginStructureData structureData =
		projectStructurePage.getStructureData();
	final IProject project = provider.getProject();

	IRunnableWithProgress operation = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) {
			try {
				setJavaSettings(project, structureData, monitor);
			} catch (JavaModelException e) {
				PDEPlugin.logException(e);
			} catch (CoreException e) {
				PDEPlugin.logException(e);
			} finally {
				monitor.done();
			}
		}
	};
	try {
		getContainer().run(false, true, operation);
	} catch (InvocationTargetException e) {
		PDEPlugin.logException(e);
	} catch (InterruptedException e) {
	}
}
private void setJavaSettings(
	IProject project,
	IPluginStructureData structureData,
	IProgressMonitor monitor)
	throws JavaModelException, CoreException {
	if (project.exists() == false) {
		project.create(monitor);
		project.open(monitor);
	}
	if (!project.hasNature(JavaCore.NATURE_ID))
		CoreUtility.addNatureToProject(project, JavaCore.NATURE_ID, monitor);
	if (!project.hasNature(PDEPlugin.PLUGIN_NATURE))
		CoreUtility.addNatureToProject(project, PDEPlugin.PLUGIN_NATURE, monitor);
	PDEPlugin.registerPlatformLaunchers(project);
	IClasspathEntry[] libraries = new IClasspathEntry[0];
	BuildPathUtil.setBuildPath(project, structureData, libraries, monitor);
}
private void setWizardListEnabled(boolean enabled) {
	if (!enabled) {
		wizardListEnableState = ControlEnableState.disable(wizardList);
	}
	else {
		if (wizardListEnableState!=null)
		   wizardListEnableState.restore();
		wizardList.setFocus();
	}
}
private void updateWizardButtons() {
	getContainer().updateButtons();
}
}
