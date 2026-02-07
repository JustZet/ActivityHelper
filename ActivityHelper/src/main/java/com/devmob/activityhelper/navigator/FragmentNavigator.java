package com.devmob.activityhelper.navigator;


import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * AppFragmentManagement:
 * Safe fragment manager for adding, replacing, and removing fragments.
 * Handles nested fragments, backstack, animation, and avoids crashes.
 */
public class FragmentNavigator {

    private final FragmentManager fragmentManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean committing = false; // queue flag
    private FragmentRemovedCallback removeCallback;
    private FragmentAddedCallback callback;

    public FragmentNavigator(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
    public static FragmentNavigator with(FragmentManager fragmentManager) {
        return new FragmentNavigator(fragmentManager);
    }

    // ----------------------------------------
    // Add or replace fragment
    // ----------------------------------------
    public FragmentNavigator addFragment(Fragment fragment, FrameLayout frameLayout) {
        queueCommit(() -> {
            if (fragmentManager.isStateSaved()) return;

            String tag = fragment.getClass().getSimpleName();

            // Prevent duplicate fragment
            Fragment existing = fragmentManager.findFragmentByTag(tag);
            if (existing != null && existing.isAdded()) return;

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setReorderingAllowed(true);

            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentViewCreated(FragmentManager fm, Fragment f, android.view.View v, android.os.Bundle savedInstanceState) {
                    if (f == fragment && callback != null) {
                        callback.onFragmentAdded(f);
                    }
                }
                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                    if (f == fragment && removeCallback != null) {
                        removeCallback.onFragmentRemoved(f);
                        fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                    }
                }
            }, false);


            transaction.replace(frameLayout.getId(), fragment, tag);
            transaction.commitAllowingStateLoss();
        });
        return this;
    }

    public FragmentNavigator removeFragment(FrameLayout frameLayout) {
        queueCommit(() -> {
            if (fragmentManager.isStateSaved()) return;

            Fragment target = null;

            // Find fragment in the container
            for (Fragment fragment : fragmentManager.getFragments()) {
                if (fragment != null && fragment.isAdded() && !fragment.isRemoving() && fragment.getView() != null) {
                    // Check if fragment's root view parent matches this container
                    if (fragment.getView().getParent() != null &&
                            ((View) fragment.getView().getParent()).getId() == frameLayout.getId()) {
                        target = fragment;
                        break;
                    }
                }
            }

            if (target == null) return; // nothing to remove

            Fragment fragmentToRemove = target;

            // Listen for fragment destroy to call callback
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                    if (f == fragmentToRemove) {
                        if (removeCallback != null) removeCallback.onFragmentRemoved(f);
                        fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                    }
                }
            }, false);

            // Remove the fragment safely
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(target)
                    .commitAllowingStateLoss();
        });

        return this;
    }





    // ----------------------------------------
    // Internal commit queue
    // ----------------------------------------
    private void queueCommit(Runnable action) {
        if (fragmentManager.isStateSaved()) return;

        if (committing) {
            handler.post(() -> queueCommit(action));
            return;
        }

        committing = true;

        handler.post(() -> {
            try {
                if (!fragmentManager.isStateSaved()) {
                    action.run();
                }
            } catch (Exception ignored) {
            } finally {
                committing = false;
            }
        });
    }

    public FragmentNavigator setOnFragmentAdded(FragmentAddedCallback callback) {
        this.callback = callback;
        return this;
    }
    public FragmentNavigator setOnFragmentRemoved(FragmentRemovedCallback callback) {
        this.removeCallback = callback;
        return this;
    }
    public interface FragmentAddedCallback {
        void onFragmentAdded(Fragment fragment);
    }
    public interface FragmentRemovedCallback {
        void onFragmentRemoved(Fragment fragment);
    }
}
