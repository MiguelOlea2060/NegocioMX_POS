package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.PasoNumLogVehiculoNotificacion
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALNotificaciones {

    // Obtener el conteo de notificaciones pendientes
    suspend fun obtenerCantidadNotificaciones(): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var cantidad = 0

        try {
            Log.d("DALNotificaciones", "🔔 Consultando cantidad de notificaciones")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALNotificaciones", "❌ No se pudo obtener conexión")
                return@withContext 0
            }

            val query = """
                SELECT COUNT(idpasonumlogvehiculonotificacion) AS CantNotificaciones 
                FROM dbo.pasonumlogvehiculonotificacion
                WHERE activo = 1 
                  AND (realizada IS NULL OR realizada = 0)
                  AND (visto IS NULL OR visto = 0)
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                cantidad = resultSet.getInt("CantNotificaciones")
            }

            Log.d("DALNotificaciones", "✅ Cantidad de notificaciones: $cantidad")

        } catch (e: Exception) {
            Log.e("DALNotificaciones", "💥 Error consultando notificaciones: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALNotificaciones", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext cantidad
    }

    // Obtener el listado de notificaciones con detalles
    suspend fun obtenerListadoNotificaciones(): List<PasoNumLogVehiculoNotificacion> = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        val listaNotificaciones = mutableListOf<PasoNumLogVehiculoNotificacion>()

        try {
            Log.d("DALNotificaciones", "📋 Consultando listado de notificaciones")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALNotificaciones", "❌ No se pudo obtener conexión")
                return@withContext emptyList()
            }

            val query = """
                SELECT p.idpasonumlogvehiculonotificacion, p.idvehiculo, p.visto, 
                       p.fechaalta, p.fechavisto, p.Realizada, p.FechaRealizada,
                       v.Vin, p.activo, p.paso, p.vez, p.posicion, p.idpasonumlogvehiculo
                FROM dbo.pasonumlogvehiculonotificacion p
                INNER JOIN dbo.Vehiculo v ON p.IdVehiculo = v.IdVehiculo
                WHERE p.activo = 1 
                  AND (p.realizada IS NULL OR p.realizada = 0)
                  AND (p.visto IS NULL OR p.visto = 0)
                ORDER BY p.fechaalta DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val notificacion = PasoNumLogVehiculoNotificacion(
                    IdPasoNumLogVehiculoNotificacion = resultSet.getInt("idpasonumlogvehiculonotificacion"),
                    IdVehiculo = resultSet.getInt("idvehiculo"),
                    Visto = resultSet.getBoolean("visto"),
                    FechaAlta = resultSet.getTimestamp("fechaalta"),
                    FechaVisto = resultSet.getTimestamp("fechavisto"),
                    Realizada = resultSet.getBoolean("Realizada"),
                    FechaRealizada = resultSet.getTimestamp("FechaRealizada"),
                    Vin = resultSet.getString("Vin") ?: "",
                    Activo = resultSet.getBoolean("activo"),
                    Paso = resultSet.getShort("paso"),
                    Vez = resultSet.getShort("vez"),
                    Posicion = resultSet.getShort("posicion"),
                    IdPasoNumLogVehiculo = resultSet.getInt("idpasonumlogvehiculo")
                )
                listaNotificaciones.add(notificacion)
            }

            Log.d("DALNotificaciones", "✅ Se encontraron ${listaNotificaciones.size} notificaciones")

        } catch (e: Exception) {
            Log.e("DALNotificaciones", "💥 Error obteniendo listado: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALNotificaciones", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext listaNotificaciones
    }

    // Marcar notificación como vista
    suspend fun marcarNotificacionComoVista(
        idNotificacion: Int,
        idUsuario: Int
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALNotificaciones", "👁️ Marcando notificación $idNotificacion como vista")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALNotificaciones", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val query = """
                UPDATE dbo.pasonumlogvehiculonotificacion 
                SET Visto = 1, 
                    FechaVisto = GETDATE(),
                    IdUsuarioVisto = ?
                WHERE idpasonumlogvehiculonotificacion = ?
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idUsuario)
            statement.setInt(2, idNotificacion)

            val filasActualizadas = statement.executeUpdate()

            if (filasActualizadas > 0) {
                Log.d("DALNotificaciones", "✅ Notificación marcada como vista")
                return@withContext true
            }

        } catch (e: Exception) {
            Log.e("DALNotificaciones", "💥 Error marcando notificación: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALNotificaciones", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext false
    }
}