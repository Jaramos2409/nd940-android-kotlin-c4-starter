package com.udacity.project4.util

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RevokePermissionRule(private vararg val permissions: String) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                revokePermissions()
                try {
                    base.evaluate()
                } catch (e: Exception) {
                    e.stackTrace
                }
            }
        }
    }

    private fun revokePermissions() {
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        permissions.forEach { permission ->
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("pm revoke $packageName $permission")
        }
    }

}
