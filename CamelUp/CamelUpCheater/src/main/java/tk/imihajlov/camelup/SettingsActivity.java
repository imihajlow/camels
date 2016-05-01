package tk.imihajlov.camelup;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import tk.imihajlov.camelup.engine.Settings;

public class SettingsActivity extends ActionBarActivity {
    public static final String MSG_SETINGS = "tk.imihajlov.camelup.settings";

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = (Settings) getIntent().getSerializableExtra(MSG_SETINGS);
        final TextView textViewPlayersCount = ((TextView) findViewById(R.id.textViewPlayersCount));
        textViewPlayersCount.setText(String.format("%d", settings.getNPlayers()));
        SeekBar seekBarPlayersCount = ((SeekBar) findViewById(R.id.seekBarPlayersCount));
        seekBarPlayersCount.setProgress(settings.getNPlayers() - 1);

        seekBarPlayersCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int nPlayers = progress + 1;
                textViewPlayersCount.setText(String.format("%d", nPlayers));
                settings = new Settings(nPlayers);
                Intent intent = new Intent();
                intent.putExtra(MSG_SETINGS, settings);
                setResult(RESULT_OK, intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
