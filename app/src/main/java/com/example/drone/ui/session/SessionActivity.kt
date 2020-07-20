package com.example.drone.ui.session

import android.os.Bundle
import android.view.View
import com.example.drone.R
import android.widget.Toast
import android.content.Intent
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import androidx.appcompat.app.AppCompatActivity
import com.example.drone.data.api.model.Session
import com.example.drone.ui.list.DetailsAdapter
import com.android.volley.toolbox.JsonArrayRequest
import com.example.drone.data.storage.SessionStorage
import com.example.drone.data.api.model.SessionDetails
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drone.data.AppConfig.Companion.API_URL
import com.example.drone.data.api.factory.VolleyFactory
import com.example.drone.data.api.factory.SessionDetailsFactory
import kotlinx.android.synthetic.main.activity_session.*

class SessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        setupHeadButton()
        setupRecyclerView()
        setupTitleTextView()
        updateLiveTextView()
    }

    private fun setupTitleTextView()
    {
        text_view_title.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateLiveTextView()
    {
        if (SessionStorage.isActive()) {
            text_view_live.visibility = View.VISIBLE
            text_view_live.setOnClickListener {
                val intent = Intent(this, LiveActivity::class.java)
                startActivity(intent)
            }
        } else {
            text_view_live.visibility = View.INVISIBLE
        }
    }

    private fun setupRecyclerView()
    {
        val detailsArray: MutableList<SessionDetails> = mutableListOf()
        val sessionId = intent.getIntExtra("sessionId", -1)
        fetchDetails(sessionId)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = DetailsAdapter(detailsArray)
    }

    private fun setupHeadButton()
    {
        val sessionId = intent.getIntExtra("sessionId", -1)
        button_details.text = "Session N°$sessionId"
    }

    private fun fetchDetails(id: Int)
    {
        val url = "${API_URL}/details/query/list/session/$id"
        val volleyRequest = Volley.newRequestQueue(this)
        val jsonArrayRequest = object :
            JsonArrayRequest(Method.GET, url, null, Response.Listener { jsonResponse ->
                val detailsArray = SessionDetailsFactory.mapFromJSONArray(jsonResponse)
                recycler_view.adapter = DetailsAdapter(detailsArray.asReversed())
            }, Response.ErrorListener { error ->
                val message = VolleyFactory.getMessage(error)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        volleyRequest.add(jsonArrayRequest)
    }
}