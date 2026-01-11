package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Artist

class ArtistAdapter(
    private val artists: List<Artist>,
    private val onArtistClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistImage: ImageView = itemView.findViewById(R.id.artist_image)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val artistInfo: TextView = itemView.findViewById(R.id.artist_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.artistName.text = artist.name
        holder.artistInfo.text = "Artist"
        
        if (artist.imageResId != 0) {
            holder.artistImage.setImageResource(artist.imageResId)
        } else {
            holder.artistImage.setImageResource(R.drawable.icon_recommend) // Placeholder
        }
        
        holder.itemView.setOnClickListener {
            onArtistClick(artist)
        }
    }

    override fun getItemCount(): Int = artists.size
}

