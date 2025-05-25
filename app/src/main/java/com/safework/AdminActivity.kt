package com.safework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.safework.utils.ViewUtils

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        ViewUtils.transparentBar(this) // Se estiver usando barra transparente no app
        findViewById<CardView>(R.id.cardMap).setOnClickListener {
            ViewUtils.changeActivity<RiskMapActivity>(
                this
            )
        }

        findViewById<CardView>(R.id.cardGerenciar).setOnClickListener {
            ViewUtils.changeActivity<AdminCollectionActivity>(
                this
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ViewUtils.changeActivity<LoginActivity>(
            this
        )
    }
}

