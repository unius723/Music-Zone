package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Song

class PlaylistAdapter(
    private val songs: List<Song>,
    private val onPlayClick: (Song) -> Unit,
    private val onMoreClick: (Song) -> Unit,
    private val onFavoriteClick: (Song, Boolean) -> Unit,
    private val isFavorite: (Long) -> Boolean
) : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>() {

    var currentPlayingIndex: Int = -1
        set(value) {
            val oldIndex = field
            field = value
            if (oldIndex != -1) notifyItemChanged(oldIndex)
            if (value != -1) notifyItemChanged(value)
        }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberText: TextView = itemView.findViewById(R.id.song_number)
        val albumArt: ImageView = itemView.findViewById(R.id.album_art)
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val playButton: ImageButton = itemView.findViewById(R.id.btn_play)
        val moreButton: ImageButton = itemView.findViewById(R.id.btn_more)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.btn_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val isPlaying = position == currentPlayingIndex
        val isFav = isFavorite(song.id)
        
        holder.numberText.text = "${position + 1}"
        holder.songTitle.text = song.title
        holder.artistName.text = song.artist.ifEmpty { "Unknown Artist" }
        
        // Use placeholder for album art
        holder.albumArt.setImageResource(R.drawable.icon_recommend)
        
        // Highlight currently playing song
        if (isPlaying) {
            holder.songTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light))
            holder.artistName.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light))
            holder.playButton.setImageResource(R.drawable.icon_pause)
        } else {
            holder.songTitle.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.purple))
            holder.artistName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.purple))
            holder.playButton.setImageResource(R.drawable.icon_play)
        }
        
        // Update favorite button state
        if (isFav) {
            holder.favoriteButton.setImageResource(R.drawable.icon_star)
        } else {
            holder.favoriteButton.setImageResource(R.drawable.icon_star_outline)
        }
        
        holder.playButton.setOnClickListener {
            onPlayClick(song)
        }
        
        holder.moreButton.setOnClickListener {
            onMoreClick(song)
        }
        
        holder.favoriteButton.setOnClickListener {
            val newFavoriteState = !isFav
            onFavoriteClick(song, newFavoriteState)
            // Update UI
            if (newFavoriteState) {
                holder.favoriteButton.setImageResource(R.drawable.icon_star)
            } else {
                holder.favoriteButton.setImageResource(R.drawable.icon_star_outline)
            }
        }
    }

    override fun getItemCount(): Int = songs.size
}

