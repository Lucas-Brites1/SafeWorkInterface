package com.safework

import android.app.AlertDialog // Importar AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.safework.api.ApiCaller
import com.safework.models.IssueLevel
import com.safework.models.IssueModel
import com.safework.utils.MapsUtils
import com.safework.utils.ViewUtils

class RiskMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var issueList: List<IssueModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_map)
        ViewUtils.transparentBar(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        MapsUtils.handleMapReady(
            googleMap,
            context = this,
            onLocationReady = { userLocation ->
                MapsUtils.moveCamera(googleMap, userLocation)
                loadIssuesAndMarkers()
            },
            onError = {
                Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
                loadIssuesAndMarkers()
            }
        )

        googleMap.setOnMarkerClickListener { marker ->
            val issue = marker.tag as? IssueModel
            issue?.let {
                showIssueDetailsDialog(it)
            }
            true
        }
    }

    private fun loadIssuesAndMarkers() {
        ApiCaller.getIssues(length = 0) { result ->
            result.onSuccess { issues ->
                issueList = issues
                addMarkersToMap()
            }.onFailure {
                Toast.makeText(this, "Erro ao buscar problemas: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addMarkersToMap() {
        googleMap.clear()
        for (issue in issueList) {
            val lat = issue.mapLocal.latitude
            val lng = issue.mapLocal.longitude
            val latLng = LatLng(lat, lng)

            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(issue.title)
                .icon(
                    when (issue.level) {
                        IssueLevel.GRAVE -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        IssueLevel.MEDIO -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        IssueLevel.LEVE -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    }
                )

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = issue
        }
    }

    private fun showIssueDetailsDialog(issue: IssueModel) {
        val variables = mutableListOf(
            mapOf("ISSUE_ID" to issue._id),
        )

        AlertDialog.Builder(this)
            .setTitle("Ver Reclamação")
            .setMessage("Deseja ver os detalhes da reclamação: \"${issue.title}\"?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                ViewUtils.changeActivity<IssueDetailAdminActivity>(
                    this,
                    variables
                )
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ViewUtils.changeActivity<AdminActivity>(
            this
        )
    }
}