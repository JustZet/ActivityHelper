package com.devmob.activityhelper;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class ActivityManager {

    private final Activity activity;
    private final Window window;

    // Default values
    private static final int DEFAULT_STATUS_BAR_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_NAVIGATION_BAR_COLOR = Color.BLACK;

    // Optional overrides
    private Integer statusBarColor = null;
    private @ColorInt Integer navigationBarColor = null;
    private Boolean isLightStatusBar = null;
    private Boolean isLightNavigationBar = null;
    private Boolean transparentStatusBar = null;
    private Boolean transparentNavigationBar = null;
    private Boolean fullscreen = null;
    private Boolean extendBehindStatusBar = null;
    private Boolean extendBehindNavigationBar = null;
    private Integer softInputMode = null;
    private Boolean fitsSystemWindows = null;


    private InsetsListener insetsListener;
    private BackListener backListener;

    private ActivityManager(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
    }

    // ENTRY POINT
    public static ActivityManager with(Activity activity) {
        return new ActivityManager(activity);
    }

    // ---- Fluent setters ----

    public ActivityManager setStatusBarColor(int color) {
        this.statusBarColor = color;
        return this;
    }

    public ActivityManager setNavigationBarColor(@ColorInt int color) {
        this.navigationBarColor = color;
        return this;
    }

    public ActivityManager setIsLightStatusBar(boolean light) {
        this.isLightStatusBar = light;
        return this;
    }

    public ActivityManager setIsLightNavigationBar(boolean light) {
        this.isLightNavigationBar = light;
        return this;
    }

    public ActivityManager setTransparentStatusBar(boolean transparent) {
        this.transparentStatusBar = transparent;
        return this;
    }

    public ActivityManager setTransparentNavigationBar(boolean transparent) {
        this.transparentNavigationBar = transparent;
        return this;
    }

    public ActivityManager setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }

    public ActivityManager setExtendBehindStatusBar(boolean extend) {
        this.extendBehindStatusBar = extend;
        return this;
    }

    public ActivityManager setExtendBehindNavigationBar(boolean extend) {
        this.extendBehindNavigationBar = extend;
        return this;
    }

    public ActivityManager setSoftInputMode(int mode) {
        this.softInputMode = mode;
        return this;
    }

    public ActivityManager setFitsSystemWindows(boolean fits) {
        this.fitsSystemWindows = fits;
        return this;
    }

    // ---- APPLY ----
    public void apply() {
        if (true) return;
        if (activity == null || window == null) return;

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // --- LIGHT/DARK ICONS ---
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());

        if (isLightStatusBar != null) insetsController.setAppearanceLightStatusBars(isLightStatusBar);
        if (isLightNavigationBar != null) insetsController.setAppearanceLightNavigationBars(isLightNavigationBar);
        if (navigationBarColor != null) window.setNavigationBarColor(navigationBarColor);

        // --- EXTEND BEHIND SYSTEM BARS ---
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();

        if (extendBehindStatusBar != null && extendBehindStatusBar) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }

        if (extendBehindNavigationBar != null && extendBehindNavigationBar) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        decorView.setSystemUiVisibility(flags);

        // --- FULLSCREEN ---
        if (fullscreen != null && fullscreen) {
            flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flags);
        }

        // --- SOFT INPUT MODE ---
        if (softInputMode != null) window.setSoftInputMode(softInputMode);

        // --- FITS SYSTEM WINDOWS ---
        if (fitsSystemWindows != null) {
            View contentView = window.findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setFitsSystemWindows(fitsSystemWindows);
            }
        }

        // --- INSETS LISTENER ---
        if (insetsListener != null) {
            final FrameLayout rootLayout = activity.findViewById(android.R.id.content);
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                insetsListener.onApply(insets);
                return insets;
            });
        }

        // --- BACK BUTTON LISTENER ---
        if (backListener != null && activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getOnBackPressedDispatcher().addCallback(
                    (AppCompatActivity) activity,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            backListener.onBack();
                        }
                    }
            );
        }
    }

    public ActivityManager setOnApplyInsetsListener(InsetsListener listener) {
        this.insetsListener = listener;
        return this;
    }
    public ActivityManager setOnBackListener(BackListener listener) {
        this.backListener = listener;
        return this;
    }
    // ---- HELPER METHODS ----

    /**
     * Quick setup for transparent status bar with light icons
     */
    public ActivityManager transparentLight() {
        return setTransparentStatusBar(true)
                .setIsLightStatusBar(true);
    }

    /**
     * Quick setup for transparent status bar with dark icons
     */
    public ActivityManager transparentDark() {
        return setTransparentStatusBar(true)
                .setIsLightStatusBar(false);
    }

    /**
     * Quick setup for edge-to-edge layout
     */
    public ActivityManager edgeToEdge() {
        return setTransparentStatusBar(true)
                .setTransparentNavigationBar(true)
                .setExtendBehindStatusBar(true)
                .setExtendBehindNavigationBar(true);
    }

    /**
     * Quick setup for immersive fullscreen
     */
    public ActivityManager immersive() {
        return setFullscreen(true)
                .setTransparentStatusBar(true)
                .setTransparentNavigationBar(true);
    }

    public interface InsetsListener {
        void onApply(WindowInsetsCompat insets);
    }
    public interface BackListener {
        void onBack();
    }

}