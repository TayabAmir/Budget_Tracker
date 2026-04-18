package com.example.budget_tracker

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budget_tracker.databinding.FragmentAboutBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val categoryEditTexts = mutableMapOf<String, EditText>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLimitsUI()
        updateStats()

        binding.btnUpdateLimit.setOnClickListener {
            updateAllLimits()
        }

        binding.btnDownloadPdf.setOnClickListener {
            exportToPdf()
        }

        binding.btnDownloadCsv.setOnClickListener {
            exportToCsv()
        }
    }

    private fun setupLimitsUI() {
        binding.etGlobalLimit.setText(DataManager.globalLimit.toString())
        binding.categoryLimitsContainer.removeAllViews()
        categoryEditTexts.clear()

        DataManager.categories.forEach { category ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val label = TextView(requireContext()).apply {
                text = "${category.name} ($):"
                layoutParams = LinearLayout.LayoutParams(120.dpToPx(), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val editText = EditText(requireContext()).apply {
                setText(category.limit.toString())
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            categoryEditTexts[category.name] = editText
            row.addView(label)
            row.addView(editText)
            binding.categoryLimitsContainer.addView(row)
        }
    }

    private fun updateAllLimits() {
        val newGlobalLimit = binding.etGlobalLimit.text.toString().toDoubleOrNull() ?: 0.0
        if (newGlobalLimit <= 0) {
            Toast.makeText(context, "Please enter a valid global limit", Toast.LENGTH_SHORT).show()
            return
        }

        val totalSpent = DataManager.expenses.sumOf { it.amount }
        if (newGlobalLimit < totalSpent) {
            Toast.makeText(
                context,
                "Global limit ($newGlobalLimit) cannot be less than total expenses ($totalSpent)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val newCategoryLimits = mutableMapOf<String, Double>()
        var sumCategoryLimits = 0.0

        for ((name, editText) in categoryEditTexts) {
            val limit = editText.text.toString().toDoubleOrNull() ?: 0.0
            if (limit < 0) {
                Toast.makeText(context, "Limit for $name cannot be negative", Toast.LENGTH_SHORT).show()
                return
            }

            val category = DataManager.categories.find { it.name == name }
            if (category != null && limit < category.spent) {
                Toast.makeText(
                    context,
                    "Limit for $name ($limit) cannot be less than current spent ($${category.spent})",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            newCategoryLimits[name] = limit
            sumCategoryLimits += limit
        }

        if (sumCategoryLimits > newGlobalLimit) {
            Toast.makeText(
                context,
                "Sum of category limits ($sumCategoryLimits) cannot exceed global limit ($newGlobalLimit)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Apply changes
        DataManager.globalLimit = newGlobalLimit
        newCategoryLimits.forEach { (name, limit) ->
            DataManager.categories.find { it.name == name }?.limit = limit
        }

        updateStats()
        Toast.makeText(context, "All limits updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun updateStats() {
        val totalSpent = DataManager.expenses.sumOf { it.amount }
        val limit = DataManager.globalLimit
        binding.tvTotalStats.text = "Total Spent: $${String.format("%.2f", totalSpent)}\n" +
                "Monthly Limit: $${String.format("%.2f", limit)}\n" +
                "Remaining: $${String.format("%.2f", limit - totalSpent)}"
        
        binding.spendingGraph.setData(DataManager.categories)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun exportToPdf() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 12f
        canvas.drawText("Budget Tracker - Expenses Report", 10f, 25f, paint)
        
        var y = 50f
        DataManager.expenses.forEach {
            canvas.drawText("${it.date}: ${it.title} - $${it.amount} (${it.category})", 10f, y, paint)
            y += 20f
        }
        
        pdfDocument.finishPage(page)

        val fileName = "Expenses_Report_${System.currentTimeMillis()}.pdf"
        saveFileToDownloads(fileName, "application/pdf") { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
    }

    private fun exportToCsv() {
        val csvHeader = "ID,Title,Amount,Category,Date\n"
        val csvData = DataManager.expenses.joinToString("\n") { 
            "${it.id},${it.title},${it.amount},${it.category},${it.date}"
        }
        val fullCsv = csvHeader + csvData
        
        val fileName = "Expenses_Report_${System.currentTimeMillis()}.csv"
        saveFileToDownloads(fileName, "text/csv") { outputStream ->
            outputStream.write(fullCsv.toByteArray())
        }
    }

    private fun saveFileToDownloads(fileName: String, mimeType: String, writer: (OutputStream) -> Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = requireContext().contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { writer(it) }
                    Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { writer(it) }
                Toast.makeText(context, "Saved to Downloads: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
