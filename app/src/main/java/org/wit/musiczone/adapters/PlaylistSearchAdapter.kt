package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Playlist

class PlaylistSearchAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistSearchAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistImage: ImageView = itemView.findViewById(R.id.playlist_image)
        val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        val playlistInfo: TextView = itemView.findViewById(R.id.playlist_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name
        holder.playlistInfo.text = "${playlist.songCount} songs"
        
        if (playlist.imageResId != 0) {
            holder.playlistImage.setImageResource(playlist.imageResId)
        } else {
            holder.playlistImage.setImageResource(R.drawable.icon_recommend) // Placeholder
        }
        
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}

