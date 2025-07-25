package com.example.snacktrack

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.snacktrack.ui.MainActivity

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 * Tests basic app functionality and UI components.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.snacktrack", appContext.packageName)
    }
    
    @Test
    fun testMainActivityLaunches() {
        // Test that the main activity launches without crashing
        activityRule.scenario.onActivity { activity ->
            assertNotNull("MainActivity should not be null", activity)
            assertFalse("MainActivity should not be finishing", activity.isFinishing)
        }
    }
    
    @Test
    fun testAppHasCorrectApplicationId() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedPackageName = "com.example.snacktrack"
        assertEquals("Application ID should match", expectedPackageName, appContext.packageName)
    }
    
    @Test
    fun testApplicationClassExists() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val appInfo = appContext.applicationInfo
        assertNotNull("Application info should exist", appInfo)
        assertTrue("App should be debuggable in test environment", 
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }
}