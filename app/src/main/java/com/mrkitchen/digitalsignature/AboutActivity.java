package com.mrkitchen.digitalsignature;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spanned policy = Html.fromHtml(getString(R.string.self_signed_link));
        TextView termsOfUse = (TextView)findViewById(R.id.self_signed_digital);
        termsOfUse.setText(policy);
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}