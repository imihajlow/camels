package tk.imihajlov.camelup;


import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import tk.imihajlov.camelup.engine.CamelPosition;
import tk.imihajlov.camelup.engine.Engine;
import tk.imihajlov.camelup.engine.LegResult;
import tk.imihajlov.camelup.engine.Settings;
import tk.imihajlov.camelup.engine.State;
import android.util.Log;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity implements GameFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Engine mEngine;

    private LegResult mResult;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEngine = new Engine();
        mEngine.setSettings(new Settings());
        mEngine.setState(State.createOnLegBegin(mEngine.getSettings(), new CamelPosition[] {
                new CamelPosition(0, 0),
                new CamelPosition(0, 1),
                new CamelPosition(1, 0),
                new CamelPosition(1, 1),
                new CamelPosition(2, 0)
        }));

        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGameStateUpdated(State state) {
        mEngine.setState(state);
    }

    @Override
    public void onCalculatePressed() {
        Log.v("CamelUp", "onCalculatePressed");
        if (mEngine.calculate(new Engine.ResultListener() {
            @Override
            public void onCompleted(LegResult result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                });

                Log.v("CamelUp", "Result has been received: ");
                StringBuilder sb = new StringBuilder();
                for (double[] row : result.getProbabilityMatrix()) {
                    for (double x : row) {
                        sb.append(String.format("%.3f,", x));
                    }
                    sb.append("\n");
                }
                Log.v("CamelUp", sb.toString());
                sb = new StringBuilder();
                sb.append("Winners: ");
                for (double x : result.getWinProbability()) {
                    sb.append(String.format("%.3f, ", x));
                }
                Log.v("CamelUp", sb.toString());
                sb = new StringBuilder();
                sb.append("Loosers: ");
                for (double x : result.getLooseProbability()) {
                    sb.append(String.format("%.3f, ", x));
                }
                Log.v("CamelUp", sb.toString());

                mResult = result;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ResultsFragment fragment = (ResultsFragment) mSectionsPagerAdapter.getRegisteredFragment(SectionsPagerAdapter.PAGE_RESULTS);
                        fragment.setResult(mResult);
                        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_RESULTS);
                    }
                });
            }

            @Override
            public void onInterrupted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                });
            }
        })) {
            mProgressDialog = ProgressDialog.show(this, "Calculating", "", true);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public static final int PAGE_SETUP = 0;
        public static final int PAGE_RESULTS = 1;

        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_SETUP:
                    return GameFragment.newInstance(mEngine.getSettings(), mEngine.getState());
                case PAGE_RESULTS:
                    return ResultsFragment.newInstance(mResult);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PAGE_SETUP:
                    return "Game setup";
                case PAGE_RESULTS:
                    return "Results";
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
