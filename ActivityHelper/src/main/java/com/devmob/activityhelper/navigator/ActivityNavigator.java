package com.devmob.activityhelper.navigator;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;


import java.util.List;

public class ActivityNavigator {
    private final FragmentManager fragmentManager;
    private final int containerId;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isTransitioning = false;
    private boolean pendingCommit = false;
    private boolean isRestoring = false;

    private static final long TRANSITION_DELAY = 300;

    public ActivityNavigator(FragmentManager fm, View view) {
        this.fragmentManager = fm;
        this.containerId = view.getId();

        isRestoring = !fm.getFragments().isEmpty();
        fragmentManager.addOnBackStackChangedListener(this::onBackStackChanged);

        if (isRestoring) {
            handler.postDelayed(() -> isRestoring = false, 500);
        }
    }

    public void openFragment(Fragment fragment, boolean addToBackStack, boolean animate) {
        // Don't handle during restoration
        if (isRestoring) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();

//        if (animate) {
//            transaction.setCustomAnimations(
//                    R.anim.slide_in_right,
//                    R.anim.slide_out_left,
//                    R.anim.slide_in_left,
//                    R.anim.slide_out_right
//            );
//        }

        transaction.setReorderingAllowed(true);

        String tag = fragment.getClass().getSimpleName();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        Fragment targetFragment = (existingFragment == null) ? fragment : existingFragment;

        // Hide and pause all current fragments BEFORE adding/showing new one
        for (Fragment f : fragmentManager.getFragments()) {
            if (f.isAdded() && f.isVisible()) {
                transaction.hide(f);
                // Move to STARTED state - this triggers onPause() but not onStop()
                transaction.setMaxLifecycle(f, Lifecycle.State.STARTED);
            }
        }

        if (!targetFragment.isAdded()) {
            transaction.add(containerId, targetFragment, tag);
        }

        transaction.show(targetFragment);
        // Move to RESUMED state - this triggers onResume()
        transaction.setMaxLifecycle(targetFragment, Lifecycle.State.RESUMED);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        safeCommit(transaction);
        dismissAllDialogs();
    }

    private void handleBackStackChange() {
        FragmentManager fm = fragmentManager;
        Fragment topFragment = null;
        int count = fm.getBackStackEntryCount();

        if (count > 0) {
            String topTag = fm.getBackStackEntryAt(count - 1).getName();
            topFragment = fm.findFragmentByTag(topTag);
        } else if (!fm.getFragments().isEmpty()) {
            topFragment = fm.getFragments().get(0);
        }

        if (topFragment != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.setReorderingAllowed(true);

            // Hide and pause all other fragments
            for (Fragment f : fm.getFragments()) {
                if (f != topFragment && f.isAdded() && !f.isRemoving()) {
                    if (f.isVisible()) {
                        transaction.hide(f);
                    }
                    transaction.setMaxLifecycle(f, Lifecycle.State.STARTED);
                }
            }

            // Show and resume the top fragment
            if (!topFragment.isVisible()) {
                transaction.show(topFragment);
            }
            transaction.setMaxLifecycle(topFragment, Lifecycle.State.RESUMED);

            transaction.commitAllowingStateLoss();
            debugFragments();

            removePoppedFragmentsAfterAnimation(fm);
        }
    }

    public void debugFragments() {
        FragmentManager fm = fragmentManager;
        List<Fragment> fragments = fm.getFragments();

        Log.d("FragmentDebug", "===== FRAGMENT DEBUG START =====");
        Log.d("FragmentDebug", "Total fragments: " + fragments.size());
        Log.d("FragmentDebug", "BackStack count: " + fm.getBackStackEntryCount());

        for (int i = 0; i < fragments.size(); i++) {
            Fragment f = fragments.get(i);
            Log.d("FragmentDebug", "Fragment #" + i + ":");
            Log.d("FragmentDebug", "  Class: " + f.getClass().getSimpleName());
            Log.d("FragmentDebug", "  Tag: " + f.getTag());
            Log.d("FragmentDebug", "  Added: " + f.isAdded());
            Log.d("FragmentDebug", "  Visible: " + f.isVisible());
            Log.d("FragmentDebug", "  Hidden: " + f.isHidden());
            Log.d("FragmentDebug", "  Removing: " + f.isRemoving());
            Log.d("FragmentDebug", "  Detached: " + f.isDetached());
            Log.d("FragmentDebug", "  InLayout: " + f.isInLayout());
            Log.d("FragmentDebug", "  Lifecycle: " + f.getLifecycle().getCurrentState());
            Log.d("FragmentDebug", "  ---");
        }

        Log.d("FragmentDebug", "BackStack entries:");
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(i);
            Log.d("FragmentDebug", "  [" + i + "] " + entry.getName());
        }
        Log.d("FragmentDebug", "===== FRAGMENT DEBUG END =====");
    }

    private void safeCommit(Runnable commitAction) {
        if (fragmentManager.isStateSaved()) return;

        if (pendingCommit) {
            handler.post(() -> safeCommit(commitAction));
            return;
        }

        pendingCommit = true;

        handler.post(() -> {
            try {
                if (!fragmentManager.isStateSaved()) {
                    commitAction.run();
                }
            } catch (Exception ignored) {}

            pendingCommit = false;
        });
    }

    private void safeCommit(FragmentTransaction transaction) {
        if (fragmentManager.isStateSaved()) return;

        if (pendingCommit) {
            handler.post(() -> safeCommit(transaction));
            return;
        }

        pendingCommit = true;

        handler.post(() -> {
            try {
                if (!fragmentManager.isStateSaved()) {
                    transaction.commitAllowingStateLoss();
                }
            } catch (Exception ignored) {}

            pendingCommit = false;
        });
    }

    private void onBackStackChanged() {
        // Don't handle during restoration
        if (isRestoring) return;
        if (isTransitioning) return;
        isTransitioning = true;

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            handleBackStackChange();
            isTransitioning = false;
        }, TRANSITION_DELAY);
    }

    private void removePoppedFragmentsAfterAnimation(FragmentManager fm) {
        handler.postDelayed(() -> {
            FragmentTransaction removeTransaction = fm.beginTransaction();
            removeTransaction.setReorderingAllowed(true);

            for (Fragment f : fm.getFragments()) {
                if (f.isRemoving()) {
                    removeTransaction.remove(f);
                }
            }

            removeTransaction.commitAllowingStateLoss();
        }, TRANSITION_DELAY);
    }

    public void dismissAllDialogs() {
        dismissAllDialogs(fragmentManager);
    }

    public void dismissAllDialogs(FragmentManager fragmentManager) {
        if (fragmentManager == null) return;

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialog = (DialogFragment) fragment;
                if (dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                    dialog.dismiss();
                }
            }
            dismissAllDialogs(fragment.getChildFragmentManager());
        }
    }

    public int countOpenDialogs() {
        return countOpenDialogs(fragmentManager);
    }

    public int countOpenDialogs(@Nullable FragmentManager fragmentManager) {
        if (fragmentManager == null) return 0;
        int count = 0;

        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialog = (DialogFragment) fragment;
                if (dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                    count++;
                }
            }
            count += countOpenDialogs(fragment.getChildFragmentManager());
        }

        return count;
    }

    public void popToFirstFragment() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack(
                    fragmentManager.getBackStackEntryAt(1).getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            );
        }

        safeCommit(() -> {
            Fragment firstFragment = fragmentManager.findFragmentById(containerId);
            if (firstFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(firstFragment);
                transaction.setMaxLifecycle(firstFragment, Lifecycle.State.RESUMED);
                transaction.commitAllowingStateLoss();
            }
        });
    }
}