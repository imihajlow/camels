package tk.imihajlov.camelup;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import tk.imihajlov.camelup.engine.LegResult;
import tk.imihajlov.camelup.engine.PlayerAction;
import tk.imihajlov.camelup.engine.actions.BetLegWinner;
import tk.imihajlov.camelup.engine.actions.Dice;
import tk.imihajlov.camelup.engine.actions.PlayerActionVisitor;
import tk.imihajlov.camelup.engine.actions.PutDesert;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TipsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TipsFragment extends Fragment implements Updatable {
    private InteractionListener mListener;

    public TipsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TipsFragment.
     */
    public static TipsFragment newInstance() {
        TipsFragment fragment = new TipsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        updateViewWithCurrentResult(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof InteractionListener) {
            mListener = (InteractionListener) activity;
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

    private class TableBuilder implements PlayerActionVisitor {
        private TableLayout table;
        private final int[] colors = new int[] {
                R.color.colorCamel0,
                R.color.colorCamel1,
                R.color.colorCamel2,
                R.color.colorCamel3,
                R.color.colorCamel4
        };

        public TableBuilder(TableLayout table) {
            this.table = table;
        }

        @Override
        public void visit(Dice action) {
            TextView text = new TextView(table.getContext());
            text.setText("Roll the dice");
            addRow(text, action.getProfitExpectation());
        }

        @Override
        public void visit(BetLegWinner action) {
            TextView text = new TextView(table.getContext());
            text.setText(String.format("Bet on camel %d", action.getCard().getCamel() + 1));
            addRow(text, action.getProfitExpectation());
        }

        @Override
        public void visit(PutDesert action) {
            TextView text = new TextView(table.getContext());
            text.setText(String.format("Put %s on tile %d", action.isOasis() ? "oasis" : "mirage", action.getX() + 1));
            addRow(text, action.getProfitExpectation());
        }

        private void addRow(View leftColumn, double gain) {
            TableRow row = new TableRow(table.getContext());
            TextView text = new TextView(table.getContext());
            text.setText(String.format("%+.1f", gain));
            row.addView(leftColumn);
            row.addView(text);
            table.addView(row);
        }
    }

    private void updateViewWithCurrentResult(View view) {
        if (mListener == null) {
            return;
        }
        if (mListener.getActionsSuggester() == null) {
            view.findViewById(R.id.textViewNoResults).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tableTips).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.textViewNoResults).setVisibility(View.GONE);
            view.findViewById(R.id.tableTips).setVisibility(View.VISIBLE);
            TableLayout table = (TableLayout) view.findViewById(R.id.tableTips);
            table.removeViews(1, table.getChildCount() - 1);
            TableBuilder builder = new TableBuilder(table);
            for (PlayerAction action : mListener.getActionsSuggester().getSuggestedActions()) {
                action.accept(builder);
            }
        }
        view.invalidate();
    }

}
