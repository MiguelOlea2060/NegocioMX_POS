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
    suspend fun consultarPaso2PorFecha(fecha: String,idUsuario:Int?): List<ConsultaPaso2Item> = withContext(Dispatchers.IO) {
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

            var query = """
                SELECT v.IdVehiculo, p.IdPaso2LogVehiculo, v.Vin, b.BL, v.IdMarca, m.Nombre as Marca, v.IdModelo, 
                    mo.Nombre as Modelo, v.Annio, as Anio, vc.IdColor, vc.IdColorInterior, c.Nombre as ColorExterior, 
                    c1.Nombre as ColorInterior, v.Motor as NumeroMotor, CONVERT(varchar, p.Fechaaltafoto1, 120) as FechaAltaFoto1,
                    CONVERT(varchar, p.Fechaaltafoto2, 120) as FechaAltaFoto2,
                    CONVERT(varchar, p.Fechaaltafoto3, 120) as FechaAltaFoto3,
                    CONVERT(varchar, p.Fechaaltafoto4, 120) as FechaAltaFoto4,
                    TieneFoto1, TieneFoto2, TieneFoto3, TieneFoto4
                FROM dbo.Paso2LogVehiculo p 
                INNER JOIN dbo.vehiculo v with (nolock) ON p.IdVehiculo = v.IdVehiculo 
                INNER JOIN dbo.MarcaAuto m with (nolock) ON v.IdMarca = m.IdMarcaAuto
                INNER JOIN dbo.Modelo mo with (nolock) ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
                LEFT JOIN dbo.VehiculoColor vc with (nolock) ON v.IdVehiculo = vc.IdVehiculo
                LEFT JOIN dbo.Color c with (nolock) ON vc.IdColor = c.IdColor
                LEFT JOIN dbo.Color c1 with (nolock) ON vc.IdColorInterior = c1.IdColor
                LEFT JOIN dbo.bl b with (nolock) ON v.IdBL = b.IdBL                                
            """.trimIndent()
            var where=" WHERE ((CONVERT(date, p.Fechaaltafoto1) = ?) OR (CONVERT(date, p.Fechaaltafoto2) = ?)\n" +
                    " OR (CONVERT(date, p.Fechaaltafoto3) = ?)\n" +
                    " OR (CONVERT(date, p.Fechltafoto4) = ?)) "
            if(idUsuario!=null)
                where+=" p.IdUsuarioNube = ? "
            var orderBy=" ORDER BY p.fechaaltafoto1 desc"
            query+=where+orderBy

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            statement.setString(2, fecha)
            statement.setString(3, fecha)
            statement.setString(4, fecha)
            if(idUsuario!=null)
                statement.setInt(5, idUsuario)

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
                    FechaAltaFoto4 = resultSet.getString("FechaAltaFoto4") ?: "",
                    TieneFoto1 = resultSet.getBoolean("TieneFoto1"),
                    TieneFoto2 = resultSet.getBoolean("TieneFoto2"),
                    TieneFoto3 = resultSet.getBoolean("TieneFoto3"),
                    TieneFoto4 = resultSet.getBoolean("TieneFoto4")
                )

                // Calcular cantidad de fotos
                var cantidadFotos = 0
                if (registro.TieneFoto1==true) cantidadFotos++
                if (registro.TieneFoto2==true) cantidadFotos++
                if (registro.TieneFoto3==true) cantidadFotos++
                if (registro.TieneFoto4==true) cantidadFotos++
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
}
