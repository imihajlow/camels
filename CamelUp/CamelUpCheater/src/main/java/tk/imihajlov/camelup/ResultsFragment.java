package tk.imihajlov.camelup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultsFragment extends Fragment implements IUpdatable {
    private IInteractionListener mListener;

    public ResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResultsFragment.
     */
    public static ResultsFragment newInstance() {
        ResultsFragment fragment = new ResultsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        updateViewWithCurrentResult(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IInteractionListener) {
            mListener = (IInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDataUpdated(Object source) {
        updateViewWithCurrentResult(getView());
    }

    private void updateViewWithCurrentResult(View view) {
        if (mListener == null) {
            return;
        }
        if (mListener.getPositionsSuggester() == null) {
            view.findViewById(R.id.textViewNoResults).setVisibility(View.VISIBLE);
            view.findViewById(R.id.chartResults).setVisibility(View.GONE);
            view.invalidate();
        } else {
            view.findViewById(R.id.textViewNoResults).setVisibility(View.GONE);
            view.findViewById(R.id.chartResults).setVisibility(View.VISIBLE);

            HorizontalBarChart chart = (HorizontalBarChart) view.findViewById(R.id.chartResults);
            chart.setDescription("Leg results");

            List<String> axisValues = new ArrayList<String>();
            axisValues.add("5");
            axisValues.add("4");
            axisValues.add("3");
            axisValues.add("2");
            axisValues.add("1");

            List<BarDataSet> dataSets = new ArrayList<BarDataSet>();

            int[] camelColors = new int[] {
                    R.color.colorCamel0,
                    R.color.colorCamel1,
                    R.color.colorCamel2,
                    R.color.colorCamel3,
                    R.color.colorCamel4
            };
            double[][] matrix = mListener.getPositionsSuggester().getProbabilityMatrix();
            assert matrix.length == 5;
            for (int i = 0; i < matrix.length; ++i) {
                List<BarEntry> valueSet = new ArrayList<BarEntry>();
                for (int j = matrix[i].length - 1; j >= 0; --j) {
                    valueSet.add(new BarEntry((float) matrix[i][j], matrix.length - j - 1));
                }
                BarDataSet dataSet = new BarDataSet(valueSet, String.format("Camel %d", i + 1));
                dataSet.setColor(getResources().getColor(camelColors[i]));
                dataSets.add(dataSet);
            }

            BarData barData = new BarData(axisValues, dataSets);
            chart.setData(barData);
            chart.animateXY(0, 2000);
            chart.getAxisLeft().setAxisMaxValue(1.0f);
            chart.getAxisRight().setAxisMaxValue(1.0f);
            view.invalidate();
        }
    }
}
