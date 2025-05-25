package com.safework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.safework.api.ApiCaller
import com.safework.utils.HomeDynamicIssuesFactory
import com.safework.utils.IssueInfo
import com.safework.utils.ViewUtils
import com.safework.utils.appendAll

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        ViewUtils.transparentBar(this)

        val activity = this@HomeActivity
        val variables = ViewUtils.getIntentVariables(
            context = activity,
            variables = listOf("userId", "email", "username")
        )

        val userId = variables[0]["userId"] as String
        val username = variables[2]["username"] as String

        activity.findViewById<TextView>(R.id.welcomeText).text = "Seja bem vindo, $username!"

        redirectToIssuesActivity(activity, variables)
        redirectToIssuesCollection(activity, variables)
        loadingIssues(activity, userId)
    }

    private fun loadingIssues(context: Context, userId: String) {
        Log.d("HomeActivity", "userId: $userId")

        ApiCaller.getIssuesByUserId(
            userId = userId,
            length = 6,
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


                                IssueInfo(
                                    id = issue._id,
                                    title = issue.title,
                                    status = issue.level.toString(),
                                )
                            }

                            val container = findViewById<ViewGroup>(R.id.recentClaimsContainer)
                            container.appendAll(
                                context = context,
                                dataList = issueInfoList,
                                factory = HomeDynamicIssuesFactory()
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

    private fun redirectToIssuesActivity(context: Context, variables: List<Map<String, Any>>) {
        val card = (context as Activity).findViewById<CardView>(R.id.cardNovaReclamacao)
        card.setOnClickListener {
            ViewUtils.changeActivity<IssueActivity>(context, variables)
        }
    }

    private fun redirectToIssuesCollection(context: Context, variables: List<Map<String, Any>>) {
        val card = (context as Activity).findViewById<CardView>(R.id.cardMinhasReclamacoes)
        card.setOnClickListener {
            ViewUtils.changeActivity<IssueCollectionActivity>(context, variables)
        }
    }
}

