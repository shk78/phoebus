/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor.app;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.csstudio.display.builder.editor.Messages;
import org.csstudio.display.builder.model.util.ModelResourceUtil;
import org.phoebus.framework.jobs.JobManager;
import org.phoebus.framework.spi.AppResourceDescriptor;
import org.phoebus.framework.spi.MenuEntry;
import org.phoebus.framework.workbench.ApplicationService;

import javafx.application.Platform;

/** Menu entry for creating a new display, opening in editor
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class NewDisplayMenuEntry implements MenuEntry
{
    @Override
    public String getName()
    {
        return Messages.NewDisplay;
    }

    @Override
    public String getMenuPath()
    {
        return "Display";
    }

    @Override
    public Void call() throws Exception
    {
        // Prompt for file
        final File file = DisplayEditorApplication.promptForFilename(Messages.NewDisplay);
        if (file == null)
            return null;

        JobManager.schedule(Messages.NewDisplay, monitor ->
        {
            // Create file with example content
            final InputStream content = ModelResourceUtil.openResourceStream("examples:/initial.bob");
            Files.copy(content, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Open editor on UI thread
            Platform.runLater(() ->
            {
                final AppResourceDescriptor editor = ApplicationService.findApplication(DisplayEditorApplication.NAME);
                editor.create(file.toURI());
            });
        });

        return null;
    }
}
