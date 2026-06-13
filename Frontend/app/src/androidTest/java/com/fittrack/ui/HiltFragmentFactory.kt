package com.fittrack.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry

inline fun <reified F : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    fragmentFactory: FragmentFactory? = null,
    crossinline action: F.() -> Unit = {}
) {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    val intent = Intent().apply {
        component = ComponentName(
            targetContext.packageName,
            "com.fittrack.ui.HiltTestActivity"
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    ActivityScenario.launch<HiltTestActivity>(intent).onActivity { activity ->
        fragmentFactory?.let { activity.supportFragmentManager.fragmentFactory = it }
        val fragment = activity.supportFragmentManager.fragmentFactory
            .instantiate(F::class.java.classLoader!!, F::class.java.name)
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()
        (fragment as F).action()
    }
}