package com.coursemanagerfx.updater;

import com.coursemanagerfx.logic.Actions;
import javafx.application.Application;
import javafx.stage.Stage;

public class UpdaterLauncher extends Application {
    @Override public void start(Stage ignored)
        { Actions.getInstance().loadingActions().updateWindow(true); }

    public static void main(String[] args)
        { launch(args); }
}
