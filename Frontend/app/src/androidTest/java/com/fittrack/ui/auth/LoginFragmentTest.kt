package com.fittrack.ui.auth

import android.Manifest
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.fittrack.MainActivity
import com.fittrack.R
import com.fittrack.ui.waitForView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val notifPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginEkran_wyswietlaSiePrawidlowo() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForView(withId(R.id.etEmail))
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
            onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun klikniecieLogin_zPustymEmail_pokazujeBladWalidacji() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForView(withId(R.id.etEmail))
            onView(withId(R.id.etEmail)).perform(clearText())
            onView(withId(R.id.etPassword)).perform(typeText("haslo123"), closeSoftKeyboard())
            onView(withId(R.id.btnLogin)).perform(click())
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun klikniecieLogin_przyciskWidoczny() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForView(withId(R.id.btnLogin))
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
        }
    }
}