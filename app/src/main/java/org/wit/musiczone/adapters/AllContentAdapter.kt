package org.wit.musiczone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.musiczone.R
import org.wit.musiczone.models.Album
import org.wit.musiczone.models.Artist
import org.wit.musiczone.models.Playlist
import org.wit.musiczone.models.Song

sealed class AllContentItem {
    data class SectionHeader(
        val title: String,
        val iconResId: Int? = null
    ) : AllContentItem()
    
    data class SongItem(val song: Song) : AllContentItem()
    data class ArtistItem(val artist: Artist) : AllContentItem()
    data class AlbumItem(val album: Album) : AllContentItem()
    data class PlaylistItem(val playlist: Playlist) : AllContentItem()
}

class AllContentAdapter(
    private val items: List<AllContentItem>,
    private val onSongClick: (Song) -> Unit = {},
    private val onArtistClick: (Artist) -> Unit = {},
    private val onAlbumClick: (Album) -> Unit = {},
    private val onPlaylistClick: (Playlist) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SECTION_HEADER = 0
        private const val TYPE_SONG = 1
        private const val TYPE_ARTIST = 2
        private const val TYPE_ALBUM = 3
        private const val TYPE_PLAYLIST = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AllContentItem.SectionHeader -> TYPE_SECTION_HEADER
            is AllContentItem.SongItem -> TYPE_SONG
            is AllContentItem.ArtistItem -> TYPE_ARTIST
            is AllContentItem.AlbumItem -> TYPE_ALBUM
            is AllContentItem.PlaylistItem -> TYPE_PLAYLIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SECTION_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                SectionHeaderViewHolder(view)
            }
            TYPE_SONG -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_playlist_song, parent, false)
                SongViewHolder(view)
            }
            TYPE_ARTIST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_artist, parent, false)
                ArtistViewHolder(view)
            }
            TYPE_ALBUM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_album, parent, false)
                AlbumViewHolder(view)
            }
            TYPE_PLAYLIST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_playlist, parent, false)
                PlaylistViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is AllContentItem.SectionHeader -> {
                (holder as SectionHeaderViewHolder).bind(item)
            }
            is AllContentItem.SongItem -> {
                (holder as SongViewHolder).bind(item.song, onSongClick)
            }
            is AllContentItem.ArtistItem -> {
                (holder as ArtistViewHolder).bind(item.artist, onArtistClick)
            }
            is AllContentItem.AlbumItem -> {
                (holder as AlbumViewHolder).bind(item.album, onAlbumClick)
            }
            is AllContentItem.PlaylistItem -> {
                (holder as PlaylistViewHolder).bind(item.playlist, onPlaylistClick)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.sectionTitle)
        private val sectionIcon: ImageView = itemView.findViewById(R.id.sectionIcon)

        fun bind(header: AllContentItem.SectionHeader) {
            sectionTitle.text = header.title
            if (header.iconResId != null) {
                sectionIcon.visibility = View.VISIBLE
                sectionIcon.setImageResource(header.iconResId)
            } else {
                sectionIcon.visibility = View.GONE
            }
        }
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberText: TextView = itemView.findViewById(R.id.song_number)
        private val albumArt: ImageView = itemView.findViewById(R.id.album_art)
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)
        private val artistName: TextView = itemView.findViewById(R.id.artist_name)
        private val playButton: android.widget.ImageButton = itemView.findViewById(R.id.btn_play)
        private val moreButton: android.widget.ImageButton = itemView.findViewById(R.id.btn_more)

        fun bind(song: Song, onSongClick: (Song) -> Unit) {
            numberText.text = "${song.id}"
            songTitle.text = song.title
            artistName.text = song.artist

            // Use placeholder for album art (albumArtPath is a String path, not a resource ID)
            albumArt.setImageResource(R.drawable.icon_recommend)

            playButton.setOnClickListener { onSongClick(song) }
            moreButton.setOnClickListener { /* Handle more */ }
        }
    }

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistImage: ImageView = itemView.findViewById(R.id.artist_image)
        private val artistName: TextView = itemView.findViewById(R.id.artist_name)
        private val artistInfo: TextView = itemView.findViewById(R.id.artist_info)

        fun bind(artist: Artist, onArtistClick: (Artist) -> Unit) {
            artistName.text = artist.name
            artistInfo.text = "Artist"

            if (artist.imageResId != 0) {
                artistImage.setImageResource(artist.imageResId)
            } else {
                artistImage.setImageResource(R.drawable.icon_recommend)
            }

            itemView.setOnClickListener { onArtistClick(artist) }
        }
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumArt: ImageView = itemView.findViewById(R.id.album_art)
        private val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        private val albumArtist: TextView = itemView.findViewById(R.id.album_artist)

        fun bind(album: Album, onAlbumClick: (Album) -> Unit) {
            albumTitle.text = album.title
            albumArtist.text = album.artist

            if (album.albumArtResId != 0) {
                albumArt.setImageResource(album.albumArtResId)
            } else {
                albumArt.setImageResource(R.drawable.icon_recommend)
            }

            itemView.setOnClickListener { onAlbumClick(album) }
        }
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistImage: ImageView = itemView.findViewById(R.id.playlist_image)
        private val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
        private val playlistInfo: TextView = itemView.findViewById(R.id.playlist_info)

        fun bind(playlist: Playlist, onPlaylistClick: (Playlist) -> Unit) {
            playlistName.text = playlist.name
            playlistInfo.text = "${playlist.songCount} songs"

            if (playlist.imageResId != 0) {
                playlistImage.setImageResource(playlist.imageResId)
            } else {
                playlistImage.setImageResource(R.drawable.icon_recommend)
            }

            itemView.setOnClickListener { onPlaylistClick(playlist) }
        }
    }
}

