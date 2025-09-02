package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.ConsultaPaso2Item
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALPaso2 {

    // ✅ CONSULTAR REGISTROS PASO2 POR FECHA
    suspend fun consultarPaso2PorFecha(fecha: String): List<ConsultaPaso2Item> = withContext(Dispatchers.IO) {
        val registros = mutableListOf<ConsultaPaso2Item>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALVehiculo_ConsultaPaso2", "🔍 Consultando registros Paso2 para fecha: $fecha")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo_ConsultaPaso2", "❌ No se pudo obtener conexión")
                return@withContext registros
            }

            val query = """
                SELECT DISTINCT
                    v.IdVehiculo, 
                    p.IdPaso2LogVehiculo, 
                    v.Vin, 
                    ISNULL(b.BL, '') as BL, 
                    v.IdMarca, 
                    m.Nombre as Marca, 
                    v.IdModelo, 
                    mo.Nombre as Modelo, 
                    ISNULL(v.Annio, 0) as Anio,
                    vc.IdColor, 
                    vc.IdColorInterior, 
                    ISNULL(c.Nombre, '') as ColorExterior, 
                    ISNULL(c1.Nombre, '') as ColorInterior, 
                    ISNULL(v.Motor, '') as NumeroMotor,
                    CONVERT(varchar, p.Fechaaltafoto1, 120) as FechaAltaFoto1,
                    CONVERT(varchar, p.Fechaaltafoto2, 120) as FechaAltaFoto2,
                    CONVERT(varchar, p.Fechaaltafoto3, 120) as FechaAltaFoto3,
                    CONVERT(varchar, p.Fechaaltafoto4, 120) as FechaAltaFoto4
                FROM dbo.Paso2LogVehiculo p 
                INNER JOIN dbo.vehiculo v ON p.IdVehiculo = v.IdVehiculo 
                INNER JOIN dbo.MarcaAuto m ON v.IdMarca = m.IdMarcaAuto
                INNER JOIN dbo.Modelo mo ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
                LEFT JOIN dbo.VehiculoColor vc ON v.IdVehiculo = vc.IdVehiculo
                LEFT JOIN dbo.Color c ON vc.IdColor = c.IdColor
                LEFT JOIN dbo.Color c1 ON vc.IdColorInterior = c1.IdColor
                LEFT JOIN dbo.bl b ON v.IdBL = b.IdBL
                WHERE (CONVERT(date, p.Fechaaltafoto1) = ? OR p.Fechaaltafoto1 IS NULL)
                   OR (CONVERT(date, p.Fechaaltafoto2) = ? OR p.Fechaaltafoto2 IS NULL)
                   OR (CONVERT(date, p.Fechaaltafoto3) = ? OR p.Fechaaltafoto3 IS NULL)
                   OR (CONVERT(date, p.Fechaaltafoto4) = ? OR p.Fechaaltafoto4 IS NULL)
                ORDER BY v.Vin
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            statement.setString(2, fecha)
            statement.setString(3, fecha)
            statement.setString(4, fecha)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val registro = ConsultaPaso2Item(
                    IdVehiculo = resultSet.getInt("IdVehiculo"),
                    IdPaso2LogVehiculo = resultSet.getInt("IdPaso2LogVehiculo"),
                    VIN = resultSet.getString("Vin") ?: "",
                    BL = resultSet.getString("BL") ?: "",
                    IdMarca = resultSet.getInt("IdMarca"),
                    Marca = resultSet.getString("Marca") ?: "",
                    IdModelo = resultSet.getInt("IdModelo"),
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getInt("Anio"),
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    NumeroMotor = resultSet.getString("NumeroMotor") ?: "",
                    FechaAltaFoto1 = resultSet.getString("FechaAltaFoto1") ?: "",
                    FechaAltaFoto2 = resultSet.getString("FechaAltaFoto2") ?: "",
                    FechaAltaFoto3 = resultSet.getString("FechaAltaFoto3") ?: "",
                    FechaAltaFoto4 = resultSet.getString("FechaAltaFoto4") ?: ""
                )

                // Calcular cantidad de fotos
                var cantidadFotos = 0
                if (registro.FechaAltaFoto1.isNotEmpty()) cantidadFotos++
                if (registro.FechaAltaFoto2.isNotEmpty()) cantidadFotos++
                if (registro.FechaAltaFoto3.isNotEmpty()) cantidadFotos++
                if (registro.FechaAltaFoto4.isNotEmpty()) cantidadFotos++
                registro.CantidadFotos = cantidadFotos

                registros.add(registro)
            }

            Log.d("DALVehiculo_ConsultaPaso2", "✅ Se encontraron ${registros.size} registros para la fecha $fecha")

        } catch (e: Exception) {
            Log.e("DALVehiculo_ConsultaPaso2", "💥 Error consultando registros Paso2: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo_ConsultaPaso2", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext registros
    }

    // ✅ OBTENER ESTADÍSTICAS DEL DÍA
    suspend fun obtenerEstadisticasPaso2PorFecha(fecha: String): Map<String, Int> = withContext(Dispatchers.IO) {
        val estadisticas = mutableMapOf<String, Int>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext estadisticas

            val query = """
                SELECT 
                    COUNT(DISTINCT p.IdVehiculo) as VehiculosUnicos,
                    COUNT(*) as TotalRegistros,
                    SUM(CASE WHEN p.Fechaaltafoto1 IS NOT NULL THEN 1 ELSE 0 END) +
                    SUM(CASE WHEN p.Fechaaltafoto2 IS NOT NULL THEN 1 ELSE 0 END) +
                    SUM(CASE WHEN p.Fechaaltafoto3 IS NOT NULL THEN 1 ELSE 0 END) +
                    SUM(CASE WHEN p.Fechaaltafoto4 IS NOT NULL THEN 1 ELSE 0 END) as TotalFotos
                FROM dbo.Paso2LogVehiculo p
                WHERE CONVERT(date, p.Fechaaltafoto1) = ? 
                   OR CONVERT(date, p.Fechaaltafoto2) = ?
                   OR CONVERT(date, p.Fechaaltafoto3) = ?
                   OR CONVERT(date, p.Fechaaltafoto4) = ?
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            statement.setString(2, fecha)
            statement.setString(3, fecha)
            statement.setString(4, fecha)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                estadisticas["VehiculosUnicos"] = resultSet.getInt("VehiculosUnicos")
                estadisticas["TotalRegistros"] = resultSet.getInt("TotalRegistros")
                estadisticas["TotalFotos"] = resultSet.getInt("TotalFotos")
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo_ConsultaPaso2", "Error obteniendo estadísticas: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext estadisticas
    }
}
