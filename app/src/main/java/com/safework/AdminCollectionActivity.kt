package com.safework

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.safework.api.ApiCaller
import com.safework.utils.IssueCollectionFactory
import com.safework.utils.IssueInfo
import com.safework.utils.ViewUtils
import com.safework.utils.appendAll
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.Button
import com.safework.utils.IssueAdminCollectionFactory
import java.time.LocalDate

class AdminCollectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_collection_layout)
        ViewUtils.transparentBar(this)

        val activity = this@AdminCollectionActivity

        loadAllIssues(activity)

        findViewById<Button>(R.id.filterButton).setOnClickListener {
            loadFilteredIssues(activity)
        }

        findViewById<EditText>(R.id.startDate).setOnClickListener {
            showDatePicker(true)
        }

        findViewById<EditText>(R.id.endDate).setOnClickListener {
            showDatePicker(false)
        }
    }

    private fun loadFilteredIssues(context: Context) {
        val startDateStr = findViewById<EditText>(R.id.startDate).text.toString()
        val endDateStr = findViewById<EditText>(R.id.endDate).text.toString()

        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            ViewUtils.showNotification(this, "Por favor, selecione as datas inicial e final.")
            return
        }

        try {
            val inputFormatter = DateTimeFormatter.ISO_DATE
            val outputFormatter = DateTimeFormatter.ISO_DATE_TIME

            val startDate = LocalDate.parse(startDateStr, inputFormatter).atStartOfDay()
            val endDate = LocalDate.parse(endDateStr, inputFormatter).atTime(23, 59, 59)

            val startDateFormatted = startDate.format(outputFormatter)
            val endDateFormatted = endDate.format(outputFormatter)

            ApiCaller.getIssuesByDateRange(startDateFormatted, endDateFormatted) { result ->
                result.onSuccess { issues ->
                    runOnUiThread {
                        if (issues.isNotEmpty()) {
                            val issueInfoList = issues.map { issue ->
                                IssueInfo(
                                    id = issue._id,
                                    title = issue.title,
                                    status = issue.level.toString()
                                )
                            }

                            val container = findViewById<LinearLayout>(R.id.adminClaimsContainer)
                            container.removeAllViews()
                            container.appendAll(
                                context = context,
                                dataList = issueInfoList,
                                factory = IssueAdminCollectionFactory()
                            )
                        } else {
                            ViewUtils.showNotification(this, "Nenhuma reclamação encontrada no período.")
                        }
                    }
                }.onFailure {
                    runOnUiThread {
                        ViewUtils.showNotification(this, "Erro ao buscar problemas: ${it.message}", ViewUtils.NotificationType.ERROR)
                    }
                }
            }
        } catch (e: Exception) {
            ViewUtils.showNotification(this, "Formato de data inválido. Use yyyy-MM-dd", ViewUtils.NotificationType.ERROR)
        }
    }

    private fun loadAllIssues(context: Context) {
        ApiCaller.getIssues(length = 0) { result ->
            result.onSuccess { issues ->
                runOnUiThread {
                    if (issues.isNotEmpty()) {
                        val issueInfoList = issues.map { issue ->
                            IssueInfo(
                                id = issue._id,
                                title = issue.title,
                                status = issue.level.toString()
                            )
                        }

                        val container = findViewById<LinearLayout>(R.id.adminClaimsContainer)
                        container.removeAllViews()
                        container.appendAll(
                            context = context,
                            dataList = issueInfoList,
                            factory = IssueAdminCollectionFactory()
                        )
                    } else {
                        Toast.makeText(context, "Nenhuma reclamação encontrada.", Toast.LENGTH_LONG).show()
                    }
                }
            }.onFailure {
                runOnUiThread {
                    Toast.makeText(context, "Erro ao buscar problemas: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                if (isStartDate) {
                    findViewById<EditText>(R.id.startDate).setText(formattedDate)
                } else {
                    findViewById<EditText>(R.id.endDate).setText(formattedDate)
                }
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ViewUtils.changeActivity<AdminActivity>(
            this
        )
    }
}
