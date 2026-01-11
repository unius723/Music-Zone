package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Song
import java.util.concurrent.TimeUnit

/**
 * Song List Adapter
 */
class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.imageViewAlbumArt)
        val title: TextView = itemView.findViewById(R.id.textViewTitle)
        val artist: TextView = itemView.findViewById(R.id.textViewArtist)
        val duration: TextView = itemView.findViewById(R.id.textViewDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        
        holder.title.text = song.title
        holder.artist.text = song.artist.ifEmpty { "Unknown Artist" }
        holder.duration.text = formatDuration(song.duration)

        // Set album art
        if (song.albumArtPath.isNotEmpty()) {
            // Can use Glide to load image later
            // Glide.with(holder.itemView.context)
            //     .load(song.albumArtPath)
            //     .placeholder(R.drawable.ic_music_note)
            //     .into(holder.albumArt)
            holder.albumArt.setImageResource(R.drawable.icon_recommend) // Temporarily use placeholder
        } else {
            holder.albumArt.setImageResource(R.drawable.icon_recommend) // Default placeholder
        }

        holder.itemView.setOnClickListener {
            onSongClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    /**
     * Format duration (milliseconds to mm:ss)
     */
    private fun formatDuration(milliseconds: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

