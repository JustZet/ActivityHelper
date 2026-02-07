<h1 align="center">🔥 ActivityManager</h1>



### Overview:

Applies changes only when .apply() is called
| Manager                                                   | Description                                                               |
| --------------------------------------------------------- | ------------------------------------------------------------------------- |
| :sparkles: `ActivityManager`                                  | Style FragmentActivity and manage behavior.                                        |
| :sparkles: `BottomSheetManager`                             | Style BottomSheetFragment and manage behavior .                                           |
| :sparkles: `DialogManager`                                 | Style DialogFragment and manage behavior.                                   |
| :sparkles: `KeyboardManager`                              | Manage behavior of Keyboard.                                            |

Applies changes only when .apply() is called

| Navigator                                                 | Description                                                               |
| --------------------------------------------------------- | ------------------------------------------------------------------------- |
| :sparkles: `ActivityNavigator`                                | Used to manage activity frame to navigate to different other fragments. |
| :sparkles: `FragmentNavigator`                              | Used to add/remove/manage fragments in frame.                                           |

### Install:
```gradle
dependencies {
		implementation 'com.github.JustZet:AppManager:1.1.1'
}
```

### Examples:

`ActivityManager.java`
```java
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityManager.with(this)
                .setFullscreen(false)
                .setIsLightNavigationBar(false) // Bottom navigation bar style
                .setIsLightStatusBar(false) // Notification bar style
                .setTransparentStatusBar(true) // Notification bar transparency
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) // Input mode
                .setOnBackListener(() -> {
                    // Handle when user taps back
                })
                .setOnApplyInsetsListener(new ActivityManager.InsetsListener() {
                    @Override
                    public void onApply(WindowInsetsCompat insets) {
                        // Get system bars insets (status bar + navigation bar)
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                        int top = systemBars.top;    // Status bar height / top inset
                        int bottom = systemBars.bottom; // Navigation bar height / bottom inset

                        // Apply top inset to appbar padding (keep original left, right, bottom)
                        binding.appbar.setPadding(
                                binding.appbar.getPaddingLeft(), top,
                                binding.appbar.getPaddingRight(), binding.appbar.getPaddingBottom());
                    }
                })
                .apply();
    }
}
```


`BottomSheetManager.java`
```java
public class MyBottomSheet extends BottomSheetDialogFragment {
   public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
	super.onViewCreated(view, savedInstanceState);
	BottomSheetManager.with((BottomSheetDialog) requireDialog())
			.setCancelable(true) // User can dismiss clicking outside
			.setDraggable(true) // User can drag
			.setHideable(false) // User can hide
			.setFitToConstraints(true)
			.setSkipCollapsed(false) // User can collapse (If true peekHeight is ignored)
			.setDimAmount(0.5f) // Darkness behind BottomSheet
			.setExpandOffset(50) // The margin on top when full expanded
			.setBlurRadius(40) // Blur behind of BottomSheet
			.setNavigationBarColor(Color.BLACK) // Set color for navigation bar
			.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.5)) // Min height amount (when STATE_COLLAPSED)
			.setState(BottomSheetBehavior.STATE_EXPANDED) // State of dropdown

			.setKeyboardListener(new BottomSheetManager.KeyboardVisibilityListener() {
				@Override
				public void onKeyboardVisibilityChanged(boolean isOpened) {
					// Manage keyboard state change
				}
			})
			.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
			.apply();
	}
}
```
`KeyboardManager.java`
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	KeyboardManager.attachKeyboardListener(this, (KeyboardManager.KeyboardListener) isOpened -> {
		// Manage keyboard state change
	});
	KeyboardManager.openKeyboard(this, binding.autoCompleteTextView); // Open keyboard for view
	KeyboardManager.closeKeyboard(binding.autoCompleteTextView); // Close keyboard and clear focus from view
	KeyboardManager.closeKeyboard(this); // Closing keyboard related to activity
}
```

`ActivityNavigator.java`
```java
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ActivityNavigator navigator =
			ActivityNavigator.with(getSupportFragmentManager(), binding.frame); // Init
				.setDebugEnabled(true) // View all backstack fragments in console
        navigator.openFragment(new SplashScreenFragment(), false, true); // Open new fragment with backstack add
        navigator.dismissAllDialogs(); // Dismiss dialogs
        navigator.popToFirstFragment(); // Pop all visible fragments and return to first fragment

        int countDialogs = navigator.countOpenDialogs(); // Get count of dialogs visible
	}
}
```

`FragmentNavigator.java`
```java 
// Adding fragment
FragmentNavigator.with(getChildFragmentManager())
	.setOnFragmentAdded(fragment -> {
		bindEnabled(false);
	})
	.setOnFragmentRemoved(fragment -> {
		bindEnabled(true);
	})
	.addFragment(bottomSheet, frameLayout);
});

// Removing fragment
FragmentNavigator.with(getChildFragmentManager())
	.removeFragment(frameLayout);
```



