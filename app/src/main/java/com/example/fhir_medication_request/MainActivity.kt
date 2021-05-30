package com.example.fhir_medication_request

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fhir_medication_request.common.GUEST
import com.example.fhir_medication_request.common.MEDICATION_REQUEST
import com.example.fhir_medication_request.common.SECRET_KEY
import com.example.fhir_medication_request.common.SNACK
import com.example.fhir_medication_request.database.model.MedicationRequest
import com.example.fhir_medication_request.viewmodel.MedicationRequestViewModel
import com.example.fhir_medication_request.databinding.ActivityMainBinding
import com.example.fhir_medication_request.ui.AddEditActivity
import com.example.fhir_medication_request.ui.LoginActivity
import com.example.fhir_medication_request.ui.MedicationRequestListAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private val auth by lazy { Firebase.auth }
    private val medicationRequestViewModel: MedicationRequestViewModel by viewModels()

    private val requests = ArrayList<MedicationRequest>()
    private val medReqListAdapter = MedicationRequestListAdapter(requests)

    private var isGuest = true
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(SECRET_KEY)?.let {
            if (it != auth.currentUser?.uid) {
                finish()
                return
            }
        }

        intent.getStringExtra(SNACK)?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        isGuest = intent.getBooleanExtra(GUEST, true)
        with(binding) {
            if (isGuest) {
                fab.isEnabled = false
                medReqListAdapter.guest = true
            } else {
                medReqListAdapter.guest = false
            }
        }

        val recyclerView = binding.medReqRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = medReqListAdapter

        val pullToRefresh = binding.refreshLayout
        pullToRefresh.setColorSchemeColors(
            resources.getColor(R.color.primaryLightColor, null),
            resources.getColor(R.color.primaryColor, null),
            resources.getColor(R.color.primaryDarkColor, null)
        )
        pullToRefresh.setOnRefreshListener {
            queryData()
            pullToRefresh.isRefreshing = false
        }
        queryData()
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        this.menuInflater.inflate(R.menu.med_req_list_menu, menu)
        val search = menu.findItem(R.id.menu_search_bar)
        val searchView = search.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean  = false

            override fun onQueryTextChange(newText: String?): Boolean {
                medReqListAdapter.filter.filter(newText)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.menu_log_out_button -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java).putExtra(SNACK, "Signed out"))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun queryData() {
        this.requests.clear()
        medicationRequestViewModel.getMedicationRequests().observe(this) { requests ->
            requests.forEach { this.requests.add(it) }
            medReqListAdapter.notifyDataSetChanged()
        }
    }

    internal fun deleteItem(medicationRequest: MedicationRequest) {
        medicationRequestViewModel.delete(medicationRequest)
        queryData()
//        Toast.makeText(this, "Deleted medication request", Toast.LENGTH_SHORT).show()
        Snackbar.make(binding.medReqRecyclerView, "Deleted medication request", Snackbar.LENGTH_LONG).show()
    }

    internal fun startAddOrEditActivity(medicationRequest: MedicationRequest?) {
        startActivity(Intent(this, AddEditActivity::class.java).putExtra(MEDICATION_REQUEST, Json.encodeToString(medicationRequest))
            .putExtra(GUEST, auth.currentUser?.isAnonymous))
        finish()
    }

    fun addRequest(@Suppress("UNUSED_PARAMETER") source: View)
        = startAddOrEditActivity(null)
}