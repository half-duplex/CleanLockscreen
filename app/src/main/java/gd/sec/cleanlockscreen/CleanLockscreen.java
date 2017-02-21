/*
 * CleanLockscreen Xposed Framework Module
 * By mal <mal@sec.gd>
 *
 * Cleans up the lockscreen by removing elements like the letters (2abc 3def) and carrier label
 *
 * Released under the terms of the GNU GPLv3 license
 */

package gd.sec.cleanlockscreen;

import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Canvas;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CleanLockscreen implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private static String packageName = null;
    private static String MODULE_PATH = null;
    private static XSharedPreferences prefs;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        XposedBridge.log("CleanLockscreen initZygote");

        if(android.os.Build.VERSION.SDK_INT<=20)
            packageName = "com.android.keyguard";
        else
            packageName = "com.android.systemui";

        MODULE_PATH = startupParam.modulePath;
        if(MODULE_PATH == null){
            XposedBridge.log("CleanLockscreen modulePath is null!");
        }
        prefs = new XSharedPreferences(CleanLockscreen.class.getPackage().getName());

        // Remove emergency call text
        if (prefs.getBoolean("remove_emergency", false)) {
            XResources.setSystemWideReplacement("android:string/lockscreen_emergency_call", "");
        }

        XposedBridge.log("CleanLockscreen initZygote complete");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        //XposedBridge.log("CleanLockscreen handleInitPackageResources check " + resparam.packageName);

        if(!resparam.packageName.equals(packageName))
            return;

        XposedBridge.log("CleanLockscreen handleInitPackageResources "+packageName);

        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);

        // Remove letters on numpad
        if (prefs.getBoolean("remove_letters", true)) {
            resparam.res.setReplacement(packageName, "array",
                    "lockscreen_num_pad_klondike", new String[0]);
        }

        // Backspace button and api<=20 recenter numbers
        if(prefs.getBoolean("remove_backspace", true)
                ||(prefs.getBoolean("remove_letters", true)
                    &&android.os.Build.VERSION.SDK_INT<=20)) {
            resparam.res.hookLayout(packageName, "layout", "keyguard_pin_view",
                    new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam)
                        throws Throwable {
                    // Remove backspace button
                    if (prefs.getBoolean("remove_backspace", true)) {
                        ImageButton deleteButton = (ImageButton) liparam.view.findViewById(
                                liparam.res.getIdentifier("delete_button", "id", packageName));
                        deleteButton.setVisibility(ImageButton.GONE);
                    }

                    // Center numbers on each numpad key
                    if (prefs.getBoolean("remove_letters", true) && android.os.Build.VERSION.SDK_INT <= 20) {
                        for (int i = 0; i < 10; i++) {
                            TextView key = (TextView) liparam.view.findViewById(
                                    liparam.res.getIdentifier("key" + Integer.toString(i), "id", packageName));
                            key.setPadding(key.getPaddingRight(), key.getPaddingTop(), key.getPaddingRight(), key.getPaddingBottom());
                            key.setGravity(Gravity.CENTER);
                        }
                    }
                }
            });
        }

        // Remove carrier label for api>20
        if(     (      prefs.getBoolean("remove_carrier", true)
                    || prefs.getBoolean("remove_icons", false)
                    || prefs.getBoolean("remove_user", false)
                ) && android.os.Build.VERSION.SDK_INT>20) {
            resparam.res.hookLayout(packageName, "layout", "keyguard_status_bar",
                    new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam)
                        throws Throwable {
                    if (prefs.getBoolean("remove_carrier", true)) {
                        TextView carrierLabel = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("keyguard_carrier_text", "id", packageName));
                        carrierLabel.setVisibility(TextView.GONE);
                    }
                    if (prefs.getBoolean("remove_icons", false)) {
                        LinearLayout systemIcons = (LinearLayout) liparam.view.findViewById(
                                liparam.res.getIdentifier("system_icons_super_container", "id", packageName));
                        systemIcons.setVisibility(LinearLayout.GONE);
                    }
                    if (prefs.getBoolean("remove_user", false)) {
                        FrameLayout userSwitcher = (FrameLayout) liparam.view.findViewById(
                                liparam.res.getIdentifier("multi_user_switch", "id", packageName));
                        userSwitcher.setVisibility(FrameLayout.GONE);
                    }
                }
            });
        }

        // Re-center pin dots textView
        if (prefs.getBoolean("remove_backspace", true)&&android.os.Build.VERSION.SDK_INT<=20) {
            resparam.res.setReplacement(packageName, "dimen", "keyguard_lockscreen_pin_margin_left",
                    modRes.fwd(R.dimen.keyguard_lockscreen_pin_margin_left)
            );
        }

        // Phone icon
        if(prefs.getBoolean("remove_phone", true)) {
            resparam.res.setReplacement(packageName, "drawable", "ic_phone_24dp",
                    modRes.fwd(R.drawable.empty)
            );
        }

        if(prefs.getBoolean("remove_lock", true)) { // goes with unlocked icon and setBackground below
            // Lock icon
            resparam.res.setReplacement(packageName, "drawable", "ic_lock_24dp",
                    modRes.fwd(R.drawable.empty)
            );
            // Unlocked icon
            resparam.res.setReplacement(packageName, "drawable", "ic_lock_open_24dp",
                    modRes.fwd(R.drawable.empty)
            );
            // Fingerprint icon
            resparam.res.setReplacement(packageName, "drawable", "ic_fingerprint",
                    modRes.fwd(R.drawable.empty)
            );
        }

        // Camera icon
        if(prefs.getBoolean("remove_camera", true)) {
            resparam.res.setReplacement(packageName, "drawable", "ic_camera_alt_24dp",
                    modRes.fwd(R.drawable.empty)
            );
        }

        // Microphone icon
        if(prefs.getBoolean("remove_microphone", true)) {
            resparam.res.setReplacement(packageName, "drawable", "ic_mic_26dp",
                    modRes.fwd(R.drawable.empty)
            );
        }

        if(prefs.getBoolean("remove_status", true)) {
            resparam.res.hookLayout(packageName, "layout", "keyguard_bottom_area",
                    new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam)
                        throws Throwable {
                    // Status text
                    TextView statusText = (TextView) liparam.view.findViewById(
                            liparam.res.getIdentifier("keyguard_indication_text", "id", packageName));
                    statusText.setMaxWidth(0);
                    statusText.setMaxHeight(0);
                }
            });
        }

        // Time
        if(prefs.getBoolean("remove_time", false)
                ||prefs.getBoolean("remove_owner", false)) {
            resparam.res.hookLayout(packageName, "layout", "keyguard_status_view",
                    new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam)
                        throws Throwable {
                    if (prefs.getBoolean("remove_time", false)) {
                        TextView clockView = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("clock_view", "id", packageName));
                        clockView.setVisibility(TextView.GONE);
                    }
                    if (prefs.getBoolean("remove_owner", false)) {
                        TextView ownerInfo = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("owner_info", "id", packageName));
                        ownerInfo.setMaxWidth(0);
                        ownerInfo.setMaxHeight(0);
                    }
                }
            });
        }

        // Date/alarm
        if(prefs.getBoolean("remove_date", false)
                ||prefs.getBoolean("remove_alarm", false)) {
            resparam.res.hookLayout(packageName, "layout", "keyguard_status_area",
                    new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam)
                        throws Throwable {
                    if (prefs.getBoolean("remove_date", false)) {
                        TextView dateView = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("date_view", "id", packageName));
                        dateView.setVisibility(TextView.GONE);
                    }
                    if (prefs.getBoolean("remove_alarm", false)) {
                        TextView alarmView = (TextView) liparam.view.findViewById(
                                liparam.res.getIdentifier("alarm_status", "id", packageName));
                        // visibility is overwritten by something else
                        alarmView.setMaxWidth(0);
                        alarmView.setMaxHeight(0);
                    }
                }
            });
        }

        XposedBridge.log("CleanLockscreen handleInitPackageResources complete");
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Remove carrier label for api<=20
        if (lpparam.packageName.equals("com.android.keyguard")&&prefs.getBoolean("remove_carrier", true)) {
            XposedHelpers.findAndHookMethod("com.android.keyguard.CarrierText", lpparam.classLoader,
                    "updateCarrierText", "com.android.internal.telephony.IccCardConstants$State",
                    CharSequence.class, CharSequence.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    //((TextView)param.thisObject).setText("Custom carrier text");
                    return true;
                }
            });
        }

        // Smart lock circle
        if (lpparam.packageName.equals("com.android.systemui")&&prefs.getBoolean("remove_lock", true)) {
            XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.TrustDrawable",
                    lpparam.classLoader, "draw", Canvas.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return true;
                }
            });
        }
    }
}
