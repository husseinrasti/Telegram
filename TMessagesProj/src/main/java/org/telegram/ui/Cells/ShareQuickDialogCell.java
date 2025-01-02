/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

public class ShareQuickDialogCell extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {

    private final BackupImageView imageView;
    private final AvatarDrawable avatarDrawable = new AvatarDrawable() {
        @Override
        public void invalidateSelf() {
            super.invalidateSelf();
            imageView.invalidate();
        }
    };
    private TLRPC.User user;

    private long currentDialog;

    private final int currentAccount = UserConfig.selectedAccount;
    public final Theme.ResourcesProvider resourcesProvider;

    private final AnimatedFloat premiumBlockedT = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private boolean premiumBlocked;

    public boolean isBlocked() {
        return premiumBlocked;
    }

    public BackupImageView getImageView() {
        return imageView;
    }

    public ShareQuickDialogCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        setWillNotDraw(false);

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(dp(28));
        addView(imageView, LayoutHelper.createFrame(40, 40, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_listSelector, resourcesProvider), dp(2), dp(2)));
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            final boolean wasPremiumBlocked = premiumBlocked;
            premiumBlocked = user != null && MessagesController.getInstance(currentAccount).isUserPremiumBlocked(user.id);
            if (premiumBlocked != wasPremiumBlocked) {
                invalidate();
            }
        }
    }

    protected String repostToCustomName() {
        return LocaleController.getString(R.string.FwdMyStory);
    }

    public void setDialog(long uid) {
        if (DialogObject.isUserDialog(uid)) {
            user = MessagesController.getInstance(currentAccount).getUser(uid);
            premiumBlocked = MessagesController.getInstance(currentAccount).isUserPremiumBlocked(uid);
            premiumBlockedT.set(premiumBlocked, true);
            invalidate();
            avatarDrawable.setInfo(currentAccount, user);
            if (UserObject.isReplyUser(user)) {
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_REPLIES);
                imageView.setImage(null, null, avatarDrawable, user);
            } else if (UserObject.isUserSelf(user)) {
                avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_SAVED);
                imageView.setImage(null, null, avatarDrawable, user);
            } else {
                imageView.setForUserOrChat(user, avatarDrawable);
            }
            imageView.setRoundRadius(dp(28));
        } else {
            user = null;
            premiumBlocked = false;
            premiumBlockedT.set(0, true);
            TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-uid);
            avatarDrawable.setInfo(currentAccount, chat);
            imageView.setForUserOrChat(chat, avatarDrawable);
            imageView.setRoundRadius(chat != null && chat.forum ? dp(16) : dp(28));
        }
        currentDialog = uid;
    }

    public long getCurrentDialog() {
        return currentDialog;
    }

}
