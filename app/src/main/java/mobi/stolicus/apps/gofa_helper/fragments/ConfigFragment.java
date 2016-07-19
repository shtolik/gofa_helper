package mobi.stolicus.apps.gofa_helper.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.ConfigActivity;
import mobi.stolicus.apps.gofa_helper.GofaHelperActivity;
import mobi.stolicus.apps.gofa_helper.helpers.ClipboardHelper;
import mobi.stolicus.apps.gofa_helper.helpers.GAnalyticWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.Prefs;
import mobi.stolicus.apps.gofa_helper.helpers.TopActivityHelper;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link ConfigFragment.OnConfigFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConfigFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFragment.class);
    private static final String TAG = ConfigFragment.class.getSimpleName();
    private OnConfigFragmentInteractionListener mListener;
    private CheckBox cbOpenOnDoubleCopy;
    private CheckBox cbObserveKeepClipboardHistory;
    private CheckBox cbOpenOnSingleCopy;
    private CheckBox cbPostNotificationOnCopy;
    private CheckBox cbObserveClipboard;
    private CheckBox cbShowManualLinkEnter;
    private CheckBox cbRunOnStartup;
    private CheckBox cbGoogleAnalytics;

    private MenuItem mHistoryMenuItem;

    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_config, container, false);

        SharedPreferences sp = Prefs.getPrefs(getActivity());

        cbObserveClipboard = (CheckBox) rootView.findViewById(R.id.observe_clipboard);
        cbObserveClipboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                    boolean old_state = sp.getBoolean(Prefs.SET_OBSERVE_CLIPBOARD, Prefs.DEF_OBSERVE_CLIPBOARD);
                    if (old_state != isChecked) {

                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                                    TopActivityHelper.needPermissionForBlocking(ConfigFragment.this.getActivity())) {
                                //won't be able to tell whether gofa is already running or not
                                // so can't monitor clipboard
//                    should complain to user and ask to set permission
                                String complain = getString(R.string.ask_to_set_permission);
                                sp.edit().putBoolean(Prefs.SET_OBSERVE_CLIPBOARD, false).apply();
                                Toast.makeText(getActivity().getApplicationContext(), complain, Toast.LENGTH_LONG).show();
                                try {
                                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                    startActivity(intent);
                                } catch (Throwable t) {
                                    Log.w(TAG, ":" + t.getMessage(), t);
                                }
                            }
                            //expecting user to set permission
                            mListener.onClipboardObservingStart();
                        } else {
                            mListener.onClipboardObservingStop();
                        }
                        sp.edit().putBoolean(Prefs.SET_OBSERVE_CLIPBOARD, isChecked).apply();
                    }
                    updateAvailableConfigs();


            }
        });

        cbOpenOnSingleCopy = (CheckBox) rootView.findViewById(R.id.open_on_single_copy);
        cbOpenOnSingleCopy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_OPEN_ON_SINGLE_COPY, Prefs.DEF_OPEN_ON_SINGLE_COPY);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_OPEN_ON_SINGLE_COPY, isChecked).apply();
                }
            }
        });


        cbPostNotificationOnCopy = (CheckBox) rootView.findViewById(R.id.post_notification);
        cbPostNotificationOnCopy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_POST_NOTIFICATION, Prefs.DEF_POST_NOTIFICATION);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_POST_NOTIFICATION, isChecked).apply();
                }
            }
        });


        cbObserveKeepClipboardHistory = (CheckBox) rootView.findViewById(R.id.keep_clipboard_history);
        cbObserveKeepClipboardHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_KEEP_LINKS_HISTORY, isChecked).apply();
                }
                updateAvailableConfigs();


            }
        });

        cbOpenOnDoubleCopy = (CheckBox) rootView.findViewById(R.id.open_on_double_copy);
        cbOpenOnDoubleCopy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, Prefs.DEF_OPEN_ON_DOUBLE_COPY);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, isChecked).apply();
                }
            }
        });

        CheckBox cbCreateWidget = (CheckBox) rootView.findViewById(R.id.create_widget);
        cbCreateWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_CREATE_WIDGET, Prefs.DEF_CREATE_WIDGET);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_CREATE_WIDGET, isChecked).apply();
                }
                if (isChecked) {
                    mListener.onCreateWidget();
                }

            }
        });

        cbShowManualLinkEnter = (CheckBox) rootView.findViewById(R.id.manual_input);
        cbShowManualLinkEnter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_MANUAL_INPUT_ENABLED, Prefs.DEF_MANUAL_INPUT_ENABLED);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_MANUAL_INPUT_ENABLED, isChecked).apply();
                }
            }
        });

        cbRunOnStartup = (CheckBox) rootView.findViewById(R.id.run_on_startup);
        cbRunOnStartup.setChecked(sp.getBoolean(Prefs.SET_RUN_ON_STARTUP, Prefs.DEF_RUN_ON_STARTUP));
        cbRunOnStartup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_RUN_ON_STARTUP, Prefs.DEF_RUN_ON_STARTUP);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_RUN_ON_STARTUP, isChecked).apply();
                }
            }
        });

        cbGoogleAnalytics = (CheckBox) rootView.findViewById(R.id.google_analytics);
        cbGoogleAnalytics.setChecked(sp.getBoolean(Prefs.SET_GOOGLE_ANALYTICS_ENABLED, Prefs.DEF_GOOGLE_ANALYTICS_ENABLED));
        cbGoogleAnalytics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = Prefs.getPrefs(getActivity());
                boolean old_state = sp.getBoolean(Prefs.SET_GOOGLE_ANALYTICS_ENABLED, Prefs.DEF_GOOGLE_ANALYTICS_ENABLED);
                if (old_state != isChecked) {
                    sp.edit().putBoolean(Prefs.SET_GOOGLE_ANALYTICS_ENABLED, isChecked).apply();
                    GAnalyticWrapper.getInstance(getContext()).setAnalyticsOptOut(!isChecked);
                }
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        return rootView;
    }

    void updateAvailableConfigs() {
        SharedPreferences sp = Prefs.getPrefs(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        boolean observe_clipboard = sp.getBoolean(Prefs.SET_OBSERVE_CLIPBOARD, Prefs.DEF_OBSERVE_CLIPBOARD);

        if (!ClipboardHelper.checkIfClipboardListeningPossible()){
            logger.warn("Observing clipboard is not possible before Android OS 3.0 (Honeycomb)");
            Toast.makeText(getActivity(), R.string.observing_clipboard_not_possible, Toast.LENGTH_LONG).show();
            editor.putBoolean(Prefs.SET_OBSERVE_CLIPBOARD, false);
            observe_clipboard = false;
        }

        if (!observe_clipboard) {
            editor.putBoolean(Prefs.SET_OPEN_ON_SINGLE_COPY, false);
            editor.putBoolean(Prefs.SET_POST_NOTIFICATION, false);
            editor.putBoolean(Prefs.SET_KEEP_LINKS_HISTORY, false);
            editor.putBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, false);
            editor.apply();

            if (cbOpenOnSingleCopy != null)
                cbOpenOnSingleCopy.setEnabled(false);

            if (cbPostNotificationOnCopy != null)
                cbPostNotificationOnCopy.setEnabled(false);

            if (cbObserveKeepClipboardHistory != null)
                cbObserveKeepClipboardHistory.setEnabled(false);

            if (cbOpenOnDoubleCopy != null)
                cbOpenOnDoubleCopy.setEnabled(false);
        } else {
            if (cbOpenOnSingleCopy != null)
                cbOpenOnSingleCopy.setEnabled(true);

            if (cbPostNotificationOnCopy != null)
                cbPostNotificationOnCopy.setEnabled(true);

            if (cbObserveKeepClipboardHistory != null)
                cbObserveKeepClipboardHistory.setEnabled(true);
        }


        boolean keepHistory = sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY);
        if (!keepHistory) {
            editor.putBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, false);
            editor.apply();
            if (cbOpenOnDoubleCopy != null)
                cbOpenOnDoubleCopy.setEnabled(false);
        } else {
            if (cbOpenOnDoubleCopy != null)
                cbOpenOnDoubleCopy.setEnabled(true);
        }

        if (!checkShowingMainActivity()) {
            GofaHelperActivity.updateHome((ConfigActivity) getActivity(), false);
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(false);
        } else {
            GofaHelperActivity.updateHome((ConfigActivity) getActivity(), true);
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(true);
        }
        updateViews();
    }

    boolean checkShowingMainActivity() {
        SharedPreferences sp = Prefs.getPrefs(getActivity().getApplicationContext());
        boolean keepHistory = sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY);
        boolean manualInput = sp.getBoolean(Prefs.SET_MANUAL_INPUT_ENABLED, Prefs.DEF_MANUAL_INPUT_ENABLED);
        return !(!keepHistory && !manualInput);
    }

    public void updateViews(){
        SharedPreferences sp = Prefs.getPrefs(getActivity().getApplicationContext());
        boolean observe_clipboard = sp.getBoolean(Prefs.SET_OBSERVE_CLIPBOARD, Prefs.DEF_OBSERVE_CLIPBOARD);
        boolean open_on_double_copy = sp.getBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, Prefs.DEF_OPEN_ON_DOUBLE_COPY);
        boolean open_on_single_copy = sp.getBoolean(Prefs.SET_OPEN_ON_SINGLE_COPY, Prefs.DEF_OPEN_ON_SINGLE_COPY);
        boolean post_notification = sp.getBoolean(Prefs.SET_POST_NOTIFICATION, Prefs.DEF_POST_NOTIFICATION);
        boolean keep_links_history = sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY);
        boolean run_on_startup = sp.getBoolean(Prefs.SET_RUN_ON_STARTUP, Prefs.DEF_KEEP_LINKS_HISTORY);
        boolean google_analytics = sp.getBoolean(Prefs.SET_GOOGLE_ANALYTICS_ENABLED, Prefs.DEF_GOOGLE_ANALYTICS_ENABLED);
        boolean manual_input_enabled = sp.getBoolean(Prefs.SET_MANUAL_INPUT_ENABLED, Prefs.DEF_MANUAL_INPUT_ENABLED);

        if (cbObserveClipboard != null)
            cbObserveClipboard.setChecked(observe_clipboard);

        if (cbOpenOnSingleCopy != null)
            cbOpenOnSingleCopy.setChecked(open_on_single_copy);

        if (cbPostNotificationOnCopy != null)
            cbPostNotificationOnCopy.setChecked(post_notification);

        if (cbObserveKeepClipboardHistory != null)
            cbObserveKeepClipboardHistory.setChecked(keep_links_history);

        if (cbOpenOnDoubleCopy != null)
            cbOpenOnDoubleCopy.setChecked(open_on_double_copy);

        if (cbShowManualLinkEnter!= null)
            cbShowManualLinkEnter.setChecked(manual_input_enabled);

        if (cbRunOnStartup != null)
            cbRunOnStartup.setChecked(run_on_startup);

        if (cbGoogleAnalytics != null)
            cbGoogleAnalytics.setChecked(google_analytics);

        if (!checkShowingMainActivity()) {
            GofaHelperActivity.updateHome((ConfigActivity) getActivity(), false);
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(false);
        } else {
            GofaHelperActivity.updateHome((ConfigActivity) getActivity(), true);
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();
        // Refresh the state of the +1 button each time the activity receives focus.
        updateViews();

        GAnalyticWrapper.getInstance(getContext()).reportScreenViewStart(
                GAnalyticWrapper.SCREEN_START + GAnalyticWrapper.SCREEN_CONFIG);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ConfigActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnConfigFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getView() != null && imm != null)
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_config, menu);
        mHistoryMenuItem = menu.findItem(R.id.action_go_history);
        if (!checkShowingMainActivity()) {
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(false);
        } else {
            if (mHistoryMenuItem != null)
                mHistoryMenuItem.setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_go_history) {
            if (checkShowingMainActivity()) {
                getActivity().finish();
            }
        } else if (id == android.R.id.home) {
            // Respond to the action bar's Up/Home button

            if (checkShowingMainActivity()) {
                getActivity().finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnConfigFragmentInteractionListener {
        void onClipboardObservingStart();
        void onClipboardObservingStop();
        void onCreateWidget();
    }

}
