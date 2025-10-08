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
    suspend fun consultarPaso1SOCPorFecha(fecha: String, idUsuario:Int?): List<Paso1SOCItem> = withContext(Dispatchers.IO) {
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

            var query = """
                SELECT v.IdVehiculo, p.IdPaso1LogVehiculo, v.Vin, b.BL, v.IdMarca, m.Nombre AS Marca, v.IdModelo, 
                       mo.Nombre AS Modelo, v.Annio, v.NumeroMotor, vc.IdColor, vc.IdColorInterior, c.Nombre 
                       AS ColorExterior, c1.Nombre AS ColorInterior, p.Odometro, p.Bateria, p.ModoTransporte,
                       p.RequiereRecarga, p.FechaAlta, p.IdUsuarioNubeAlta
                    , (SELECT pf.NombreArchivo FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=1 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) NombreArchivo1
                    , (SELECT pf.NombreArchivo FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=2 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) NombreArchivo2
                    , (SELECT pf.NombreArchivo FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=3 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) NombreArchivo3
                    , (SELECT pf.NombreArchivo FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=4 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) NombreArchivo4
                    , (SELECT COUNT(*) FROM Paso1LogVehiculoFotos pf WHERE pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo) AS CantidadFotos
                    ,Format((SELECT top 1 pf.FechaAlta FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=1 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo), 'dd/MMM/yyyy HH:mm:ss') FechaAltaFoto1
                    ,Format((SELECT top 1 pf.FechaAlta FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=2 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo), 'dd/MMM/yyyy HH:mm:ss') FechaAltaFoto2
                    ,Format((SELECT top 1 pf.FechaAlta FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=3 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo), 'dd/MMM/yyyy HH:mm:ss') FechaAltaFoto3
                    ,Format((SELECT top 1 pf.FechaAlta FROM Paso1LogVehiculoFotos pf WHERE pf.Posicion=4 and pf.IdPaso1LogVehiculo = p.IdPaso1LogVehiculo), 'dd/MMM/yyyy HH:mm:ss') FechaAltaFoto4
                FROM dbo.Paso1LogVehiculo p
                INNER JOIN dbo.Vehiculo v ON p.IdVehiculo = v.IdVehiculo
                INNER JOIN dbo.MarcaAuto m ON v.IdMarca = m.IdMarcaAuto
                INNER JOIN dbo.Modelo mo ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
                LEFT JOIN dbo.VehiculoColor vc ON v.IdVehiculo = vc.IdVehiculo
                LEFT JOIN dbo.Color c ON vc.IdColor = c.IdColor
                LEFT JOIN dbo.Color c1 ON vc.IdColorInterior = c1.IdColor
                LEFT JOIN dbo.bl b ON v.IdBL = b.IdBL                                
            """.trimIndent()
            var where="WHERE DATEPART(YEAR, p.FechaAlta) = ?  AND DATEPART(MONTH, p.FechaAlta) = ?\n" +
                    " AND DATEPART(DAY, p.FechaAlta) = ? "
            if(idUsuario!=null)
                where+=" AND p.IdUsuarioNubeAlta = ? "
            var orderBy=" ORDER BY p.FechaAlta DESC"
            query+=where+orderBy

            statement = conexion.prepareStatement(query)
            statement.setInt(1, anio)
            statement.setInt(2, mes)
            statement.setInt(3, dia)
            if (idUsuario!=null)
                statement.setInt(4, idUsuario) //aqui era 3
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
                    CantidadFotos = resultSet.getInt("CantidadFotos"),
                    FechaAltaFoto1 = resultSet.getString("FechaAltaFoto1")?:" ",
                    FechaAltaFoto2 = resultSet.getString("FechaAltaFoto2")?:" ",
                    FechaAltaFoto3 = resultSet.getString("FechaAltaFoto3")?:" ",
                    FechaAltaFoto4 = resultSet.getString("FechaAltaFoto4")?:" ",

                    NombreArchivoFoto1 = resultSet.getString("NombreArchivo1")?:"",
                    NombreArchivoFoto2 = resultSet.getString("NombreArchivo2")?:"",
                    NombreArchivoFoto3 = resultSet.getString("NombreArchivo3")?:"",
                    NombreArchivoFoto4 = resultSet.getString("NombreArchivo4")?:"",
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


    // ✅ NUEVO MÉTtodo: OBTENER URLs DE FOTOS PARA DESCARGA
    suspend fun obtenerURLsFotosPaso1(idPaso1LogVehiculo: Int): List<String> = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALPaso1SOC", "🔍 Obteniendo URLs de fotos para IdPaso1LogVehiculo: $idPaso1LogVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALPaso1SOC", "❌ No se pudo obtener conexión")
                return@withContext urls
            }

            val query = """
                SELECT Posicion, URLFoto, NombreArchivo
                FROM dbo.Paso1LogVehiculoFotos 
                WHERE IdPaso1LogVehiculo = ?
                ORDER BY Posicion
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idPaso1LogVehiculo)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val urlFoto = resultSet.getString("URLFoto")
                if (!urlFoto.isNullOrEmpty()) {
                    urls.add(urlFoto)
                    Log.d("DALPaso1SOC", "📸 URL encontrada para posición ${resultSet.getInt("Posicion")}: $urlFoto")
                }
            }

            Log.d("DALPaso1SOC", "✅ Se obtuvieron ${urls.size} URLs de fotos")

        } catch (e: Exception) {
            Log.e("DALPaso1SOC", "💥 Error obteniendo URLs de fotos: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALPaso1SOC", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext urls
    }





}
