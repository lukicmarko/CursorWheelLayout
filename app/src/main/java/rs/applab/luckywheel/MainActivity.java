package rs.applab.luckywheel;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rs.applab.cursorwheel.LuckyWheel;
import rs.applab.luckywheel.adapter.SimpleImageAdapter;
import rs.applab.luckywheel.data.ImageData;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_button_random_selected)
    Button mMainButtonRadonSelected;

    @BindView(R.id.test_circle_main)
    LuckyWheel mTestCircleMain;

    private Random mRandom = new Random();

    List<ImageData> wheelData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {

        wheelData = new ArrayList<ImageData>();
        wheelData.add(new ImageData(R.drawable.ic_bank_bc, "0"));
        wheelData.add(new ImageData(R.drawable.ic_bank_china, "1"));
        wheelData.add(new ImageData(R.drawable.ic_bank_guangda, "2"));
        wheelData.add(new ImageData(R.drawable.ic_bank_guangfa, "3"));
        wheelData.add(new ImageData(R.drawable.ic_bank_jianshe, "4"));
        wheelData.add(new ImageData(R.drawable.ic_bank_jiaotong, "5"));

        SimpleImageAdapter simpleImageAdapter = new SimpleImageAdapter(this, wheelData);
        mTestCircleMain.setAdapter(simpleImageAdapter);
        mTestCircleMain.setOnSelectionListener(new LuckyWheel.OnSelectionListener() {
            @Override
            public void onItemSelected(LuckyWheel parent, int pos) {
                Toast.makeText(MainActivity.this, "Wheel stopped at poition:" + pos, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @OnClick(R.id.main_button_random_selected)
    void onRandomClick() {
        int index = mRandom.nextInt(11);
        mTestCircleMain.setSelection(index);
        mMainButtonRadonSelected.setText("Random Selected:" + index);
    }
}
