package com.example.drone.ui.list

import android.view.View
import com.example.drone.R
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.drone.data.api.model.SessionDetails
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.cell_details.*

class DetailsAdapter(val items: MutableList<SessionDetails>) : RecyclerView.Adapter<DetailsAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), LayoutContainer {

        override val containerView: View?
            get() = itemView

        fun bindDetails(details: SessionDetails) {
            text_view_time.text = details.time
            text_view_speed.text = "${details.speed} m/s"
            text_view_location.text = "📍"
            text_view_collision.text = "${details.distance} cm"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.cell_details))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDetails(items[position])
    }
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}
