package com.avela.android.mpa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avela.android.mpa.R
import com.avela.android.mpa.data.Album
import com.avela.android.mpa.databinding.ListItemBrowseLibraryBinding
import com.bumptech.glide.Glide
import timber.log.Timber

class BrowseLibraryAdapter(private val itemClickListener: (Album.AlbumItemData) -> Unit) :
    ListAdapter<Album.AlbumItemData, BrowseLibraryAdapter.ViewHolder>(
        BrowseLibraryDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBrowseLibraryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ListItemBrowseLibraryBinding,
        private val itemClickListener: (Album.AlbumItemData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
        }

        private fun navigateToAlbum(
            item: String,
            view: View
        ) {
//            val direction = MusicListFragmentDirections.actionMusicListPagerFragmentToNavigationBrowseDetail(
//                item.tag,
//                item.name
//            )
//            view.findNavController().navigate(direction)
            Timber.d("")

        }

        fun bind(album: Album.AlbumItemData) {
            with(binding) {
                root.setOnClickListener {
                    itemClickListener(album)
                }
                this.albumName.text = album.name

                when {
                    album.coverUri != null -> {
                        Glide.with(this.albumImage)
                            .load(album.coverUri)
                            .placeholder(R.drawable.ic_music_art)
                            .fallback(R.drawable.ic_music_art)
                            .into(this.albumImage)
                    }
                    album.embeddedArt != null -> {
                        Glide.with(this.albumImage)
                            .load(album.embeddedArt)
                            .placeholder(R.drawable.ic_music_art)
                            .fallback(R.drawable.ic_music_art)
                            .into(this.albumImage)
                    }
                    else -> {
                        Glide.with(this.albumImage)
                            .load(R.drawable.ic_music_art)
                            .into(this.albumImage)
                    }
                }
            }
        }
    }
}

private class BrowseLibraryDiffCallback : DiffUtil.ItemCallback<Album.AlbumItemData>() {
    override fun areItemsTheSame(oldItem: Album.AlbumItemData, newItem: Album.AlbumItemData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Album.AlbumItemData, newItem: Album.AlbumItemData): Boolean {
        return oldItem == newItem
    }

}