package com.miteksystems.misnap.misnapworkflow.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.miteksystems.misnap.misnapworkflow.R;

import java.util.List;

/**
 * Created by awood on 9/1/2015.
 *
 * This class keeps all the Fragment loading, UI-related code out of the UX workflow code.
 * It's swapping the current workflow fragment for another.
 */
public class FragmentLoader {

    private static final String TAG_OVERLAY = "TAG_OVERLAY";

    public static boolean showScreen(FragmentManager fm, Fragment nextFragment) {
        if (fm == null || nextFragment == null) {
            return false;
        }

        removeOverlayScreens(fm);

        // We identify fragments by their tags to help with unit and regression testing.
        fm.beginTransaction()
                .replace(R.id.misnapWorkflowFragmentContainer, nextFragment, nextFragment.getClass().getSimpleName())
                .commit();

        return true;
    }

    public static boolean overlayScreen(FragmentManager fm, Fragment overlayFragment) {
        if (fm == null || overlayFragment == null) {
            return false;
        }

        // We identify overlays by one tag to make it easy to remove them.
        fm.beginTransaction()
                .add(R.id.misnapWorkflowFragmentContainer, overlayFragment, TAG_OVERLAY)
                .commit();

        return true;
    }

    public static void removeOverlayScreens(FragmentManager fm) {
        if (fm == null) {
            return;
        }

        List<Fragment> fragments = fm.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                // Remove ONLY overlays
                if (fragment != null
                        && fragment.getTag().equals(TAG_OVERLAY)) {
                    fm.beginTransaction()
                            .remove(fragment)
                            .commit();
                }
            }
        }
    }
}
