/*
 * CleanLockscreen Xposed Framework Module
 * By mal <mal@sec.gd>
 *
 * Removes the backspace button and the letters ("2abc 3def") from the lockscreen
 *
 * Released under the terms of the GNU GPLv3 license
 */

package gd.sec.cleanlockscreen;

import android.content.res.XModuleResources;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CleanLockscreen implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private static String MODULE_PATH = null;
    private boolean config_remove_backspace = true;
    private boolean config_remove_letters = true;
    private boolean config_remove_carrier = true;
    //private boolean config_remove_googlenow = true;
    //private boolean config_remove_camera = true;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.android.keyguard"))
            return;

        XposedBridge.log("CleanLockscreen: Setting resources");

        // Remove letters on numpad
        if (config_remove_letters) {
            resparam.res.setReplacement("com.android.keyguard:array/lockscreen_num_pad_klondike", new String[0]);
        }

        // Mess with layouts and styles
        resparam.res.hookLayout("com.android.keyguard", "layout", "keyguard_pin_view", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                // Remove backspace button
                if (config_remove_backspace) {
                    ImageButton deleteButton = (ImageButton) liparam.view.findViewById(
                            liparam.res.getIdentifier("delete_button", "id", "com.android.keyguard"));
                    XposedBridge.log("CleanLockscreen: deleteButton=" + deleteButton.toString());
                    deleteButton.setVisibility(ImageButton.GONE);
                }

                // Center numbers on each numpad key
                if (config_remove_letters) {
                    for (int i = 0; i < 10; i++) {
                        TextView key = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("key" + Integer.toString(i), "id", "com.android.keyguard"));
                        key.setPadding(key.getPaddingRight(), key.getPaddingTop(), key.getPaddingRight(), key.getPaddingBottom());
                        key.setGravity(Gravity.CENTER);
                    }
                }
            }
        });

        // Re-center pin dots textView
        if (config_remove_backspace) {
            XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
            resparam.res.setReplacement("com.android.keyguard:dimen/keyguard_lockscreen_pin_margin_left",
                    modRes.fwd(R.dimen.keyguard_lockscreen_pin_margin_left)
            );
        }

        XposedBridge.log("CleanLockscreen: Setting resources completed");
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.keyguard"))
            return;

        XposedBridge.log("CleanLockscreen: Overriding methods");

        if (config_remove_carrier) {
            XposedHelpers.findAndHookMethod("com.android.keyguard.CarrierText", lpparam.classLoader, "updateCarrierText", "com.android.internal.telephony.IccCardConstants$State", CharSequence.class, CharSequence.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    //((TextView)param.thisObject).setText("Custom carrier text");
                    return true;
                }
            });
        }

        XposedBridge.log("CleanLockscreen: Overriding methods completed.");
    }
}