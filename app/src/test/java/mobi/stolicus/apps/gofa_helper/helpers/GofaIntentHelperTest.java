package mobi.stolicus.apps.gofa_helper.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing of gofa links parsing
 * Created by shtolik on 06.09.2015.
 */
@SuppressWarnings("SpellCheckingInspection")
@RunWith(MockitoJUnitRunner.class)
public class GofaIntentHelperTest extends TestCase {

    @Mock
    Context mMockContext;

    @Test
    public void testPrepareGofaIntentDataUri() throws Exception {
        Intent intent = new Intent();
        Intent result = GofaIntentHelper.prepareGofaIntentDataUri(mMockContext, intent, "gofa://2001/players/zzz");
        assertNotNull(result);
        assertThat(result.getData(), is(Uri.parse("gofa://2001/players/zzz")));

        Intent resultNull = GofaIntentHelper.prepareGofaIntentDataUri(mMockContext, intent, "//2001/players/zzz");
        assertEquals(resultNull, null);

        resultNull = GofaIntentHelper.prepareGofaIntentDataUri(mMockContext, intent, "//2001/players/zzz/gofa://");
        assertEquals(resultNull, null);
    }


    @Test
    public void testCheckAndFindGofaLinks() throws Exception {
        List<String> emptyResults = new ArrayList<>();
        List<String> result = GofaIntentHelper.checkAndFindGofaLinks("gofa://2001/players/z1z2z3");
        assertNotNull(result);
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is("gofa://2001/players/z1z2z3"));

        result = GofaIntentHelper.checkAndFindGofaLinks("//2001/players/z1z2z3");
        assertEquals(result, emptyResults);

        result = GofaIntentHelper.checkAndFindGofaLinks("//2001/players/z1z2z3/gofa://");
        assertEquals(result, emptyResults);

        result = GofaIntentHelper.checkAndFindGofaLinks("gofa://2001/players/Grifter%2egofa://2002/planets/660/510/Hinelt1");
        assertNotNull(result);
        assertThat(result.size(), is(2));
        assertThat(result.get(0), is("gofa://2001/players/Grifter%2e"));
        assertThat(result.get(1), is("gofa://2002/planets/660/510/Hinelt1"));

        result = GofaIntentHelper.checkAndFindGofaLinks("gofa://2001/players/z1z2z3 gofa://2001/alliances/%5cV%2f gofa://2001/planets/570/150/Lonyro4 gofa://2001/planets/570/150/Lonyro5");
        assertNotNull(result);
        assertThat(result.size(), is(4));
        assertThat(result.get(0), is("gofa://2001/players/z1z2z3"));
        assertThat(result.get(1), is("gofa://2001/alliances/%5cV%2f"));
        assertThat(result.get(2), is("gofa://2001/planets/570/150/Lonyro4"));
        assertThat(result.get(3), is("gofa://2001/planets/570/150/Lonyro5"));


        result = GofaIntentHelper.checkAndFindGofaLinks("gofa://2001/players/z1z2z3 gofa://2001/alliances/%5cV%2f gofa://2001/planets/570/150/Lonyro4 gofa://2001/planets/570/150/Lonyro5\n" +
                "        gofa://2001/players/Grifter%2egofa://2002/planets/660/510/Hinelt1gofa://2002/planets/660/480/Tonefo1;gofa://2002/players/Tela%20Vazir\n" +
                "        gofa://2002/alliances/DED\n");
        assertNotNull(result);
        assertThat(result.size(), is(9));
        assertThat(result.get(0), is("gofa://2001/players/z1z2z3"));
        assertThat(result.get(1), is("gofa://2001/alliances/%5cV%2f"));
        assertThat(result.get(2), is("gofa://2001/planets/570/150/Lonyro4"));
        assertThat(result.get(3), is("gofa://2001/planets/570/150/Lonyro5"));
        assertThat(result.get(4), is("gofa://2001/players/Grifter%2e"));
        assertThat(result.get(5), is("gofa://2002/planets/660/510/Hinelt1"));
        assertThat(result.get(6), is("gofa://2002/planets/660/480/Tonefo1"));
        assertThat(result.get(7), is("gofa://2002/players/Tela%20Vazir"));
        assertThat(result.get(8), is("gofa://2002/alliances/DED"));

        result = GofaIntentHelper.checkAndFindGofaLinks("gofa://2003/planets/330/510/Banud2  300am server time");
        assertNotNull(result);
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is("gofa://2003/planets/330/510/Banud2"));
    }
}