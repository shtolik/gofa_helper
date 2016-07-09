package mobi.stolicus.apps.gofa_helper.fragments;

import mobi.stolicus.apps.gofa_helper.db.Clip;

/**
 * Clip list interface
 * Created by shtolik on 27.08.2015.
 */
public interface ClipListListener {
    void onClipCopyClicked(Clip clip);
}
