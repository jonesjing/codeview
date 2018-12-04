package com.jones.code;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private PhoneCodeView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        codeView = findViewById(R.id.code_view);
        codeView.showSoftInput();
        codeView.setOnInputCompleteListener(new PhoneCodeView.OnInputCompleteListener() {
            @Override
            public void onInputComplete(String code) {
                Toast.makeText(MainActivity.this, code, Toast.LENGTH_SHORT).show();
                codeView.setError();
            }
        });

    }
}
