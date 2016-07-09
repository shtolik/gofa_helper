package mobi.stolicus.apps.gofa_helper.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.ClipboardMonitorService;
import mobi.stolicus.apps.gofa_helper.GofaHelperActivity;
import mobi.stolicus.apps.gofa_helper.db.Clip;
import mobi.stolicus.apps.gofa_helper.db.ClipWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.ClipboardHelper;
import mobi.stolicus.apps.gofa_helper.helpers.GAnalyticWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.GofaIntentHelper;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class ClipHistoryFragment extends Fragment implements AbsListView.OnItemClickListener, ClipListListener
        , LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, ActionMode.Callback
        , AbsListView.MultiChoiceModeListener {
    private static final Logger logger = LoggerFactory.getLogger(ClipHistoryFragment.class);

    private static final int CLIP_HISTORY_LIST_LOADER = 1;
    private static final String STATE_CHOICE_MODE = "STATE_CHOICE_MODE";
    private static final String STATE_ACTION_MODE = "STATE_ACTION_MODE";

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ClipCursorAdapter mAdapter;
    private TextView mEmptyText;
    private ClearableEditText mSearchBox = null;
    private Bundle mLastArgument;
    private final TextWatcher mFilterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            Bundle bundle = new Bundle();
            bundle.putString(GofaHelperActivity.BUNDLE_SEARCH_WORD, s.toString());

            updateAdapter(bundle);

        }

    };

    private ActionMode cabMode;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ClipHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CLIP_HISTORY_LIST_LOADER, savedInstanceState, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clip, container, false);

        mEmptyText = (TextView) view.findViewById(android.R.id.empty);
        mEmptyText.requestFocus();

        mListView = (AbsListView) view.findViewById(R.id.clip_history_list);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setMultiChoiceModeListener(this);

        int choiceMode =
                (savedInstanceState == null ? ListView.CHOICE_MODE_NONE
                        : savedInstanceState.getInt(STATE_CHOICE_MODE));

        mListView.setChoiceMode(choiceMode);
        if (choiceMode == AbsListView.CHOICE_MODE_MULTIPLE) {
            mListView.startActionMode(this);
        }


        // Set the adapter
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        mSearchBox = (ClearableEditText) view.findViewById(R.id.search_box);
        mSearchBox.clearFocus();
        if (mSearchBox != null) {
            mSearchBox.addTextChangedListener(mFilterTextWatcher);
            mSearchBox.showSearchButton(true);
            mSearchBox.reInit();
        }
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (cabMode != null) {
            SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
            long[] checkedItemIds = mListView.getCheckedItemIds();
            logger.trace("/onItemClick/but cabMode on, checked before handling: " + checkedPositions.toString()
                    + "or " + Arrays.toString(checkedItemIds));
            boolean gottaCheck = false;
            for (long checkedItemId : checkedItemIds) {
                if (checkedItemId == id) {
                    gottaCheck = true;
                    break;
                }
            }
            if (gottaCheck) {
                mListView.setItemChecked(position, true);
            } else {
                mListView.setItemChecked(position, false);

            }
            updateSubtitle(cabMode);
        } else {
            Clip clip = ClipWrapper.getInstance(getActivity()).getClipById(id);
            if (clip == null) {
                logger.warn("/no clip found for id=" + id);
            } else {
                Intent gofaIntent = GofaIntentHelper.prepareFiringGofaIntent(getActivity(), clip.getText(),
                        getString(R.string.opening_hint_from_4gofa));
                if (gofaIntent != null) {
                    startActivity(gofaIntent);
                } else {
                    logger.warn("/no clip found for id=" + id);
                }
            }
        }
    }

    @Override
    public void onClipCopyClicked(Clip clip) {
        if (clip == null) {

            return;
        }

        if (ClipboardHelper.copyToClipboard(getActivity().getApplicationContext(), clip.getText())) {
            GAnalyticWrapper.getInstance(getContext()).reportEvent(
                    GAnalyticWrapper.Action, GAnalyticWrapper.HistoryClipClicked, "");
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.

        String FilterSearch = "";
        if (bundle != null) {
            FilterSearch = bundle.getString(GofaHelperActivity.BUNDLE_SEARCH_WORD);
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        CursorLoader cl = ClipWrapper.getInstance(getActivity()).getClipsCursorLoader(FilterSearch);


        if (mEmptyText != null) {
            mEmptyText.setVisibility(View.VISIBLE);
            mEmptyText.setText(R.string.clip_list_loading);
        }

        return cl;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (mLastArgument != null)
            bundle = mLastArgument;

        updateAdapter(bundle);

        GAnalyticWrapper.getInstance(getContext()).reportScreenViewStart(
                GAnalyticWrapper.SCREEN_START + GAnalyticWrapper.SCREEN_HISTORY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSearchBox != null)
            mSearchBox.removeTextChangedListener(mFilterTextWatcher);
    }

    /**
     * Updates list of cases
     */
    private void updateAdapter(Bundle bundle) {
        // init the query initialization.
        LoaderManager lm = getLoaderManager();

        if (bundle != null && !bundle.containsKey(GofaHelperActivity.BUNDLE_SEARCH_WORD)) {
            if (mLastArgument != null && mLastArgument.containsKey(GofaHelperActivity.BUNDLE_SEARCH_WORD)) {

                //passed intent doesn't have search word. keeping existing
                bundle.putString(GofaHelperActivity.BUNDLE_SEARCH_WORD,
                        mLastArgument.getString(GofaHelperActivity.BUNDLE_SEARCH_WORD));
            }
        }
        setUIArguments(bundle);


        lm.restartLoader(CLIP_HISTORY_LIST_LOADER, bundle, this);
        logger.trace("/updateAdapter/-");
    }

    public void setUIArguments(Bundle bundle) {
        mLastArgument = bundle;
    }

    private ClipCursorAdapter updateList(Cursor cursor) {
        ClipCursorAdapter adapter = null;
        try {
            int autoRequery = 0;
            adapter = new ClipCursorAdapter(getActivity(), cursor, autoRequery, this);

        } catch (SecurityException se) {
            logger.error("/updateList/" + se.getMessage(), se);
        } catch (Exception e) {
            logger.error("/updateList/error: " + e.toString(), e);
        }
        return adapter;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, final Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (isAdded()) {
                    if (mAdapter != null) {
                        mAdapter.changeCursor(cursor);
                    } else {
                        mAdapter = updateList(cursor);
                        if (mAdapter != null && mListView != null) {
                            mListView.setAdapter(mAdapter);
                        }
                    }
                }
            }
        });

        //mEmptyText is only shown when list is empty
        if (cursor != null && cursor.getCount() > 0) {
            mEmptyText.setVisibility(View.GONE);
//            mListView.requestFocus();
        } else {
            mEmptyText.setVisibility(View.VISIBLE);
//            mEmptyText.requestFocus();
        }
        mEmptyText.setText(R.string.clip_list_empty_clipboard_history);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getArguments() != null) {
            String FilterSearch = getArguments().getString(GofaHelperActivity.BUNDLE_SEARCH_WORD);
            outState.putInt(STATE_CHOICE_MODE, mListView.getChoiceMode());
            outState.putString(GofaHelperActivity.BUNDLE_SEARCH_WORD, FilterSearch != null ? FilterSearch : "");
            outState.putBoolean(STATE_ACTION_MODE, cabMode != null);

//            outState.putLongArray(CHECKED_ITEMS_IDS, mListView.getCheckedItemIds());

//            SparseBooleanArray selectedItems = mListView.getCheckedItemPositions();
//            SparseBooleanArray positions = mListView.getCheckedItemPositions();
//            outState.putBooleanArray(CHECKED_ITEMS_IDS, positions);
//            for (int i = 0; i < selectedItems.size(); i++) {
//                if (selectedItems.valueAt(i) == false)
//                    continue;
//                selectedItems_intArray[i] = selectedItems.keyAt(i);
//            }
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.cabMode != null)
            return false;
        cabMode = getActivity().startActionMode(this);
        mAdapter.setSelectingMode(true);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setItemChecked(position, true);
        mListView.setMultiChoiceModeListener(this);
        logger.debug("/onItemLongClick/enabled CAB mode");
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
//        mListView.getCheckedItemPositions();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
        // Here you can do something when items are selected/de-selected,
        // such as update the title in the CAB
        if (cabMode != null) {
            updateSubtitle(mode);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_copy_selected: {
                long[] checked = mListView.getCheckedItemIds();
                String copyBuffer = getClipTextsFromIds(checked);
//                stop service so item is not parsed again by buffer
                ClipboardMonitorService.clipboardListeningStop(getActivity());
                ClipboardHelper.copyToClipboard(getActivity(), copyBuffer);
//                restart service
                ClipboardMonitorService.clipboardListeningStart(getActivity());
            }
            break;
            case R.id.menu_select_all: {
                if (mListView.getCheckedItemCount() < mListView.getCount()) {
//                    select all if at least one unselected
                    logger.trace("/onActionItemClicked/selecting all ");
                    for (int i = 0; i < mListView.getAdapter().getCount(); i++) {
                        mListView.setItemChecked(i, true);
                    }
                } else {
//                  deselect all
                    logger.trace("/onActionItemClicked/deselecting all, currently checked" + mListView.getCheckedItemCount() + ">=" + mListView.getCount());
                    for (int i = 0; i < mListView.getAdapter().getCount(); i++) {
                        mListView.setItemChecked(i, false);
                    }
                }
            }
            break;
            case R.id.menu_delete: {
                // Calls getSelectedIds method from ListViewAdapter Class
                long[] checked = mListView.getCheckedItemIds();
                confirmDeletion(checked);
            }
            break;
            case R.id.menu_share: {
                long[] checked = mListView.getCheckedItemIds();
                String copyBuffer = getClipTextsFromIds(checked);
                ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setText(copyBuffer)
                        .setType("text/plain")
//                        .setChooserTitle(yourChooserTitle)
                        .startChooser();
            }
            break;
            default:
                return false;
        }
        updateSubtitle(cabMode);
        return true;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        cabMode = null;
        mListView.clearChoices();
        mAdapter.setSelectingMode(false);
        mListView.setMultiChoiceModeListener(null);
        mAdapter.notifyDataSetChanged();
        logger.debug("/onDestroyActionMode/");
    }

    private void confirmDeletion(final long[] checkedClips) {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        if (checkedClips.length == 1) {
            adb.setTitle(R.string.confirm_deletion);
        } else if (checkedClips.length > 1) {
            String strMeatFormat = getResources().getString(R.string.confirm_multi_deletion);
            String strMeatMsg = String.format(strMeatFormat, checkedClips.length);
            adb.setTitle(strMeatMsg);
        } else
            return;
        adb.setCancelable(true);
        adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipWrapper cw = ClipWrapper.getInstance(getActivity());
                cw.dbDeleteItems(checkedClips);
                dialog.dismiss();
                mAdapter.notifyDataSetChanged();
            }
        });
        adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        adb.show();
    }

    private void updateSubtitle(ActionMode mode) {
        mode.setTitle(mListView.getCheckedItemCount() + getActivity().getString(R.string.history_selection_items_selected));
    }

    @NonNull
    private String getClipTextsFromIds(long[] checked) {
        ClipWrapper cw = ClipWrapper.getInstance(getActivity());
        String selection = cw.parseSelection(-1, null, -1, null, checked);
        List<Clip> clips = cw.dbQueryList(selection, null, Clip.SORT_ORDER_TIMESTAMP_OLD_FIRST);
        StringBuilder copyBuffer = new StringBuilder();
        for (Clip clip : clips) {
            copyBuffer.append(clip.getText());
            copyBuffer.append("\n");
        }
        logger.info("/onActionItemClicked/menu_copy_selected/" + clips.size() + " selected clips copied to buffer");
        return copyBuffer.toString();
    }
}
