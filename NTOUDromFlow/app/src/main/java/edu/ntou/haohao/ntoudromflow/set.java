package edu.ntou.haohao.ntoudromflow;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class set extends AppCompatActivity {

    protected NumberPicker IPPicker1, IPPicker2, WARNPicker;
    protected Button ok;
    Intent intent;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = this.getIntent();
        bundle = intent.getExtras();
        String[] warns = {getString(R.string.No_Warning),
                "30%", "35%", "40%", "45%", "50%", "55%", "60%",
                "65%", "70%", "75%", "80%", "85%", "90%", "95%"};

        IPPicker1=(NumberPicker) findViewById(R.id.IPPicker1);
        IPPicker1.setMaxValue(222);
        IPPicker1.setMinValue(204);
        IPPicker1.setValue((bundle.getInt("userNO1") == 0) ? 204 : (bundle.getInt("userNO1")));
        IPPicker2=(NumberPicker) findViewById(R.id.IPPicker2);
        IPPicker2.setMaxValue(255);
        IPPicker2.setMinValue(0);
        IPPicker2.setValue(bundle.getInt("userNO2"));
        WARNPicker=(NumberPicker) findViewById(R.id.WARNPicker);
        WARNPicker.setMaxValue(warns.length - 1);
        WARNPicker.setMinValue(0);
        WARNPicker.setDisplayedValues(warns);
        WARNPicker.setValue(bundle.getInt("warnNO"));

        ok = (Button)findViewById(R.id.OK);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                bundle.putInt("userNO1", IPPicker1.getValue());
                bundle.putInt("userNO2", IPPicker2.getValue());
                bundle.putInt("warnNO", WARNPicker.getValue());
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
