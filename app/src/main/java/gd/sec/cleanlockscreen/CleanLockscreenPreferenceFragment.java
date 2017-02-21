package gd.sec.cleanlockscreen;

import android.util.Log;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.io.DataOutputStream;
import java.io.IOException;

public class CleanLockscreenPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference restartSystemUI = findPreference("restart_systemui");
        if (restartSystemUI == null){
            Log.e("CleanLockscreen", "restartSystemUI is null");
            return;
        }
        Log.e("CleanLockscreen", "restartSystemUI setting listener");

        restartSystemUI.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.e("CleanLockscreen", "attempting to kill SystemUI");

                // https://stackoverflow.com/a/36792428 (or wherever they got it from)
                // Does not detect su present but refusing elevation
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                } catch (IOException e) {
                    Log.e("CleanLockscreen", "error getting root");
                    e.printStackTrace();
                }

                if (process == null ) {
                    Log.e("CleanLockscreen", "su process is null");
                    return false;
                }
                try {
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("pkill com.android.systemui\n");
                    os.flush();
                    os.writeBytes("exit\n");
                    os.flush();
                    process.waitFor();
                } catch (Exception e) {
                    Log.e("CleanLockscreen", "error killing SystemUI");
                    e.printStackTrace();
                    return false;
                }
                Log.e("CleanLockscreen", "SystemUI kill looks successful");
                return true;
            }
        });

        Log.e("CleanLockscreen", "restartSystemUI set listener");
    }
}