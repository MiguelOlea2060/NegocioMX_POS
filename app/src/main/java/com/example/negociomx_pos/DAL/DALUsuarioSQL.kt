package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.CfgApp
import com.example.negociomx_pos.BE.Usuario
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALUsuarioSQL {
    suspend fun getUsuarioByEmailAndPassword(email: String, password: String): Pair<Usuario?,CfgApp?> = withContext(
        Dispatchers.IO) {
        Log.d("DALUsuarioSQL", "🔍 Buscando usuario: $email")

        var usuario: Usuario? = null
        var cfg:CfgApp?=null

        var connection = ConexionSQLServer.obtenerConexion()

        try {
            if (connection != null) {
                val query = """
                    SELECT u.IdUsuario, u.NombreCompleto, u.Email, u.IdRol, u.IdEmpresa, u.Activo, u.CuentaVerificada
                        , u.Contrasena, e.razonsocial, e.nombrecomercial, e.Rfc, c.ManejaGuardadoArchivosEnBD
                        , c.ManejaSeleccionBloquePosXTablero, c.FormatoCarpetaArchivos, c.IdCfgApp, c.IdConfiguracion
                        , c.reglasnotificaciones, c.urlguardadoarchivos, c.UrlAPIControllerGuardadoArchivos
                        , c.UrlAPIControllerVisualArchivos, c.CarpetaGuardadoArchivosNube
                        , c.ManejaSeleccionObsMovimientoLocal, c.ManejaSeleccionObsEnTaller
                    FROM Usuario u with (nolock) left join dbo.empresa e with (nolock) on u.idempresa=e.idempresa
                        left join dbo.CfgApp c with (nolock) on u.IdEmpresa=c.IdEmpresa
                    WHERE Email = ?
                """

                val statement: PreparedStatement = connection.prepareStatement(query)
                statement.setString(1, email)

                val resultSet: ResultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val idEmpresa=resultSet.getInt("IdEmpresa")?:0
                    val rfcEmpresa=resultSet.getString("Rfc")?:""
                    usuario = Usuario().apply {
                        IdUsuario = resultSet.getInt("IdUsuario")
                        NombreCompleto = resultSet.getString("NombreCompleto")
                        Email = resultSet.getString("Email")
                        IdRol = resultSet.getInt("IdRol")
                        IdEmpresa = resultSet.getInt("IdEmpresa")
                        Activo = resultSet.getBoolean("Activo")
                        CuentaVerificada = resultSet.getBoolean("CuentaVerificada")
                        Contrasena = resultSet.getString("Contrasena")
                        IdEmpresa=idEmpresa
                    }

                    val idCfgApp=resultSet.getShort("IdCfgApp")?:0
                    if(idCfgApp>0)
                    {
                        cfg=CfgApp(
                            IdEmpresa = idEmpresa,
                            RfcEmpresa = rfcEmpresa,
                            UrlGuardadoArchivos = resultSet.getString("UrlGuardadoArchivos")?:"",
                            ManejaGuardadoArchivosEnBD = resultSet.getBoolean("ManejaGuardadoArchivosEnBD")?:false,
                            ManejaSeleccionObsMovimientoLocal = resultSet.getBoolean("ManejaSeleccionObsMovimientoLocal")?:false,
                            ManejaSeleccionObsEnTaller = resultSet.getBoolean("ManejaSeleccionObsEnTaller")?:false,
                            FormatoCarpetaArchivos = resultSet.getString("FormatoCarpetaArchivos")?:"",
                            IdCfgApp = idCfgApp,
                            IdConfiguracion = resultSet.getInt("IdConfiguracion")?:0,
                            ReglasNotificaciones = resultSet.getString("ReglasNotificaciones")?:"",
                            ManejaSeleccionBloquePosXTablero = resultSet.getBoolean("ManejaSeleccionBloquePosXTablero")?:false,
                            UrlAPIControllerVisualArchivos = resultSet.getString("UrlAPIControllerVisualArchivos")?:"",
                            UrlAPIControllerGuardadoArchivos = resultSet.getString("UrlAPIControllerGuardadoArchivos")?:"",
                            CarpetaGuardadoArchivosNube = resultSet.getString("CarpetaGuardadoArchivosNube")?:""
                        )
                    }
                    Log.d("DALUsuarioSQL", "✅ Usuario encontrado: ${usuario.NombreCompleto}")
                }

                resultSet.close()
                statement.close()
            }
        } catch (e: Exception) {
            Log.e("DALUsuarioSQL", "❌ Error: ${e.message}")
        } finally {
            connection?.close()
        }

        return@withContext Pair(usuario,cfg)
    }

    suspend fun getUsuarioByEmail(email: String): Usuario? = withContext(Dispatchers.IO) {
        Log.d("DALUsuarioSQL", "🔍 Buscando usuario: $email")

        var usuario: Usuario? = null
        var connection = ConexionSQLServer.obtenerConexion()

        try {
            if (connection != null) {
                val query = """
                    SELECT IdUsuario, NombreCompleto, Email, IdRol, IdEmpresa,  Activo, CuentaVerificada
                    FROM Usuario 
                    WHERE Email = ?
                """

                val statement: PreparedStatement = connection.prepareStatement(query)
                statement.setString(1, email)

                val resultSet: ResultSet = statement.executeQuery()

                if (resultSet.next()) {
                    usuario = Usuario().apply {
                        IdUsuario = resultSet.getInt("IdUsuario")
                        NombreCompleto = resultSet.getString("NombreCompleto")
                        Email = resultSet.getString("Email")
                        IdRol = resultSet.getInt("IdRol")
                        IdEmpresa = resultSet.getInt("IdEmpresa")?:0
                        Activo = resultSet.getBoolean("Activo")
                        CuentaVerificada = resultSet.getBoolean("CuentaVerificada")
                    }
                    //Log.d("DALUsuarioSQL", "✅ Usuario encontrado: ${usuario.NombreCompleto}")
                }

                resultSet.close()
                statement.close()
            }
        } catch (e: Exception) {
            Log.e("DALUsuarioSQL", "❌ Error: ${e.message}")
        } finally {
            connection?.close()
        }

        return@withContext usuario
    }

    suspend fun addUsuario(usuario: Usuario): Int? = withContext(Dispatchers.IO) {
        Log.d("DALUsuarioSQL", "🔍 Dando de alta a Usuario ${usuario.Email}")

        var IdUsuario:Int=0
        var usuario: Usuario? = null
        var connection = ConexionSQLServer.obtenerConexion()

        try {
            if (connection != null) {
                val query = """
                        INSERT INTO Usuario (NombreCompleto, Email, Contrasena, FechaUltAcceso, Domicilio, IdRol, Bloqueado, Activo, Predeterminado, NombreUsuario, UsuarioConcentrador, CuentaVerificada)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """

                val statement: PreparedStatement = connection.prepareStatement(query)
                statement.setString(1, usuario?.NombreCompleto)
                statement.setString(2, usuario?.Email)
                statement.setString(3, usuario?.Contrasena)
                statement.setString(4, "")
                statement.setString(5, usuario?.Domicilio)
                statement.setInt(6, usuario?.IdRol!!)
                statement.setBoolean(7, usuario?.Bloqueado!!)
                statement.setBoolean(8, usuario?.Activo!!)
                statement.setBoolean(9, false)
                statement.setString(10, usuario?.NombreUsuario)
                statement.setBoolean(11, false)//UsuarioConsentrador
                statement.setBoolean(12, false)
                statement.setBoolean(13, false)

                statement.executeUpdate()

                val rs = statement.generatedKeys
                if (rs.next()) {
                    IdUsuario = rs.getInt(1)
                }

                Log.d("DALUsuarioSQL", "✅ Se genero el IdUsuario: ${IdUsuario}")
            }
        } catch (e: Exception) {
            Log.e("DALUsuarioSQL", "❌ Error: ${e.message}")
        } finally {
            connection?.close()
        }

        return@withContext IdUsuario
    }

}