package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

class DALPaso1SOC {

    // ✅ CONSULTAR REGISTROS PASO1 SOC POR FECHA
    suspend fun consultarPaso1SOCPorFecha(fecha: String): List<Paso1SOCItem> = withContext(Dispatchers.IO) {
        val registros = mutableListOf<Paso1SOCItem>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALConsultaPaso1SOC", "🔍 Consultando registros SOC para fecha: $fecha")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALConsultaPaso1SOC", "❌ No se pudo obtener conexión")
                return@withContext registros
            }

            // Parsear fecha para obtener año, mes y día
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = formatoFecha.parse(fecha)
            val calendar = Calendar.getInstance()
            calendar.time = fechaDate!!

            val anio = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-based
            val dia = calendar.get(Calendar.DAY_OF_MONTH)

            val query = """
                SELECT v.IdVehiculo,
                       p.IdPaso1LogVehiculo,
                       v.Vin,
                       b.BL,
                       v.IdMarca,
                       m.Nombre AS Marca,
                       v.IdModelo,
                       mo.Nombre AS Modelo,
                       v.Annio,
                       v.NumeroMotor,
                       vc.IdColor,
                       vc.IdColorInterior,
                       c.Nombre AS ColorExterior,
                       c1.Nombre AS ColorInterior,
                       p.Odometro,
                       p.Bateria,
                       p.ModoTransporte,
                       p.RequiereRecarga,
                       p.FechaAlta,
                       p.IdUsuarioNubeAlta,
                       (SELECT COUNT(*) FROM Paso1LogVehiculoFotos pf WHERE pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) AS CantidadFotos
                FROM dbo.Paso1LogVehiculo p
                INNER JOIN dbo.Vehiculo v ON p.IdVehiculo = v.IdVehiculo
                INNER JOIN dbo.MarcaAuto m ON v.IdMarca = m.IdMarcaAuto
                INNER JOIN dbo.Modelo mo ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
                LEFT JOIN dbo.VehiculoColor vc ON v.IdVehiculo = vc.IdVehiculo
                LEFT JOIN dbo.Color c ON vc.IdColor = c.IdColor
                LEFT JOIN dbo.Color c1 ON vc.IdColorInterior = c1.IdColor
                LEFT JOIN dbo.bl b ON v.IdBL = b.IdBL
                WHERE DATEPART(YEAR, p.FechaAlta) = ?
                  AND DATEPART(MONTH, p.FechaAlta) = ?
                  AND DATEPART(DAY, p.FechaAlta) = ?
                ORDER BY v.Vin, p.FechaAlta DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, anio)
            statement.setInt(2, mes)
            statement.setInt(3, dia)

            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val registro = Paso1SOCItem(
                    IdVehiculo = resultSet.getInt("IdVehiculo"),
                    IdPaso1LogVehiculo = resultSet.getInt("IdPaso1LogVehiculo"),
                    VIN = resultSet.getString("Vin") ?: "",
                    BL = resultSet.getString("BL") ?: "",
                    IdMarca = resultSet.getInt("IdMarca"),
                    Marca = resultSet.getString("Marca") ?: "",
                    IdModelo = resultSet.getInt("IdModelo"),
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getInt("Annio"),
                    NumeroMotor = resultSet.getString("NumeroMotor") ?: "",
                    IdColor = resultSet.getInt("IdColor"),
                    IdColorInterior = resultSet.getInt("IdColorInterior"),
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    Odometro = resultSet.getInt("Odometro"),
                    Bateria = resultSet.getInt("Bateria"),
                    ModoTransporte = resultSet.getBoolean("ModoTransporte"),
                    RequiereRecarga = resultSet.getBoolean("RequiereRecarga"),
                    FechaAlta = resultSet.getString("FechaAlta") ?: "",
                    UsuarioAlta = resultSet.getInt("IdUsuarioNubeAlta").toString(),
                    CantidadFotos = resultSet.getInt("CantidadFotos")
                )
                registros.add(registro)
            }

            Log.d("DALConsultaPaso1SOC", "✅ Se obtuvieron ${registros.size} registros")

        } catch (e: Exception) {
            Log.e("DALConsultaPaso1SOC", "💥 Error consultando registros: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALConsultaPaso1SOC", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext registros
    }
}
