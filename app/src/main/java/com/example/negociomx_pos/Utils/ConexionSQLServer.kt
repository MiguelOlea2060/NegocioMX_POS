package com.example.negociomx_pos.Utils

import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

object ConexionSQLServer {

    // ⚙️ CONFIGURA ESTOS DATOS CON TU SERVIDOR SQL
    ////Servidor local
    /*private const val SERVER = "192.168.1.162"
    private const val DATABASE = "NegocioMX_PatioLaPalma"
    private const val USERNAME = "sa"
    private const val PASSWORD = "NITr11ziu7.#"
    private const val PORT = "1433"*/

    //Servidor Nube
    private const val SERVER = "P3NWPLSK12SQL-v13.shr.prod.phx3.secureserver.net"
    private const val DATABASE = "neg0c10mx_byd"
    private const val USERNAME = "sanegoc10MX"
    private const val PASSWORD = "NITr2025MX.#MXL1c"
    private const val PORT = "1433"

    fun obtenerConexion(): Connection? {
        return try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")

            val connectionString = "jdbc:jtds:sqlserver://$SERVER:$PORT/$DATABASE;instance=SQLEXPRESS;user=$USERNAME;password=$PASSWORD"

            Log.d("ConexionSQLServer", "🔗 Intentando conectar a: $SERVER")

            val connection = DriverManager.getConnection(connectionString)

            Log.d("ConexionSQLServer", "✅ Conexión exitosa")
            connection

        } catch (e: Exception) {
            Log.e("ConexionSQLServer", "❌ Error de conexión: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
