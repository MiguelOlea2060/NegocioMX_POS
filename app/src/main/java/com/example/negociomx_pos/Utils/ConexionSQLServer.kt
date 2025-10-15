package com.example.negociomx_pos.Utils

import android.os.StrictMode
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
    private const val SERVER = "softsystemmx.com"
    private const val DATABASE = "softsyst_n1egoc10mx_byd"
    private const val USERNAME = "softsyst_samx01"
    private const val PASSWORD = "N1T1z1u7MX#.25L1c@01"

    private const val PORT = "1433"


    fun obtenerConexion(): Connection? {
        return try {
            val policy=StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

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
