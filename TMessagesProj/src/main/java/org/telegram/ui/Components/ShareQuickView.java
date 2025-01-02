/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ShareQuickDialogCell;
import org.telegram.ui.ChatActivity;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class ShareQuickView implements NotificationCenter.NotificationCenterDelegate {

    private final int currentAccount = UserConfig.selectedAccount;

    private ShareQuickDelegate delegate;

    private ShareDialogsAdapter listAdapter;
    private final ArrayList<MessageObject> sendingMessageObjects;
    public boolean includeStoryFromMessage;

    private final ChatActivity parentFragment;

    public boolean forceDarkThemeForHint;

    private final Theme.ResourcesProvider resourcesProvider;

    private PopupWindow popupWindow;

    public ShareQuickView(final Context context, final int x, final int y, ChatActivity fragment, ArrayList<MessageObject> messages, Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;

        parentFragment = fragment;

        sendingMessageObjects = messages;

        showPopupWindow(context, x, y);
    }

    public void setDelegate(ShareQuickDelegate shareQuickDelegate) {
        delegate = shareQuickDelegate;
    }

    public void showPopupWindow(Context context, int positionX, int positionY) {
        RecyclerView container = createRecyclerDialogs(context);

        popupWindow = new PopupWindow(container, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.showAtLocation(container, Gravity.NO_GRAVITY, positionX, positionY);

        container.setPivotX(positionX);
        container.setPivotY(positionY);
        playEnterAnimation(container);
        popupWindow.setOnDismissListener(() -> playExitAnimation(container));
    }

    private RecyclerView createRecyclerDialogs(Context context) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(Theme.getColor(Theme.key_dialogBackground));
        background.setCornerRadii(new float[]{100, 100, 100, 100, 100, 100, 100, 100});

        RecyclerListView recyclerDialogs = new RecyclerListView(context, resourcesProvider);
        recyclerDialogs.setPadding(dp(16),  dp(4), dp(16),  dp(4));
        recyclerDialogs.setClipToPadding(false);
        recyclerDialogs.setBackground(background);
        recyclerDialogs.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, true));
        recyclerDialogs.setHorizontalScrollBarEnabled(false);
        recyclerDialogs.setVerticalScrollBarEnabled(false);
        recyclerDialogs.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerDialogs.setAdapter(listAdapter = new ShareDialogsAdapter(context));
        recyclerDialogs.setOnItemClickListener((view, position) -> {
            if (position < 0) {
                return;
            }
            TLRPC.Dialog dialog = listAdapter.getItem(position);
            if (dialog == null) {
                return;
            }
            SendMessagesHelper.getInstance(currentAccount).sendMessage(sendingMessageObjects, dialog.id, false, false, true, 0);
            if (delegate != null) {
                delegate.onSend(dialog);
            }
            popupWindow.dismiss();
        });
        return recyclerDialogs;
    }

    private LinearLayout createDynamicUserList(Context context) {
        LinearLayout container = getWrapperLayout(context);

        GradientDrawable backgroundImageAvatar = new GradientDrawable();
        backgroundImageAvatar.setShape(GradientDrawable.RECTANGLE);
        backgroundImageAvatar.setColor(Theme.getColor(Theme.key_dialogBackground));
        backgroundImageAvatar.setCornerRadii(new float[]{100, 100, 100, 100, 100, 100, 100, 100});

        LinearLayout layoutNameAvatar = new LinearLayout(context);
        layoutNameAvatar.setOrientation(LinearLayout.HORIZONTAL);
        layoutNameAvatar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layoutNameAvatar.setPadding(32, 0, 32, 16);

        LinearLayout layoutImageAvatar = new LinearLayout(context);
        layoutImageAvatar.setOrientation(LinearLayout.HORIZONTAL);
        layoutImageAvatar.setPadding(32, 0, 32, 0);
        layoutImageAvatar.setBackground(backgroundImageAvatar);

        // Add user profile images dynamically
        for (int i = 0; i < 5; i++) {
            ImageView avatarImage = getImageView(context);
            TextView avatarText = getTextView(context, "" + i);
            layoutNameAvatar.addView(avatarText);
            layoutImageAvatar.addView(avatarImage);
        }

        container.addView(layoutNameAvatar);
        container.addView(layoutImageAvatar);
        return container;
    }

    private static @NonNull LinearLayout getWrapperLayout(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(48, 32, 48, 32);
        container.setLayoutParams(params);
//        container.setPadding(32, 32, 32, 32);
        container.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return container;
    }

    private static @NonNull ImageView getImageView(Context context) {
        ImageView profileImage = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMargins(16, 16, 16, 16);
        profileImage.setLayoutParams(params);
        profileImage.setImageResource(R.drawable.ic_launcher_dr);

        // Add click listener if needed
        profileImage.setOnClickListener(v -> {
            // Handle click on the user profile image
        });
        return profileImage;
    }

    private static @NonNull TextView getTextView(Context context, String text) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(16, 0, 16, 0);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Theme.getColor(Theme.key_avatar_text));

        GradientDrawable backgroundText = new GradientDrawable();
        backgroundText.setShape(GradientDrawable.RECTANGLE);
        backgroundText.setColor(Color.alpha(Theme.getColor(Theme.key_dialogBackground)) / 127);
        backgroundText.setCornerRadii(new float[]{100, 100, 100, 100, 100, 100, 100, 100});

        textView.setBackground(backgroundText);

        return textView;
    }

    // Play the enter animation (scale and fade in)
    private void playEnterAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(300);
        animatorSet.playTogether(scaleX, scaleY, fadeIn);
        animatorSet.start();
    }

    private void playExitAnimation(final View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.7f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.7f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(300);
        animatorSet.playTogether(scaleX, scaleY, fadeOut);
        animatorSet.start();
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                popupWindow.dismiss();
            }
        });
    }

    private class ShareDialogsAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;
        private ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();

        public ShareDialogsAdapter(Context context) {
            this.context = context;
            fetchDialogs();
        }

        public void fetchDialogs() {
            dialogs.clear();
            long selfUserId = UserConfig.getInstance(currentAccount).clientUserId;
            ArrayList<TLRPC.Dialog> archivedDialogs = new ArrayList<>();
            ArrayList<TLRPC.Dialog> allDialogs = MessagesController.getInstance(currentAccount).getAllDialogs();
            for (int a = 0; a < allDialogs.size(); a++) {
                TLRPC.Dialog dialog = allDialogs.get(a);
                if (!(dialog instanceof TLRPC.TL_dialog)) {
                    continue;
                }
                if (dialog.id == selfUserId) {
                    continue;
                }
                if (!DialogObject.isEncryptedDialog(dialog.id)) {
                    if (DialogObject.isUserDialog(dialog.id)) {
                        if (dialog.folder_id == 1) {
                            archivedDialogs.add(dialog);
                        } else {
                            dialogs.add(dialog);
                        }
                    } else {
                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialog.id);
                        if (!(chat == null || ChatObject.isNotInChat(chat) || chat.gigagroup && !ChatObject.hasAdminRights(chat) || ChatObject.isChannel(chat) && !chat.creator && (chat.admin_rights == null || !chat.admin_rights.post_messages) && !chat.megagroup)) {
                            if (dialog.folder_id == 1) {
                                archivedDialogs.add(dialog);
                            } else {
                                dialogs.add(dialog);
                            }
                        }
                    }
                }
            }
            dialogs.addAll(archivedDialogs);
            if (parentFragment != null) {
                switch (parentFragment.shareAlertDebugMode) {
                    case ChatActivity.DEBUG_SHARE_ALERT_MODE_LESS:
                        ArrayList<TLRPC.Dialog> sublist = new ArrayList<>(dialogs.subList(0, Math.min(4, dialogs.size())));
                        dialogs.clear();
                        dialogs.addAll(sublist);
                        break;
                    case ChatActivity.DEBUG_SHARE_ALERT_MODE_MORE:
                        while (!dialogs.isEmpty() && dialogs.size() < 80) {
                            dialogs.add(dialogs.get(dialogs.size() - 1));
                        }
                        break;
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            int count = dialogs.size();
//            if (count > 6) {
//                count = 6;
//            }
//            return count;
            if (count != 0) {
                count++;
            }
            return count;
        }

        public TLRPC.Dialog getItem(int position) {
            position--;
            if (position < 0 || position >= dialogs.size()) { //dialogs.size()
                return null;
            }
            return dialogs.get(position);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(dp(40), dp(40));
            layoutParams.setMargins(dp(4), dp(4), dp(4), dp(4));
            switch (viewType) {
                case 0: {
                    view = new ShareQuickDialogCell(context, resourcesProvider) {
                        @Override
                        protected String repostToCustomName() {
                            if (includeStoryFromMessage) {
                                return LocaleController.getString(R.string.RepostToStory);
                            }
                            return super.repostToCustomName();
                        }
                    };
                    view.setLayoutParams(layoutParams);
                    break;
                }
                case 1:
                default: {
                    view = new View(context);
                    view.setLayoutParams(layoutParams);
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ShareQuickDialogCell cell = (ShareQuickDialogCell) holder.itemView;
            TLRPC.Dialog dialog = getItem(position);
            if (dialog == null) return;
            cell.setDialog(dialog.id);
        }

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (listAdapter != null) {
                listAdapter.fetchDialogs();
            }
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.dialogsNeedReload);
        }
    }

    public interface ShareQuickDelegate {
        void onSend(TLRPC.Dialog dialog);
    }
}
