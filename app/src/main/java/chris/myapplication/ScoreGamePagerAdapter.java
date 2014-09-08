package chris.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages. This provides the data for the {@link android.support.v4.view.ViewPager}.
 */
public class ScoreGamePagerAdapter extends FragmentPagerAdapter {

    public ScoreGamePagerAdapter(FragmentManager fm) {
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
        if (position == 0) {
            return new ScoreGameFragment();
        } else {
            return new ScoringPlaysFragment();
        }
    }

    /**
     * Get number of pages the {@link android.support.v4.view.ViewPager} should render.
     *
     * @return Number of fragments to be rendered as pages.
     */
    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    /**
     * Get title for each of the pages. This will be displayed on each of the tabs.
     *
     * @param position Page to fetch title for.
     * @return Title for specified page.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Score Game";
        } else {
            return "Scoring Plays";
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}