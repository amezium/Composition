package com.example.composition.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.*
import com.example.composition.R
import com.example.composition.databinding.FragmentGameBinding
import com.example.composition.domain.entity.GameResult
import com.example.composition.domain.entity.Level


class GameFragment : Fragment() {

    private lateinit var level: Level
    private val viewModel: GameViewModel by lazy {
        ViewModelProvider(this, AndroidViewModelFactory
            .getInstance(requireActivity().application))[GameViewModel::class.java]
    }

    private var _binding: FragmentGameBinding? = null
    private val binding: FragmentGameBinding
    get() = _binding ?: throw RuntimeException("FragmentGameBinding = null")

    private val tvOptions by lazy {
        mutableListOf<TextView>().apply {
            add(binding.tvOption1)
            add(binding.tvOption2)
            add(binding.tvOption3)
            add(binding.tvOption4)
            add(binding.tvOption5)
            add(binding.tvOption6)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parsArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setClickListenersToOptions()
        viewModel.startGame(level)
    }

    private fun setClickListenersToOptions(){
        for (tvOptions in tvOptions) {
            tvOptions.setOnClickListener {
                viewModel.chooseAnswer(tvOptions.text.toString().toInt())
            }
        }
    }


    private fun observeViewModel(){
        with(binding){
            viewModel.question.observe(viewLifecycleOwner){
                tvSum.text = it.sum.toString()
                tvLeftNumber.text = it.visibleNumber.toString()
                for (i in 0 until tvOptions.size) {
                    tvOptions[i].text = it.options[i].toString()
                }

            }
            viewModel.percentOfRightsAnswer.observe(viewLifecycleOwner){
                progressBar.setProgress(it, true)
            }
            viewModel.enoughCount.observe(viewLifecycleOwner){
                tvAnswersProgress.setTextColor(getColorByState(it))
            }
            viewModel.enoughPercent.observe(viewLifecycleOwner){
                val color = getColorByState(it)
                progressBar.progressTintList = ColorStateList.valueOf(color)
            }
            viewModel.formattedTime.observe(viewLifecycleOwner){
                tvTimer.text = it
            }
            viewModel.minPercent.observe(viewLifecycleOwner){
                progressBar.secondaryProgress = it
            }
            viewModel.gameResult.observe(viewLifecycleOwner){
                launchGameFinishedFragment(it)
            }
            viewModel.progressAnswer.observe(viewLifecycleOwner){
                tvAnswersProgress.text = it
            }
        }
    }

    private fun getColorByState(goodState: Boolean): Int{
        val colorResId = if(goodState){
            android.R.color.holo_green_dark
        }else android.R.color.holo_red_dark
        return ContextCompat.getColor(requireContext(), colorResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun launchGameFinishedFragment(result: GameResult){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, GameFinishedFragment.newInstance(result))
            .addToBackStack(null).commit()
    }

    private fun parsArgs(){
       requireArguments().getParcelable<Level>(KEY_LEVEL)?.let {
           level = it
       }
    }

    companion object{

        const val NAME = "GameFragment"
        const val KEY_LEVEL = "level"

        fun newInstance(level: Level): GameFragment{
            return GameFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_LEVEL, level)
                }
            }
        }
    }
}