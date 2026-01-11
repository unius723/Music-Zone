package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Album

class AlbumAdapter(
    private val albums: List<Album>,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.album_art)
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val albumArtist: TextView = itemView.findViewById(R.id.album_artist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.albumTitle.text = album.title
        holder.albumArtist.text = album.artist
        
        if (album.albumArtResId != 0) {
            holder.albumArt.setImageResource(album.albumArtResId)
        } else {
            holder.albumArt.setImageResource(R.drawable.icon_recommend) // Placeholder
        }
        
        holder.itemView.setOnClickListener {
            onAlbumClick(album)
        }
    }

    override fun getItemCount(): Int = albums.size
}

