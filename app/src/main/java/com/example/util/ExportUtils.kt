package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Project
import com.example.data.model.RabCategory
import com.example.data.model.RabItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    fun exportRabPdf(
        context: Context,
        project: Project,
        categories: List<RabCategory>,
        itemsMap: Map<Long, List<RabItem>>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint()
            val boldPaint = Paint()

            // Header Background Accent
            paint.color = Color.parseColor("#0F172A") // Navy
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, paint)

            // Header Text
            titlePaint.color = Color.WHITE
            titlePaint.textSize = 18f
            titlePaint.isFakeBoldText = true
            canvas.drawText("LAPORAN RENCANA ANGGARAN BIAYA (RAB)", 20f, 38f, titlePaint)

            titlePaint.textSize = 10f
            titlePaint.isFakeBoldText = false
            canvas.drawText("Dihasilkan oleh Quantity Surveyor (QS) Estimator", 20f, 55f, titlePaint)

            // Project Info Section
            var y = 95f
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("PROYEK: ${project.title}", 20f, y, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            y += 16f
            canvas.drawText("Pemilik / Klien: ${project.clientName} | Wilayah: ${project.locationRegion} | Tipe: ${project.projectType}", 20f, y, paint)
            y += 14f
            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
            canvas.drawText("Tanggal Laporan: $dateStr | Tim Pekerja: ${project.targetWorkerCount} Orang", 20f, y, paint)

            // Table Header
            y += 25f
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(20f, y - 12f, pageWidth - 20f, y + 12f, paint)

            boldPaint.color = Color.parseColor("#0F172A")
            boldPaint.textSize = 10f
            boldPaint.isFakeBoldText = true

            canvas.drawText("No / Uraian Pekerjaan", 25f, y + 3f, boldPaint)
            canvas.drawText("Vol", 320f, y + 3f, boldPaint)
            canvas.drawText("Sat", 365f, y + 3f, boldPaint)
            canvas.drawText("Harga Satuan (Rp)", 410f, y + 3f, boldPaint)
            canvas.drawText("Total (Rp)", 500f, y + 3f, boldPaint)

            y += 20f

            var grandSubtotal = 0.0
            val linePaint = Paint()
            linePaint.color = Color.parseColor("#CBD5E1")
            linePaint.strokeWidth = 0.5f

            val textPaint = Paint()
            textPaint.textSize = 9.5f
            textPaint.color = Color.parseColor("#334155")

            var itemCounter = 1

            for (category in categories) {
                val categoryItems = itemsMap[category.id] ?: emptyList()
                if (categoryItems.isEmpty()) continue

                // Category Row Header
                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawRect(20f, y - 10f, pageWidth - 20f, y + 8f, paint)

                boldPaint.textSize = 9.5f
                canvas.drawText(category.categoryName.uppercase(), 25f, y, boldPaint)
                y += 16f

                for (item in categoryItems) {
                    val total = item.totalPrice
                    grandSubtotal += total

                    canvas.drawText("$itemCounter. ${item.itemName}", 30f, y, textPaint)
                    canvas.drawText(String.format(Locale.US, "%.1f", item.volume), 320f, y, textPaint)
                    canvas.drawText(item.unit, 365f, y, textPaint)
                    canvas.drawText(formatRupiah(item.unitPrice).replace("Rp ", ""), 410f, y, textPaint)
                    canvas.drawText(formatRupiah(total).replace("Rp ", ""), 500f, y, textPaint)

                    y += 14f
                    canvas.drawLine(20f, y - 4f, pageWidth - 20f, y - 4f, linePaint)
                    itemCounter++

                    if (y > pageHeight - 120) {
                        // Keep simple A4 single/multi-page or draw overflow safely
                        break
                    }
                }
            }

            // Calculation Footer
            y += 10f
            val ppnAmount = grandSubtotal * (project.ppnTaxPercent / 100.0)
            val overheadAmount = grandSubtotal * (project.overheadPercent / 100.0)
            val grandTotal = grandSubtotal + ppnAmount + overheadAmount

            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRect(280f, y, pageWidth - 20f, y + 65f, paint)

            boldPaint.textSize = 9.5f
            canvas.drawText("Subtotal Pekerjaan:", 290f, y + 15f, textPaint)
            canvas.drawText(formatRupiah(grandSubtotal), 480f, y + 15f, textPaint)

            canvas.drawText("PPN (${project.ppnTaxPercent.toInt()}%):", 290f, y + 30f, textPaint)
            canvas.drawText(formatRupiah(ppnAmount), 480f, y + 30f, textPaint)

            canvas.drawText("Keuntungan & Overhead (${project.overheadPercent.toInt()}%):", 290f, y + 45f, textPaint)
            canvas.drawText(formatRupiah(overheadAmount), 480f, y + 45f, textPaint)

            boldPaint.textSize = 10.5f
            canvas.drawText("GRAND TOTAL RAB:", 290f, y + 60f, boldPaint)
            canvas.drawText(formatRupiah(grandTotal), 480f, y + 60f, boldPaint)

            // Signatures block
            y += 85f
            if (y < pageHeight - 60) {
                textPaint.textSize = 9f
                canvas.drawText("Disetujui Oleh (Owner/Klien)", 50f, y, textPaint)
                canvas.drawText("Disusun Oleh (Quantity Surveyor)", 380f, y, textPaint)

                y += 40f
                canvas.drawText("( ______________________ )", 50f, y, textPaint)
                canvas.drawText("( ______________________ )", 380f, y, textPaint)
            }

            pdfDocument.finishPage(page)

            // Save PDF File
            val fileName = "RAB_${project.title.replace("\\s+".toRegex(), "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            // Open or Share PDF
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan PDF RAB"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportRabCsv(
        context: Context,
        project: Project,
        categories: List<RabCategory>,
        itemsMap: Map<Long, List<RabItem>>
    ) {
        try {
            val sb = StringBuilder()
            sb.append("NO;KATEGORI PEKERJAAN;URAIAN PEKERJAAN;VOLUME;SATUAN;HARGA SATUAN (RP);TOTAL HARGA (RP)\n")

            var totalGrand = 0.0
            var no = 1

            for (cat in categories) {
                val catItems = itemsMap[cat.id] ?: emptyList()
                for (item in catItems) {
                    val total = item.totalPrice
                    totalGrand += total
                    sb.append("$no;\"${cat.categoryName}\";\"${item.itemName}\";${item.volume};\"${item.unit}\";${item.unitPrice.toLong()};${total.toLong()}\n")
                    no++
                }
            }

            val ppn = totalGrand * (project.ppnTaxPercent / 100.0)
            val overhead = totalGrand * (project.overheadPercent / 100.0)
            val finalTotal = totalGrand + ppn + overhead

            sb.append("\n;;;SUBTOTAL BIAYA FISIK;;;${totalGrand.toLong()}\n")
            sb.append(";;;PPN (${project.ppnTaxPercent}%);;;${ppn.toLong()}\n")
            sb.append(";;;KEUNTUNGAN & OVERHEAD (${project.overheadPercent}%);;;${overhead.toLong()}\n")
            sb.append(";;;GRAND TOTAL RAB;;;${finalTotal.toLong()}\n")

            val fileName = "RAB_${project.title.replace("\\s+".toRegex(), "_")}_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            file.writeText(sb.toString())

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Ekspor File Excel / CSV RAB"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor Excel CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
