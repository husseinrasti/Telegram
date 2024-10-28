package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ShareQuickDialogCell;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class ShareQuickView implements NotificationCenter.NotificationCenterDelegate {

    private final int currentAccount = UserConfig.selectedAccount;

    private ShareQuickDelegate delegate;

    private ShareDialogsAdapter listAdapter;
    private final ArrayList<MessageObject> sendingMessageObjects;
    public boolean includeStoryFromMessage;

    public boolean forceDarkThemeForHint;

    private final Theme.ResourcesProvider resourcesProvider;

    private ActionBarPopupWindow actionBarPopupWindow;

    public ShareQuickView(final Context context, final int positionStartX, final int positionStartY, ArrayList<MessageObject> messages, Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;
        sendingMessageObjects = messages;
        showPopupWindow(context, positionStartX, positionStartY);
    }

    public void setDelegate(ShareQuickDelegate shareQuickDelegate) {
        delegate = shareQuickDelegate;
    }

    public void showPopupWindow(Context context, int positionX, int positionY) {
        RecyclerView container = createRecyclerDialogs(context);
        container.setVisibility(View.INVISIBLE);

        actionBarPopupWindow = new ActionBarPopupWindow(container, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        actionBarPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        actionBarPopupWindow.showAtLocation(container, Gravity.NO_GRAVITY, positionX, positionY);

        container.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        container.setPivotX(positionX);
        container.setPivotY(positionY);

        container.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                playEnterAnimation(container, positionX, positionY);
            } else {
                playEnterAnimation(container);
            }
        });

        actionBarPopupWindow.setOnDismissListener(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                playExitAnimation(container, positionX, positionY);
            } else {
                playExitAnimation(container);
            }
        });
    }

    private RecyclerView createRecyclerDialogs(Context context) {
        RecyclerListView recyclerDialogs = new RecyclerListView(context, resourcesProvider);
        recyclerDialogs.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        recyclerDialogs.setPadding(dp(16), dp(4), dp(16), dp(4));
        recyclerDialogs.setClipToPadding(true);
        recyclerDialogs.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(50), getThemedColor(Theme.key_dialogBackground)));
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
            actionBarPopupWindow.dismiss();
        });

        recyclerDialogs.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View touchedView = rv.findChildViewUnder(e.getX(), e.getY());
                int action = e.getAction();
                if (touchedView != null) {
                    int position = rv.getChildAdapterPosition(touchedView);
                    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                        listAdapter.setSelectedPosition(position);
                        listAdapter.notifyDataSetChanged();
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        listAdapter.setSelectedPosition(-1);
                        listAdapter.notifyDataSetChanged();
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        return recyclerDialogs;
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    private void playEnterAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(400);
        animatorSet.playTogether(scaleX, scaleY, fadeIn);
        view.setVisibility(View.VISIBLE);
        animatorSet.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playEnterAnimation(View view, int positionX, int positionY) {
        int finalRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(view, positionX, positionY, 0, finalRadius);
        circularReveal.setDuration(400);
        view.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private void playExitAnimation(final View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setDuration(300);
        animatorSet.playTogether(scaleX, scaleY, fadeOut);
        animatorSet.start();
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                actionBarPopupWindow.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playExitAnimation(final View view, int positionX, int positionY) {
        int finalRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator circularConceal = ViewAnimationUtils.createCircularReveal(view, positionX, positionY, finalRadius, 0);
        circularConceal.setDuration(300);
        circularConceal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                actionBarPopupWindow.dismiss();
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        circularConceal.start();
    }

    private class ShareDialogsAdapter extends RecyclerListView.SelectionAdapter {

        private Context context;
        private ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();

        private int selectedPosition = 0;
        private final Paint blurPaint;

        public ShareDialogsAdapter(Context context) {
            this.context = context;
            blurPaint = new Paint();
            blurPaint.setAlpha(50);
            fetchDialogs();
        }

        public void fetchDialogs() {
            dialogs.clear();
            long selfUserId = UserConfig.getInstance(currentAccount).clientUserId;
            if (!MessagesController.getInstance(currentAccount).dialogsForward.isEmpty()) {
                TLRPC.Dialog dialog = MessagesController.getInstance(currentAccount).dialogsForward.get(0);
                dialogs.add(dialog);
            }
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
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            int count = dialogs.size();
            if (count > 6) {
                count = 6;
            }
            return count;
//            if (count != 0) {
//                count++;
//            }
//            return count;
        }

        public TLRPC.Dialog getItem(int position) {
            if (position < 0 || position >= 7) { //dialogs.size()
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
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(dp(40), dp(40));
            layoutParams.setMargins(dp(4), dp(4), dp(4), dp(4));
            ShareQuickDialogCell view = new ShareQuickDialogCell(context, resourcesProvider) {
                @Override
                protected String repostToCustomName() {
                    if (includeStoryFromMessage) {
                        return LocaleController.getString(R.string.RepostToStory);
                    }
                    return super.repostToCustomName();
                }
            };
            view.setLayoutParams(layoutParams);
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ShareQuickDialogCell cell = (ShareQuickDialogCell) holder.itemView;
            TLRPC.Dialog dialog = getItem(position);
            if (dialog == null) return;
            cell.setDialog(dialog.id);
            if (position != selectedPosition) {
                holder.itemView.setLayerType(View.LAYER_TYPE_SOFTWARE, blurPaint);
                holder.itemView.setAlpha(0.9f);
            } else {
                holder.itemView.setLayerType(View.LAYER_TYPE_NONE, null);
                holder.itemView.setAlpha(1f);
            }
        }

        public void setSelectedPosition(int position) {
            this.selectedPosition = position;
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
