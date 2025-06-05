package com.safework

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.safework.api.ApiCaller
import com.safework.models.IssueModel
import com.safework.utils.ViewUtils
import android.graphics.BitmapFactory

class IssueDetailAdminActivity : AppCompatActivity(), OnMapReadyCallback {

    private var issueId: String? = null
    private lateinit var mMap: GoogleMap
    private var issueLocation: LatLng? = null

    private lateinit var screenTitle: TextView
    private lateinit var textUrgency: TextView
    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView
    private lateinit var textComments: TextView
    private lateinit var imagePreview: ImageView
    private lateinit var labelImage: TextView
    private lateinit var mapFragmentContainer: View
    private lateinit var labelMap: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail_admin)

        ViewUtils.transparentBar(this)

        screenTitle = findViewById(R.id.screenTitleDetailAdmin)
        textUrgency = findViewById(R.id.textUrgencyDetailAdmin)
        textTitle = findViewById(R.id.textTitleDetailAdmin)
        textDescription = findViewById(R.id.textDescriptionDetailAdmin)
        textComments = findViewById(R.id.textCommentsDetailAdmin)
        imagePreview = findViewById(R.id.imagePreviewDetailAdmin)
        labelImage = findViewById(R.id.labelImageDetailAdmin)
        mapFragmentContainer = findViewById(R.id.mapDetailAdmin)
        labelMap = findViewById(R.id.labelMapDetailAdmin)

        issueId = intent.getStringExtra("ISSUE_ID")

        if (issueId == null) {
            ViewUtils.showNotification(this, "ID da ocorrência não encontrado!", ViewUtils.NotificationType.ERROR)
            finish()
            return
        }

        fetchIssueDetails(issueId!!)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapDetailAdmin) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun fetchIssueDetails(id: String) {
        ApiCaller.getDetailedIssueById(id) { result ->
            result.onSuccess { detailedIssue ->
                populateViews(detailedIssue)
                fetchIssueImage(id)
            }.onFailure { exception ->
                ViewUtils.showNotification(this, "Erro ao buscar detalhes da ocorrência: ${exception.localizedMessage}", ViewUtils.NotificationType.ERROR)
                labelImage.visibility = View.GONE
                imagePreview.visibility = View.GONE
                labelMap.visibility = View.GONE
                mapFragmentContainer.visibility = View.GONE
            }
        }
    }

    private fun fetchIssueImage(id: String) {
        labelImage.visibility = View.GONE
        imagePreview.visibility = View.GONE

        ApiCaller.getImageBytesByIssueId(id) { result ->
            result.onSuccess { imageData ->
                if (imageData.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    imagePreview.setImageBitmap(bitmap)
                    labelImage.visibility = View.VISIBLE
                    imagePreview.visibility = View.VISIBLE
                    Log.d("IssueDetailAdminActivity", "Imagem carregada com sucesso para issue ID: $id")
                } else {
                    Log.d("IssueDetailAdminActivity", "Nenhum dado de imagem retornado ou dados vazios para issue ID: $id")
                }
            }.onFailure { exception ->
                Log.e("IssueDetailAdminActivity", "Erro ao carregar imagem para issue ID $id: ${exception.localizedMessage}", exception)
            }
        }
    }

    private fun populateViews(issue: IssueModel) {
        Log.d("IssueDetailAdminActivity", issue.toString())
        screenTitle.text = "Detalhes: ${issue.title}"
        textUrgency.text = issue.level.name
        textTitle.text = issue.title
        textDescription.text = issue.description
        textComments.text = if (issue.comments.isNullOrEmpty()) "Nenhum." else issue.comments

        labelImage.visibility = View.GONE
        imagePreview.visibility = View.GONE

        val latitude = issue.mapLocal.latitude
        val longitude = issue.mapLocal.longitude

        if (latitude != null && longitude != null) {
            issueLocation = LatLng(latitude, longitude)
            labelMap.visibility = View.VISIBLE
            mapFragmentContainer.visibility = View.VISIBLE
            if (::mMap.isInitialized) {
                updateMapLocation()
            }
        } else {
            labelMap.visibility = View.GONE
            mapFragmentContainer.visibility = View.GONE
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        updateMapLocation()
    }

    private fun updateMapLocation() {
        issueLocation?.let { loc ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(loc).title("Local da Ocorrência"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }

    override fun finish() {
        super.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ViewUtils.changeActivity<AdminActivity>(
            this
        )
    }
}