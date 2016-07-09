package mobi.stolicus.apps.gofa_helper.db;

import junit.framework.TestCase;

import mobi.stolicus.app.gofa_helper.R;

/**
 * tests
 * Created by shtolik on 06.09.2015.
 */
public class ClipTest extends TestCase {

    public void testGetResIconFromClipText() throws Exception {
        assertEquals(R.drawable.geography6_, Clip.getResIconFromClipText("planets"));
        assertEquals(R.drawable.users30_, Clip.getResIconFromClipText("alliances"));
        assertEquals(R.drawable.science28_, Clip.getResIconFromClipText("users"));
    }
}