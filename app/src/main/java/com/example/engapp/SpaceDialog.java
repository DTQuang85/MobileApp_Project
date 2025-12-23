package com.example.engapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class SpaceDialog {

    public interface OnDialogClickListener {
        void onClick();
    }

    public static Dialog showResult(Context context, String icon, String title,
                                  String message, int stars, String buttonText,
                                  OnDialogClickListener listener) {
        // Kiểm tra xem context có phải Activity và đang bị destroy không
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                // Activity đang bị destroy, không show dialog
                if (listener != null) {
                    listener.onClick();
                }
                return null;
            }
        }
        
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_space_result, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvIcon = view.findViewById(R.id.tvDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        TextView tvStars = view.findViewById(R.id.tvDialogStars);
        TextView btnOk = view.findViewById(R.id.btnDialogOk);

        tvIcon.setText(icon);
        tvTitle.setText(title);
        tvMessage.setText(message);

        // Build stars string
        StringBuilder starsStr = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < stars) {
                starsStr.append("⭐");
            } else {
                starsStr.append("☆");
            }
        }
        tvStars.setText(starsStr.toString());

        btnOk.setText(buttonText);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick();
            }
        });

        dialog.show();
        return dialog;
    }

    public static Dialog showSuccess(Context context, String message, int stars, OnDialogClickListener listener) {
        return showResult(context, "🎉", "Tuyệt vời!", message, stars, "Tiếp tục", listener);
    }

    public static void showVictory(Context context, String message, int stars, OnDialogClickListener listener) {
        showResult(context, "🏆", "Chiến thắng!", message, stars, "Hoàn thành", listener);
    }

    public static void showTryAgain(Context context, String message, int stars, OnDialogClickListener listener) {
        showResult(context, "💪", "Cố gắng thêm!", message, stars, "Thử lại", listener);
    }

    public static Dialog showInfo(Context context, String icon, String title, String message, OnDialogClickListener listener) {
        // Kiểm tra xem context có phải Activity và đang bị destroy không
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                // Activity đang bị destroy, không show dialog
                if (listener != null) {
                    listener.onClick();
                }
                return null;
            }
        }
        
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_space_result, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvIcon = view.findViewById(R.id.tvDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        TextView tvStars = view.findViewById(R.id.tvDialogStars);
        TextView btnOk = view.findViewById(R.id.btnDialogOk);

        tvIcon.setText(icon);
        tvTitle.setText(title);
        tvMessage.setText(message);
        tvStars.setVisibility(View.GONE);

        btnOk.setText("OK");
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick();
            }
        });

        dialog.show();
        return dialog;
    }

    /**
     * Show dialog with two buttons (confirm/cancel style)
     */
    public static Dialog show(Context context, String title, String message,
                           String positiveText, View.OnClickListener positiveListener,
                           String negativeText, View.OnClickListener negativeListener) {
        // Kiểm tra xem context có phải Activity và đang bị destroy không
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                // Activity đang bị destroy, không show dialog
                if (positiveListener != null) {
                positiveListener.onClick(null);
            }
            return null;
        }
        }
        
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_space_result, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvIcon = view.findViewById(R.id.tvDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        TextView tvStars = view.findViewById(R.id.tvDialogStars);
        TextView btnOk = view.findViewById(R.id.btnDialogOk);

        // Extract emoji from title if present
        if (title.length() > 2 && Character.isHighSurrogate(title.charAt(0))) {
            tvIcon.setText(title.substring(0, 2));
            tvTitle.setText(title.substring(2).trim());
        } else if (title.startsWith("🚀") || title.startsWith("🎉") || title.startsWith("💪") ||
                   title.startsWith("🎯") || title.startsWith("🎁") || title.startsWith("⚔️")) {
            int spaceIndex = title.indexOf(" ");
            if (spaceIndex > 0) {
                tvIcon.setText(title.substring(0, spaceIndex));
                tvTitle.setText(title.substring(spaceIndex + 1));
            } else {
                tvIcon.setText("🚀");
                tvTitle.setText(title);
            }
        } else {
            tvIcon.setText("🚀");
            tvTitle.setText(title);
        }

        tvMessage.setText(message);
        tvStars.setVisibility(View.GONE);

        btnOk.setText(positiveText);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (positiveListener != null) {
                positiveListener.onClick(v);
            }
        });

        // Handle negative button if provided
        if (negativeText != null && negativeListener != null) {
            // For simplicity, we make the dialog cancelable and treat cancel as negative
            dialog.setCancelable(true);
            dialog.setOnCancelListener(d -> {
                if (negativeListener != null) {
                    negativeListener.onClick(null);
                }
            });
        }

        dialog.show();
        return dialog;
    }

    /**
     * Show simple dialog with one button
     */
    public static Dialog show(Context context, String title, String message,
                           String buttonText, View.OnClickListener listener) {
        return show(context, title, message, buttonText, listener, null, null);
    }
}

