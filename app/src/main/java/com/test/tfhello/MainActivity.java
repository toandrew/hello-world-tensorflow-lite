package com.test.tfhello;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    private MappedByteBuffer tfliteModel;

    Interpreter tflite;

    private TextView tvRun;

    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tfliteModel = loadModelFile(this);
            tfliteOptions.setNumThreads(1);
            tflite = new Interpreter(tfliteModel, tfliteOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRun = findViewById(R.id.run);
        tvRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runInference();
            }
        });

        tvResult = findViewById(R.id.result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        close();
    }


    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String getModelPath() {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "converted_model.tflite";
    }

    private void runInference() {
        if (tflite != null) {
            float[][] input = new float[1][1];
            input[0][0] = (int) (Math.random() * 100);

            float[][] output = new float[1][1];
            tflite.run(input, output);

            tvResult.setText("input[" + input[0][0] + "]output[" + output[0][0] + "]");
            Log.w(TAG, "input[" + input[0][0] + "]output[" + output[0][0] + "]");
        }
    }

    private void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        tfliteModel = null;
    }

}
