package mobi.stolicus.apps.gofa_helper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.fragments.ConfigFragment;

public class ConfigActivity extends AppCompatActivity implements ConfigFragment.OnConfigFragmentInteractionListener {

    protected static final Logger logger = LoggerFactory.getLogger(ConfigActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_activity_layout);
    }

    @Override
    public void onClipboardObservingStart() {
        ClipboardMonitorService.clipboardListeningStart(this);
    }

    @Override
    public void onClipboardObservingStop() {
        ClipboardMonitorService.clipboardListeningStop(this);
    }

    @Override
    public void onCreateWidget() {
        GofaHelperActivity.createWidget(this);
    }

}
