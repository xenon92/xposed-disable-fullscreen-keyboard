package com.shubhangrathore.xposed.disablefullscreenkeyboard;

import android.view.Window;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by Shubhang Rathore on 29/7/2014.
 */
public class XposedDisableFullscreenKeyboard implements IXposedHookZygoteInit {

    public static final String CLASS_INPUT_METHOD_SERVICE = "android.inputmethodservice.InputMethodService";
    public static String MODULE_PATH = null;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        MODULE_PATH = startupParam.modulePath;

        final Class<?> mInputMethodServiceClass = XposedHelpers.findClass(CLASS_INPUT_METHOD_SERVICE, null);

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "onEvaluateFullscreenMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "isFullscreenMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "isExtractViewShown", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "onConfigureWindow", Window.class, boolean.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                methodHookParam.args[1] = false;
            }
        });
    }
}
