package com.astrolog.app.util

import android.content.Context
import android.net.Uri
import com.astrolog.app.data.entity.AstroObject
import com.astrolog.app.data.entity.Session
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

object ExcelManager {

    // ─────────────────────────────────────────────
    // IMPORTAR desde el Excel de control
    // ─────────────────────────────────────────────
    fun importSessions(context: Context, uri: Uri): List<Session> {
        val sessions = mutableListOf<Session>()
        val stream: InputStream = context.contentResolver.openInputStream(uri) ?: return sessions

        try {
            val workbook: Workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheet("Registro de sesiones") ?: workbook.getSheetAt(0)

            // Datos empiezan en fila 7 (índice 6, cabecera en filas 4-5)
            for (rowIndex in 6..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val num = row.getCell(0)?.numericCellValue?.toInt() ?: continue
                if (num <= 0) continue

                val date = getCellString(row.getCell(1))
                val objName = getCellString(row.getCell(2))
                if (objName.isBlank()) continue

                val visibility = getCellString(row.getCell(3))
                val conditions = getCellString(row.getCell(4))
                val seeing = row.getCell(5)?.numericCellValue?.toInt() ?: 3

                val lproSubs = row.getCell(6)?.numericCellValue?.toInt() ?: 0
                val lproExp = row.getCell(7)?.numericCellValue?.toInt() ?: 0

                val haSubs = row.getCell(9)?.numericCellValue?.toInt() ?: 0
                val haExp = row.getCell(10)?.numericCellValue?.toInt() ?: 0

                val oiiiSubs = row.getCell(12)?.numericCellValue?.toInt() ?: 0
                val oiiiExp = row.getCell(13)?.numericCellValue?.toInt() ?: 0

                val notes = getCellString(row.getCell(16))

                sessions.add(
                    Session(
                        sessionNumber = num,
                        date = date,
                        objectName = objName,
                        visibility = visibility,
                        conditions = conditions,
                        seeing = seeing.coerceIn(1, 5),
                        lproSubs = lproSubs,
                        lproExpSec = lproExp,
                        haSubs = haSubs,
                        haExpSec = haExp,
                        oiiiSubs = oiiiSubs,
                        oiiiExpSec = oiiiExp,
                        notes = notes
                    )
                )
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream.close()
        }
        return sessions
    }

    fun importObjects(context: Context, uri: Uri): List<AstroObject> {
        val objects = mutableListOf<AstroObject>()
        val stream: InputStream = context.contentResolver.openInputStream(uri) ?: return objects

        try {
            val workbook: Workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheet("Calendario visibilidad") ?: return objects

            // Datos desde fila 4 (índice 3), cabecera en fila 3
            for (rowIndex in 3..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val name = getCellString(row.getCell(0))
                if (name.isBlank()) continue

                objects.add(
                    AstroObject(
                        name = name,
                        visibilityMarch = getCellString(row.getCell(1)).take(1).ifEmpty { "—" },
                        visibilityApril = getCellString(row.getCell(2)).take(1).ifEmpty { "—" },
                        visibilityMay = getCellString(row.getCell(3)).take(1).ifEmpty { "—" },
                        visibilityJune = getCellString(row.getCell(4)).take(1).ifEmpty { "—" },
                        mainFilter = getCellString(row.getCell(5))
                    )
                )
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream.close()
        }
        return objects
    }

    // ─────────────────────────────────────────────
    // EXPORTAR a Excel
    // ─────────────────────────────────────────────
    fun exportToExcel(sessions: List<Session>, objects: List<AstroObject>, outputStream: OutputStream) {
        val workbook = XSSFWorkbook()

        // ── Hoja 1: Registro de sesiones ──
        val sheet1 = workbook.createSheet("Registro de sesiones")

        // Cabecera de metadatos
        sheet1.createRow(0).createCell(0).setCellValue(
            "CONTROL DE EXPOSICIÓN · AstroLog · Refractor 80/380 f4.8 · ZWO ASI 533MC · Bortle 4"
        )
        sheet1.createRow(1).createCell(0).setCellValue(
            "Filtros: L-Pro · Askar C1 (Hα) · Askar C2 (OIII)  ·  Campo 102′×102′ · 2.04″/px"
        )

        // Cabecera de columnas (fila 4)
        val header = sheet1.createRow(3)
        listOf("Nº","Fecha","Objeto","Visibilidad","Condiciones","Seeing",
            "L-Pro Subs","L-Pro Exp(s)","L-Pro HH:MM",
            "Hα Subs","Hα Exp(s)","Hα HH:MM",
            "OIII Subs","OIII Exp(s)","OIII HH:MM",
            "Total sesión","Notas")
            .forEachIndexed { i, title -> header.createCell(i).setCellValue(title) }

        // Datos desde fila 5 (índice 4 → Excel fila 6 para mantener la misma estructura)
        sessions.forEachIndexed { idx, s ->
            val row = sheet1.createRow(5 + idx)
            row.createCell(0).setCellValue(s.sessionNumber.toDouble())
            row.createCell(1).setCellValue(s.date)
            row.createCell(2).setCellValue(s.objectName)
            row.createCell(3).setCellValue(s.visibility)
            row.createCell(4).setCellValue(s.conditions)
            row.createCell(5).setCellValue(s.seeing.toDouble())
            row.createCell(6).setCellValue(s.lproSubs.toDouble())
            row.createCell(7).setCellValue(s.lproExpSec.toDouble())
            row.createCell(8).setCellValue(s.lproTime)
            row.createCell(9).setCellValue(s.haSubs.toDouble())
            row.createCell(10).setCellValue(s.haExpSec.toDouble())
            row.createCell(11).setCellValue(s.haTime)
            row.createCell(12).setCellValue(s.oiiiSubs.toDouble())
            row.createCell(13).setCellValue(s.oiiiExpSec.toDouble())
            row.createCell(14).setCellValue(s.oiiiTime)
            row.createCell(15).setCellValue(s.totalTime)
            row.createCell(16).setCellValue(s.notes)
        }

        // ── Hoja 2: Calendario visibilidad ──
        val sheet2 = workbook.createSheet("Calendario visibilidad")
        sheet2.createRow(0).createCell(0).setCellValue(
            "CALENDARIO DE VISIBILIDAD — Cataluña 41.5°N"
        )
        val calHeader = sheet2.createRow(2)
        listOf("Objeto","Marzo","Abril","Mayo","Junio","Filtro principal")
            .forEachIndexed { i, t -> calHeader.createCell(i).setCellValue(t) }
        objects.forEachIndexed { idx, o ->
            val row = sheet2.createRow(3 + idx)
            row.createCell(0).setCellValue(o.name)
            row.createCell(1).setCellValue(o.visibilityMarch)
            row.createCell(2).setCellValue(o.visibilityApril)
            row.createCell(3).setCellValue(o.visibilityMay)
            row.createCell(4).setCellValue(o.visibilityJune)
            row.createCell(5).setCellValue(o.mainFilter)
        }

        workbook.write(outputStream)
        workbook.close()
    }

    // ── Exportar CSV ──
    fun exportToCsv(sessions: List<Session>, outputStream: OutputStream) {
        val sb = StringBuilder()
        sb.appendLine("Nº,Fecha,Objeto,Condiciones,Seeing,LPro_Subs,LPro_ExpS,LPro_HH:MM,Ha_Subs,Ha_ExpS,Ha_HH:MM,OIII_Subs,OIII_ExpS,OIII_HH:MM,Total_HH:MM,Notas")
        sessions.forEach { s ->
            sb.appendLine("${s.sessionNumber},${s.date},\"${s.objectName}\",${s.conditions},${s.seeing}," +
                    "${s.lproSubs},${s.lproExpSec},${s.lproTime}," +
                    "${s.haSubs},${s.haExpSec},${s.haTime}," +
                    "${s.oiiiSubs},${s.oiiiExpSec},${s.oiiiTime}," +
                    "${s.totalTime},\"${s.notes}\"")
        }
        outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
    }

    // Helper
    private fun getCellString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val d = cell.numericCellValue
                if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try { cell.stringCellValue.trim() }
                catch (e: Exception) {
                    try { cell.numericCellValue.toLong().toString() }
                    catch (e2: Exception) { "" }
                }
            }
            else -> ""
        }
    }
}
