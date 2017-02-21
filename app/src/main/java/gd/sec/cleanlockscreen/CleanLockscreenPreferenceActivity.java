package gd.sec.cleanlockscreen;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CleanLockscreenPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CleanLockscreenPreferenceFragment())
                .commit();
    }
}