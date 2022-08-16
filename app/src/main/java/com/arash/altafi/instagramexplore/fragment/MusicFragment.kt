package com.arash.altafi.instagramexplore.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arash.altafi.instagramexplore.databinding.FragmentMusicBinding

class MusicFragment : Fragment() {

    private lateinit var binding: FragmentMusicBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {

    }

}