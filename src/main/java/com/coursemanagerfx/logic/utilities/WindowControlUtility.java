package com.coursemanagerfx.logic.utilities;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.lang.reflect.Method;

public class WindowControlUtility {

    public static HWND getHWND(Stage stage) {
        try {
            Method getPeer = Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            Object tkStage = getPeer.invoke(stage);

            Method getRawHandle = tkStage.getClass().getDeclaredMethod("getRawHandle");
            getRawHandle.setAccessible(true);
            long handle = (Long) getRawHandle.invoke(tkStage);

            return new HWND(Pointer.createConstant(handle));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get HWND from Stage", e);
        }
    }

    public static void minimize(Stage stage) {
        HWND hwnd = getHWND(stage);
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_MINIMIZE); // Win-native
    }

    public static void maximize(Stage stage) {
        HWND hwnd = getHWND(stage);
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
    }

    public static void restore(Stage stage) {
        HWND hwnd = getHWND(stage);
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
    }

    public static void close(Stage stage) {
        HWND hwnd = getHWND(stage);
        User32.INSTANCE.PostMessage(hwnd, User32.WM_CLOSE, null, null);
    }
}