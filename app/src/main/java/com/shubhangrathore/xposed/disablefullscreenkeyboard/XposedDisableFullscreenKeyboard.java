/*
 * Disable Fullscreen Keyboard
 *
 * Xposed module for android to disable keyboard and text input field
 * covering the whole screen when the device is in landscape orientation
 *
 * Copyright (c) 2014 Shubhang Rathore
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shubhangrathore.xposed.disablefullscreenkeyboard;

import android.util.Log;
import android.view.Window;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by Shubhang Rathore on 29/7/2014.
 */
public class XposedDisableFullscreenKeyboard implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static final String CLASS_INPUT_METHOD_SERVICE = "android.inputmethodservice.InputMethodService";
    private static final String SWIFTKEY_KEYBOARD = "com.touchtype.swiftkey";
    private static final String SWIFTKEY_BETA_KEYBOARD = "com.touchtype.swiftkey.beta";
    private static final String[] SWIFTKEY_KEYBOARD_SERVICES = {
            "com.touchtype.keyboard.service.TouchTypeSoftKeyboard",             //SwiftKey 6.0+
            "com.touchtype.KeyboardService"                                     //SwiftKey 6.2+
    };

    private static final String TAG = "DisableFullKeyboard";

    private static boolean DEBUG = false;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        final Class<?> mInputMethodServiceClass = XposedHelpers.findClass(CLASS_INPUT_METHOD_SERVICE, null);

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "onEvaluateFullscreenMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                if (DEBUG) { Log.i(TAG, "beforeHookedMethod: onEvaluateFullscreenMode"); }
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "isFullscreenMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                if (DEBUG) { Log.i(TAG, "beforeHookedMethod: isFullscreenMode"); }
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "isExtractViewShown", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                if (DEBUG) { Log.i(TAG, "beforeHookedMethod: isExtractViewShown"); }
                methodHookParam.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod(mInputMethodServiceClass, "onConfigureWindow", Window.class, boolean.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                if (DEBUG) { Log.i(TAG, "beforeHookedMethod: onConfigureWindow"); }
                // This forces the 2nd argument of the method to be always false.
                // Hence when the onConfigureWindow method is executed, it executes assuming
                // that it doesn't have to configure the window as fullscreen.
                // This fixes window re-configuration of input fields in landscape mode.
                methodHookParam.args[1] = false;
            }
        });
    }

    /**
     * New updates to SwiftKey have implemented a custom onEvaluateFullscreenMode method
     * instead of using the standard onEvaluateFullscreenMode method from the android
     * InputMethodService. Hooking the custom method so it returns "false" to emulate
     * potrait mode even in landscape. Hence, in landscape mode, the keyboard doesn't
     * re-configure to full screen.
     *
     * @param loadPackageParam
     * @throws Throwable
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        //If package is neither com.touchtype.swiftkey nor com.touchtype.swiftkey.beta,
        //return and not execute further
        if (!loadPackageParam.packageName.equals(SWIFTKEY_KEYBOARD)
                && !loadPackageParam.packageName.equals(SWIFTKEY_BETA_KEYBOARD)) {
            return;
        }

        for (String className : SWIFTKEY_KEYBOARD_SERVICES) {
            final Class<?> mSwiftKeyClass = XposedHelpers.findClassIfExists(className, loadPackageParam.classLoader);
            if (mSwiftKeyClass != null) {
                //Try to hook onto the method, only when the class is found in the package
                XposedHelpers.findAndHookMethod(mSwiftKeyClass, "onEvaluateFullscreenMode", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        if (DEBUG) { Log.i(TAG, "Hooking to SwiftKey onEvaluateFullscreenMode method" +
                                "of Class : " + mSwiftKeyClass.getName()); }
                        methodHookParam.setResult(false);
                    }
                });
            }
        }
    }
}
