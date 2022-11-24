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
                        title = "Instagram"
                    )
                )
            }

            btnMusic.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMusicFragment(
                        title = "Test Music",
                        url = "https://irsv.upmusics.com/Downloads/Musics/Aron%20Afshar%20-%20Tabibe%20Maher%20(128).mp3",
                        background = "https://upmusics.com/wp-content/uploads/2019/03/giuklj.jpg"
                    )
                )
            }

            btnVideo.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToVideoFragment(
                        title = "Test Video",
                        url = "https://upmusics.com/wp-content/uploads/2021/01/143971813_1338518893210690_8280265321842515046_n.mp4"
                    )
                )
            }
        }
    }

}