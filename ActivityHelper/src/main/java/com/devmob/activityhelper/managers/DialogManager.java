package com.devmob.activityhelper.managers;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.StyleRes;

public class DialogManager {

    private final Dialog dialog;
    private final Window window;

    // Default values
    private static final float DEFAULT_ELEVATION = 0f;
    private static final float DEFAULT_DIM = 0.3f;
    private static final int DEFAULT_BLUR_RADIUS = 20;
    // Optional overrides
    private Integer width = null;
    private Integer height = null;
    private Integer gravity = Gravity.CENTER;
    private Integer softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
    private Boolean cancelable = null;
    private Float dimAmount = null;
    private @StyleRes Integer animations = null;
    private Integer blurRadius = null;

    private DialogManager(Dialog dialog) {
        this.dialog = dialog;
        this.window = dialog.getWindow();


    }

    // ENTRY POINT
    public static DialogManager with(Dialog dialog) {
        return new DialogManager(dialog);
    }

    // ---- Fluent setters ----

    public DialogManager setCancelable(boolean value) {
        this.cancelable = value;
        return this;
    }

    public DialogManager setGravity(int value) {
        this.gravity = value;
        return this;
    }

    public DialogManager setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public DialogManager setSoftInputMode(int mode) {
        this.softInputMode = mode;
        return this;
    }

    public DialogManager setDimAmount(float amount) {
        this.dimAmount = amount;
        return this;
    }

    public DialogManager setBlurRadius(int radius) {
        this.blurRadius = radius;
        return this;
    }
    public DialogManager setAnimations(int animations) {
        this.animations = animations;
        return this;
    }

    // ---- APPLY ----
    public void apply() {
        if (dialog == null || window == null) return;

        // --- DEFAULTS ---
        if (animations != null) window.setWindowAnimations(animations);
        window.setDimAmount(DEFAULT_DIM);
//        window.setBackgroundDrawableResource(DEFAULT_BACKGROUND);
        window.setElevation(DEFAULT_ELEVATION); // Biggest elevation changes background color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            window.getAttributes().setBlurBehindRadius(DEFAULT_BLUR_RADIUS);
        }
//
////        dialog.
//        // --- OVERRIDES ---
        if (dimAmount != null) window.setDimAmount(dimAmount);
        if (cancelable != null) dialog.setCancelable(cancelable);
        if (gravity != null) window.setGravity(gravity);
        if (width != null && height != null) window.setLayout(width, height);
        if (softInputMode != null) window.setSoftInputMode(softInputMode);
    }

    public DialogManager setKeyboardListener(Activity activity, final KeyboardVisibilityListener listener) {
        if (activity == null) return this;
        final FrameLayout rootLayout = activity.findViewById(android.R.id.content);
        rootLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean wasOpened = false;

            @Override
            public boolean onPreDraw() {
                Rect rect = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootLayout.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                boolean isKeyboardOpen = keypadHeight > screenHeight * 0.15;
                if (isKeyboardOpen != wasOpened) {
                    wasOpened = isKeyboardOpen;
                    listener.onKeyboardVisibilityChanged(isKeyboardOpen);
                }
                return true;
            }
        });
        return this;
    }

    public interface KeyboardVisibilityListener {
        void onKeyboardVisibilityChanged(boolean isOpen);
    }


}
