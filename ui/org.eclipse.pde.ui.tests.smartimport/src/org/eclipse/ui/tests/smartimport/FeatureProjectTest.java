/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.smartimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.tests.smartimport.plugins.ImportedProject;
import org.eclipse.ui.tests.smartimport.plugins.ProjectProposal;

public class FeatureProjectTest extends ProjectTestTemplate {

	private static final String PROJECT_NAME = "FeatureProject";

	@Override
	File getProjectPath() {
		return new File("target/resources/FeatureProject");
	}

	@Override
	List<ProjectProposal> getExpectedProposals() {
		ArrayList<ProjectProposal> returnList = new ArrayList<>();
		ProjectProposal projectProposal = new ProjectProposal(PROJECT_NAME);
		returnList.add(projectProposal);
		return returnList;
	}

	@Override
	List<ImportedProject> getExpectedImportedProjects() {
		ArrayList<ImportedProject> returnList = new ArrayList<>();
		ImportedProject project = new ImportedProject(PROJECT_NAME, "");
		project.addImportedAs("Eclipse Feature");
		returnList.add(project);
		return returnList;
	}

	@Override
	void checkImportedProject() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		String[] natureIds = {};
		try {
			natureIds = project.getDescription().getNatureIds();
		} catch (CoreException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals("Project should have exactly 1 nature", 1, natureIds.length);
		assertEquals("Project should have feature nature", "org.eclipse.pde.FeatureNature", natureIds[0]);
	}

}
