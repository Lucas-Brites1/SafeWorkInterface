package com.safework

import android.app.Activity
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.Button

class AdminCollectionActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var email: String
    private lateinit var username: String
    private lateinit var selectedStartDate: LocalDateTime
    private lateinit var selectedEndDate: LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_collection_layout) // Certifique-se de que o layout está correto
        ViewUtils.transparentBar(this)

        val activity = this@AdminCollectionActivity

        // Carregar todas as reclamações inicialmente
        loadAllIssues(activity)

        // Configurar o botão de filtro
        findViewById<Button>(R.id.filterButton).setOnClickListener {
            loadFilteredIssues(activity)
        }

        // Configurar os campos de data
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
            // Se as datas de início ou fim não estão preenchidas, carrega todas as reclamações
            loadAllIssues(context)
        } else {
            // Caso contrário, realiza a busca filtrada pelas datas
            val startDate = LocalDateTime.parse(startDateStr + "T00:00:00")
            val endDate = LocalDateTime.parse(endDateStr + "T23:59:59")

            ApiCaller.getIssuesByDateRange(startDate.toString(), endDate.toString()) { result ->
                result.onSuccess { issues ->
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
                            factory = IssueCollectionFactory()
                        )
                    } else {
                        Toast.makeText(context, "Nenhuma reclamação encontrada no período", Toast.LENGTH_LONG).show()
                    }
                }.onFailure {
                    runOnUiThread {
                        Toast.makeText(context, "Erro ao buscar problemas: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadAllIssues(context: Context) {
        ApiCaller.getIssues(length = 0) { result ->
            result.onSuccess { issues ->
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
                        factory = IssueCollectionFactory()
                    )
                }
            }.onFailure {
                runOnUiThread {
                    Toast.makeText(
                        context,
                        "Erro ao buscar problemas: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val now = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                if (isStartDate) {
                    selectedStartDate = selectedDate
                    findViewById<EditText>(R.id.startDate).setText(formattedDate)
                } else {
                    selectedEndDate = selectedDate
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
            this,
            variables = listOf(
                mapOf("userId" to userId),
                mapOf("email" to email),
                mapOf("username" to username)
            )
        )
    }
}
