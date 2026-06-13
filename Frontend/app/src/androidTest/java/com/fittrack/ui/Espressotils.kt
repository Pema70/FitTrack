package com.fittrack.ui

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException

/**
 * Czeka maksymalnie [timeoutMs] ms aż widok pasujący do [viewMatcher] pojawi się w hierarchii.
 * Przydatne gdy Navigation Component ładuje fragment asynchronicznie po starcie Activity.
 *
 * Użycie:
 *   waitForView(withId(R.id.etEmail))
 *   waitForView(withId(R.id.etEmail), timeoutMs = 5000)
 */
fun waitForView(viewMatcher: Matcher<View>, timeoutMs: Long = 3000) {
    onView(isRoot()).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription() = "Czeka $timeoutMs ms na widok: $viewMatcher"

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()
            val deadline = System.currentTimeMillis() + timeoutMs
            while (System.currentTimeMillis() < deadline) {
                val found = TreeIterables.breadthFirstViewTraversal(view)
                    .any { viewMatcher.matches(it) }
                if (found) return
                uiController.loopMainThreadForAtLeast(100)
            }
            throw PerformException.Builder()
                .withActionDescription(description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(TimeoutException("Widok nie pojawił się w ciągu $timeoutMs ms"))
                .build()
        }
    })
}