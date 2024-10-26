package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;

import static org.telegram.messenger.AndroidUtilities.dp;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class TooltipBotStartButton {

    private PopupWindow popupWindow;
    private final Context context;
    private final Theme.ResourcesProvider resourcesProvider;

    public TooltipBotStartButton(Context context, Theme.ResourcesProvider provider) {
        this.context = context;
        resourcesProvider = provider;
    }

    public void hide() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        popupWindow = null;
    }

    public void show(View anchorView) {
        LinearLayout tooltipView = createContainerView();
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }

        popupWindow = new PopupWindow(tooltipView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);

        tooltipView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int tooltipWidth = tooltipView.getMeasuredWidth();
        int tooltipHeight = tooltipView.getMeasuredHeight();

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int xOffset = AndroidUtilities.dp(8) + (anchorView.getWidth() / 2) - (tooltipWidth / 2);
        int yOffset = location[1] - tooltipHeight - 32;

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset);
    }

    private LinearLayout createContainerView() {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(0xff222e3a);
        background.setCornerRadii(new float[]{20, 20, 20, 20, 20, 20, 20, 20});

        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout layoutText = new LinearLayout(context);
        layoutText.setPadding(dp(16), 0, dp(16), 0);
        layoutText.setOrientation(LinearLayout.HORIZONTAL);
        layoutText.setBackground(background);

        ImageView tooltipImageView = new ImageView(context);
        tooltipImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.double_arrow_down_24));
        tooltipImageView.setContentDescription(LocaleController.getString(R.string.TapHereToUseThisBot));
        tooltipImageView.setScaleType(ImageView.ScaleType.CENTER);
        layoutText.addView(tooltipImageView, LayoutHelper.createLinear(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 0, 4, 8, 4));

        TextView tooltipTextView = new TextView(context);
        tooltipTextView.setTextColor(Color.WHITE);
        tooltipTextView.setText(LocaleController.getString(R.string.TapHereToUseThisBot));
        tooltipTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tooltipTextView.setGravity(Gravity.CENTER);
        layoutText.addView(tooltipTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 0, 4, 0, 4));

        container.addView(layoutText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        ImageView arrowImageView = new ImageView(context);
        arrowImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_down_drop));
        arrowImageView.setContentDescription(LocaleController.getString(R.string.TapHereToUseThisBot));
        arrowImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        container.addView(arrowImageView, LayoutHelper.createLinear(16, 8, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        return container;
    }

}
