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

    public static void showResult(Context context, String icon, String title,
                                  String message, int stars, String buttonText,
                                  OnDialogClickListener listener) {
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
    }

    public static void showSuccess(Context context, String message, int stars, OnDialogClickListener listener) {
        showResult(context, "🎉", "Tuyệt vời!", message, stars, "Tiếp tục", listener);
    }

    public static void showVictory(Context context, String message, int stars, OnDialogClickListener listener) {
        showResult(context, "🏆", "Chiến thắng!", message, stars, "Hoàn thành", listener);
    }

    public static void showTryAgain(Context context, String message, int stars, OnDialogClickListener listener) {
        showResult(context, "💪", "Cố gắng thêm!", message, stars, "Thử lại", listener);
    }

    public static void showInfo(Context context, String icon, String title, String message, OnDialogClickListener listener) {
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
    }
}

