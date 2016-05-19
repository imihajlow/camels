package tk.imihajlov.camelup;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import tk.imihajlov.camelup.engine.ActionFragment;
import tk.imihajlov.camelup.engine.CamelPosition;
import tk.imihajlov.camelup.engine.Engine;
import tk.imihajlov.camelup.engine.LegResult;
import tk.imihajlov.camelup.engine.Settings;
import tk.imihajlov.camelup.engine.State;
import tk.imihajlov.camelup.engine.suggesters.ISuggester;
import tk.imihajlov.camelup.engine.suggesters.PositionsSuggester;

import android.util.Log;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity implements InteractionListener {

    private static final int SETTINGS_REQUEST = 1;

    private static final String PARAM_ENGINE = "engine";

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

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mEngine = (Engine) savedInstanceState.getSerializable(PARAM_ENGINE);
        } else {
            mEngine = new Engine();
            mEngine.setSettings(new Settings());
            mEngine.setState(createDefaultState(mEngine.getSettings()));
        }

        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putSerializable(PARAM_ENGINE, mEngine);
        super.onSaveInstanceState(state);
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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.MSG_SETINGS, mEngine.getSettings());
            startActivityForResult(intent, SETTINGS_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Settings settings = (Settings) data.getSerializableExtra(SettingsActivity.MSG_SETINGS);
                mEngine.setSettings(settings);
                if (mEngine.getState() == null) {
                    mEngine.setState(createDefaultState(mEngine.getSettings()));
                }
                mSectionsPagerAdapter.updateAll(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static State createDefaultState(Settings settings) {
        return State.createOnLegBegin(settings, new CamelPosition[] {
                new CamelPosition(0, 0),
                new CamelPosition(0, 1),
                new CamelPosition(1, 0),
                new CamelPosition(1, 1),
                new CamelPosition(2, 0)
        });
    }

    @Override
    public void onGameStateUpdated(Object source, State state) {
        mEngine.setState(state);
        mSectionsPagerAdapter.updateAll(source);
    }

    @Override
    public State getState() {
        return mEngine.getState();
    }

    @Override
    public Settings getSettings() {
        return mEngine.getSettings();
    }

    @Override
    public ISuggester getActionsSuggester() {
        return mEngine.getActionsSuggester();
    }

    @Override
    public PositionsSuggester getPositionsSuggester() {
        return mEngine.getPositionsSuggester();
    }

    @Override
    public void onCalculatePressed() {
        Log.v("CamelUp", "onCalculatePressed");
        if (mEngine.calculateAsync(new Engine.ResultListener() {
            @Override
            public void onCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                });

                PositionsSuggester ps = mEngine.getPositionsSuggester();
                Log.v("CamelUp", "PositionsSuggester has been received: ");
                StringBuilder sb = new StringBuilder();
                for (double[] row : ps.getProbabilityMatrix()) {
                    for (double x : row) {
                        sb.append(String.format("%.3f,", x));
                    }
                    sb.append("\n");
                }
                Log.v("CamelUp", sb.toString());
                sb = new StringBuilder();
                sb.append("Winners: ");
                for (double x : ps.getWinProbability()) {
                    sb.append(String.format("%.3f, ", x));
                }
                Log.v("CamelUp", sb.toString());
                sb = new StringBuilder();
                sb.append("Loosers: ");
                for (double x : ps.getLooseProbability()) {
                    sb.append(String.format("%.3f, ", x));
                }
                Log.v("CamelUp", sb.toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSectionsPagerAdapter.updateAll(MainActivity.this);
                        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_TIPS);
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
        public static final int PAGE_ACTIONS = 1;
        public static final int PAGE_TIPS = 2;
        public static final int PAGE_RESULTS = 3;

        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PAGE_SETUP:
                    return GameFragment.newInstance();
                case PAGE_RESULTS:
                    return ResultsFragment.newInstance();
                case PAGE_TIPS:
                    return TipsFragment.newInstance();
                case PAGE_ACTIONS:
                    return ActionFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PAGE_SETUP:
                    return "Game setup";
                case PAGE_RESULTS:
                    return "Results";
                case PAGE_TIPS:
                    return "Tips";
                case PAGE_ACTIONS:
                    return "Actions";
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

        public void updateAll(Object source) {
            for(int i = 0; i < registeredFragments.size(); i++) {
                int key = registeredFragments.keyAt(i);
                Updatable u = (Updatable) registeredFragments.get(key);
                if (u != null) {
                    u.onDataUpdated(source);
                }
            }
        }
    }
}
