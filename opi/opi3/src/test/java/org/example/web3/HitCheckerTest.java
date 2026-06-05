package org.example.web3;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HitCheckerTest {

    @Test
    public void shouldReturnTrueForCenterPoint() {
        assertTrue(HitChecker.isHit(0, 0, 2));
    }

    @Test
    public void shouldReturnTrueForRectangleArea() {
        assertTrue(HitChecker.isHit(0.5, -1, 2));
    }

    @Test
    public void shouldReturnFalseForPointOutsideArea() {
        assertFalse(HitChecker.isHit(-2, -2, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNonPositiveRadius() {
        HitChecker.isHit(0, 0, 0);
    }
}
