package app.indvel.ibucheon;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class AppInfo extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public static class MyPreferenceFragment extends
            PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_info);

            Preference pref = findPreference("version");
            pref.setSummary(getAppVersion(getActivity().getApplicationContext()));
        }

        public static String getAppVersion(Context context) {

            // application version
            String versionName = "";
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return versionName;
        }
    }
}
