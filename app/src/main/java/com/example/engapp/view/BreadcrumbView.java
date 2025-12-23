package com.example.engapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.engapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Breadcrumb navigation component for showing current location in app hierarchy.
 * Max 3 levels deep, clickable segments, lightweight design.
 * 
 * Usage:
 * breadcrumbView.addSegment("🌌", "Beginner Galaxy", () -> navigateToGalaxy());
 * breadcrumbView.addSegment("🪐", "Coloria Prime", () -> navigateToPlanet());
 * breadcrumbView.addSegment("⚔️", "Battle", null); // Last segment not clickable
 */
public class BreadcrumbView extends LinearLayout {
    
    private List<BreadcrumbSegment> segments;
    private LinearLayout container;
    
    public BreadcrumbView(Context context) {
        super(context);
        init();
    }
    
    public BreadcrumbView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public BreadcrumbView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setOrientation(HORIZONTAL);
        int padding = dpToPx(12);
        setPadding(padding, padding / 2, padding, padding / 2);
        segments = new ArrayList<>();
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    /**
     * Add a breadcrumb segment
     * @param icon Emoji or icon text
     * @param text Segment label
     * @param onClickListener Click handler (null for last segment)
     */
    public void addSegment(String icon, String text, @Nullable OnClickListener onClickListener) {
        if (segments.size() >= 3) {
            // Max 3 levels - remove oldest if adding new
            removeSegment(0);
        }
        
        BreadcrumbSegment segment = new BreadcrumbSegment(icon, text, onClickListener);
        segments.add(segment);
        updateView();
    }
    
    /**
     * Clear all segments
     */
    public void clear() {
        segments.clear();
        removeAllViews();
    }
    
    /**
     * Remove segment at index
     */
    public void removeSegment(int index) {
        if (index >= 0 && index < segments.size()) {
            segments.remove(index);
            updateView();
        }
    }
    
    /**
     * Update the visual representation
     */
    private void updateView() {
        removeAllViews();
        
        for (int i = 0; i < segments.size(); i++) {
            BreadcrumbSegment segment = segments.get(i);
            
            // Create segment view
            TextView segmentView = createSegmentView(segment);
            
            // Add separator (except for last segment)
            if (i < segments.size() - 1) {
                addView(segmentView);
                addView(createSeparator());
            } else {
                addView(segmentView);
            }
        }
    }
    
    private TextView createSegmentView(BreadcrumbSegment segment) {
        TextView view = new TextView(getContext());
        view.setText(segment.icon + " " + segment.text);
        view.setTextSize(14);
        int padding = dpToPx(8);
        view.setPadding(padding, 0, padding, 0);
        
        // Make clickable if has listener
        if (segment.onClickListener != null) {
            view.setClickable(true);
            view.setFocusable(true);
            view.setOnClickListener(segment.onClickListener);
            view.setTextColor(getResources().getColor(R.color.accent_orange, null));
        } else {
            view.setTextColor(getResources().getColor(R.color.text_white, null));
        }
        
        return view;
    }
    
    private TextView createSeparator() {
        TextView separator = new TextView(getContext());
        separator.setText(" › ");
        separator.setTextSize(14);
        separator.setTextColor(getResources().getColor(R.color.text_hint, null));
        int padding = dpToPx(4);
        separator.setPadding(padding, 0, padding, 0);
        return separator;
    }
    
    private static class BreadcrumbSegment {
        String icon;
        String text;
        OnClickListener onClickListener;
        
        BreadcrumbSegment(String icon, String text, OnClickListener onClickListener) {
            this.icon = icon;
            this.text = text;
            this.onClickListener = onClickListener;
        }
    }
}

