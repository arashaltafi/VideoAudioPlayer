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
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper

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
                getString(R.string.lorem),
                getString(R.string.lorem),
                "https://videos1.varzeshe3.com/videos-quality/2022/06/28/B/naiqrfhh.mp4",
                TypeMedia.VIDEO,
                "105",
                "15",
                "۵۹ دقیقه قبل"
            )
        )
        mediaResponse.add(
            MediaResponse(
                "پخش زنده منوتو",
                getString(R.string.lorem),
                "https://edge-cdn1.manoto.click/live_500.m3u8",
                TypeMedia.VIDEO,
                "105",
                "15",
                "۵۹ دقیقه قبل"
            )
        )
        mediaResponse.add(
            MediaResponse(
                "موزیک سلام فرمانده",
                getString(R.string.lorem),
                "https://dls.music-fa.com/tagdl/1401/Abozar%20Roohi%20-%20Salam%20Farmande%20(320).mp3",
                TypeMedia.MUSIC,
                "105",
                "15",
                "۵۹ دقیقه قبل",
                "https://i1.delgarm.com/i/804/0102/31/62893a8f7aeef.jpeg"
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.lorem),
                getString(R.string.lorem),
                "https://i1.delgarm.com/i/804/0102/31/62893a8f7aeef.jpeg",
                TypeMedia.IMAGE,
                "200",
                "54",
                "۴۵ دقیقه قبل"
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.lorem),
                getString(R.string.lorem),
                "https://cld7.hostdl.net/play/movie/2022/07/The_Witch_Part_2_The_Other_One_2022_Trailer.mkv",
                TypeMedia.VIDEO,
                "523",
                "32",
                "۳۲ دقیقه قبل"
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.lorem),
                getString(R.string.lorem),
                "https://cld7.hostdl.net/play/movie/2022/07/Jurassic_World_Dominion_2022_Trailer.mkv",
                TypeMedia.VIDEO,
                "523",
                "32",
                "۳۲ دقیقه قبل"
            )
        )
        mediaResponse.add(
            MediaResponse(
                getString(R.string.lorem),
                getString(R.string.lorem),
                "https://filesamples.com/samples/video/mp4/sample_960x540.mp4",
                TypeMedia.VIDEO,
                "897",
                "96",
                "۲۰ دقیقه قبل"
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
        binding?.rvMedia?.changePlayingState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.rvMedia?.changePlayingState(false)
    }

    override fun onPause() {
        super.onPause()
        binding?.rvMedia?.changePlayingState(false)
    }

    override fun onResume() {
        super.onResume()
        binding?.rvMedia?.changePlayingState(true)
    }


}