package com.example.wellnessapp.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.wellnessapp.databinding.FragmentMoodChartBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class MoodChartFragment : Fragment() {

    private var _binding: FragmentMoodChartBinding? = null
    private val binding get() = _binding!!
    private val vm: MoodViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoodChartBinding.inflate(inflater, container, false)

        setupBarChart()
        setupLineChart()

        vm.todaySlotScores.observe(viewLifecycleOwner) { list ->
            updateBarChart(list)
        }

        vm.weeklyLabels.observe(viewLifecycleOwner) { labels ->
            binding.lineChartWeek.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            // also set x-axis labels for bar if needed
            binding.barChartToday.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Morning", "Afternoon", "Evening", "Night"))
        }

        vm.weeklyValues.observe(viewLifecycleOwner) { values ->
            updateLineChart(values)
        }

        return binding.root
    }

    private fun setupBarChart() {
        val chart = binding.barChartToday
        chart.description.isEnabled = false
        chart.setFitBars(true)
        chart.axisRight.isEnabled = false
        val x = chart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.granularity = 1f
        x.setDrawGridLines(false)
        x.valueFormatter = IndexAxisValueFormatter(listOf("Morning", "Afternoon", "Evening", "Night"))
    }

    private fun updateBarChart(values: List<Float>) {
        val entries = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val set = BarDataSet(entries, "Mood score (1-5)").apply {
            setColors(*ColorTemplate.MATERIAL_COLORS)
            valueTextSize = 12f
        }
        val data = BarData(set)
        data.barWidth = 0.6f
        val chart = binding.barChartToday
        chart.data = data
        chart.invalidate()
    }

    private fun setupLineChart() {
        val chart = binding.lineChartWeek
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        val x = chart.xAxis
        x.position = XAxis.XAxisPosition.BOTTOM
        x.granularity = 1f
        x.setDrawGridLines(false)
    }

    private fun updateLineChart(values: List<Float>) {
        val entries = values.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val set = LineDataSet(entries, "Avg mood (1-5)").apply {
            setDrawCircles(true)
            lineWidth = 2f
            valueTextSize = 12f
            setColors(*ColorTemplate.MATERIAL_COLORS)
        }
        val data = LineData(set)
        val chart = binding.lineChartWeek
        chart.data = data
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
