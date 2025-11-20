package uk.org.mattford.scoutlink.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {
        // Utility class
    }

    public static void prepareWindow(Activity activity) {
        if (!shouldApply()) {
            return;
        }
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void applyInsetsIfNeeded(Activity activity) {
        if (!shouldApply()) {
            return;
        }
        View root = findContentRoot(activity);
        if (root == null) {
            return;
        }

        final int paddingLeft = root.getPaddingLeft();
        final int paddingTop = root.getPaddingTop();
        final int paddingRight = root.getPaddingRight();
        final int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                paddingLeft + bars.left,
                paddingTop + bars.top,
                paddingRight + bars.right,
                paddingBottom + bars.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private static @Nullable View findContentRoot(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return null;
        }
        if (content.getChildCount() > 0) {
            return content.getChildAt(0);
        }
        return content;
    }

    private static boolean shouldApply() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
    }
}


