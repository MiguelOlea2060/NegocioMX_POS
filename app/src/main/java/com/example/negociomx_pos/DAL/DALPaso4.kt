package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.ConsultaPaso4Item
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALPaso4 {

    // ✅ CONSULTAR REGISTROS PASO4 POR FECHA
    suspend fun consultarPaso4PorFecha(fecha: String): List<ConsultaPaso4Item> = withContext(Dispatchers.IO) {
        val registros = mutableListOf<ConsultaPaso4Item>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALPaso4", "🔍 Consultando registros Paso4 para fecha: $fecha")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALPaso4", "❌ No se pudo obtener conexión")
                return@withContext registros
            }

            val query = """
    SELECT v.IdVehiculo, p.IdPaso4LogVehiculo, v.Vin, b.BL, v.IdMarca, m.Nombre as Marca, v.IdModelo, mo.Nombre as Modelo, v.Annio
		, vc.IdColor, vc.IdColorInterior, c.Nombre as ColorExterior, c1.Nombre as ColorInterior,  v.Motor as NumeroMotor
		, CONVERT(varchar, p.FechaAlta, 120) as FechaAlta, p.IdUsuarioNubeAlta as IdUsuarioNube, COUNT(f.IdPaso4LogVehiculoFotos) as CantidadLlantas,
		SUM(CASE WHEN f.Verificada = 1 THEN 1 ELSE 0 END) as LlantasVerificadas,
		SUM(CASE WHEN f.Foto IS NOT NULL THEN 1 ELSE 0 END) as LlantasConFoto
    FROM dbo.Paso4LogVehiculo p 
    INNER JOIN dbo.vehiculo v ON p.IdVehiculo = v.IdVehiculo 
    INNER JOIN dbo.MarcaAuto m ON v.IdMarca = m.IdMarcaAuto
    INNER JOIN dbo.Modelo mo ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
    LEFT JOIN dbo.VehiculoColor vc ON v.IdVehiculo = vc.IdVehiculo
    LEFT JOIN dbo.Color c ON vc.IdColor = c.IdColor
    LEFT JOIN dbo.Color c1 ON vc.IdColorInterior = c1.IdColor
    LEFT JOIN dbo.bl b ON v.IdBL = b.IdBL
    LEFT JOIN dbo.Paso4LogVehiculoFotos f ON p.IdPaso4LogVehiculo = f.IdPaso4LogVehiculo
    WHERE CONVERT(date, p.FechaAlta) = ?
    GROUP BY v.IdVehiculo, p.IdPaso4LogVehiculo, v.Vin, b.BL, v.IdMarca, m.Nombre, 
                v.IdModelo, mo.Nombre, v.Annio, vc.IdColor, vc.IdColorInterior, 
                c.Nombre, c1.Nombre, v.Motor, p.FechaAlta, p.IdUsuarioNubeAlta
    ORDER BY p.FechaAlta
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val registro = ConsultaPaso4Item(
                    IdVehiculo = resultSet.getInt("IdVehiculo"),
                    IdPaso4LogVehiculo = resultSet.getInt("IdPaso4LogVehiculo"),
                    VIN = resultSet.getString("Vin") ?: "",
                    BL = resultSet.getString("BL") ?: "",
                    IdMarca = resultSet.getInt("IdMarca"),
                    Marca = resultSet.getString("Marca") ?: "",
                    IdModelo = resultSet.getInt("IdModelo"),
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getInt("Annio"),
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    NumeroMotor = resultSet.getString("NumeroMotor") ?: "",
                    FechaAlta = resultSet.getString("FechaAlta") ?: "",
                    CantidadLlantas = resultSet.getInt("CantidadLlantas"),
                    LlantasVerificadas = resultSet.getInt("LlantasVerificadas"),
                    LlantasConFoto = resultSet.getInt("LlantasConFoto"),
                    IdUsuarioNube = resultSet.getInt("IdUsuarioNube")
                )

                // Obtener detalles por posición
                obtenerDetallesPorPosicion(registro)
                registros.add(registro)
            }

            Log.d("DALPaso4", "✅ Se encontraron ${registros.size} registros para la fecha $fecha")

        } catch (e: Exception) {
            Log.e("DALPaso4", "💥 Error consultando registros Paso4: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALPaso4", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext registros
    }

    // ✅ OBTENER DETALLES POR POSICIÓN
    private suspend fun obtenerDetallesPorPosicion(registro: ConsultaPaso4Item) = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext

            val query = """
                SELECT Posicion, Verificada, CASE WHEN Foto IS NOT NULL THEN 1 ELSE 0 END as TieneFoto
                FROM Paso4LogVehiculoFotos 
                WHERE IdPaso4LogVehiculo = ?
                ORDER BY Posicion
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, registro.IdPaso4LogVehiculo)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val posicion = resultSet.getByte("Posicion")
                val verificada = resultSet.getBoolean("Verificada")
                val tieneFoto = resultSet.getBoolean("TieneFoto")

                when (posicion.toInt()) {
                    1 -> {
                        registro.Llanta1Verificada = verificada
                        registro.Llanta1TieneFoto = tieneFoto
                    }
                    2 -> {
                        registro.Llanta2Verificada = verificada
                        registro.Llanta2TieneFoto = tieneFoto
                    }
                    3 -> {
                        registro.Llanta3Verificada = verificada
                        registro.Llanta3TieneFoto = tieneFoto
                    }
                    4 -> {
                        registro.Llanta4Verificada = verificada
                        registro.Llanta4TieneFoto = tieneFoto
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("DALPaso4", "Error obteniendo detalles por posición: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }
    }

    // ✅ OBTENER ESTADÍSTICAS DEL DÍA
    suspend fun obtenerEstadisticasPaso4PorFecha(fecha: String): Map<String, Int> = withContext(Dispatchers.IO) {
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
                    SUM(CASE WHEN f.Foto IS NOT NULL THEN 1 ELSE 0 END) as TotalFotos
                FROM dbo.Paso4LogVehiculo p
                LEFT JOIN dbo.Paso4LogVehiculoFotos f ON p.IdPaso4LogVehiculo = f.IdPaso4LogVehiculo
                WHERE CONVERT(date, p.FechaAlta) = ?
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                estadisticas["VehiculosUnicos"] = resultSet.getInt("VehiculosUnicos")
                estadisticas["TotalRegistros"] = resultSet.getInt("TotalRegistros")
                estadisticas["TotalFotos"] = resultSet.getInt("TotalFotos")
            }

        } catch (e: Exception) {
            Log.e("DALPaso4", "Error obteniendo estadísticas: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext estadisticas
    }
}
