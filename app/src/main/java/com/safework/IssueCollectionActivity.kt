package com.safework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.safework.api.ApiCaller
import com.safework.utils.HomeDynamicIssuesFactory
import com.safework.utils.IssueCollectionFactory
import com.safework.utils.IssueInfo
import com.safework.utils.ViewUtils
import com.safework.utils.appendAll

class IssueCollectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issue_collection_layout)
        ViewUtils.transparentBar(this)

        val activity = this@IssueCollectionActivity
        val variables = ViewUtils.getIntentVariables(
            context = activity,
            variables = listOf("userId", "email", "username")
        )

        val userId = variables[0]["userId"] as String

        loadingIssues(activity, userId)
    }

    private fun loadingIssues(context: Context, userId: String) {
        Log.d("IssueCollectionActivity", "userId: $userId")

        ApiCaller.getIssuesByUserId(
            userId = userId,
            length = 0,
            callback = { result ->
                result.fold(
                    onSuccess = { issues ->
                        if (issues.isNotEmpty()) {
                            val iconMap = listOf(
                                R.drawable.ic_progress,
                                R.drawable.pending_icon,
                                R.drawable.analysis_icon,
                                R.drawable.ic_resolved,
                            )

                            val issueInfoList = issues.mapIndexed { index, issue ->
                                val icon = when (issue.status.toString()) {
                                    "ANDAMENTO" -> 0
                                    "PENDENTE" -> 1
                                    "ANALISE" -> 2
                                    else -> 3
                                }

                                IssueInfo(
                                    title = issue.title,
                                    status = issue.status.toString(),
                                    icon = iconMap[icon]
                                )
                            }

                            val container = findViewById<LinearLayout>(R.id.recentClaimsContainer)
                            container.appendAll(
                                context = context,
                                dataList = issueInfoList,
                                factory = IssueCollectionFactory()
                            )
                        }
                    },
                    onFailure = {
                        runOnUiThread {
                            ViewUtils.showNotification(
                                this,
                                "Erro ao carregar ocorrências!",
                                type = ViewUtils.NotificationType.ERROR
                            )
                        }
                    }
                )
            }
        )
    }
}
