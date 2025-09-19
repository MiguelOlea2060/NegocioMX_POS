package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.Usuario
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALUsuarioSQL {
    suspend fun getUsuarioByEmailAndPassword(email: String, password: String): Usuario? = withContext(
        Dispatchers.IO) {
        Log.d("DALUsuarioSQL", "🔍 Buscando usuario: $email")

        var usuario: Usuario? = null
        var connection = ConexionSQLServer.obtenerConexion()

        try {
            if (connection != null) {
                val query = """
                    SELECT IdUsuario, NombreCompleto, Email, IdRol, IdEmpresa, 
                           Activo, CuentaVerificada, Contrasena
                    FROM Usuario 
                    WHERE Email = ? AND Contrasena = ? AND Activo = 1
                """

                val statement: PreparedStatement = connection.prepareStatement(query)
                statement.setString(1, email)
                statement.setString(2, password) // En producción, usar hash

                val resultSet: ResultSet = statement.executeQuery()

                if (resultSet.next()) {
                    usuario = Usuario().apply {
                        IdUsuario = resultSet.getInt("IdUsuario")
                        NombreCompleto = resultSet.getString("NombreCompleto")
                        Email = resultSet.getString("Email")
                        IdRol = resultSet.getInt("IdRol")
                        IdEmpresa = resultSet.getInt("IdEmpresa")
                        Activo = resultSet.getBoolean("Activo")
                        CuentaVerificada = resultSet.getBoolean("CuentaVerificada")
                        Contrasena = resultSet.getString("Contrasena")
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

        return@withContext usuario
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