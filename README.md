Clean Lockscreen
================

Clean Lockscreen is an Xposed Framework module that cleans up your lockscreen by removing the backspace button and dialpad style numbers.

Using
-----

Have a rooted Android phone. Install the Xposed Framework Installer. Use the installer to install the Xposed Framework. Install this app. Enable this module in the Xposed Installer. Reboot phone. Profit.

Download
--------
[Xposed Module Repository](http://repo.xposed.info/module/gd.sec.cleanlockscreen)

Building
--------

You will need Android Studio and Android API 22 installed.
* Create the folder ANDROID_SDK/add-ons/addon-xposed_bridge-rovo89-22/
* Download the Xposed Bridge **API** to libs/ in your newly created addon folder
* In your new addon folder, paste the following (edited to your version) into manifest.ini
```
name=Xposed Bridge
vendor=rovo89
description=Xposed Bridge
api=22
revision=64
libraries=de.robv.android.xposed
de.robv.android.xposed=XposedBridgeApi-64.jar;Xposed Bridge
```
* In the same folder, paste the following (edited to your version) into source.properties
```
Addon.NameDisplay=Xposed Bridge
Addon.NameId=xposed_bridge
Addon.VendorDisplay=rovo89
Addon.VendorId=rovo89
AndroidVersion.ApiLevel=22
Pkg.Desc=Xposed Bridge
Pkg.License=Apache 2.0
Pkg.LicenseRef=http\://www.apache.org/licenses/LICENSE-2.0
Pkg.Revision=64
Pkg.SourceUrl=https\://github.com/rovo89/XposedBridge
```
* Running `android list targets` should then show "rovo89:Xposed Bridge:22"
* Open the project in Android Studio and click build

If you don't want to use Android Studio, you're on your own.
