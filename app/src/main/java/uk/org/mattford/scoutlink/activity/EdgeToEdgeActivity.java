package uk.org.mattford.scoutlink.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import uk.org.mattford.scoutlink.utils.EdgeToEdgeHelper;

public abstract class EdgeToEdgeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdgeHelper.prepareWindow(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        EdgeToEdgeHelper.applyInsetsIfNeeded(this);
    }

    @Override
    public void setContentView(android.view.View view) {
        super.setContentView(view);
        EdgeToEdgeHelper.applyInsetsIfNeeded(this);
    }

    @Override
    public void setContentView(android.view.View view, android.view.ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        EdgeToEdgeHelper.applyInsetsIfNeeded(this);
    }
}


