package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.danielnilsson9.colorpickerview.view.ColorPanelView;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;

import at.smartshopper.smartshopper.R;

public class Colorpicker extends AppCompatActivity {

    private ColorPickerView mColorPickerView;
    private ColorPanelView mOldColorPanelView;
    private ColorPanelView mNewColorPanelView;

    private Button mOkButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpicker);
        init();
    }

    private void init() {


        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpickerview__color_picker_view);
        mOldColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_old);
        mNewColorPanelView = (ColorPanelView) findViewById(R.id.colorpickerview__color_panel_new);

        mOkButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);


        ((LinearLayout) mOldColorPanelView.getParent()).setPadding(
                mColorPickerView.getPaddingLeft(), 0,
                mColorPickerView.getPaddingRight(), 0);


        mColorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                mNewColorPanelView.setColor(mColorPickerView.getColor());
            }
        });
        mColorPickerView.setColor(Color.parseColor("#FFFFFF"), true);
        mOldColorPanelView.setColor(Color.parseColor("#FFFFFF"));

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitResult(mColorPickerView.getColor() + "");
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitResult(null);
            }
        });

    }


    /**
     * Bendet den Colorpicker und sendet an die vorherige activity einen STring
     *
     * @param result String der an die Aufrufactivity zurückgesendet werden soll. Wenn null dann wird nichts gesendet
     */
    private void exitResult(String result) {
        if (result == null) {
            finish();
        } else {
            Intent data = new Intent();

            //---set the data to pass back---
            data.setData(Uri.parse(result));
            setResult(RESULT_OK, data);
            //---close the activity---
            finish();
        }
    }

    /**
     * Wenn zurück geklickt wird, wird das Program ohne result geschlosen
     */
    @Override
    public void onBackPressed() {
        exitResult(mColorPickerView.getColor() + "");
    }
}
