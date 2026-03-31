package com.civicguard.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.civicguard.data.remote.dto.ComplaintResponse
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generateComplaintReport(context: Context, complaint: ComplaintResponse): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 16f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        canvas.drawText("CivicEye Official Report", 200f, 50f, titlePaint)
        canvas.drawText("Ticket Number: ${complaint.ticketNumber}", 50f, 100f, textPaint)
        canvas.drawText("Category: ${complaint.category}", 50f, 130f, textPaint)
        canvas.drawText("Title: ${complaint.title}", 50f, 160f, textPaint)
        canvas.drawText("Description: ${complaint.description}", 50f, 190f, textPaint)
        canvas.drawText("Status: ${complaint.status}", 50f, 220f, textPaint)
        canvas.drawText("Pincode: ${complaint.pincode}", 50f, 250f, textPaint)
        canvas.drawText("Location: ${complaint.latitude}, ${complaint.longitude}", 50f, 280f, textPaint)
        
        if (complaint.authentic) {
            paint.color = Color.GREEN
            canvas.drawText("AI VERIFIED AUTHENTIC", 50f, 310f, paint.apply { textSize = 18f; isFakeBoldText = true })
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Report_${complaint.ticketNumber}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }
}
