package mobi.stolicus.apps.gofa_helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.fragments.ClipHistoryFragment;
import mobi.stolicus.apps.gofa_helper.fragments.LinkInputFragment;
import mobi.stolicus.apps.gofa_helper.helpers.ClipboardHelper;
import mobi.stolicus.apps.gofa_helper.helpers.GAnalyticWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.GofaIntentHelper;
import mobi.stolicus.apps.gofa_helper.helpers.Prefs;
import mobi.stolicus.apps.gofa_helper.support.ConfigureLog4J;


public class GofaHelperActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {
    private static final Logger logger = LoggerFactory.getLogger(GofaHelperActivity.class);

    public static final String ACTION_WIDGET_CLICKED = "action_widget_clicked";
    public static final String ACTION_NOTIFICATION_CLICKED = "action_notification_clicked";
    public static final String ACTION_REQUEST_BOOTUP = "action_request_bootup";
    public static final String BUNDLE_SEARCH_WORD = "bundle_search_word";
    public static final String START_FROM_CONFIG = "FROM_CONFIG";
    private static final int ADD_WIDGET_KEY_CODE = 5;
    private static final int HOST_CODE = 1024;

    public static boolean mGofaHelperRunning = false;

    static {
        ConfigureLog4J.configure();
    }

    public static void createWidget(Activity act) {
        // suggesting user to add widget to dashboard
        logger.debug("/onCreateWidget/suggesting user to add widget to dashboard");
        AppWidgetHost host = new AppWidgetHost(act, HOST_CODE);
        int nextId = host.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, nextId);
        act.startActivityForResult(pickIntent, ADD_WIDGET_KEY_CODE);

    }

    public static void updateHome(AppCompatActivity context, boolean show) {
        ActionBar ab = context.getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(show);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_link_testing);

        final Intent intent = getIntent();

        if (handleIntent(intent, false)) {
            return;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

        if (Prefs.getPrefs(this).getBoolean(Prefs.SET_OBSERVE_CLIPBOARD, Prefs.DEF_OBSERVE_CLIPBOARD)) {
            startService(new Intent(this, ClipboardMonitorService.class));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }

    private boolean handleIntent(Intent intent, boolean newIntent) {
        if (intent == null) {
            logger.info("/handleIntent/no intent to parse");
            return false;
        }
        String action = intent.getAction();
        if (action == null) {
            logger.info("/handleIntent/no action to parse in intent");
            return false;
        }

        boolean hideApp = false;
        if (ACTION_REQUEST_BOOTUP.equals(action)) {
            // going foreground on boot
            logger.info("/handleIntent/starting app from boot up. going to background");
            hideApp = true;
        } else if (Intent.ACTION_MAIN.equals(action) && newIntent) {
            logger.info("/handleIntent/asking user if he wants to start app or gofa.");
            String link = ClipboardHelper.getTextFromClipboard(this);
            Intent gofaIntent = GofaIntentHelper.prepareFiringGofaIntent(this, link, null);
            if (gofaIntent != null) {
                confirmGofaAppStart(gofaIntent);
                hideApp = false;
            } else {
                hideApp = false;
            }

        } else if (Intent.ACTION_VIEW.equals(action)) {
            logger.info("/handleIntent/starting app on link click. going to background");
            String link = intent.getDataString();
            GofaIntentHelper.fireGofaIntent(GofaHelperActivity.this, link);
            hideApp = true;
        } else if (ACTION_NOTIFICATION_CLICKED.equals(action)) {
            logger.info("/handleIntent/starting app on notification click. going to background");
            String link = intent.getDataString();
            GofaIntentHelper.fireGofaIntent(GofaHelperActivity.this, link);
            hideApp = true;
            GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                    GAnalyticWrapper.Action, GAnalyticWrapper.NotificationClicked, "");
        } else if (ACTION_WIDGET_CLICKED.equals(action)) {

            String bufferText = ClipboardHelper.getTextFromClipboard(this);
            if (GofaIntentHelper.fireGofaIntent(this, bufferText)) {
                //starting gofa with path
                logger.info("/onCreate/started by widget with text in " +
                        "clipboard(" + bufferText + "), starting gofa");
                logger.info("/handleIntent/starting app on widget click. going to background");
                hideApp = true;
                GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                        GAnalyticWrapper.Action, GAnalyticWrapper.WidgetClicked, "");
            } else {
                logger.info("/onCreate/started by widget with text in clipboard=" + bufferText);
                //exit because not gofa?
                Toast.makeText(this, R.string.fire_no_gofa_scheme_in_text + bufferText, Toast.LENGTH_LONG).show();
                hideApp = false;
                GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                        GAnalyticWrapper.Action, GAnalyticWrapper.WidgetClickedNoGofa, "");
            }
        }
        return hideApp;
    }

    private void confirmGofaAppStart(final Intent gofaIntent) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.start_gofa_or_helper);
        adb.setMessage(getString(R.string.start_gofa_desc1) + gofaIntent.getData().toString() + getString(R.string.start_gofa_desc2));
        adb.setCancelable(true);
        adb.setPositiveButton(R.string.button_start_game, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                        GAnalyticWrapper.Action, GAnalyticWrapper.LauncherClickStartGame, "");

                Toast.makeText(GofaHelperActivity.this, getString(R.string.opening_link) + gofaIntent.getData().toString()
                        + getString(R.string.open_link_on_launcher_icon_click), Toast.LENGTH_LONG)
                        .show();
                startActivity(gofaIntent);
                dialog.dismiss();
                finish();
            }
        });
        adb.setNegativeButton(R.string.start_gofa_helper, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                        GAnalyticWrapper.Action, GAnalyticWrapper.LauncherClickStartHelper, "");
                dialog.dismiss();
            }
        });

        adb.show();
    }

    @Override
    protected void onDestroy() {
        ClipboardMonitorService.clipboardListeningStop(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (handleIntent(intent, true)) {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_link_testing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            openConfigActivity();
            return true;
        } else if (id == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                updateFragments();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFragments();
    }

    public void updateFragments() {
        FragmentManager fm = getSupportFragmentManager();
        SharedPreferences sp = Prefs.getPrefs(this);
        Fragment linkInputFragment = fm.findFragmentByTag(LinkInputFragment.class.getSimpleName());
        Fragment clipHistoryFragment = fm.findFragmentByTag(ClipHistoryFragment.class.getSimpleName());

        boolean manual_input_enabled = sp.getBoolean(Prefs.SET_MANUAL_INPUT_ENABLED, Prefs.DEF_MANUAL_INPUT_ENABLED);
        boolean keep_links_history = sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY);
        if (manual_input_enabled) {
            if (linkInputFragment == null) {
                logger.debug("linkInputFragment == null and should be added. adding");
                fm.beginTransaction()
                        .add(R.id.container_for_start_fragment, new LinkInputFragment(), LinkInputFragment.class.getSimpleName())
                        .commit();
            }
        } else {
            if (linkInputFragment != null) {
                logger.debug("linkInputFragment != null but should be removed. removing");
                fm.beginTransaction().remove(linkInputFragment).commit();
            }
        }
        if (keep_links_history) {
            if (clipHistoryFragment == null) {
                logger.debug("clipHistoryFragment == null and should be added. adding");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container_for_clip_fragment, new ClipHistoryFragment(), ClipHistoryFragment.class.getSimpleName())
                        .commit();
            }
        } else {
            if (clipHistoryFragment != null) {
                logger.debug("clipHistoryFragment != null but should be removed. removing");
                fm.beginTransaction().remove(clipHistoryFragment).commit();
            }

        }

        if (!keep_links_history && !manual_input_enabled) {
            openConfigActivity();
        }
    }

    void openConfigActivity() {
        Intent configActivity = new Intent(this, ConfigActivity.class);
        startActivity(configActivity);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            logger.trace("/onBackPressed/fragments in stack:" + count);
            getSupportFragmentManager().popBackStack();
            updateFragments();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGofaHelperRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGofaHelperRunning = false;
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(
                    getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }
}
