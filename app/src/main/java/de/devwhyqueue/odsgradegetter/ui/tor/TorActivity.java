package de.devwhyqueue.odsgradegetter.ui.tor;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.devwhyqueue.odsgradegetter.R;
import de.devwhyqueue.odsgradegetter.tordownloader.model.TranscriptOfRecords;

public class TorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tor);

        final TextView avgView = findViewById(R.id.avg);
        final TextView bestAvgView = findViewById(R.id.best_avg);
        TranscriptOfRecords tor = (TranscriptOfRecords) getIntent().getSerializableExtra("TOR");

        avgView.setText(String.format("%.2f", tor.getGradePointAvg()));
        bestAvgView.setText(String.format("(%.2f)", tor.getBestPossibleGradePointAvg()));

    }
}