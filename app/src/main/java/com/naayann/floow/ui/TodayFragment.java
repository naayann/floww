package com.naayann.floow.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
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
import com.naayann.floow.MainActivity;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.CompletionEntity;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
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

        if (cardStackContainer != null) {
            cardStackContainer.setClipChildren(false);
            cardStackContainer.setClipToPadding(false);
        }

        try {
            dao = AppDatabase.getInstance(requireContext()).todoDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        pref = new PrefHelper(requireContext());
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

//        ImageView imgHeaderMascot = view.findViewById(R.id.imgHeaderMascot);
//        if (imgHeaderMascot != null) {
//            imgHeaderMascot.setImageResource(pref.getMascotDrawableRes());
//        }

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).selectHomeTab();
                } else {
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                            .replace(R.id.fragmentContainer, new HomeFragment())
                            .commit();
                }
            });
        }

        View btnGotIt = view.findViewById(R.id.btnGotIt);
        if (btnGotIt != null) {
            btnGotIt.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (tutorialOverlay != null) {
                    tutorialOverlay.setVisibility(View.GONE);
                }
                pref.setTutorialDone(true);
            });
        }

        loadPending();
        
        if (tutorialOverlay != null && !pref.isTutorialDone() && !pending.isEmpty()) {
            tutorialOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void loadPending() {
        if (!isAdded()) return;

        pending = new ArrayList<>();
        if (dao != null) {
            try {
                List<TodoEntity> list = dao.getPendingToday(today);
                if (list != null) pending = list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (cardStackContainer != null) {
            cardStackContainer.removeAllViews();
        }

        if (pending.isEmpty()) {
            if (tvHint != null) tvHint.setVisibility(View.GONE);
            showEmptyState();
            return;
        }

        if (tvHint != null) tvHint.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);

        int count = Math.min(pending.size(), 3);
        for (int i = count - 1; i >= 0; i--) {
            addCard(pending.get(i), i);
        }
    }

    private void addCard(TodoEntity todo, int stackIndex) {
        if (cardStackContainer == null || !isAdded()) return;

        View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_card, cardStackContainer, false);
        TextView emoji = card.findViewById(R.id.tvEmoji);
        TextView title = card.findViewById(R.id.tvTitle);
        LinearLayout root = card.findViewById(R.id.cardRoot);

        if (emoji != null) emoji.setText(todo.emoji);
        if (title != null) title.setText(todo.title);

        if (root != null) {
            try {
                int baseColor = Color.parseColor(todo.bgColor != null ? todo.bgColor : "#d4a373");
                float[] hsv = new float[3];
                Color.colorToHSV(baseColor, hsv);
                hsv[2] *= 0.8f;
                int darkColor = Color.HSVToColor(hsv);
                
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {baseColor, darkColor});
                gd.setCornerRadius(0);
                root.setBackground(gd);
            } catch (Exception e) {
                root.setBackgroundColor(Color.parseColor("#d4a373"));
            }
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
                        if (right) SoundManager.playRight(requireContext());
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
                        if (cardStackContainer != null) {
                            cardStackContainer.removeView(card);
                        }
                        if (right && dao != null) {
                            try {
                                dao.insertCompletion(new CompletionEntity(todo.id, today));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        pending.remove(todo);
                        hasClearedItemsThisSession = true;
                        rebuildStack();
                    }
                })
                .start();
    }

    private void rebuildStack() {
        if (cardStackContainer != null) {
            cardStackContainer.removeAllViews();
        }
        if (pending.isEmpty()) {
            if (hasClearedItemsThisSession && isAdded()) {
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
        if (emptyState == null || !isAdded()) return;
        emptyState.setVisibility(View.VISIBLE);
        ImageView imgMascot = emptyState.findViewById(R.id.imgMascot);
        
        if (imgMascot != null) {
            imgMascot.setImageResource(pref.getMascotDrawableRes());
            playMascotJumpAnimation(imgMascot);
        }

        try {
            String[] quotes = getResources().getStringArray(R.array.motivational_quotes);
            if (quotes.length > 0 && tvEmptyQuote != null) {
                int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                tvEmptyQuote.setText(quotes[dayOfYear % quotes.length]);
            }
        } catch (Exception ignored) {}

        if (tvEmptyTitle != null) {
            if (hasClearedItemsThisSession) {
                tvEmptyTitle.setText(R.string.empty_done);
            } else {
                int count = 0;
                if (dao != null) {
                    try {
                        count = dao.getTodoCount();
                    } catch (Exception ignored) {}
                }
                if (count == 0) {
                    tvEmptyTitle.setText(R.string.empty_welcome);
                    if (tvEmptyQuote != null) tvEmptyQuote.setText(R.string.empty_first_goal);
                } else {
                    tvEmptyTitle.setText(R.string.empty_clear);
                }
            }
        }
    }

    private void playMascotJumpAnimation(ImageView imgMascot) {
        if (imgMascot == null) return;
        imgMascot.setPivotY(imgMascot.getHeight());
        imgMascot.animate()
                .translationY(-14f)
                .scaleX(1.03f)
                .scaleY(0.97f)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> imgMascot.animate()
                        .translationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(400)
                        .setInterpolator(new OvershootInterpolator(1.2f))
                        .start())
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) loadPending();
    }
}