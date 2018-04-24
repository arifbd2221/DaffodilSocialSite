package com.socialsite.daffodilvarsity.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.socialsite.daffodilvarsity.fragments.CreateGroup;
import com.socialsite.daffodilvarsity.fragments.Groups;

/**
 * Created by AkshayeJH on 11/06/17.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                Groups groups = new Groups();
                return groups;

            case 1:
                CreateGroup createGroup = new CreateGroup();
                return  createGroup;



            default:
                return  null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){

        switch (position) {
            case 0:
                return "Groups";

            case 1:
                return "Create";

            default:
                return null;
        }

    }

}
