package tk.imihajlov.camelup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableRow;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import tk.imihajlov.camelup.engine.CamelPosition;
import tk.imihajlov.camelup.engine.Settings;
import tk.imihajlov.camelup.engine.State;

public class GameFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Settings mGameSettings;
    private State mGameState;
    private List<EditText> mPlusMinusEdits;
    private List<CheckBox> mPlusMinusCheckboxes;
    public static final String ARG_STATE = "state";
    public static final String ARG_SETTINGS = "settings";

    public GameFragment() {
        // Required empty public constructor
        mPlusMinusCheckboxes = new ArrayList<CheckBox>();
        mPlusMinusEdits = new ArrayList<EditText>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameFragment.
     */
    public static GameFragment newInstance(Settings settings, State state) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATE, state);
        args.putSerializable(ARG_SETTINGS, settings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGameState = (State) getArguments().getSerializable(ARG_STATE);
            mGameSettings = (Settings) getArguments().getSerializable(ARG_SETTINGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        view = updateViewWithCurrentSettings(view);
        if (mGameState != null) {
            view = updateViewWithState(view, mGameState);
        }
        attachHandlers(view);
        ((Button) view.findViewById(R.id.buttonCalculate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCalculatePressed();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
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

    public void setData(Settings settings, State state) {
        mGameState = state;
        mGameSettings = settings;
        updateViewWithCurrentSettings(getView());
        updateViewWithState(getView(), mGameState);
    }

    private void parseAndCheck() {
        State newState = tryParseState(getView());
        mGameState = newState;
        mListener.onGameStateUpdated(newState);
        ((Button) getView().findViewById(R.id.buttonCalculate)).setEnabled(mGameState != null);
    }

    private void attachHandlers(View view) {
        TextWatcher editWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                parseAndCheck();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        int[] edits = new int[] {
                R.id.editCamelX0,
                R.id.editCamelX1,
                R.id.editCamelX2,
                R.id.editCamelX3,
                R.id.editCamelX4,
                R.id.editCamelY0,
                R.id.editCamelY1,
                R.id.editCamelY2,
                R.id.editCamelY3,
                R.id.editCamelY4
        };
        for (int id : edits) {
            ((EditText) view.findViewById(id)).addTextChangedListener(editWatcher);
        }

        CompoundButton.OnCheckedChangeListener checkboxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                parseAndCheck();
            }
        };
        int[] checkboxes = new int[] {
                R.id.checkBoxDice0,
                R.id.checkBoxDice1,
                R.id.checkBoxDice2,
                R.id.checkBoxDice3,
                R.id.checkBoxDice4
        };
        for (int id : checkboxes) {
            ((CheckBox) view.findViewById(id)).setOnCheckedChangeListener(checkboxListener);
        }
    }

    private View updateViewWithCurrentSettings(View view) {
        // Remove all edits and checkboxes
        ViewGroup rowEdits = (ViewGroup) view.findViewById(R.id.rowPlusMinusX);
        rowEdits.removeViews(1, rowEdits.getChildCount() - 1);
        ViewGroup rowCheckBoxes = (ViewGroup) view.findViewById(R.id.rowPlusMinusIsPlus);
        rowCheckBoxes.removeViews(1, rowCheckBoxes.getChildCount() - 1);
        mPlusMinusCheckboxes.clear();
        mPlusMinusEdits.clear();

        for (int i = 0; i < mGameSettings.getNPlayers(); ++i) {
            EditText edit = new EditText(getActivity());
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
            edit.setMaxEms(5);
            edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    parseAndCheck();
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    parseAndCheck();
                }
            });

            mPlusMinusEdits.add(edit);
            mPlusMinusCheckboxes.add(checkBox);
            rowEdits.addView(edit);
            rowCheckBoxes.addView(checkBox);
        }
        return view;
    }

    private View updateViewWithState(View view, State state) {
        if (state.isGameEnd()) {
            return view; // TODO what?
        }
        ((CheckBox) view.findViewById(R.id.checkBoxDice0)).setChecked(state.getDice()[0]);
        ((CheckBox) view.findViewById(R.id.checkBoxDice1)).setChecked(state.getDice()[1]);
        ((CheckBox) view.findViewById(R.id.checkBoxDice2)).setChecked(state.getDice()[2]);
        ((CheckBox) view.findViewById(R.id.checkBoxDice3)).setChecked(state.getDice()[3]);
        ((CheckBox) view.findViewById(R.id.checkBoxDice4)).setChecked(state.getDice()[4]);

        ((EditText) view.findViewById(R.id.editCamelX0)).setText(String.format("%d", state.getCamelPosition(0).getX() + 1));
        ((EditText) view.findViewById(R.id.editCamelX1)).setText(String.format("%d", state.getCamelPosition(1).getX() + 1));
        ((EditText) view.findViewById(R.id.editCamelX2)).setText(String.format("%d", state.getCamelPosition(2).getX() + 1));
        ((EditText) view.findViewById(R.id.editCamelX3)).setText(String.format("%d", state.getCamelPosition(3).getX() + 1));
        ((EditText) view.findViewById(R.id.editCamelX4)).setText(String.format("%d", state.getCamelPosition(4).getX() + 1));

        ((EditText) view.findViewById(R.id.editCamelY0)).setText(String.format("%d", state.getCamelPosition(0).getY() + 1));
        ((EditText) view.findViewById(R.id.editCamelY1)).setText(String.format("%d", state.getCamelPosition(1).getY() + 1));
        ((EditText) view.findViewById(R.id.editCamelY2)).setText(String.format("%d", state.getCamelPosition(2).getY() + 1));
        ((EditText) view.findViewById(R.id.editCamelY3)).setText(String.format("%d", state.getCamelPosition(3).getY() + 1));
        ((EditText) view.findViewById(R.id.editCamelY4)).setText(String.format("%d", state.getCamelPosition(4).getY() + 1));

        assert mPlusMinusCheckboxes.size() == mPlusMinusEdits.size();
        int i = 0;
        for (int n : state.getOasises()) {
            if (i >= mPlusMinusEdits.size()) {
                break;
            }
            mPlusMinusEdits.get(i).setText(String.format("%d", n + 1));
            mPlusMinusCheckboxes.get(i).setChecked(true);
            i += 1;
        }
        for (int n : state.getMirages()) {
            if (i >= mPlusMinusEdits.size()) {
                break;
            }
            mPlusMinusEdits.get(i).setText(String.format("%d", n + 1));
            mPlusMinusCheckboxes.get(i).setChecked(false);
            i += 1;
        }
        return view;
    }

    private State tryParseState(View view) {
        boolean[] dice = new boolean[] {
                ((CheckBox) view.findViewById(R.id.checkBoxDice0)).isChecked(),
                ((CheckBox) view.findViewById(R.id.checkBoxDice1)).isChecked(),
                ((CheckBox) view.findViewById(R.id.checkBoxDice2)).isChecked(),
                ((CheckBox) view.findViewById(R.id.checkBoxDice3)).isChecked(),
                ((CheckBox) view.findViewById(R.id.checkBoxDice4)).isChecked()
        };
        CamelPosition[] camels;
        try {
            camels = new CamelPosition[]{
                    new CamelPosition(Integer.valueOf(((EditText) view.findViewById(R.id.editCamelX0)).getText().toString()) - 1,
                            Integer.valueOf(((EditText) view.findViewById(R.id.editCamelY0)).getText().toString()) - 1),
                    new CamelPosition(Integer.valueOf(((EditText) view.findViewById(R.id.editCamelX1)).getText().toString()) - 1,
                            Integer.valueOf(((EditText) view.findViewById(R.id.editCamelY1)).getText().toString()) - 1),
                    new CamelPosition(Integer.valueOf(((EditText) view.findViewById(R.id.editCamelX2)).getText().toString()) - 1,
                            Integer.valueOf(((EditText) view.findViewById(R.id.editCamelY2)).getText().toString()) - 1),
                    new CamelPosition(Integer.valueOf(((EditText) view.findViewById(R.id.editCamelX3)).getText().toString()) - 1,
                            Integer.valueOf(((EditText) view.findViewById(R.id.editCamelY3)).getText().toString()) - 1),
                    new CamelPosition(Integer.valueOf(((EditText) view.findViewById(R.id.editCamelX4)).getText().toString()) - 1,
                            Integer.valueOf(((EditText) view.findViewById(R.id.editCamelY4)).getText().toString()) - 1),
            };
        } catch (NumberFormatException e) {
            return null;
        }
        List<Integer> mirages = new ArrayList<Integer>();
        List<Integer> oasises = new ArrayList<Integer>();

        assert mPlusMinusEdits.size() == mPlusMinusCheckboxes.size();
        assert mPlusMinusEdits.size() == mGameSettings.getNPlayers();
        for (int i = 0; i < mGameSettings.getNPlayers(); ++i) {
            try {
                Integer x = Integer.parseInt(mPlusMinusEdits.get(i).getText().toString()) - 1;
                if (mPlusMinusCheckboxes.get(i).isChecked()) {
                    oasises.add(x);
                } else {
                    mirages.add(x);
                }
            } catch (NumberFormatException e) {
            }
        }
        return State.validateAndCreate(mGameSettings, camels, dice,
                ArrayUtils.toPrimitive(mirages.toArray(new Integer[mirages.size()])),
                ArrayUtils.toPrimitive(oasises.toArray(new Integer[oasises.size()])));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onGameStateUpdated(State state);

        void onCalculatePressed();
    }
}
