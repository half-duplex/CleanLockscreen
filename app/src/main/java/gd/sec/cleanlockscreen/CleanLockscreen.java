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
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class CleanLockscreen implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        // replacements only for KeyGuard
        if (!resparam.packageName.equals("com.android.keyguard"))
            return;

        XposedBridge.log("CleanLockscreen: Setting resources");

        // Remove letters on numpad
        resparam.res.setReplacement("com.android.keyguard:array/lockscreen_num_pad_klondike", new String[0]);

        // Mess with layouts and styles
        resparam.res.hookLayout("com.android.keyguard", "layout", "keyguard_pin_view", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                // Remove backspace button
                ImageButton deleteButton = (ImageButton) liparam.view.findViewById(
                        liparam.res.getIdentifier("delete_button", "id", "com.android.keyguard"));
                deleteButton.setVisibility(ImageButton.GONE);

                // Center numbers on each numpad key
                for (int i = 0; i < 10; i++) {
                    TextView key = (TextView) liparam.view.findViewById(
                            liparam.res.getIdentifier("key" + Integer.toString(i), "id", "com.android.keyguard"));
                    key.setPadding(key.getPaddingRight(), key.getPaddingTop(), key.getPaddingRight(), key.getPaddingBottom());
                    key.setGravity(Gravity.CENTER);
                }
            }
        });

        // Re-center pin dots textView
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
        resparam.res.setReplacement("com.android.keyguard:dimen/keyguard_lockscreen_pin_margin_left",
                modRes.fwd(R.dimen.keyguard_lockscreen_pin_margin_left)
        );

        XposedBridge.log("CleanLockscreen: Setting resources completed");
    }
}