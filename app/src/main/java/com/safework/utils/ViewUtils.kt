package com.safework.utils

import android.widget.AdapterView
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import com.safework.IssueDetailAdminActivity
import com.safework.R
import com.safework.api.ApiCaller
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ViewUtils {
    internal var notificationIsRunning: Boolean = false
    enum class NotificationType {
        WARNING,
        ERROR
    }

    fun showNotification(context: Activity, message: String, type: NotificationType? = NotificationType.WARNING) {
        if(this.notificationIsRunning) return
        this.notificationIsRunning =  true

        val notificationLayout = LayoutInflater.from(context)
            .inflate(R.layout.notificacao_layout, null)

        val notificationContainer: ViewGroup = notificationLayout.findViewById(R.id.notification_container)
        val notificationText: TextView = notificationLayout.findViewById(R.id.notification_text)
        val notificationIcon: ImageView = notificationLayout.findViewById(R.id.notification_icon)

        notificationText.text = message

        val rootLayout = context.findViewById<ViewGroup>(android.R.id.content)
        rootLayout.addView(notificationLayout)

        val slideIn = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        ).apply { duration = 500 }

        val slideOut = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        ).apply { duration = 500 }

        notificationContainer.startAnimation(slideIn)

        Handler(Looper.getMainLooper()).postDelayed({
            notificationContainer.startAnimation(slideOut)
            Handler(Looper.getMainLooper()).postDelayed({
                rootLayout.removeView(notificationLayout)
                this.notificationIsRunning = false
            }, slideOut.duration)
        }, 3000)

        animateIcon(icon = notificationIcon, type = type!!)
    }

    fun animateIcon(icon: ImageView, type: NotificationType) {
        val colorResId = when (type) {
            NotificationType.WARNING -> android.R.color.holo_orange_light
            NotificationType.ERROR -> android.R.color.holo_red_light
        }

        val color = icon.context.getColor(colorResId)
        icon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)

        val rotate = android.view.animation.RotateAnimation(
            -10f, 15f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.0f
        ).apply {
            duration = 100
            repeatCount = 5
            repeatMode = Animation.REVERSE
        }

        icon.startAnimation(rotate)
    }

    fun transparentBar(context: Activity) {
        context.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun clearInputs(vararg editTexts: EditText) {
        editTexts.forEach { it.text.clear() }
    }

    data class Seconds(val q: Int)
    fun delayThen(action: () -> Unit, seconds: Seconds) {
        Handler(Looper.getMainLooper()).postDelayed(
            { action() },
            seconds.q * 1000L
        )
    }

    inline fun <reified T : Activity> changeActivity(
        context: Context,
        variables: List<Map<String, Any>>? = null
    ) {
        val intent = Intent(context, T::class.java)

        variables?.forEach { map ->
            map.forEach { (key, value) ->
                when (value) {
                    is String -> intent.putExtra(key, value)
                    is Int -> intent.putExtra(key, value)
                    is Boolean -> intent.putExtra(key, value)
                    is Float -> intent.putExtra(key, value)
                    is Double -> intent.putExtra(key, value)
                    else -> Log.d("change_activity", "Tipo de variável não suportada: $key")
                }
            }
        }

        context.startActivity(intent)
        animateChangeActivity(context as Activity)
        context.finish()
    }

    fun getIntentVariables(context: Context, variables: List<String>): List<Map<String, Any>> {
        val intent: Intent = (context as Activity).intent
        val extractedVariables = mutableListOf<Map<String, Any>>()

        variables.forEach { currentKey ->
            val extractedMap = mutableMapOf<String, Any>()
            intent.extras?.let { extras ->
                if (extras.containsKey(currentKey)) {
                    val value = extras.get(currentKey)
                    if (value != null) {
                        extractedMap[currentKey] = value
                    }
                }
            }

            if (extractedMap.isNotEmpty()) {
                extractedVariables.add(extractedMap)
            }
        }

        return extractedVariables
    }

    fun animateChangeActivity(context: Activity) {
        context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    internal fun buttonAnimation(button: Button) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
    }

    fun configSpinner(context: Context) {
        val spinner = (context as Activity).findViewById<Spinner>(R.id.spinnerUrgency)
        val issuesLevels = arrayOf("Selecione o nível do problema", "GRAVE", "MÉDIO", "LEVE")

        // Criar o adapter
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, issuesLevels)

        // Configurar o layout do dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Definir o adapter para o Spinner
        spinner.adapter = adapter

        // Tornar a primeira opção não selecionável (desabilitada)
        spinner.setSelection(0, false) // Definir a seleção inicial para o primeiro item, sem permitir que o usuário o selecione

        // Impedir a seleção do primeiro item, tornando-o visualmente não selecionável
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Se a opção selecionada for a primeira (índice 0), apenas a desmarque
                if (position == 0) {
                    spinner.setSelection(0, false) // Desmarcar a opção
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        })
    }

}

fun Button.onClick(action: () -> Unit) {
    this.setOnClickListener {
        action()
    }

    ViewUtils.buttonAnimation(this)
}

