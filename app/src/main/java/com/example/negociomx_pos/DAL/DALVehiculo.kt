package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

class DALVehiculo {

    // ✅ FUNCIÓN PARA PROBAR CONEXIÓN
    suspend fun probarConexion(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val conexion = ConexionSQLServer.obtenerConexion()
            val isValid = conexion?.isValid(5) == true
            conexion?.close()
            Log.d("DALVehiculo", "✅ Conexión probada: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e("DALVehiculo", "❌ Error probando conexión: ${e.message}")
            false
        }
    }

    // ✅ CONSULTAR VEHÍCULO POR VIN - CORREGIDO PARA ESQUEMA REAL
    suspend fun consultarVehiculoPorVIN(vin: String): Vehiculo? = withContext(Dispatchers.IO) {
        var vehiculo: Vehiculo? = null
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALVehiculo", "🔍 Consultando vehículo con VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """
                
                select vin, v.idmarca, v.idmodelo, m.nombre Marca, mo.nombre Modelo,
                 v.Annio, v.Motor v.version from dbo.vehiculo v
                		inner join dbo.MarcaAuto m on v.IdMarca=m.IdMarcaAuto
                		inner join dbo.Modelo mo on v.IdModelo=mo.IdModelo
                where v.vin != ?
              
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)

            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                vehiculo = Vehiculo(
                    Id = resultSet.getInt("IdVehiculo").toString(),
                    VIN = resultSet.getString("Vin") ?: "",
                    Marca = resultSet.getString("Marca") ?: "",
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getInt("Annio"),
                    Color = resultSet.getString("Version") ?: "", // Usamos Version como Color temporalmente
                    Placa = "", // No existe en el esquema actual
                    NumeroSerie = resultSet.getString("Motor") ?: "",
                    IdEmpresa = "", // No existe en el esquema actual
                    Activo = true, // Asumimos que está activo si existe
                    FechaCreacion = "", // No existe en el esquema actual
                   // FechaModificacion = resultSet.getString("FechaModificacion") ?: "",
                    // CAMPOS SOC - Valores por defecto ya que no existen en la BD actual
                    Odometro = 0,
                    Bateria = 0,
                    ModoTransporte = false,
                    RequiereRecarga = false,
                    Evidencia1 = "",
                    Evidencia2 = "",
                    FechaActualizacion = ""
                )
                Log.d("DALVehiculo", "✅ Vehículo encontrado: ${vehiculo.Marca} ${vehiculo.Modelo} ${vehiculo.Anio}")
            } else {
                Log.d("DALVehiculo", "❌ No se encontró vehículo con VIN: $vin")
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando vehículo: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext vehiculo
    }

    // ✅ ACTUALIZAR DATOS SOC DEL VEHÍCULO - NECESITARÁS AGREGAR ESTAS COLUMNAS A TU TABLA
    suspend fun actualizarSOC(
        vin: String,
        odometro: Int,
        bateria: Int,
        modoTransporte: Boolean,
        requiereRecarga: Boolean,
        evidencia1: String,
        evidencia2: String
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALVehiculo", "💾 Actualizando SOC para VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL
            val query = """
                UPDATE Vehiculo 
                SET FechaModificacion = ?
                WHERE Vin = ?
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fechaActual)
            statement.setString(2, vin)

            val filasAfectadas = statement.executeUpdate()

            if (filasAfectadas > 0) {
                Log.d("DALVehiculo", "✅ Vehículo actualizado exitosamente. Filas afectadas: $filasAfectadas")
                // TODO: Aquí podrías guardar los datos SOC en una tabla separada
                Log.d("DALVehiculo", "📊 Datos SOC: Odómetro=$odometro, Batería=$bateria, Transporte=$modoTransporte")
                return@withContext true
            } else {
                Log.w("DALVehiculo", "⚠️ No se actualizó ningún registro para VIN: $vin")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error actualizando SOC: ${e.message}")
            e.printStackTrace()
            return@withContext false
        } finally {
            try {
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }
    }
}
