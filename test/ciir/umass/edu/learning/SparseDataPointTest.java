package ciir.umass.edu.learning;

import org.junit.Test;

import static org.junit.Assert.*;

public class SparseDataPointTest {

    @Test
    public void getFeatureValue() {
        final SparseDataPoint sdp = new SparseDataPoint(new float[]{Float.NaN, .1f, .2f, .3f, .4f}, new int[]{0, 2, 5, 13, 22}, "1", 0);
        assertTrue(sdp.hasFeature(2));
        assertTrue(sdp.hasFeature(5));
        assertTrue(sdp.hasFeature(13));
        assertTrue(sdp.hasFeature(22));

        assertFalse(sdp.hasFeature(1));
        assertFalse(sdp.hasFeature(6));
        assertFalse(sdp.hasFeature(7));
        assertFalse(sdp.hasFeature(8));
        assertFalse(sdp.hasFeature(9));
        assertFalse(sdp.hasFeature(10));
        assertFalse(sdp.hasFeature(23));
        assertFalse(sdp.hasFeature(57));
        assertFalse(sdp.hasFeature(43236));

        assertEquals(.1f, sdp.getFeatureValue(2), 0f);
        assertEquals(.2f, sdp.getFeatureValue(5), 0f);
        assertEquals(.3f, sdp.getFeatureValue(13), 0f);
        assertEquals(.4f, sdp.getFeatureValue(22), 0f);

        assertEquals(0, sdp.getFeatureValue(10), 0f);
    }
}