fun TextView.onClick(action: () -> Unit) {
    this.setOnClickListener {
        action()
    }
}

interface WidgetFactory<T> {
    fun create(context: Context, data: List<T>): List<View>
}

data class IssueInfo(val id: String, val title: String, val status: String)

class HomeDynamicIssuesFactory : WidgetFactory<IssueInfo> {
    override fun create(context: Context, data: List<IssueInfo>): List<View> {
        val views = mutableListOf<View>()


        data.forEach { issue ->
            val view = LayoutInflater.from(context).inflate(R.layout.issue_item_layout, null)

            val titleText = view.findViewById<TextView>(R.id.title)
            val status = view.findViewById<TextView>(R.id.issue_status)

            titleText.text = issue.title
            status.text = issue.status
            when (issue.status) {
                "GRAVE" -> status.setBackgroundResource(R.drawable.status_wip)
                "MEDIO" -> status.setBackgroundResource(R.drawable.status_analysis)
                "LEVE" -> status.setBackgroundResource(R.drawable.status_done)
            }

            views.add(view)
        }

        return views
    }
}

class IssueCollectionFactory : WidgetFactory<IssueInfo> {
    override fun create(context: Context, data: List<IssueInfo>): List<View> {
        val views = mutableListOf<View>()

        val parent = LinearLayout(context)
        parent.orientation = LinearLayout.VERTICAL

        data.forEach { issue ->
            val view = LayoutInflater.from(context).inflate(R.layout.issue_item_collection_layout, parent, false)
            val deleteIcon = view.findViewById<ImageView>(R.id.issue_delete)
            val titleText = view.findViewById<TextView>(R.id.title)
            val status = view.findViewById<TextView>(R.id.issue_status)

            deleteIcon.setOnClickListener {
                val builder = android.app.AlertDialog.Builder(context)
                builder.setTitle("Confirmar exclusão")
                builder.setMessage("Deseja realmente excluir esta reclamação?")

                builder.setPositiveButton("Excluir") { _, _ ->
                    ApiCaller.deleteIssue(issue.id) { success ->
                        if (success) {
                            (view.parent as? ViewGroup)?.removeView(view)
                            ViewUtils.showNotification(
                                context as Activity,
                                "Ocorrência deletada com sucesso"
                            )
                        } else {
                            ViewUtils.showNotification(
                                context as Activity,
                                "Erro ao deletar ocorrência",
                                type = ViewUtils.NotificationType.ERROR
                            )
                        }
                    }
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

            titleText.text = issue.title
            status.text = issue.status
            when (issue.status) {
                "GRAVE" -> status.setBackgroundResource(R.drawable.status_wip)
                "MEDIO" -> status.setBackgroundResource(R.drawable.status_analysis)
                "LEVE" -> status.setBackgroundResource(R.drawable.status_done)
            }


            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 12
            view.layoutParams = layoutParams

            views.add(view)
        }

        return views
    }
}

class IssueAdminCollectionFactory : WidgetFactory<IssueInfo> {
    override fun create(context: Context, data: List<IssueInfo>): List<View> {
        val views = mutableListOf<View>()

        val parent = LinearLayout(context)
        parent.orientation = LinearLayout.VERTICAL

        data.forEach { issue ->
            val view = LayoutInflater.from(context).inflate(R.layout.issue_item_collection_admin_layout, parent, false)
            val titleText = view.findViewById<TextView>(R.id.title)
            val status = view.findViewById<TextView>(R.id.issue_status)

            titleText.text = issue.title
            status.text = issue.status
            when (issue.status) {
                "GRAVE" -> status.setBackgroundResource(R.drawable.status_wip)
                "MEDIO" -> status.setBackgroundResource(R.drawable.status_analysis)
                "LEVE" -> status.setBackgroundResource(R.drawable.status_done)
            }

            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 12
            view.layoutParams = layoutParams

            view.setOnClickListener {
                val intent = Intent(context, IssueDetailAdminActivity::class.java)
                intent.putExtra("ISSUE_ID", issue.id)
                // Você também pode passar outros dados se forem úteis para exibição imediata
                // intent.putExtra("ISSUE_TITLE", issue.title)
                // intent.putExtra("ISSUE_STATUS", issue.status)
                context.startActivity(intent)

                // Opcional: Adicionar animação de transição, se ViewUtils.changeActivity faz isso
                if (context is Activity) {
                    // Usar a animação padrão do ViewUtils se quiser consistência,
                    // ou a padrão do Android ao iniciar uma nova activity.
                    // A linha abaixo é se você quiser a mesma animação do seu ViewUtils.changeActivity
                    // mas sem o context.finish() que tem lá.
                    context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            views.add(view)
        }

        return views
    }
}

fun <T> ViewGroup.append(
    context: Context,
    data: T,
    factory: WidgetFactory<T>
) {
    val view = factory.create(context = context, data = listOf(data))
    this.addView(view[0])
}

fun <T> ViewGroup.appendAll(
    context: Context,
    dataList: List<T>,
    factory: WidgetFactory<T>
) {
    dataList.forEach {
        data -> this.append(context, data, factory)
    }
}


