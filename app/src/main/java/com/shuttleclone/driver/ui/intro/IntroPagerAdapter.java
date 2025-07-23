package com.shuttleclone.driver.ui.intro;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class IntroPagerAdapter extends FragmentStatePagerAdapter {

    private List<IntroFragment> fragments;


    public IntroPagerAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
        fragments = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            addIntroFragment(new IntroFragment());
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        IntroFragment intro = new IntroFragment();

        return intro.getInstance(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addIntroFragment(IntroFragment fragment) {
        fragments.add(fragment);
    }
}
