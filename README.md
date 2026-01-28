## Why Managers?

- ✔ Centralized UI logic
- ✔ Cleaner Activities & Fragments
- ✔ Consistent behavior across the app
- ✔ Easy to extend and maintain

## Each manager:

Is responsible for one UI container

Uses a builder-style API

Applies changes only when .apply() is called


```
ActivityManager	AppCompatActivity 

DialogManager	DialogFragment

BottomSheetManager	BottomSheetDialogFragment
```
### Install
```gradle
	dependencies {
	        implementation 'com.github.JustZet:ActivityHelper:1.0.0'
	}
```

### Examples:
## ActivityManager
``` java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityManager.with(this)
            .setExtendBehindStatusBar(true)
            .setNavigationBarColor(
                ColorManager.getDynamicColor(
                    this,
                    com.google.android.material.R.attr.colorPrimary
                )
            )
            .setOnBackListener(() -> {
                if (homeViewModel != null) {
                    List<Task> selectedTasks = homeViewModel.getSelectedTaskList().getValue();
                    if (selectedTasks != null && !selectedTasks.isEmpty()) {
                        homeViewModel.clearSelectedTasks();
                    }
                }
            })
            .apply();
}

```


## DialogManager
``` java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    DialogManager.with(getDialog())
            .setCancelable(true)
            .setSize(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.8)
            )
            .setKeyboardListener(requireActivity(), isOpen -> {
                Dialog dialog = getDialog();
                if (dialog != null && dialog.getWindow() != null) {
                    int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
                    int height = isOpen
                            ? (int) (getResources().getDisplayMetrics().heightPixels * 0.5)
                            : (int) (getResources().getDisplayMetrics().heightPixels * 0.8);
                    dialog.getWindow().setLayout(width, height);
                }

                ll_footer.setVisibility(isOpen ? View.GONE : View.VISIBLE);

                if (historyAdapter.getItemCount() > 0) {
                    historyAdapter.scrollToPosition(historyAdapter.getItemCount() - 1);
                }
            })
            .apply();
}
```

## BottomSheetManager
``` java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    BottomSheetManager.with((BottomSheetDialog) requireDialog())
            .setCancelable(true)
            .setSkipCollapsed(true)
            .setNavigationBarColor(
                ColorManager.getDynamicColor(
                    context,
                    com.google.android.material.R.attr.colorSurface
                )
            )
            .setState(BottomSheetBehavior.STATE_EXPANDED)
            .apply();
}
```
