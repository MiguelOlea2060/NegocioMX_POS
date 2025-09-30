package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.DireccionVehiculo
import com.example.negociomx_pos.BE.Marca
import com.example.negociomx_pos.BE.Modelo
import com.example.negociomx_pos.BE.StatusFotoVehiculo
import com.example.negociomx_pos.BE.Transmision
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.BE.VehiculoPaso1
import com.example.negociomx_pos.BE.VehiculoPaso2
import com.example.negociomx_pos.BE.VehiculoPaso3
import com.example.negociomx_pos.BE.VehiculoPaso4
import com.example.negociomx_pos.BE.VehiculoResumen
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

        try  {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }





            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """                
                select v.vin, v.idmarca, v.idmodelo, ma.nombre Marca, m.nombre Modelo, v.Annio, Motor, 
                        v.idvehiculo, ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible, 
                        tv.Nombre TipoVehiculo, bl
                from vehiculo v inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                        inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo
                        left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                        left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.tipovehiculo tv with (nolock) on v.idtipovehiculo=tv.idtipovehiculo
                        left join dbo.bl b with (nolock) on v.idbl=b.idbl
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
                //Log.d("DALVehiculo", "✅ Vehículo encontrado: ${vehiculo.Marca} ${vehiculo.Modelo} ${vehiculo.Anio}")
            } else {
                //Log.d("DALVehiculo", "❌ No se encontró vehículo con VIN: $vin")
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

    suspend fun consultarVehiculoPorVINParaPaso1(vin: String): VehiculoPaso1? = withContext(Dispatchers.IO) {
        var vehiculo: VehiculoPaso1? = null
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try  {
            //Log.d("DALVehiculo", "🔍 Consultando vehículo con VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """                
                select p.IdPaso1LogVehiculo, p.Odometro, p.Bateria, p.ModoTransporte, p.RequiereRecarga, p.FechaAlta
				        ,(SELECT count(*) FROM Paso1LogVehiculoFotos pf WHERE pF.IdPaso1LogVehiculo =P.IdPaso1LogVehiculo and pf.posicion=1) FotosPosicion1
				        ,(SELECT count(*) FROM Paso1LogVehiculoFotos pf WHERE pF.IdPaso1LogVehiculo =p.IdPaso1LogVehiculo and pf.posicion=2) FotosPosicion2
				        ,(SELECT count(*) FROM Paso1LogVehiculoFotos pf WHERE pf.IdPaso1LogVehiculo =p.IdPaso1LogVehiculo and pf.posicion=3) FotosPosicion3
				        ,(SELECT count(*) FROM Paso1LogVehiculoFotos pf WHERE pf.IdPaso1LogVehiculo =p.IdPaso1LogVehiculo and pf.posicion=4) FotosPosicion4                        
                        ,v.vin, v.idmarca, v.idmodelo, ma.nombre Marca, m.nombre Modelo, v.Annio, Motor, 
                        v.idvehiculo, ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible, 
                        tv.Nombre TipoVehiculo, bl
                from vehiculo v inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                        inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo
                        left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                        left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.tipovehiculo tv with (nolock) on v.idtipovehiculo=tv.idtipovehiculo
                        left join dbo.bl b with (nolock) on v.idbl=b.idbl
						left join dbo.Paso1LogVehiculo p on p.IdVehiculo=v.IdVehiculo
                where v.vin = ?
       
              
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)

            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                vehiculo = VehiculoPaso1(
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
                    Odometro = resultSet.getInt("Odometro")?:0,
                    Bateria = resultSet.getInt("Bateria")?:0,
                    ModoTransporte = resultSet.getBoolean("ModoTransporte")?:false,
                    RequiereRecarga = resultSet.getBoolean("RequiereRecarga")?:false,
                    Evidencia1 = "",
                    Evidencia2 = "",
                    FechaActualizacion = "",

                    FotosPosicion1 = resultSet.getInt("FotosPosicion1").toInt(),
                    FotosPosicion2 = resultSet.getInt("FotosPosicion2").toInt(),
                    FotosPosicion3 = resultSet.getInt("FotosPosicion3").toInt(),
                    FotosPosicion4 = resultSet.getInt("FotosPosicion4").toInt(),

                    FechaAltaPaso1 = resultSet.getString("FechaAlta")?:"",
                    IdPaso1LogVehiculo =resultSet.getInt("IdPaso1LogVehiculo") ?:0
                )
                //Log.d("DALVehiculo", "✅ Vehículo encontrado: ${vehiculo.Marca} ${vehiculo.Modelo} ${vehiculo.Anio}")
            } else {
                //Log.d("DALVehiculo", "❌ No se encontró vehículo con VIN: $vin")
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
        var cadena=vehiculo?.Id.toString()
        return@withContext vehiculo
    }

    suspend fun consultarVehiculoPorVINParaPaso2(vin: String): VehiculoPaso2? = withContext(Dispatchers.IO) {
        var vehiculo: VehiculoPaso2? = null
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try  {
            //Log.d("DALVehiculo", "🔍 Consultando vehículo con VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """                
                select v.vin, v.idmarca, v.idmodelo, ma.nombre Marca, m.nombre Modelo, v.Annio, Motor, v.idvehiculo
                        , ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible, tv.Nombre TipoVehiculo
                        , bl ,p.Idpaso2logvehiculo, p.Tienefoto1, p.Tienefoto2, p.Tienefoto3, p.Tienefoto4,
        p.Nombrearchivofoto1, p.Nombrearchivofoto2, p.Nombrearchivofoto3, p.Nombrearchivofoto4
                from vehiculo v  left join dbo.paso2logvehiculo p with (nolock) on v.IdVehiculo=p.Idvehiculo 
                        inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                        inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo
                        left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                        left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.tipovehiculo tv with (nolock) on v.idtipovehiculo=tv.idtipovehiculo
                        left join dbo.bl b with (nolock) on v.idbl=b.idbl
                where v.vin = ?
              
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)

            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                vehiculo = VehiculoPaso2(
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
                    FechaCreacion = "", // No existe en el esquema actual
                    IdPaso2LogVehiculo=resultSet.getInt("IdPaso2LogVehiculo")?:0,
                    TieneFoto1=resultSet.getBoolean("TieneFoto1")?:false,
                    TieneFoto2=resultSet.getBoolean("TieneFoto2")?:false,
                    TieneFoto3=resultSet.getBoolean("TieneFoto3")?:false,
                    TieneFoto4=resultSet.getBoolean("TieneFoto4")?:false,
                    NombreArchivoFoto1=resultSet.getString("NombreArchivoFoto1")?:"",
                     NombreArchivoFoto2=resultSet.getString("NombreArchivoFoto2")?:"",
                     NombreArchivoFoto3=resultSet.getString("NombreArchivoFoto3")?:"",
                     NombreArchivoFoto4=resultSet.getString("NombreArchivoFoto4")?:""
                )
                //Log.d("DALVehiculo", "✅ Vehículo encontrado: ${vehiculo.Marca} ${vehiculo.Modelo} ${vehiculo.Anio}")
            } else {
                //Log.d("DALVehiculo", "❌ No se encontró vehículo con VIN: $vin")
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

    suspend fun consultarVehiculoPorVINParaPaso3(vin: String): VehiculoPaso3? = withContext(Dispatchers.IO) {
        var vehiculo: VehiculoPaso3? = null
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try  {
            //Log.d("DALVehiculo", "🔍 Consultando vehículo con VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """                
                select v.vin, v.idmarca, v.idmodelo, ma.nombre Marca, m.nombre Modelo, v.Annio, Motor, v.idvehiculo
                       ,ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible, tv.Nombre TipoVehiculo
                       , bl, p.IdPaso3LogVehiculo, p.Tienefoto, p.NombreArchivoFoto, p.FechaAlta FechaAltaFoto                        
                from vehiculo v left join dbo.paso3logvehiculo p with (nolock) on v.IdVehiculo=p.Idvehiculo 
                        inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                        inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo
                        left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                        left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.tipovehiculo tv with (nolock) on v.idtipovehiculo=tv.idtipovehiculo
                        left join dbo.bl b with (nolock) on v.idbl=b.idbl
                where v.vin = ?
                
                
              
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)

            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                vehiculo = VehiculoPaso3(
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
                    FechaCreacion = "", // No existe en el esquema actual
                    IdPaso3LogVehiculo=resultSet.getInt("IdPaso3LogVehiculo")?:0,
                     TieneFoto=resultSet.getBoolean("TieneFoto")?:false,
                    NombreArchivoFoto=resultSet.getString("NombreArchivoFoto")?:"",
                    FechaAltaFoto=resultSet.getString("FechaAltaFoto")?:""
                )
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

    // ✅ CONSULTAR VEHÍCULO POR VIN - CORREGIDO PARA ESQUEMA REAL
    suspend fun consultarVehiculoPorVINParaPaso4(vin: String): VehiculoPaso4? = withContext(Dispatchers.IO) {
        var vehiculo: VehiculoPaso4? = null
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var posicion:Int?=null
        var tieneFoto:Boolean?=null
        var verificada:Boolean?=null

        try  {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            // ✅ QUERY CORREGIDO PARA EL ESQUEMA REAL DE LA BASE DE DATOS
            val query = """                
                select v.vin, v.idmarca, v.idmodelo, ma.nombre Marca, m.nombre Modelo, v.Annio, Motor, v.idvehiculo
                        , ce.Nombre ColorExterior, ci.Nombre ColorInterior, tc.Nombre TipoCombustible
                        , tv.Nombre TipoVehiculo, bl, p.IdPaso4LogVehiculo, pd.Posicion, pd.Verificada
                        , pd.Verificada, CASE WHEN pd.Foto IS NOT NULL THEN 1 ELSE 0 END as TieneFoto
                from vehiculo v left join dbo.Paso4LogVehiculo p on p.IdVehiculo=v.IdVehiculo left join dbo.Paso4LogVehiculoFotos pd on p.IdPaso4LogVehiculo=pd.IdPaso4LogVehiculo
                    inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                    inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo
                    left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                    left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                    left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                    left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                    left join dbo.tipovehiculo tv with (nolock) on v.idtipovehiculo=tv.idtipovehiculo
                    left join dbo.bl b with (nolock) on v.idbl=b.idbl
                where v.vin = ?   
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)

            resultSet = statement.executeQuery()

            var primera=false
            while(resultSet.next()) {
                if(!primera) {
                    vehiculo = VehiculoPaso4(
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
                        IdPaso4LogVehiculo = resultSet.getInt("IdPaso4LogVehiculo")?:0
                    )
                    primera=true
                }
                posicion=resultSet.getInt("Posicion")?:0
                tieneFoto=resultSet.getBoolean("TieneFoto")?:false
                verificada=resultSet.getBoolean("Verificada")?:false
                if(posicion!=null)
                {
                    if(posicion==1) {
                        vehiculo?.TieneFoto1 =tieneFoto
                        vehiculo?.Verificada1=verificada
                    }
                    else if(posicion==2) {
                        vehiculo?.TieneFoto2 =tieneFoto
                        vehiculo?.Verificada2=verificada
                    }
                    else if(posicion==3) {
                        vehiculo?.TieneFoto3 =tieneFoto
                        vehiculo?.Verificada3=verificada
                    }
                    else if(posicion==4) {
                        vehiculo?.TieneFoto4 =tieneFoto
                        vehiculo?.Verificada4=verificada
                    }
                }
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
 /*   suspend fun insertarPaso1LogVehiculo(
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
    }*/


    // ✅ INSERTAR O ACTUALIZAR DATOS SOC EN LA TABLA PRINCIPAL (ÚNICO REGISTRO)
    suspend fun insertarOActualizarPaso1LogVehiculo(
        idVehiculo: Int,
        odometro: Int,
        bateria: Int,
        modoTransporte: Boolean,
        requiereRecarga: Boolean,
        idUsuarioNubeAlta: Int
    ): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var idResultado: Int = -1

        try {
            Log.d("DALVehiculo", "💾 Insertando/Actualizando datos SOC para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext -1
            }

            // ✅ VERIFICAR SI YA EXISTE REGISTRO
            val queryVerificar = """
            SELECT IdPaso1LogVehiculo FROM Paso1LogVehiculo WHERE IdVehiculo = ?
        """.trimIndent()

            val stmtVerificar = conexion.prepareStatement(queryVerificar)
            stmtVerificar.setInt(1, idVehiculo)
            val rsVerificar = stmtVerificar.executeQuery()

            if (rsVerificar.next()) {
                // ✅ ACTUALIZAR REGISTRO EXISTENTE
                idResultado = rsVerificar.getInt("IdPaso1LogVehiculo")

                val queryActualizar = """
                UPDATE Paso1LogVehiculo 
                SET Odometro = ?, Bateria = ?, ModoTransporte = ?, RequiereRecarga = ?, 
                    FechaAlta = GETDATE()
                WHERE IdVehiculo = ?
            """.trimIndent()

                statement = conexion.prepareStatement(queryActualizar)
                statement.setShort(1, odometro.toShort())
                statement.setByte(2, bateria.toByte())
                statement.setBoolean(3, modoTransporte)
                statement.setBoolean(4, requiereRecarga)
                statement.setInt(5, idVehiculo)

                statement.executeUpdate()
                Log.d("DALVehiculo", "✅ Registro SOC actualizado. ID: $idResultado")

            } else {
                // ✅ INSERTAR NUEVO REGISTRO
                val queryInsertar = """
                INSERT INTO Paso1LogVehiculo (IdVehiculo, Odometro, Bateria, ModoTransporte, RequiereRecarga, FechaAlta, IdUsuarioNubeAlta)
                VALUES (?, ?, ?, ?, ?, GETDATE(), ?)
            """.trimIndent()

                statement = conexion.prepareStatement(queryInsertar, PreparedStatement.RETURN_GENERATED_KEYS)
                statement.setInt(1, idVehiculo)
                statement.setShort(2, odometro.toShort())
                statement.setByte(3, bateria.toByte())
                statement.setBoolean(4, modoTransporte)
                statement.setBoolean(5, requiereRecarga)
                statement.setInt(6, idUsuarioNubeAlta)

                statement.executeUpdate()

                val rs = statement.generatedKeys
                if (rs.next()) {
                    idResultado = rs.getInt(1)
                }
                Log.d("DALVehiculo", "✅ Nuevo registro SOC insertado. ID: $idResultado")
            }

            rsVerificar.close()
            stmtVerificar.close()
            return@withContext idResultado

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando/actualizando SOC: ${e.message}")
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



    // ✅ OBTENER ID DE PASO1LOGVEHICULO EXISTENTE
    suspend fun obtenerIdPaso1LogVehiculoExistente(idVehiculo: Int): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext -1

            val query = """
            SELECT IdPaso1LogVehiculo FROM Paso1LogVehiculo WHERE IdVehiculo = ?
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                return@withContext resultSet.getInt("IdPaso1LogVehiculo")
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error obteniendo ID Paso1: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext -1
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


    // ✅ MÉTODOS PARA PASO 4 - LLANTAS

    // ✅ INSERTAR REGISTRO EN PASO4LOGVEHICULO
    suspend fun insertarPaso4LogVehiculo(
        idVehiculo: Int,
        idUsuarioNubeAlta: Int
    ): Int = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var generatedKey: Int = -1

        try {
            Log.d("DALVehiculo", "💾 Insertando registro en Paso4LogVehiculo para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext -1
            }

            val query = """
                INSERT INTO Paso4LogVehiculo (IdVehiculo, FechaAlta, IdUsuarioNubeAlta)
                VALUES (?, GETDATE(), ?)
            """.trimIndent()

            statement = conexion.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)
            statement.setInt(1, idVehiculo)
            statement.setInt(2, idUsuarioNubeAlta)

            statement.executeUpdate()

            val rs = statement.generatedKeys
            if (rs.next()) {
                generatedKey = rs.getInt(1)
            }

            Log.d("DALVehiculo", "✅ Registro Paso4 insertado exitosamente. Id generado: $generatedKey")
            return@withContext generatedKey

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando registro Paso4: ${e.message}")
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

    // ✅ INSERTAR FOTO DE LLANTA
    suspend fun insertarFotoPaso4(
        idPaso4LogVehiculo: Int,
        idUsuarioNubeAlta: Int,
        posicion: Byte,
        verificada: Boolean,
        fotoBase64: String?
    ): Boolean = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null

        try {
            Log.d("DALVehiculo", "💾 Insertando foto Paso4 posición $posicion")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext false
            }

            val nombreArchivo = if (fotoBase64 != null) {
                "Paso4Llanta${posicion}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
            } else null

            val query = """
                INSERT INTO Paso4LogVehiculoFotos (IdPaso4LogVehiculo, IdUsuarioNubeAlta, FechaAlta, Consecutivo, Posicion, Verificada, Foto, NombreArchivo)
                VALUES (?, ?, GETDATE(), 1, ?, ?, ?, ?)
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idPaso4LogVehiculo)
            statement.setInt(2, idUsuarioNubeAlta)
            statement.setByte(3, posicion)
            statement.setBoolean(4, verificada)
            statement.setString(5, fotoBase64)
            statement.setString(6, nombreArchivo)

            val filasAfectadas = statement.executeUpdate()

            if (filasAfectadas > 0) {
                Log.d("DALVehiculo", "✅ Foto Paso4 posición $posicion insertada exitosamente")
                return@withContext true
            } else {
                Log.w("DALVehiculo", "⚠️ No se insertó la foto Paso4 posición $posicion")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error insertando foto Paso4: ${e.message}")
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

    // ✅ CONSULTAR DATOS PASO4 EXISTENTES
    suspend fun consultarPaso4Existente(idVehiculo: Int): Map<Byte, Pair<Boolean, Boolean>> = withContext(Dispatchers.IO) {
        val resultado = mutableMapOf<Byte, Pair<Boolean, Boolean>>() // Posicion -> (Verificada, TieneFoto)
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALVehiculo", "🔍 Consultando datos Paso4 existentes para IdVehiculo: $idVehiculo")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext resultado
            }

            val query = """
                SELECT f.Posicion, f.Verificada, CASE WHEN f.Foto IS NOT NULL THEN 1 ELSE 0 END as TieneFoto
                FROM Paso4LogVehiculo p
                INNER JOIN Paso4LogVehiculoFotos f ON p.IdPaso4LogVehiculo = f.IdPaso4LogVehiculo
                WHERE p.IdVehiculo = ?
                ORDER BY f.Posicion
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val posicion = resultSet.getByte("Posicion")
                val verificada = resultSet.getBoolean("Verificada")
                val tieneFoto = resultSet.getBoolean("TieneFoto")
                resultado[posicion] = Pair(verificada, tieneFoto)
            }

            Log.d("DALVehiculo", "✅ Consulta Paso4 completada. Encontradas ${resultado.size} llantas")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando datos Paso4: ${e.message}")
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

        return@withContext resultado
    }

    // ✅ OBTENER FOTO BASE64 PASO4
    suspend fun obtenerFotoBase64Paso4(idVehiculo: Int, posicion: Byte): String? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext null

            val query = """
                SELECT f.Foto 
                FROM Paso4LogVehiculo p
                INNER JOIN Paso4LogVehiculoFotos f ON p.IdPaso4LogVehiculo = f.IdPaso4LogVehiculo
                WHERE p.IdVehiculo = ? AND f.Posicion = ? AND f.Foto IS NOT NULL
                ORDER BY f.FechaAlta DESC
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setInt(1, idVehiculo)
            statement.setByte(2, posicion)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                return@withContext resultSet.getString("Foto")
            }
        } catch (e: Exception) {
            Log.e("DALVehiculo", "Error obteniendo foto Paso4: ${e.message}")
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


    suspend fun obtenerFotoBase64Paso1(idVehiculo: Int, posicion: Int): String? = withContext(Dispatchers.IO) {
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




    // ✅ metodo PARA CONSULTAR RESUMEN COMPLETO DEL VEHÍCULO
    suspend fun consultarResumenVehiculo(vin: String): VehiculoResumen? = withContext(Dispatchers.IO) {
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var resumen: VehiculoResumen? = null

        try {
            Log.d("DALVehiculo", "🔍 Consultando resumen para VIN: $vin")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALVehiculo", "❌ No se pudo obtener conexión")
                return@withContext null
            }

            val query = """
            select  v.IdVehiculo, v.VIN, bl, v.idmarca, ma.nombre Marca, v.idmodelo, m.nombre Modelo, v.Annio, ce.Nombre ColorExterior, ci.Nombre ColorInterior,
                            tc.Nombre TipoCombustible, v.NumeroMotor, p1.FechaAlta as FechaPrimerRegistro, DATEDIFF(DAY, p1.FechaAlta, GETDATE()) as DiasEstadia,
                 
                            -- Datos SOC (Paso 1)
                            p1.IdPaso1LogVehiculo, p1.Bateria SOC, p1.Odometro as Odometro, p1.FechaAlta as FechaPrimerSOC,
                            
                            -- Datos Accesorios (Paso 2)
                            p2.Idpaso2logvehiculo, p2.Tienefoto1 as TieneAccesorios, p2.Fechaaltafoto1 as FechaAccesorios,
                            
            				-- Datos REPUVE (Paso 3)
                            p3.IdPaso3LogVehiculo, p3.Tienefoto as Repuve, p3.FechaAlta as FechaRepuve 
            from dbo.vehiculo v 
            			inner join dbo.MarcaAuto ma with (nolock) on v.IdMarca=ma.IdMarcaAuto
                        inner join dbo.Modelo m with (nolock) on v.IdModelo=m.IdModelo 
                        left join dbo.VehiculoColor vc with (nolock) on v.IdVehiculo=vc.IdVehiculo
                        left join dbo.Color ce with (nolock) on vc.IdColor=ce.IdColor
                        left join dbo.Color ci with (nolock) on vc.IdColorInterior=ci.IdColor
                        left join dbo.TipoCombustible tc with (nolock) on v.idtipocombustible=tc.idtipocombustible
                        left join dbo.bl b with (nolock) on v.idbl=b.idbl

            			left join dbo.Paso1LogVehiculo p1 on v.IdVehiculo=p1.IdVehiculo
            			left join dbo.paso2logvehiculo p2 on v.IdVehiculo=p2.Idvehiculo
            			left join dbo.paso3logvehiculo p3 on v.IdVehiculo=p3.IdVehiculo
            where v.vin=?
        """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, vin)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                val diasRegistrado = resultSet.getInt("DiasEstadia")
                val anios = diasRegistrado / 365
                val mesesRestantes = (diasRegistrado % 365) / 30
                val semanasRestantes = ((diasRegistrado % 365) % 30) / 7
                val diasRestantes = ((diasRegistrado % 365) % 30) % 7

                resumen = VehiculoResumen(
                    IdVehiculo = resultSet.getInt("IdVehiculo"),
                    VIN = resultSet.getString("VIN") ?: "",
                    BL = resultSet.getString("bl") ?: "",
                    Marca = resultSet.getString("Marca") ?: "",
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getString("Annio") ?: "",
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    TipoCombustible = resultSet.getString("TipoCombustible") ?: "",
                    NumeroMotor = resultSet.getString("NumeroMotor") ?: "",

                    FechaPrimerRegistro = resultSet.getString("FechaPrimerRegistro") ?: "",
                    DiasRegistrado = diasRegistrado,
                    AniosRegistrado = anios,
                    MesesRegistrado = mesesRestantes,
                    SemanasRegistrado = semanasRestantes,
                    DiasRestantes = diasRestantes,


                    Odometro = resultSet.getInt("Odometro"),
                    Bateria = resultSet.getInt("SOC"),
                    FechaPrimerSOC = resultSet.getString("FechaPrimerSOC") ?: "",

                    TieneAccesorios = resultSet.getInt("TieneAccesorios") == 1,
                    FechaPrimerAccesorio = resultSet.getString("FechaAccesorios") ?: "",

                    TieneRepuve = resultSet.getInt("Repuve") == 1,
                    FechaPrimerRepuve = resultSet.getString("FechaRepuve") ?: ""
                )
            }

            Log.d("DALVehiculo", "✅ Resumen consultado exitosamente")

        } catch (e: Exception) {
            Log.e("DALVehiculo", "💥 Error consultando resumen: ${e.message}")
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

        return@withContext resumen
    }


}

