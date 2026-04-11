package com.cs173.myapplication.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs173.myapplication.AppData
import com.cs173.myapplication.R
import com.cs173.myapplication.databinding.FragmentReportsBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateMonthDisplay()

        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
        }

        binding.btnExportCsv.setOnClickListener { exportToCSV() }
        binding.btnExportPdf.setOnClickListener { exportToPDF() }
    }

    private fun updateMonthDisplay() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = sdf.format(calendar.time)
        generateReport()
    }

    private fun generateReport() {
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val monthlyExpenses = AppData.expenses.filter {
            val date = sdf.parse(it.date)
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }

        val totalSpent = monthlyExpenses.sumOf { it.amount }
        binding.tvReportTotal.text = "Rs. $totalSpent"

        val biggest = monthlyExpenses.maxByOrNull { it.amount }?.amount ?: 0.0
        binding.tvReportBiggest.text = "Rs. $biggest"

        val subsTotal = AppData.subscriptions.filter { it.isActive }.sumOf { it.amount }
        binding.tvReportSubs.text = "Rs. $subsTotal"

        val categoryGroups = monthlyExpenses.groupBy { it.categoryId }
        val topCatId = categoryGroups.maxByOrNull { it.value.sumOf { e -> e.amount } }?.key
        val topCatName = AppData.categories.find { it.id == topCatId }?.name ?: "None"
        binding.tvReportTop_cat.text = topCatName

        // Category Breakdown List
        binding.llCategoryBreakdown.removeAllViews()
        for (category in AppData.categories) {
            val spent = monthlyExpenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            if (spent > 0) {
                val percent = (spent / totalSpent * 100).toInt()
                val row = LayoutInflater.from(context).inflate(R.layout.item_category_report, binding.llCategoryBreakdown, false)
                row.findViewById<TextView>(R.id.tv_cat_name).text = category.name
                row.findViewById<TextView>(R.id.tv_cat_amount).text = "Rs. $spent ($percent%)"
                
                val barContainer = row.findViewById<LinearLayout>(R.id.ll_bar_container)
                val bar = View(context)
                bar.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, spent.toFloat())
                bar.setBackgroundColor(Color.parseColor(category.colorHex))
                barContainer.addView(bar)
                
                val empty = View(context)
                empty.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (totalSpent - spent).toFloat())
                barContainer.addView(empty)

                binding.llCategoryBreakdown.addView(row)
            }
        }
    }

    private fun exportToCSV() {
        try {
            val csvString = StringBuilder()
            csvString.append("ID,Title,Amount,Date,Category,Recurring\n")
            for (e in AppData.expenses) {
                val cat = AppData.categories.find { it.id == e.categoryId }?.name ?: ""
                csvString.append("${e.id},${e.title},${e.amount},${e.date},$cat,${e.isRecurring}\n")
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "expenses.csv")
            FileOutputStream(file).use { it.write(csvString.toString().toByteArray()) }
            Toast.makeText(context, "Exported to Downloads/expenses.csv", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToPDF() {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint()
            
            paint.textSize = 12f
            canvas.drawText("SmartBudget Expense Report", 10f, 25f, paint)
            
            var y = 50f
            paint.textSize = 8f
            for (e in AppData.expenses) {
                canvas.drawText("${e.date} - ${e.title}: Rs. ${e.amount}", 10f, y, paint)
                y += 15f
                if (y > 580) break // Simple page limit
            }
            
            pdfDocument.finishPage(page)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "expenses.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Toast.makeText(context, "PDF saved to Downloads!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF export failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
