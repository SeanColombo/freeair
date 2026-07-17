package com.seancolombo.freeair.airquality

import org.junit.Assert.assertEquals
import org.junit.Test

class AqiCategoryTest {
    @Test
    fun `maps AQI values to the correct category at each boundary`() {
        assertEquals(AqiCategory.GOOD, AqiCategory.forAqi(0))
        assertEquals(AqiCategory.GOOD, AqiCategory.forAqi(50))
        assertEquals(AqiCategory.MODERATE, AqiCategory.forAqi(51))
        assertEquals(AqiCategory.MODERATE, AqiCategory.forAqi(100))
        assertEquals(AqiCategory.UNHEALTHY_FOR_SENSITIVE_GROUPS, AqiCategory.forAqi(101))
        assertEquals(AqiCategory.UNHEALTHY_FOR_SENSITIVE_GROUPS, AqiCategory.forAqi(150))
        assertEquals(AqiCategory.UNHEALTHY, AqiCategory.forAqi(151))
        assertEquals(AqiCategory.UNHEALTHY, AqiCategory.forAqi(200))
        assertEquals(AqiCategory.VERY_UNHEALTHY, AqiCategory.forAqi(201))
        assertEquals(AqiCategory.VERY_UNHEALTHY, AqiCategory.forAqi(300))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.forAqi(301))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.forAqi(501))
    }
}
