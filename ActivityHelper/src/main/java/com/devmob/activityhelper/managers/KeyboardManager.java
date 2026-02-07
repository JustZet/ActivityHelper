package com.devmob.activityhelper.managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyboardManager {

    private static KeyboardManager instance;
    private final List<KeyboardListener> listeners = new ArrayList<>();
    private final List<InsetsListener> insetsListeners = new ArrayList<>();

    private boolean isKeyboardVisible = false;

    public static KeyboardManager getInstance() {
        if (instance == null) {
            instance = new KeyboardManager();
        }
        return instance;
    }
    public void initialize(Activity activity, @Nullable InsetsListener listener) {
        View rootView = activity.findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (isVisible != isKeyboardVisible) {
                isKeyboardVisible = isVisible;
                notifyListeners(insets, isVisible);
            }

            if (listener != null) {
                listener.onChange(insets);
            }
            return insets;
        });
    }

    public static void attachKeyboardListener(Activity activity, final KeyboardListener listener) {
        if (activity == null) return;

        final FrameLayout rootLayout = activity.findViewById(android.R.id.content);
        rootLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean wasOpened = false;

            private long lastCloseTime = 0;

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
    }

    public static void closeKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
    public static void closeKeyboard(View view) {
        if (view == null) return;

        InputMethodManager imm = (InputMethodManager)
                view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm == null) return;

        boolean wasKeyboardOpen = imm.isAcceptingText();

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // ✅ Only remove focus if keyboard was open
        if (wasKeyboardOpen) {
            view.clearFocus();
        }
    }


    public static void hideSoftKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
    public static void openKeyboard(Context context, EditText view) {
        view.requestFocus(); // Request focus on the view

        String text = Objects.requireNonNull(view.getText()).toString();
        // Move the cursor to the end of the text (simplified)
        view.setSelection(text.length());

        // Show the keyboard
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void addListener(KeyboardListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void addListener(InsetsListener listener) {
        if (!insetsListeners.contains(listener)) {
            insetsListeners.add(listener);
        }
    }
    public void removeListener(KeyboardListener listener) {
        listeners.remove(listener);
    }
    public void removeListener(InsetsListener listener) {
        insetsListeners.remove(listener);
    }
    private void notifyListeners(WindowInsetsCompat insets, boolean isVisible) {
        for (KeyboardListener listener : new ArrayList<>(listeners)) {
            listener.onKeyboardVisibilityChanged(isVisible);
        }

        for (InsetsListener listener : new ArrayList<>(insetsListeners)) {
            listener.onChange(insets);
        }

    }

    public void cleanup() {
        listeners.clear();
        insetsListeners.clear();
    }

    public interface KeyboardListener {
        void onKeyboardVisibilityChanged(boolean isOpen);
    }
    public interface InsetsListener {
        void onChange(WindowInsetsCompat insets);
    }
}
