package tk.imihajlov.camelup.engine;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import tk.imihajlov.camelup.IInteractionListener;
import tk.imihajlov.camelup.R;
import tk.imihajlov.camelup.IUpdatable;

public class ActionFragment extends Fragment implements IUpdatable {
    private IInteractionListener mListener;
    private RadioButton[] mDiceButtons;
    private State mStateDice;
    private State mStatePlusMinus;

    public ActionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ActionFragment.
     */
    public static ActionFragment newInstance() {
        ActionFragment fragment = new ActionFragment();
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
        View view = inflater.inflate(R.layout.fragment_action, container, false);
        mDiceButtons = new RadioButton[] {
                (RadioButton) view.findViewById(R.id.radioButtonDie1),
                (RadioButton) view.findViewById(R.id.radioButtonDie2),
                (RadioButton) view.findViewById(R.id.radioButtonDie3),
                (RadioButton) view.findViewById(R.id.radioButtonDie4),
                (RadioButton) view.findViewById(R.id.radioButtonDie5)
        };

        attachHandlers(view);

        ((SeekBar) view.findViewById(R.id.seekBarDieValue)).setProgress(1);
        enableDisableWidgets(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        mDiceButtons = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IInteractionListener) {
            mListener = (IInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement IInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDataUpdated(Object source) {
        if (source == this) {
            return;
        }
        if (!isResumed() || mListener == null) {
            return;
        }

        enableDisableWidgets(getView());
    }

    private void attachHandlers(final View view) {
        ((Button) view.findViewById(R.id.buttonDice)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onGameStateUpdated(ActionFragment.this, mStateDice);
            }
        });
        ((Button) view.findViewById(R.id.buttonPutPlusMinus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onGameStateUpdated(ActionFragment.this, mStatePlusMinus);
            }
        });
        ((EditText) view.findViewById(R.id.editPlusMinusPosition)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableDisableWidgets(getView());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        ((Switch) view.findViewById(R.id.switchPlusOrMinus)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((Button) view.findViewById(R.id.buttonPutPlusMinus)).setText(getString(b ? R.string.put_oasis : R.string.put_mirage));
                updateFutureStates(getView());
            }
        });
        ((SeekBar) view.findViewById(R.id.seekBarDieValue)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) view.findViewById(R.id.textViewDieValue)).setText(String.format("%d", progress + 1));
                updateFutureStates(getView());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        for (int i = 0; i < mDiceButtons.length; ++i) {
            mDiceButtons[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    enableDisableWidgets(getView());
                }
            });
        }
    }

    private void enableDisableWidgets(View view) {
        updateFutureStates(view);

        State state = mListener.getState();
        assert state.getDice().length == mDiceButtons.length;
        boolean diceEnabled = false;
        for (int i = 0; i < state.getDice().length; ++i) {
            mDiceButtons[i].setEnabled(state.getDice()[i]);
            if (!state.getDice()[i]) {
                mDiceButtons[i].setChecked(false);
            }
            diceEnabled |= state.getDice()[i];
        }
        diceEnabled &= mStateDice != null;
        view.findViewById(R.id.buttonDice).setEnabled(diceEnabled);
        view.findViewById(R.id.buttonPutPlusMinus).setEnabled(mStatePlusMinus != null);
    }

    private void updateFutureStates(View view) {
        mStateDice = null;
        mStatePlusMinus = null;
        if (mListener == null || mListener.getState() == null || view == null) {
            return;
        }
        if (mDiceButtons != null) {
            for (int i = 0; i < mDiceButtons.length; ++i) {
                if (mDiceButtons[i].isChecked()) {
                    int steps = ((SeekBar) view.findViewById(R.id.seekBarDieValue)).getProgress() + 1;
                    mStateDice = mListener.getState().jump(i, steps);
                    break;
                }
            }
        }
        try {
            boolean isPlus = ((Switch) view.findViewById(R.id.switchPlusOrMinus)).isChecked();
            int cell = Integer.parseInt(((EditText) view.findViewById(R.id.editPlusMinusPosition)).getText().toString(), 10) - 1;
            if (isPlus) {
                mStatePlusMinus = mListener.getState().putOasis(cell);
            } else {
                mStatePlusMinus = mListener.getState().putMirage(cell);
            }
        } catch (NumberFormatException e) {
        }
    }
}
