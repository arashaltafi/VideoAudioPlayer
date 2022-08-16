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
                    HomeFragmentDirections.actionHomeFragmentToInstagramFragment()
                )
            }

            btnMusic.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMusicFragment()
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