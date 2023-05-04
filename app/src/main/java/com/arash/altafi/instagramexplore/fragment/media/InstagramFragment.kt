package com.arash.altafi.instagramexplore.fragment.media

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arash.altafi.instagramexplore.R
import com.arash.altafi.instagramexplore.databinding.FragmentInstagramBinding
import com.arash.altafi.instagramexplore.fragment.media.adapter.VideoAdapter
import com.arash.altafi.instagramexplore.widget.CustomToolbar
import com.arash.altafi.instagramexplore.widget.gravitySnapHelper.GravitySnapHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class InstagramFragment : Fragment() {

    private lateinit var binding: FragmentInstagramBinding
    private val mediaPlayerAdapter: VideoAdapter = VideoAdapter()
    private var toolbarView: CustomToolbar? = null
    private var mediaResponse: ArrayList<MediaResponse> = arrayListOf()
    private val args by navArgs<InstagramFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInstagramBinding.inflate(inflater, container, false)
        setupToolbar(args.title)
        init()
        return binding.root
    }

    private fun setupToolbar(title: String) {
        toolbarView = binding.toolbar
        toolbarView?.initToolbar(title = title)
        toolbarView?.onBackClickToolbar = {
            findNavController().navigateUp()
        }
    }

    private fun init() {
        binding.apply {
            srMediaPlayer.setOnRefreshListener {
                mediaPlayerAdapter.setData(emptyList())
                mediaPlayerAdapter.setData(mediaResponse)
                srMediaPlayer.apply {
                    if (isRefreshing)
                        isRefreshing = false
                }
            }
        }

        //init adapter list
        mediaResponse.add(
            MediaResponse(
                getString(R.string.video_title),
                getString(R.string.lorem),
                "https://upmusics.com/wp-content/uploads/2021/01/143971813_1338518893210690_8280265321842515046_n.mp4",
                TypeMedia.VIDEO,
                "946",
                "15",
                getString(R.string.minute_35),
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.music_title),
                getString(R.string.lorem),
                "https://irsv.upmusics.com/Downloads/Musics/Aron%20Afshar%20-%20Tabibe%20Maher%20(128).mp3",
                TypeMedia.MUSIC,
                "105",
                "15",
                getString(R.string.minute_20),
                "https://upmusics.com/wp-content/uploads/2019/03/giuklj.jpg"
            )
        )
        /*mediaResponse.add(
            MediaResponse(
                getString(R.string.image_title),
                getString(R.string.lorem),
                "http://arashaltafi.ir/arash.jpg",
                TypeMedia.IMAGE,
                "200",
                "54",
                getString(R.string.minute_20),
            )
        )*/
        mediaResponse.add(
            MediaResponse(
                getString(R.string.video_title),
                getString(R.string.lorem),
                "https://cld7.hostdl.net/play/movie/2022/07/The_Witch_Part_2_The_Other_One_2022_Trailer.mkv",
                TypeMedia.VIDEO,
                "895",
                "32",
                getString(R.string.hour_2),
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.video_title),
                getString(R.string.lorem),
                "https://cld7.hostdl.net/play/movie/2022/07/Jurassic_World_Dominion_2022_Trailer.mkv",
                TypeMedia.VIDEO,
                "523",
                "32",
                getString(R.string.hour_5),
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.video_title),
                getString(R.string.lorem),
                "https://filesamples.com/samples/video/mp4/sample_960x540.mp4",
                TypeMedia.VIDEO,
                "897",
                "96",
                getString(R.string.minute_59),
            )
        )
        mediaPlayerAdapter.setData(mediaResponse)

        binding.apply {
            rvMedia.adapter = mediaPlayerAdapter
            val snapHelper = GravitySnapHelper(Gravity.TOP)
//            val snapHelper: SnapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(rvMedia)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMedia.changePlayingState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvMedia.changePlayingState(false)
    }

    override fun onPause() {
        super.onPause()
        binding.rvMedia.changePlayingState(false)
    }

    override fun onResume() {
        super.onResume()
        binding.rvMedia.changePlayingState(true)
    }


}