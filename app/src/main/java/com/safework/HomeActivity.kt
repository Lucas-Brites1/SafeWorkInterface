package com.safework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.safework.api.ApiCaller
import com.safework.models.IssueModel
import com.safework.utils.HomeDynamicIssuesFactory
import com.safework.utils.IssueInfo
import com.safework.utils.ViewUtils
import com.safework.utils.appendAll
import kotlinx.coroutines.selects.SelectInstance

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        ViewUtils.transparentBar(this)

        val activity = this@HomeActivity
        val variables = ViewUtils.getIntentVariables(context = activity, variables = listOf("userId", "email", "username"))
        val userId = variables[0]["userId"] as String
        activity.findViewById<TextView>(R.id.welcomeText).text = "Seja bem vindo, ${variables[2]["username"] as String}"

        redirectToIssuesActivity(this, variables)

        fun loadingIssues(userId: String) {
            Log.d("HomeActivity", "userId: $userId")
            ApiCaller.getIssuesByUserId(
                userId = userId,
                length = 3,
                callback = { result ->
                    result.fold(
                        onSuccess = { issues ->
                            if (issues.isNotEmpty()) {
                                val first = issues[0]
                                val second = issues[1]
                                val third = issues[2]

                                val issueInfo1 = IssueInfo(
                                    title = first.title,
                                    status = first.status.toString(),
                                    icon = R.drawable.status_done
                                )

                                val issueInfo2 = IssueInfo(
                                    title = second.title,
                                    status = second.status.toString(),
                                    icon = R.drawable.status_pending
                                )

                                val issuesInfo3 = IssueInfo(
                                    title = third.title,
                                    status = second.status.toString(),
                                    icon = R.drawable.status_analysis
                                )

                                val container = findViewById<ViewGroup>(R.id.recentClaimsContainer)
                                container.appendAll(
                                    context = this,
                                    dataList = listOf(issueInfo1, issueInfo2, issuesInfo3),
                                    factory = HomeDynamicIssuesFactory()
                                )
                            }
                        },
                        onFailure = { exception ->
                            activity.runOnUiThread {
                                ViewUtils.showNotification(
                                    activity,
                                    "Erro ao carregar ocorrências!",
                                    type = ViewUtils.NotificationType.ERROR
                                )
                            }
                        }
                    )
                }
            )
        }

        loadingIssues(userId)
    }
}

fun redirectToIssuesActivity(context: Context, variables: List<Map<String, Any>>) {
    val card = (context as Activity).findViewById<CardView>(R.id.cardNovaReclamacao)
    card.setOnClickListener {  ViewUtils.changeActivity<IssueActivity>(context, variables) }
}