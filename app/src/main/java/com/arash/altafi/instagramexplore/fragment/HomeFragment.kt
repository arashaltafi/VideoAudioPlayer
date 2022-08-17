package com.arash.altafi.instagramexplore.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.arash.altafi.instagramexplore.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.apply {
            btnInstagram.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToInstagramFragment(
                        title = "اینستاگرام"
                    )
                )
            }

            btnMusic.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMusicFragment(
                        title = "تست موزیک 1",
                        url = "https://dls.music-fa.com/tagdl/1401/Abozar%20Roohi%20-%20Salam%20Farmande%20(320).mp3",
                        background = "https://i1.delgarm.com/i/804/0102/31/62893a8f7aeef.jpeg"
                    )
                )
            }

            btnVideo.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToVideoFragment(
                        title = "تست فیلم 1",
                        url = "https://videos1.varzeshe3.com/videos-quality/2022/06/28/B/naiqrfhh.mp4"
                    )
                )
            }
        }
    }

}