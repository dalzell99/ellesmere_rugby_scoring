package chris.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages. This provides the data for the {@link android.support.v4.view.ViewPager}.
 */
public class DrawPagerAdapter extends FragmentPagerAdapter {

    public static int weekNumber = 0;

    public DrawPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Get fragment corresponding to a specific position. This will be used to populate the
     * contents of the {@link android.support.v4.view.ViewPager}.
     *
     * @param position Position to fetch fragment for.
     * @return Fragment for specified position.
     */
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DrawDisplayFragment with the page number as its lone argument.
        weekNumber = position;
        return new DrawDisplayFragment();
    }

    /**
     * Causes all fragments to be recreated with notifyDataSetChanged() called which
     * in turn updates the game info.
     *
     */
    @Override
    public int getItemPosition(Object o) {
        return POSITION_NONE;
    }

    /**
     * Get number of pages the {@link android.support.v4.view.ViewPager} should render.
     *
     * @return Number of fragments to be rendered as pages.
     */
    @Override
    public int getCount() {
        // Show 7 total pages.
        return 7;
    }

    /**
     * Get title for each of the pages. This will be displayed on each of the tabs.
     *
     * @param position Page to fetch title for.
     * @return Title for specified page.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return "Week " + String.valueOf(position + 1);
    }
}