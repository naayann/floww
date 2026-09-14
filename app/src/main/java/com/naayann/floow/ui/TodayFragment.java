package com.naayann.floow.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.CompletionEntity;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {

    private FrameLayout cardStackContainer;
    private LinearLayout emptyState;
    private TextView tvEmptyTitle, tvEmptyQuote, tvHint;
    private View tutorialOverlay;
    private TodoDao dao;
    private PrefHelper pref;
    private List<TodoEntity> pending = new ArrayList<>();
    private String today;
    private float downX, downY;
    private boolean isDragging = false;
    private boolean hasClearedItemsThisSession = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardStackContainer = view.findViewById(R.id.cardStackContainer);
        emptyState = view.findViewById(R.id.emptyState);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyQuote = view.findViewById(R.id.tvEmptyQuote);
        tvHint = view.findViewById(R.id.tvHint);
        tutorialOverlay = view.findViewById(R.id.tutorialOverlay);

        cardStackContainer.setClipChildren(false);
        cardStackContainer.setClipToPadding(false);

        dao = AppDatabase.getInstance(requireContext()).todoDao();
        pref = new PrefHelper(requireContext());
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        view.findViewById(R.id.btnGotIt).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            tutorialOverlay.setVisibility(View.GONE);
            pref.setTutorialDone(true);
        });

        loadPending();
        
        if (!pref.isTutorialDone() && !pending.isEmpty()) {
            tutorialOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void loadPending() {
        pending = dao.getPendingToday(today);
        cardStackContainer.removeAllViews();

        if (pending.isEmpty()) {
            tvHint.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        tvHint.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        int count = Math.min(pending.size(), 3);
        for (int i = count - 1; i >= 0; i--) {
            addCard(pending.get(i), i);
        }
    }

    private void addCard(TodoEntity todo, int stackIndex) {
        View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_card, cardStackContainer, false);
        TextView emoji = card.findViewById(R.id.tvEmoji);
        TextView title = card.findViewById(R.id.tvTitle);
        LinearLayout root = card.findViewById(R.id.cardRoot);

        emoji.setText(todo.emoji);
        title.setText(todo.title);

        try {
            int baseColor = Color.parseColor(todo.bgColor);
            float[] hsv = new float[3];
            Color.colorToHSV(baseColor, hsv);
            hsv[2] *= 0.8f; // Darken for gradient
            int darkColor = Color.HSVToColor(hsv);
            
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {baseColor, darkColor});
            gd.setCornerRadius(0); // CardView handles corners
            root.setBackground(gd);

            // Styling the Focus for today text
            TextView header = getView().findViewById(R.id.tvHeader);
            if (header != null) {
                header.setLetterSpacing(-0.02f);
            }
        } catch (Exception e) {
            root.setBackgroundColor(Color.parseColor("#d4a373"));
        }

        float scale = 1f - (stackIndex * 0.05f);
        float translationY = 80f + (stackIndex * 24f);
        card.setScaleX(scale);
        card.setScaleY(scale);
        card.setTranslationY(translationY);
        card.setElevation(12f - stackIndex * 2f);

        if (stackIndex == 0) {
            setupSwipe(card, todo);
        }

        cardStackContainer.addView(card);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipe(View card, TodoEntity todo) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    isDragging = true;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) return false;
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    v.setTranslationX(dx);
                    v.setTranslationY(dy);
                    v.setRotation(dx / 25f);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    float finalX = v.getTranslationX();
                    if (Math.abs(finalX) > 160) {
                        boolean right = finalX > 0;
                        if (right) SoundManager.playDone(requireContext());
                        else SoundManager.playThrow(requireContext());
                        flyOut(v, right, todo);
                    } else {
                        v.animate()
                                .translationX(0)
                                .translationY(0)
                                .rotation(0)
                                .setDuration(250)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void flyOut(View card, boolean right, TodoEntity todo) {
        float targetX = right ? 1500f : -1500f;
        card.animate()
                .translationX(targetX)
                .rotation(right ? 30f : -30f)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cardStackContainer.removeView(card);
                        if (right) {
                            dao.insertCompletion(new CompletionEntity(todo.id, today));
                        }
                        pending.remove(todo);
                        hasClearedItemsThisSession = true;
                        rebuildStack();
                    }
                })
                .start();
    }

    private void rebuildStack() {
        cardStackContainer.removeAllViews();
        if (pending.isEmpty()) {
            if (hasClearedItemsThisSession) {
                SoundManager.playCompleted(requireContext());
            }
            showEmptyState();
            return;
        }
        int count = Math.min(pending.size(), 3);
        for (int i = count - 1; i >= 0; i--) {
            addCard(pending.get(i), i);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        ImageView imgMascot = emptyState.findViewById(R.id.imgMascot);
        
        // Character level animation
        imgMascot.setPivotY(imgMascot.getHeight());
        imgMascot.animate()
                .scaleX(1.1f)
                .scaleY(0.9f)
                .translationY(20f)
                .rotation(5f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> imgMascot.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .translationY(0f)
                        .rotation(0f)
                        .setDuration(600)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();

        String[] quotes = getResources().getStringArray(R.array.motivational_quotes);
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        tvEmptyQuote.setText(quotes[dayOfYear % quotes.length]);

        if (hasClearedItemsThisSession) {
            tvEmptyTitle.setText(R.string.empty_done);
        } else {
            // Check if there are ANY todos at all
            if (dao.getTodoCount() == 0) {
                tvEmptyTitle.setText(R.string.empty_welcome);
                tvEmptyQuote.setText(R.string.empty_first_goal);
            } else {
                tvEmptyTitle.setText(R.string.empty_clear);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dao != null) loadPending();
    }
}