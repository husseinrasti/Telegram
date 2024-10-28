package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;

import static org.telegram.messenger.AndroidUtilities.dp;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;

public class TooltipBotStartButton extends View {

    private ActionBarPopupWindow actionBarPopupWindow;

    private Theme.ResourcesProvider resourcesProvider;

    private LinearLayout contentView;

    private String text;
    private int icon = -1;
    private int arrowGravity = Gravity.CENTER;
    private boolean isAutoCancelable = false;

    public TooltipBotStartButton(Context context) {
        super(context);
    }

    public TooltipBotStartButton(Context context, Theme.ResourcesProvider provider) {
        this(context);
        resourcesProvider = provider;
    }

    public void dismiss() {
        if (actionBarPopupWindow != null) {
            actionBarPopupWindow.dismiss(false);
        }
        actionBarPopupWindow = null;
    }

    public void show(View anchorView) {
        postDelayed(() -> create(anchorView), 300);
    }

    private void create(View anchorView) {
        createContainerView();
        if (actionBarPopupWindow != null) {
            dismiss();
        }

        actionBarPopupWindow = new ActionBarPopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        actionBarPopupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        actionBarPopupWindow.setOutsideTouchable(true);

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int tooltipWidth = contentView.getMeasuredWidth();
        int tooltipHeight = contentView.getMeasuredHeight();

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int xOffset = AndroidUtilities.dp(8) + (anchorView.getWidth() / 2) - (tooltipWidth / 2);
        int yOffset = location[1] - tooltipHeight - 32;

        actionBarPopupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset);

        if (isAutoCancelable) {
            postDelayed(actionBarPopupWindow::dismiss, 3000);
        }
    }

    private void createContainerView() {
        contentView = new LinearLayout(getContext());
        contentView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout layoutText = new LinearLayout(getContext());
        layoutText.setPadding(dp(8), dp(8), dp(8), dp(8));
        layoutText.setOrientation(LinearLayout.HORIZONTAL);
        layoutText.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(6), getThemedColor(Theme.key_chat_gifSaveHintBackground)));

        if (icon != -1) {
            ImageView tooltipImageView = new ImageView(getContext());
            tooltipImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), icon));
            tooltipImageView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_chat_gifSaveHintText), PorterDuff.Mode.MULTIPLY));
            tooltipImageView.setContentDescription(text);
            tooltipImageView.setScaleType(ImageView.ScaleType.CENTER);
            layoutText.addView(tooltipImageView, LayoutHelper.createLinear(24, 24, Gravity.CENTER, 0, 0, 0, 0));
        }

        CorrectlyMeasuringTextView tooltipTextView = new CorrectlyMeasuringTextView(getContext());
        tooltipTextView.setTextColor(getThemedColor(Theme.key_chat_gifSaveHintText));
        tooltipTextView.setText(text);
        tooltipTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tooltipTextView.setGravity(Gravity.CENTER);
        layoutText.addView(tooltipTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, dp(4), 0, dp(4), 0));

        contentView.addView(layoutText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        ImageView arrowImageView = new ImageView(getContext());
        arrowImageView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_chat_gifSaveHintBackground), PorterDuff.Mode.SRC_IN));
        arrowImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.tooltip_arrow));
        arrowImageView.setContentDescription(text);
        arrowImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        contentView.addView(arrowImageView, getArrowParams());
    }

    private LinearLayout.LayoutParams getArrowParams() {
        LinearLayout.LayoutParams params;
        switch (arrowGravity) {
            case Gravity.RIGHT:
                params = LayoutHelper.createLinear(16, 8, Gravity.RIGHT, 0, 0, dp(16), 0);
                break;
            case Gravity.LEFT:
                params = LayoutHelper.createLinear(16, 8, Gravity.LEFT, dp(16), 0, 0, 0);
                break;
            default:
                params = LayoutHelper.createLinear(16, 8, Gravity.CENTER, 0, 0, 0, 0);
                break;
        }
        return params;
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    public void setAutoCancelable(boolean autoCancelable) {
        isAutoCancelable = autoCancelable;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setArrowGravity(int arrowGravity) {
        this.arrowGravity = arrowGravity;
    }

    public void setIcon(@DrawableRes int icon) {
        this.icon = icon;
    }

    @Override
    protected void onDetachedFromWindow() {
        dismiss();
        super.onDetachedFromWindow();
    }

}
