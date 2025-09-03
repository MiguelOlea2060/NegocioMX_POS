package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.DireccionVehiculo
import com.example.negociomx_pos.BE.Marca
import com.example.negociomx_pos.BE.Modelo
import com.example.negociomx_pos.BE.Paso2LogVehiculo
import com.example.negociomx_pos.BE.Paso3LogVehiculo
import com.example.negociomx_pos.BE.StatusFotoVehiculo
import com.example.negociomx_pos.BE.Transmision
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


    // ✅ INSERTAR NUEVO VEHÍCULO
    suspend fun insertarVehiculo(
        vin: String,
        motor: String,
        idMarca: Int,
        idModelo: Int,
        anio: Int,
        idTransmision: Int,
        idDireccion: Int,
        version: String
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALVehiculo", "💾 Insertando nuevo vehículo con VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val query = """
                INSERT INTO Vehiculo (Vin, Motor, IdMarca, IdModelo, Annio, IdTransmision, IdDireccionVehiculo, Version, FechaModificacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)
            statement.setString(2, motor)
            statement.setInt(3, idMarca)
            statement.setInt(4, idModelo)
            statement.setInt(5, anio)
            statement.setInt(6, idTransmision)
            statement.setInt(7, idDireccion)
            statement.setString(8, version)

            val filasAfectadas = statement.executeUpdate()

            if (filasAfectadas > 0) {
                Log.d("DALVehiculo", "✅ Vehículo insertado exitosamente")
                return@withContext true
            } else {
                Log.w("DALVehiculo", "⚠️ No se insertó el vehículo")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando vehículo: ${e.message}")
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


    // ✅ OBTENER TODAS LAS MARCAS
    suspend fun obtenerMarcas(): List<Marca> = withContext(Dispatchers.IO) {
        val marcas = mutableListOf<Marca>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext marcas
            }

            val query = "SELECT IdMarcaAuto, Nombre FROM MarcaAuto ORDER BY Nombre"
            statement = conexion.prepareStatement(query)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val marca = Marca(
                    IdMarcaAuto = resultSet.getInt("IdMarcaAuto"),
                    Nombre = resultSet.getString("Nombre") ?: ""
                )
                marcas.add(marca)
            }

            Log.d("DALVehiculo", "✅ Se obtuvieron ${marcas.size} marcas")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error obteniendo marcas: ${e.message}")
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

        return@withContext marcas
    }


    // ✅ OBTENER MODELOS POR MARCA
    suspend fun obtenerModelosPorMarca(idMarca: Int): List<Modelo> = withContext(Dispatchers.IO) {
        val modelos = mutableListOf<Modelo>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext modelos
            }

            val query = "SELECT IdModelo, Nombre, IdMarca FROM Modelo WHERE IdMarca = ? ORDER BY Nombre"
            statement = conexion.prepareStatement(query)
            statement.setInt(1, idMarca)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val modelo = Modelo(
                    IdModelo = resultSet.getInt("IdModelo"),
                    Nombre = resultSet.getString("Nombre") ?: "",
                    IdMarcaAuto = resultSet.getInt("IdMarca")
                )
                modelos.add(modelo)
            }

            Log.d("DALVehiculo", "✅ Se obtuvieron ${modelos.size} modelos para marca $idMarca")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error obteniendo modelos: ${e.message}")
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

        return@withContext modelos
    }


    // ✅ OBTENER TODAS LAS TRANSMISIONES
    suspend fun obtenerTransmisiones(): List<Transmision> = withContext(Dispatchers.IO) {
        val transmisiones = mutableListOf<Transmision>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext transmisiones
            }

            val query = "SELECT IdTransmision, Nombre FROM Transmision ORDER BY Nombre"
            statement = conexion.prepareStatement(query)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val transmision = Transmision(
                    IdTransmision = resultSet.getInt("IdTransmision"),
                    Nombre = resultSet.getString("Nombre") ?: ""
                )
                transmisiones.add(transmision)
            }

            Log.d("DALVehiculo", "✅ Se obtuvieron ${transmisiones.size} transmisiones")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error obteniendo transmisiones: ${e.message}")
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

        return@withContext transmisiones
    }


    // ✅ OBTENER TODAS LAS DIRECCIONES
    suspend fun obtenerDirecciones(): List<DireccionVehiculo> = withContext(Dispatchers.IO) {
        val direcciones = mutableListOf<DireccionVehiculo>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext direcciones
            }

            val query = "SELECT IdDireccionVehiculo, Nombre FROM DireccionVehiculo ORDER BY Nombre"
            statement = conexion.prepareStatement(query)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val direccion = DireccionVehiculo(
                    IdDireccionVehiculo = resultSet.getInt("IdDireccionVehiculo"),
                    Nombre = resultSet.getString("Nombre") ?: ""
                )
                direcciones.add(direccion)
            }

            Log.d("DALVehiculo", "✅ Se obtuvieron ${direcciones.size} direcciones")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error obteniendo direcciones: ${e.message}")
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

        return@withContext direcciones
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
                select v.vin, v.idmarca, v.idmodelo, marcaauto.nombre Marca, modelo.nombre Modelo, v.Annio, Motor, 
                        v.idvehiculo, ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible, 
                        tv.Nombre TipoVehiculo, bl
                from vehiculo v inner join dbo.MarcaAuto on v.IdMarca=MarcaAuto.IdMarcaAuto
                        inner join dbo.Modelo on v.IdModelo=modelo.IdModelo
                        left join dbo.VehiculoColor vc on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce on vc.IdColor=ce.IdColor
                        left join dbo.Color ci on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.tipovehiculo tv on v.idtipovehiculo=tv.idtipovehiculo
                        left join dbo.bl b on v.idbl=b.idbl
                where v.vin = ?
              
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
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    BL = resultSet.getString("ColorInterior") ?: "",
                    NumeroSerie = resultSet.getString("BL") ?: "",
                    TipoVehiculo = resultSet.getString("TipoVehiculo") ?: "",
                    TipoCombustible = resultSet.getString("TipoCombustible") ?: "",
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



    // ✅ INSERTAR DATOS SOC EN LA NUEVA TABLA
    suspend fun insertarPaso1LogVehiculo(
        idVehiculo: Int,
        odometro: Int,
        bateria: Int,
        modoTransporte: Boolean,
        requiereRecarga: Boolean,
        idUsuarioNubeAlta: Int
    ): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var generatedKey: Int = -1

        try {
            Log.d("DALVehiculo", "💾 Insertando datos SOC en Paso1LogVehiculo para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext -1
            }

            val query = """
            INSERT INTO Paso1LogVehiculo (IdVehiculo, Odometro, Bateria, ModoTransporte, RequiereRecarga, FechaAlta, IdUsuarioNubeAlta)
            VALUES (?, ?, ?, ?, ?, GETDATE(), ?)
        """.trimIndent()

            statement = conexion.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)
            statement.setInt(1, idVehiculo)
            statement.setShort(2, odometro.toShort())
            statement.setByte(3, bateria.toByte())
            statement.setBoolean(4, modoTransporte)
            statement.setBoolean(5, requiereRecarga)
            statement.setInt(6, idUsuarioNubeAlta)

            statement.executeUpdate()

            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKey = rs.getInt(1)
            }

            Log.d("DALVehiculo", "✅ Datos SOC insertados exitosamente. Id generado: $generatedKey")
            return@withContext generatedKey

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando datos SOC: ${e.message}")
            e.printStackTrace()
            return@withContext -1
        } finally {
            try {
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }
    }



    // ✅ MÉTODOS PARA PASO 2 - EVIDENCIA FINAL

    // ✅ INSERTAR REGISTRO EN PASO2LOGVEHICULO
    suspend fun insertarPaso2LogVehiculo(
        idVehiculo: Int,
        idUsuarioNube: Int
    ): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var generatedKey: Int = -1

        try {
            Log.d("DALVehiculo", "💾 Insertando registro en Paso2LogVehiculo para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext -1
            }

            val query = """
                INSERT INTO paso2logvehiculo (Idvehiculo, Idusuarionube, Tienefoto1, Tienefoto2, Tienefoto3, Tienefoto4)
                VALUES (?, ?, 0, 0, 0, 0)
            """.trimIndent()

            statement = conexion.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)
            statement.setInt(1, idVehiculo)
            statement.setInt(2, idUsuarioNube)

            statement.executeUpdate()

            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKey = rs.getInt(1)
            }

            Log.d("DALVehiculo", "✅ Registro Paso2 insertado exitosamente. Id generado: $generatedKey")
            return@withContext generatedKey

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando registro Paso2: ${e.message}")
            e.printStackTrace()
            return@withContext -1
        } finally {
            try {
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }
    }





    // ✅ ACTUALIZAR FOTO EN PASO2LOGVEHICULO
    suspend fun actualizarFotoPaso2(
        idPaso2LogVehiculo: Int,
        numeroFoto: Int,
        fotoBase64: String
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALVehiculo", "💾 Actualizando foto $numeroFoto en Paso2LogVehiculo ID: $idPaso2LogVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val nombreArchivo = "Paso2Foto${numeroFoto}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

            val query = when (numeroFoto) {
                1 -> """
                    UPDATE paso2logvehiculo 
                    SET Foto1 = ?, Tienefoto1 = 1, Nombrearchivofoto1 = ?, Fechaaltafoto1 = GETDATE()
                    WHERE Idpaso2logvehiculo = ?
                """.trimIndent()
                2 -> """
                    UPDATE paso2logvehiculo 
                    SET Foto2 = ?, Tienefoto2 = 1, Nombrearchivofoto2 = ?, Fechaaltafoto2 = GETDATE()
                    WHERE Idpaso2logvehiculo = ?
                """.trimIndent()
                3 -> """
                    UPDATE paso2logvehiculo 
                    SET Foto3 = ?, Tienefoto3 = 1, Nombrearchivofoto3 = ?, Fechaaltafoto3 = GETDATE()
                    WHERE Idpaso2logvehiculo = ?
                """.trimIndent()
                4 -> """
                    UPDATE paso2logvehiculo 
                    SET Foto4 = ?, Tienefoto4 = 1, Nombrearchivofoto4 = ?, Fechaaltafoto4 = GETDATE()
                    WHERE Idpaso2logvehiculo = ?
                """.trimIndent()
                else -> return@withContext false
            }

            statement = conexion.prepareStatement(query)
            statement.setString(1, fotoBase64)
            statement.setString(2, nombreArchivo)
            statement.setInt(3, idPaso2LogVehiculo)

            val filasAfectadas = statement.executeUpdate()

            if (filasAfectadas > 0) {
                Log.d("DALVehiculo", "✅ Foto $numeroFoto actualizada exitosamente")
                return@withContext true
            } else {
                Log.w("DALVehiculo", "⚠️ No se actualizó la foto $numeroFoto")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error actualizando foto $numeroFoto: ${e.message}")
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

    // ✅ CONSULTAR FOTOS EXISTENTES PASO2
    suspend fun consultarFotosPaso2Existentes(idVehiculo: Int): Paso2LogVehiculo? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var paso2LogVehiculo: Paso2LogVehiculo? = null

        try {
            Log.d("DALVehiculo", "🔍 Consultando fotos Paso2 existentes para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            val query = """
                SELECT TOP 1 Idpaso2logvehiculo, Tienefoto1, Tienefoto2, Tienefoto3, Tienefoto4,
                           Nombrearchivofoto1, Nombrearchivofoto2, Nombrearchivofoto3, Nombrearchivofoto4
                FROM paso2logvehiculo 
                WHERE Idvehiculo = ?
                ORDER BY Idpaso2logvehiculo DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                paso2LogVehiculo = Paso2LogVehiculo(
                    IdPaso2LogVehiculo = resultSet.getInt("Idpaso2logvehiculo"),
                    IdVehiculo = idVehiculo,
                    TieneFoto1 = resultSet.getBoolean("Tienefoto1"),
                    TieneFoto2 = resultSet.getBoolean("Tienefoto2"),
                    TieneFoto3 = resultSet.getBoolean("Tienefoto3"),
                    TieneFoto4 = resultSet.getBoolean("Tienefoto4"),
                    NombreArchivoFoto1 = resultSet.getString("Nombrearchivofoto1"),
                    NombreArchivoFoto2 = resultSet.getString("Nombrearchivofoto2"),
                    NombreArchivoFoto3 = resultSet.getString("Nombrearchivofoto3"),
                    NombreArchivoFoto4 = resultSet.getString("Nombrearchivofoto4")
                )
            }

            Log.d("DALVehiculo", "✅ Consulta Paso2 completada")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando fotos Paso2: ${e.message}")
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

        return@withContext paso2LogVehiculo
    }

    // ✅ OBTENER FOTO BASE64 PASO2
    suspend fun obtenerFotoBase64Paso2(idVehiculo: Int, numeroFoto: Int): String? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext null

            val columnaFoto = when (numeroFoto) {
                1 -> "Foto1"
                2 -> "Foto2"
                3 -> "Foto3"
                4 -> "Foto4"
                else -> return@withContext null
            }

            val query = """
                SELECT TOP 1 $columnaFoto 
                FROM paso2logvehiculo 
                WHERE Idvehiculo = ? AND $columnaFoto IS NOT NULL
                ORDER BY Idpaso2logvehiculo DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                return@withContext resultSet.getString(columnaFoto)
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error obteniendo foto Paso2: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext null
    }



    // ✅ MÉTODOS PARA PASO 3 - REPUVE

    // ✅ INSERTAR REGISTRO EN PASO3LOGVEHICULO
    suspend fun insertarPaso3LogVehiculo(
        idVehiculo: Int,
        idUsuarioNube: Int,
        fotoBase64: String
    ): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var generatedKey: Int = -1

        try {
            Log.d("DALVehiculo", "💾 Insertando registro en Paso3LogVehiculo para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext -1
            }

            val nombreArchivo = "Paso3Foto_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

            val query = """
                INSERT INTO Paso3LogVehiculo (IdVehiculo, IdUsuarioNube, FechaAlta, Foto, Tienefoto, NombreArchivoFoto)
                VALUES (?, ?, GETDATE(), ?, 1, ?)
            """.trimIndent()

            statement = conexion.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)
            statement.setInt(1, idVehiculo)
            statement.setInt(2, idUsuarioNube)
            statement.setString(3, fotoBase64)
            statement.setString(4, nombreArchivo)

            statement.executeUpdate()

            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKey = rs.getInt(1)
            }

            Log.d("DALVehiculo", "✅ Registro Paso3 insertado exitosamente. Id generado: $generatedKey")
            return@withContext generatedKey

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando registro Paso3: ${e.message}")
            e.printStackTrace()
            return@withContext -1
        } finally {
            try {
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }
    }

    // ✅ CONSULTAR FOTO PASO3 EXISTENTE
    suspend fun consultarFotoPaso3Existente(idVehiculo: Int): Paso3LogVehiculo? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var paso3LogVehiculo: Paso3LogVehiculo? = null

        try {
            Log.d("DALVehiculo", "🔍 Consultando foto Paso3 existente para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            val query = """
                SELECT TOP 1 IdPaso3LogVehiculo, Tienefoto, NombreArchivoFoto, FechaAlta
                FROM Paso3LogVehiculo 
                WHERE IdVehiculo = ?
                ORDER BY IdPaso3LogVehiculo DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                paso3LogVehiculo = Paso3LogVehiculo(
                    IdPaso3LogVehiculo = resultSet.getInt("IdPaso3LogVehiculo"),
                    IdVehiculo = idVehiculo,
                    TieneFoto = resultSet.getBoolean("Tienefoto"),
                    NombreArchivoFoto = resultSet.getString("NombreArchivoFoto") ?: "",
                    FechaAlta = resultSet.getString("FechaAlta") ?: ""
                )
            }

            Log.d("DALVehiculo", "✅ Consulta Paso3 completada")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando foto Paso3: ${e.message}")
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

        return@withContext paso3LogVehiculo
    }

    // ✅ OBTENER FOTO BASE64 PASO3
    suspend fun obtenerFotoBase64Paso3(idVehiculo: Int): String? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext null

            val query = """
                SELECT TOP 1 Foto 
                FROM Paso3LogVehiculo 
                WHERE IdVehiculo = ? AND Foto IS NOT NULL
                ORDER BY IdPaso3LogVehiculo DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                return@withContext resultSet.getString("Foto")
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error obteniendo foto Paso3: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext null
    }




    // ✅ INSERTAR DATOS DE FOTOS EN LA NUEVA TABLA
    suspend fun insertarPaso1LogVehiculoFotos(
        idPaso1LogVehiculo: Int,
        idEntidadArchivoFoto: Int?,
        idUsuarioNubeAlta: Int,
        consecutivo: Short,
        posicion: Byte?,
        fotoBase64: String?
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALVehiculo", "💾 Insertando datos de foto en Paso1LogVehiculoFotos para IdPaso1LogVehiculo: $idPaso1LogVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val query = """
            INSERT INTO Paso1LogVehiculoFotos (IdPaso1LogVehiculo, IdEntidadArchivoFoto, IdUsuarioNubeAlta, FechaAlta, Consecutivo, Posicion, FotoBase64)
            VALUES (?, ?, ?, GETDATE(), ?, ?, ?)
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idPaso1LogVehiculo)
            if (idEntidadArchivoFoto == null) {
                statement.setNull(2, java.sql.Types.INTEGER)
            } else {
                statement.setInt(2, idEntidadArchivoFoto)
            }
            statement.setInt(3, idUsuarioNubeAlta)
            statement.setShort(4, consecutivo)
            if (posicion == null) {
                statement.setNull(5, java.sql.Types.TINYINT)
            } else {
                statement.setByte(5, posicion)
                if (fotoBase64 == null) {
                    statement.setNull(6, java.sql.Types.NVARCHAR)
                } else {
                    statement.setString(6, fotoBase64)
                }
            }

            val filasAfectadas = statement.executeUpdate()

            if (filasAfectadas > 0) {
                Log.d("DALVehiculo", "✅ Datos de foto insertados exitosamente")
                return@withContext true
            } else {
                Log.w("DALVehiculo", "⚠️ No se insertaron datos de foto")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando datos de foto: ${e.message}")
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

    // ✅ CONSULTAR FOTOS EXISTENTES PARA UN VEHÍCULO
    suspend fun consultarFotosExistentes(idVehiculo: Int): StatusFotoVehiculo? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var cantidadFotos = 0
        var status:StatusFotoVehiculo?= null

        try {
            Log.d("DALVehiculo", "🔍 Consultando fotos existentes para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext status
            }

            val query = """
            select (SELECT count(*) FROM Paso1LogVehiculoFotos pf INNER JOIN Paso1LogVehiculo pv ON pf.IdPaso1LogVehiculo = pv.IdPaso1LogVehiculo
            WHERE pv.IdVehiculo =? and pf.posicion=1) FotosPosicion1,
		(SELECT count(*) FROM Paso1LogVehiculoFotos pf
            INNER JOIN Paso1LogVehiculo pv ON pf.IdPaso1LogVehiculo = pv.IdPaso1LogVehiculo
            WHERE pv.IdVehiculo =? and pf.posicion=2) FotosPosicion2,
		(SELECT count(*) FROM Paso1LogVehiculoFotos pf
            INNER JOIN Paso1LogVehiculo pv ON pf.IdPaso1LogVehiculo = pv.IdPaso1LogVehiculo
            WHERE pv.IdVehiculo =? and pf.posicion=3) FotosPosicion3,
		(SELECT count(*) FROM Paso1LogVehiculoFotos pf
            INNER JOIN Paso1LogVehiculo pv ON pf.IdPaso1LogVehiculo = pv.IdPaso1LogVehiculo
            WHERE pv.IdVehiculo =? and pf.posicion=4) FotosPosicion4
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            statement.setInt(2, idVehiculo)
            statement.setInt(3, idVehiculo)
            statement.setInt(4, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val fotosPosicion1:Int= resultSet.getInt("FotosPosicion1").toInt()
                val fotosPosicion2:Int= resultSet.getInt("FotosPosicion2").toInt()
                val fotosPosicion3:Int= resultSet.getInt("FotosPosicion3").toInt()
                val fotosPosicion4:Int= resultSet.getInt("FotosPosicion4").toInt()

                status=StatusFotoVehiculo(FotosPosicion1 =fotosPosicion1, FotosPosicion2 = fotosPosicion2,
                    FotosPosicion3 = fotosPosicion3, FotosPosicion4 = fotosPosicion4)
            }

            Log.d("DALVehiculo", "✅ Fotos existentes para vehículo $idVehiculo: $cantidadFotos")
            //return@withContext status

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando fotos existentes: ${e.message}")
            e.printStackTrace()
            //return@withContext 0
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALVehiculo", "Error cerrando recursos: ${e.message}")
            }
        }
        return@withContext status
    }



    suspend fun consultarDatosSOCExistentes(idVehiculo: Int): Vehiculo? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var vehiculoSOC: Vehiculo? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext null

            val query = """
            SELECT TOP 1 Odometro, Bateria, ModoTransporte, RequiereRecarga, FechaAlta
            FROM Paso1LogVehiculo 
            WHERE IdVehiculo = ? 
            ORDER BY FechaAlta DESC
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                vehiculoSOC = Vehiculo().apply {
                    Odometro = resultSet.getInt("Odometro")
                    Bateria = resultSet.getInt("Bateria")
                    ModoTransporte = resultSet.getBoolean("ModoTransporte")
                    RequiereRecarga = resultSet.getBoolean("RequiereRecarga")
                    FechaActualizacion = resultSet.getString("FechaAlta") ?: ""
                }
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error consultando datos SOC: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext vehiculoSOC
    }


    suspend fun obtenerFotoBase64(idVehiculo: Int, posicion: Int): String? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext null

            val query = """
            SELECT TOP 1 FotoBase64 
            FROM Paso1LogVehiculoFotos pf 
            INNER JOIN Paso1LogVehiculo pv ON pf.IdPaso1LogVehiculo = pv.IdPaso1LogVehiculo
            WHERE pv.IdVehiculo = ? AND pf.Posicion = ?
            ORDER BY pf.FechaAlta DESC
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            statement.setInt(2, posicion)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                return@withContext resultSet.getString("FotoBase64")
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error obteniendo foto: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext null
    }




}

