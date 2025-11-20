package uk.org.mattford.scoutlink.utils;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import uk.org.mattford.scoutlink.R;

public final class EdgeToEdgeHelper {

    private static final String STATUS_OVERLAY_TAG = "edge_to_edge_status_overlay";

    private EdgeToEdgeHelper() {
        // Utility class
    }

    public static void prepareWindow(Activity activity) {
        if (!shouldApply()) {
            return;
        }
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
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
        final ViewGroup contentParent = activity.findViewById(android.R.id.content);
        final @ColorInt int statusColor = ContextCompat.getColor(activity, R.color.scoutlink_blue);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                paddingLeft + bars.left,
                paddingTop + bars.top,
                paddingRight + bars.right,
                paddingBottom + bars.bottom
            );
            ensureStatusBarOverlay(contentParent, bars.top, statusColor);
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

    private static void ensureStatusBarOverlay(@Nullable ViewGroup parent, int insetTop, @ColorInt int color) {
        if (parent == null) {
            return;
        }
        View overlay = parent.findViewWithTag(STATUS_OVERLAY_TAG);
        if (overlay == null) {
            overlay = new View(parent.getContext());
            overlay.setTag(STATUS_OVERLAY_TAG);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                insetTop
            );
            params.gravity = Gravity.TOP;
            parent.addView(overlay, 0, params);
        } else {
            ViewGroup.LayoutParams params = overlay.getLayoutParams();
            params.height = insetTop;
            overlay.setLayoutParams(params);
        }
        overlay.setBackgroundColor(color);
        overlay.setVisibility(insetTop > 0 ? View.VISIBLE : View.GONE);
    }

    private static boolean shouldApply() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
    }
}

