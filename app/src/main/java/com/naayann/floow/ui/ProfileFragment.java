package com.naayann.floow.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.naayann.floow.R;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;

public class ProfileFragment extends Fragment {

    private PrefHelper pref;
    private ImageView imgProfile;
    private TextView tvUserName, tvBio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pref = new PrefHelper(requireContext());

        imgProfile = view.findViewById(R.id.imgProfile);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvBio = view.findViewById(R.id.tvEmail);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                            .replace(R.id.fragmentContainer, new HomeFragment())
                            .commit();
                }
            });
        }

        View rowManageProfile = view.findViewById(R.id.rowManageProfile);
        if (rowManageProfile != null) {
            rowManageProfile.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                        .replace(R.id.fragmentContainer, new ManageProfileFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        View rowNotifications = view.findViewById(R.id.rowNotifications);
        if (rowNotifications != null) {
            rowNotifications.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                        .replace(R.id.fragmentContainer, new NotificationsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        View rowCustomize = view.findViewById(R.id.rowCustomize);
        if (rowCustomize != null) {
            rowCustomize.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                Toast.makeText(requireContext(), "floww v2.0 • Feat launching soon", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                        .addToBackStack(null)
                        .commit();
            });
        }

        View rowFaq = view.findViewById(R.id.rowFaq);
        if (rowFaq != null) {
            rowFaq.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
            });
        }

        View rowReport = view.findViewById(R.id.rowReport);

        if (rowReport != null) {
            rowReport.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());

                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:nayan.sarania.work@gmail.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Floow App Feedback");
                    emailIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "Hi Floow team,\n\n" +
                                    "I want to report an issue or give feedback:\n\n"
                    );

                    startActivity(Intent.createChooser(emailIntent, "Send feedback"));

                } catch (Exception e) {
                    Toast.makeText(
                            requireContext(),
                            "No email app found",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        View cardInvite = view.findViewById(R.id.cardInvite);

        if (cardInvite != null) {
            cardInvite.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());

                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "Hey, I am on floww! Building habits, focus, and improving flow of life ✨\n\n" +
                                    "Join floww and let's grow together! \n\n" +
                                    "Download here: https://floww-productivity.netlify.app/"
                    );
                    sendIntent.setType("text/plain");

                    startActivity(Intent.createChooser(sendIntent, "Invite friends via"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        refresh();
    }

    private void refresh() {
        if (!isAdded()) return;

        SharedPreferences floowPrefs = requireContext().getSharedPreferences("FloowPrefs", Context.MODE_PRIVATE);
        String name = floowPrefs.getString("user_name", pref.getName());
        String goal = floowPrefs.getString("user_goal", pref.getGoal());

        if (tvUserName != null) {
            tvUserName.setText(name);
        }
        if (tvBio != null) {
            tvBio.setText(goal);
        }

        if (imgProfile != null) {
            imgProfile.setImageResource(pref.getMascotDrawableRes());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}