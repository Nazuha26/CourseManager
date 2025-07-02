/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities.update;

import com.coursemanagerfx.logic.Actions;
import javafx.application.Application;
import javafx.stage.Stage;

public class UpdaterLauncher extends Application {
    @Override public void start(Stage ignored)
        { Actions.getInstance().uiFlowActions().runUpdateFlow(true); }

    public static void main(String[] args)
        { launch(args); }
}
