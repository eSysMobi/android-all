package mobi.esys.dastarhan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class CurrentOrderActivity extends Activity {

    int count = 1;

    EditText etCurrOrderNotice;
    SeekBar sbCurrOrderCount;
    TextView tvCurrOrderCount2;
    Button bCurrOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_order);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        etCurrOrderNotice = (EditText)findViewById(R.id.etCurrOrderNotice);
        sbCurrOrderCount = (SeekBar) findViewById(R.id.sbCurrOrderCount);
        tvCurrOrderCount2 = (TextView) findViewById(R.id.tvCurrOrderCount2);
        bCurrOrder = (Button) findViewById(R.id.bCurrOrder);

        sbCurrOrderCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                count = progress + 1;
                tvCurrOrderCount2.setText(String.valueOf(count));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bCurrOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dtagCurrentOrder", "Return order");
                Intent intent = new Intent();
                intent.putExtra("count", count);
                intent.putExtra("notice", etCurrOrderNotice.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }
}
