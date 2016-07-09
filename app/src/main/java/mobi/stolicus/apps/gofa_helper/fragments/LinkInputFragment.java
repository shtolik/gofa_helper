package mobi.stolicus.apps.gofa_helper.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.helpers.GAnalyticWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.GofaIntentHelper;
import mobi.stolicus.apps.gofa_helper.helpers.Prefs;


/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class LinkInputFragment extends Fragment {


    public LinkInputFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textLabel);
        tv.requestFocus();
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        Pattern wikiWordMatcher = Pattern.compile(Prefs.GOFA_PATTERN);
        String scheme = "";
        Linkify.addLinks(tv, wikiWordMatcher, scheme);
        final EditText editText = (EditText) rootView.findViewById(R.id.editLinkText);

        Button fireIntent = (Button) rootView.findViewById(R.id.fire_intent);
        fireIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText != null) {
                    String text2 = editText.getText().toString();
                    GofaIntentHelper.fireGofaIntent(getActivity(), text2);
                    GAnalyticWrapper.getInstance(getContext()).reportEvent(
                            GAnalyticWrapper.Action, GAnalyticWrapper.ManualLinkInput, "");

                }
            }
        });

        Button openBrowsable = (Button) rootView.findViewById(R.id.openBrowsable);
        openBrowsable.setVisibility(View.GONE);
        openBrowsable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText != null) {
                    String text2 = editText.getText().toString();
                    GofaIntentHelper.openBrowsable(getActivity(), text2);
                    GAnalyticWrapper.getInstance(getContext()).reportEvent(
                            GAnalyticWrapper.Action, GAnalyticWrapper.ManualLinkInput, "");
                }
            }
        });

        return rootView;
    }

}
