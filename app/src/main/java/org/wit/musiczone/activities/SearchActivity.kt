package org.wit.musiczone.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wit.musiczone.R
import org.wit.musiczone.adapters.AlbumAdapter
import org.wit.musiczone.adapters.AllContentAdapter
import org.wit.musiczone.adapters.AllContentItem
import org.wit.musiczone.adapters.ArtistAdapter
import org.wit.musiczone.adapters.PlaylistAdapter
import org.wit.musiczone.adapters.PlaylistSearchAdapter
import org.wit.musiczone.database.MusicDBHelper
import org.wit.musiczone.models.Album
import org.wit.musiczone.models.Artist
import org.wit.musiczone.models.Playlist
import org.wit.musiczone.models.Song

class SearchActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var filterAll: TextView
    private lateinit var filterSongs: TextView
    private lateinit var filterArtists: TextView
    private lateinit var filterAlbums: TextView
    private lateinit var filterPlaylists: TextView
    
    // Content sections
    private lateinit var songsSection: FrameLayout
    private lateinit var artistsSection: FrameLayout
    private lateinit var albumsSection: FrameLayout
    private lateinit var playlistsSection: FrameLayout
    
    // RecyclerViews
    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var albumsRecyclerView: RecyclerView
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var allContentRecyclerView: RecyclerView
    
    // Placeholders
    private lateinit var songsPlaceholder: TextView
    private lateinit var artistsPlaceholder: TextView
    private lateinit var albumsPlaceholder: TextView
    private lateinit var playlistsPlaceholder: TextView
    
    private var currentFilter: String = "ALL"
    
    // Database
    private lateinit var dbHelper: MusicDBHelper
    
    // Data from database
    private val allSongs = mutableListOf<Song>()
    private val allArtists = mutableListOf<Artist>()
    private val allAlbums = mutableListOf<Album>()
    private val allPlaylists = mutableListOf<Playlist>()
    
    // Search debounce
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var searchJob: Job? = null
    private val SEARCH_DELAY_MS = 300L // 300ms delay for debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Initialize database
        dbHelper = MusicDBHelper(this)

        initializeViews()
        setupBackButton()
        setupSearchInput()
        setupFilterButtons()
        setupRecyclerViews()
        loadDataFromDatabase()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        filterAll = findViewById(R.id.filterAll)
        filterSongs = findViewById(R.id.filterSongs)
        filterArtists = findViewById(R.id.filterArtists)
        filterAlbums = findViewById(R.id.filterAlbums)
        filterPlaylists = findViewById(R.id.filterPlaylists)
        
        songsSection = findViewById(R.id.songsSection)
        artistsSection = findViewById(R.id.artistsSection)
        albumsSection = findViewById(R.id.albumsSection)
        playlistsSection = findViewById(R.id.playlistsSection)
        
        songsRecyclerView = findViewById(R.id.songsRecyclerView)
        artistsRecyclerView = findViewById(R.id.artistsRecyclerView)
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView)
        playlistsRecyclerView = findViewById(R.id.playlistsRecyclerView)
        allContentRecyclerView = findViewById(R.id.allContentRecyclerView)
        
        songsPlaceholder = findViewById(R.id.songsPlaceholder)
        artistsPlaceholder = findViewById(R.id.artistsPlaceholder)
        albumsPlaceholder = findViewById(R.id.albumsPlaceholder)
        playlistsPlaceholder = findViewById(R.id.playlistsPlaceholder)
    }
    
    /**
     * Load data from database
     */
    private fun loadDataFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load songs from database
                val songs = dbHelper.getAllSongs()
                
                // Extract unique artists from songs
                val artistMap = mutableMapOf<String, Int>()
                songs.forEach { song ->
                    if (song.artist.isNotBlank() && !artistMap.containsKey(song.artist)) {
                        artistMap[song.artist] = artistMap.size + 1
                    }
                }
                
                // Extract unique albums from songs
                val albumMap = mutableMapOf<Pair<String, String>, Int>()
                songs.forEach { song ->
                    if (song.album.isNotBlank()) {
                        val key = Pair(song.album, song.artist)
                        if (!albumMap.containsKey(key)) {
                            albumMap[key] = albumMap.size + 1
                        }
                    }
                }
                
                // Switch to main thread to update UI
                withContext(Dispatchers.Main) {
                    allSongs.clear()
                    allSongs.addAll(songs)
                    
                    allArtists.clear()
                    artistMap.forEach { (name, id) ->
                        allArtists.add(Artist(id, name, R.drawable.icon_recommend))
                    }
                    
                    allAlbums.clear()
                    albumMap.forEach { (pair, id) ->
                        allAlbums.add(Album(id, pair.first, pair.second, R.drawable.icon_recommend))
                    }
                    
                    // Playlists remain empty for now (can be extended later)
                    allPlaylists.clear()
                    
                    // Set initial filter and perform search
                    selectFilter(filterAll)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchInput() {
        // Ensure EditText can receive input
        searchInput.isFocusable = true
        searchInput.isFocusableInTouchMode = true
        searchInput.isEnabled = true
        searchInput.isClickable = true
        searchInput.isLongClickable = true
        
        // Set up click listener to focus and show keyboard
        searchInput.setOnClickListener {
            searchInput.requestFocus()
            showKeyboard()
        }
        
        // Set up focus change listener
        searchInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }
        
        searchButton.setOnClickListener {
            // Cancel any pending search
            cancelPendingSearch()
            // Perform immediate search when button is clicked
            performSearch()
            // Hide keyboard after search
            hideKeyboard()
        }

        searchInput.setOnEditorActionListener { _, _, _ ->
            // Cancel any pending search
            cancelPendingSearch()
            // Perform immediate search when Enter is pressed
            performSearch()
            // Hide keyboard after search
            hideKeyboard()
            true
        }
        
        // Add text change listener for real-time search with debounce
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Cancel previous pending search
                cancelPendingSearch()
                
                // Schedule new search after delay (debounce)
                searchRunnable = Runnable {
                    performSearch()
                }
                searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY_MS)
            }
        })
    }
    
    /**
     * Show keyboard for search input
     */
    private fun showKeyboard() {
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * Hide keyboard
     */
    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }
    
    /**
     * Cancel any pending search operation
     */
    private fun cancelPendingSearch() {
        searchRunnable?.let {
            searchHandler.removeCallbacks(it)
            searchRunnable = null
        }
        searchJob?.cancel()
    }

    private fun setupFilterButtons() {
        val filters = listOf(filterAll, filterSongs, filterArtists, filterAlbums, filterPlaylists)
        
        filters.forEach { filter ->
            filter.setOnClickListener {
                selectFilter(filter)
            }
        }

        // Set initial filter
        selectFilter(filterAll)
    }

    private fun selectFilter(selectedFilter: TextView) {
        // Reset all filters
        filterAll.isSelected = false
        filterSongs.isSelected = false
        filterArtists.isSelected = false
        filterAlbums.isSelected = false
        filterPlaylists.isSelected = false

        // Set selected filter
        selectedFilter.isSelected = true
        currentFilter = when (selectedFilter) {
            filterAll -> "ALL"
            filterSongs -> "SONGS"
            filterArtists -> "ARTISTS"
            filterAlbums -> "ALBUMS"
            filterPlaylists -> "PLAYLISTS"
            else -> "ALL"
        }

        // Perform search with new filter
        performSearch()
    }

    private fun setupRecyclerViews() {
        // Setup Songs RecyclerView
        songsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup Artists RecyclerView
        artistsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup Albums RecyclerView
        albumsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup Playlists RecyclerView
        playlistsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup All Content RecyclerView
        allContentRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Perform search based on current filter and query
     */
    private fun performSearch() {
        val query = searchInput.text.toString().trim()
        
        // Cancel any previous search job
        searchJob?.cancel()
        
        // Hide all sections first
        songsSection.visibility = View.GONE
        artistsSection.visibility = View.GONE
        albumsSection.visibility = View.GONE
        playlistsSection.visibility = View.GONE
        allContentRecyclerView.visibility = View.GONE
        
        // Perform search in background thread
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                when (currentFilter) {
                    "ALL" -> {
                        val songs = if (query.isBlank()) {
                            dbHelper.getAllSongs()
                        } else {
                            dbHelper.searchSongs(query)
                        }
                        val artists = dbHelper.getAllArtists(query)
                        val albums = dbHelper.getAllAlbums(query)
                        
                        withContext(Dispatchers.Main) {
                            showAllContent(query, songs, artists, albums)
                        }
                    }
                    "SONGS" -> {
                        val songs = if (query.isBlank()) {
                            dbHelper.getAllSongs()
                        } else {
                            dbHelper.searchSongs(query)
                        }
                        withContext(Dispatchers.Main) {
                            showSongsResults(songs, showSection = true)
                        }
                    }
                    "ARTISTS" -> {
                        val artists = dbHelper.getAllArtists(query)
                        withContext(Dispatchers.Main) {
                            showArtistsResults(artists, showSection = true)
                        }
                    }
                    "ALBUMS" -> {
                        val albums = dbHelper.getAllAlbums(query)
                        withContext(Dispatchers.Main) {
                            showAlbumsResults(albums, showSection = true)
                        }
                    }
                    "PLAYLISTS" -> {
                        withContext(Dispatchers.Main) {
                            showPlaylistsResults(query, showSection = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun showAllContent(query: String, songs: List<Song>, artists: List<String>, albums: List<Pair<String, String>>) {
        val items = mutableListOf<AllContentItem>()
        
        if (query.isEmpty()) {
            // Show TRENDING section with recent songs
            if (songs.isNotEmpty()) {
                items.add(AllContentItem.SectionHeader("TRENDING"))
                songs.take(5).forEach { song ->
                    items.add(AllContentItem.SongItem(song))
                }
            }
        } else {
            // Show filtered results with section headers
            if (songs.isNotEmpty()) {
                items.add(AllContentItem.SectionHeader("SONGS"))
                songs.forEach { song ->
                    items.add(AllContentItem.SongItem(song))
                }
            }
            
            if (artists.isNotEmpty()) {
                items.add(AllContentItem.SectionHeader("ARTISTS"))
                artists.forEachIndexed { index, artistName ->
                    val artist = Artist(index + 1, artistName, R.drawable.icon_recommend)
                    items.add(AllContentItem.ArtistItem(artist))
                }
            }
            
            if (albums.isNotEmpty()) {
                items.add(AllContentItem.SectionHeader("ALBUMS"))
                albums.forEachIndexed { index, albumPair ->
                    val album = Album(index + 1, albumPair.first, albumPair.second, R.drawable.icon_recommend)
                    items.add(AllContentItem.AlbumItem(album))
                }
            }
        }
        
        if (items.isNotEmpty()) {
            allContentRecyclerView.visibility = View.VISIBLE
            val adapter = AllContentAdapter(
                items = items,
                onSongClick = { song ->
                    // Navigate to MusicPlayerActivity with the selected song
                    navigateToMusicPlayer(song)
                },
                onArtistClick = { artist ->
                    // Filter by artist
                    searchInput.setText(artist.name)
                    currentFilter = "SONGS"
                    filterSongs.isSelected = true
                    filterAll.isSelected = false
                    performSearch()
                },
                onAlbumClick = { album ->
                    // Filter by album
                    searchInput.setText(album.title)
                    currentFilter = "SONGS"
                    filterSongs.isSelected = true
                    filterAll.isSelected = false
                    performSearch()
                },
                onPlaylistClick = { playlist ->
                    // Handle playlist click
                }
            )
            allContentRecyclerView.adapter = adapter
        } else {
            allContentRecyclerView.visibility = View.GONE
        }
    }
    
    private fun showSongsResults(songs: List<Song>, showSection: Boolean = true): Boolean {
        if (showSection) {
            if (songs.isNotEmpty()) {
                songsSection.visibility = View.VISIBLE
                songsPlaceholder.visibility = View.GONE
                songsRecyclerView.visibility = View.VISIBLE
                
                val adapter = PlaylistAdapter(
                    songs = songs,
                    onPlayClick = { song ->
                        // Navigate to MusicPlayerActivity with the selected song
                        navigateToMusicPlayer(song)
                    },
                    onMoreClick = { song ->
                        // Handle more options click
                    },
                    onFavoriteClick = { song, isFavorite ->
                        handleFavoriteClick(song, isFavorite)
                    },
                    isFavorite = { songId ->
                        dbHelper.isFavorite(songId)
                    }
                )
                songsRecyclerView.adapter = adapter
            } else {
                songsSection.visibility = View.VISIBLE
                songsPlaceholder.visibility = View.VISIBLE
                songsRecyclerView.visibility = View.GONE
            }
        }
        
        return songs.isNotEmpty()
    }
    
    private fun showArtistsResults(artists: List<String>, showSection: Boolean = true): Boolean {
        val artistList = artists.mapIndexed { index, name ->
            Artist(index + 1, name, R.drawable.icon_recommend)
        }
        
        if (showSection) {
            if (artistList.isNotEmpty()) {
                artistsSection.visibility = View.VISIBLE
                artistsPlaceholder.visibility = View.GONE
                artistsRecyclerView.visibility = View.VISIBLE
                
                val adapter = ArtistAdapter(
                    artists = artistList,
                    onArtistClick = { artist ->
                        // Filter by artist
                        searchInput.setText(artist.name)
                        currentFilter = "SONGS"
                        filterSongs.isSelected = true
                        filterArtists.isSelected = false
                        performSearch()
                    }
                )
                artistsRecyclerView.adapter = adapter
            } else {
                artistsSection.visibility = View.VISIBLE
                artistsPlaceholder.visibility = View.VISIBLE
                artistsRecyclerView.visibility = View.GONE
            }
        }
        
        return artistList.isNotEmpty()
    }
    
    private fun showAlbumsResults(albums: List<Pair<String, String>>, showSection: Boolean = true): Boolean {
        val albumList = albums.mapIndexed { index, pair ->
            Album(index + 1, pair.first, pair.second, R.drawable.icon_recommend)
        }
        
        if (showSection) {
            if (albumList.isNotEmpty()) {
                albumsSection.visibility = View.VISIBLE
                albumsPlaceholder.visibility = View.GONE
                albumsRecyclerView.visibility = View.VISIBLE
                
                val adapter = AlbumAdapter(
                    albums = albumList,
                    onAlbumClick = { album ->
                        // Filter by album
                        searchInput.setText(album.title)
                        currentFilter = "SONGS"
                        filterSongs.isSelected = true
                        filterAlbums.isSelected = false
                        performSearch()
                    }
                )
                albumsRecyclerView.adapter = adapter
            } else {
                albumsSection.visibility = View.VISIBLE
                albumsPlaceholder.visibility = View.VISIBLE
                albumsRecyclerView.visibility = View.GONE
            }
        }
        
        return albumList.isNotEmpty()
    }
    
    private fun showPlaylistsResults(query: String, showSection: Boolean = true): Boolean {
        // Playlists are not implemented in database yet
        val filteredPlaylists = allPlaylists.filter { 
            if (query.isEmpty()) true
            else it.name.lowercase().contains(query.lowercase())
        }
        
        if (showSection) {
            if (filteredPlaylists.isNotEmpty()) {
                playlistsSection.visibility = View.VISIBLE
                playlistsPlaceholder.visibility = View.GONE
                playlistsRecyclerView.visibility = View.VISIBLE
                
                val adapter = PlaylistSearchAdapter(
                    playlists = filteredPlaylists,
                    onPlaylistClick = { playlist ->
                        // Handle playlist click
                    }
                )
                playlistsRecyclerView.adapter = adapter
            } else {
                playlistsSection.visibility = View.VISIBLE
                playlistsPlaceholder.visibility = View.VISIBLE
                playlistsRecyclerView.visibility = View.GONE
            }
        }
        
        return filteredPlaylists.isNotEmpty()
    }
    
    /**
     * Navigate to MusicPlayerActivity with the selected song
     */
    private fun navigateToMusicPlayer(song: Song) {
        val intent = Intent(this, MusicPlayerActivity::class.java).apply {
            putExtra("song_id", song.id)
            putExtra("song_title", song.title)
            putExtra("song_artist", song.artist)
            putExtra("song_path", song.filePath)
        }
        startActivity(intent)
    }
    
    /**
     * Handle favorite button click
     */
    private fun handleFavoriteClick(song: Song, isFavorite: Boolean) {
        if (isFavorite) {
            val success = dbHelper.addToFavorite(song.id)
            if (success) {
                Toast.makeText(this, "Added to favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Already in favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        } else {
            val count = dbHelper.removeFromFavorite(song.id)
            if (count > 0) {
                Toast.makeText(this, "Removed from favorites: ${song.title}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel any pending searches
        cancelPendingSearch()
        searchJob?.cancel()
        dbHelper.close()
    }
}
