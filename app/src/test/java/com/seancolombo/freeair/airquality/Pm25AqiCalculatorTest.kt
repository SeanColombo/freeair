package com.seancolombo.freeair.airquality

import org.junit.Assert.assertEquals
import org.junit.Test

class Pm25AqiCalculatorTest {
    @Test
    fun `zero concentration is zero AQI`() {
        assertEquals(0, Pm25AqiCalculator.calculate(0.0))
    }

    @Test
    fun `negative concentration is clamped to zero AQI`() {
        assertEquals(0, Pm25AqiCalculator.calculate(-3.0))
    }

    @Test
    fun `midpoint of the good range`() {
        assertEquals(25, Pm25AqiCalculator.calculate(4.5))
    }

    @Test
    fun `top of the good range`() {
        assertEquals(50, Pm25AqiCalculator.calculate(9.0))
    }

    @Test
    fun `bottom of the moderate range`() {
        assertEquals(51, Pm25AqiCalculator.calculate(9.1))
    }

    @Test
    fun `midpoint of the moderate range`() {
        assertEquals(71, Pm25AqiCalculator.calculate(20.0))
    }

    @Test
    fun `top of the moderate range`() {
        assertEquals(100, Pm25AqiCalculator.calculate(35.4))
    }

    @Test
    fun `bottom of the unhealthy-for-sensitive-groups range`() {
        assertEquals(101, Pm25AqiCalculator.calculate(35.5))
    }

    @Test
    fun `midpoint of the unhealthy-for-sensitive-groups range`() {
        assertEquals(124, Pm25AqiCalculator.calculate(45.0))
    }

    @Test
    fun `top of the unhealthy-for-sensitive-groups range`() {
        assertEquals(150, Pm25AqiCalculator.calculate(55.4))
    }

    @Test
    fun `midpoint of the unhealthy range`() {
        assertEquals(182, Pm25AqiCalculator.calculate(100.0))
    }

    @Test
    fun `top of the very unhealthy range`() {
        assertEquals(300, Pm25AqiCalculator.calculate(225.4))
    }

    @Test
    fun `top of the hazardous range`() {
        assertEquals(500, Pm25AqiCalculator.calculate(325.4))
    }

    @Test
    fun `extreme concentrations extrapolate past 500 instead of clamping`() {
        assertEquals(501, Pm25AqiCalculator.calculate(326.0))
    }
}
