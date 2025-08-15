package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.UsuarioNube
import com.google.firebase.database.*
import java.util.UUID

class DALUsuario {

    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://negociomx-fr-default-rtdb.firebaseio.com/").reference

    fun getUsuarioByEmail(email: String, onFinish: (UsuarioNube?) -> Unit) {
        Log.d("DALUsuario", "🔍 Buscando usuario por email: '$email'")
        Log.d("DALUsuario", "🌐 URL Firebase: https://negociomx-fr-default-rtdb.firebaseio.com/")
        Log.d("DALUsuario", "📂 Ruta: NEGOCIOMX-FB/Usuario")

        try {
            val usuariosRef = database.child("NEGOCIOMX-FB").child("Usuario")

            // Timeout de 30 segundos
            val timeoutRunnable = Runnable {
                Log.e("DALUsuario", "⏰ TIMEOUT: Búsqueda tardó más de 30 segundos")
                onFinish(null)
            }

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(timeoutRunnable, 30000)

            usuariosRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Cancelar timeout
                        android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                        Log.d("DALUsuario", "📊 Snapshot existe: ${snapshot.exists()}")
                        Log.d("DALUsuario", "📊 Número de hijos: ${snapshot.childrenCount}")

                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                Log.d("DALUsuario", "🔑 Key encontrada: ${userSnapshot.key}")

                                try {
                                    val firebaseData = userSnapshot.value as? Map<String, Any>

                                    if (firebaseData != null) {
                                        val usuario = UsuarioNube().apply {
                                            // ✅ MANEJO DUAL: camelCase Y PascalCase
                                            Id = (firebaseData["id"] ?: firebaseData["Id"]) as? String
                                            IdLocal = (firebaseData["idLocal"] ?: firebaseData["IdLocal"]) as? String
                                            NombreCompleto = (firebaseData["nombreCompleto"] ?: firebaseData["NombreCompleto"]) as? String
                                            Email = (firebaseData["email"] ?: firebaseData["Email"]) as? String
                                            Password = (firebaseData["password"] ?: firebaseData["Password"]) as? String
                                            IdRol = (firebaseData["idRol"] ?: firebaseData["IdRol"]) as? String
                                            IdEmpresa = (firebaseData["idEmpresa"] ?: firebaseData["IdEmpresa"]) as? String
                                            Activo = (firebaseData["activo"] ?: firebaseData["Activo"]) as? Boolean
                                            CuentaVerificada = (firebaseData["cuentaVerificada"] ?: firebaseData["CuentaVerificada"]) as? Boolean
                                            RazonSocialEmpresa = (firebaseData["razonSocialEmpresa"] ?: firebaseData["RazonSocialEmpresa"]) as? String
                                            NombreCuentaVerificada = (firebaseData["nombreCuentaVerificada"] ?: firebaseData["NombreCuentaVerificada"]) as? String
                                            RfcEmpresa = (firebaseData["rfcEmpresa"] ?: firebaseData["RfcEmpresa"]) as? String
                                        }

                                        Log.d("DALUsuario", "✅ Usuario mapeado exitosamente")
                                        Log.d("DALUsuario", "👤 Email: ${usuario.Email}")
                                        Log.d("DALUsuario", "🟢 Activo: ${usuario.Activo}")
                                        Log.d("DALUsuario", "✅ Verificado: ${usuario.CuentaVerificada}")
                                        Log.d("DALUsuario", "🆔 ID: ${usuario.Id}")
                                        Log.d("DALUsuario", "👤 Nombre: ${usuario.NombreCompleto}")

                                        onFinish(usuario)
                                        return
                                    }
                                } catch (e: Exception) {
                                    Log.e("DALUsuario", "❌ Error mapeando usuario: ${e.message}")
                                }
                            }
                        }

                        // ✅ BÚSQUEDA ADICIONAL CON PascalCase
                        Log.d("DALUsuario", "🔍 Búsqueda adicional con Email (PascalCase)")
                        usuariosRef.orderByChild("Email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshotPascal: DataSnapshot) {
                                    Log.d("DALUsuario", "📊 Snapshot PascalCase existe: ${snapshotPascal.exists()}")

                                    if (snapshotPascal.exists()) {
                                        for (userSnapshot in snapshotPascal.children) {
                                            Log.d("DALUsuario", "🔑 Key PascalCase encontrada: ${userSnapshot.key}")

                                            try {
                                                val firebaseData = userSnapshot.value as? Map<String, Any>

                                                if (firebaseData != null) {
                                                    val usuario = UsuarioNube().apply {
                                                        // ✅ MANEJO DUAL: camelCase Y PascalCase
                                                        Id = (firebaseData["id"] ?: firebaseData["Id"]) as? String
                                                        IdLocal = (firebaseData["idLocal"] ?: firebaseData["IdLocal"]) as? String
                                                        NombreCompleto = (firebaseData["nombreCompleto"] ?: firebaseData["NombreCompleto"]) as? String
                                                        Email = (firebaseData["email"] ?: firebaseData["Email"]) as? String
                                                        Password = (firebaseData["password"] ?: firebaseData["Password"]) as? String
                                                        IdRol = (firebaseData["idRol"] ?: firebaseData["IdRol"]) as? String
                                                        IdEmpresa = (firebaseData["idEmpresa"] ?: firebaseData["IdEmpresa"]) as? String
                                                        Activo = (firebaseData["activo"] ?: firebaseData["Activo"]) as? Boolean
                                                        CuentaVerificada = (firebaseData["cuentaVerificada"] ?: firebaseData["CuentaVerificada"]) as? Boolean
                                                        RazonSocialEmpresa = (firebaseData["razonSocialEmpresa"] ?: firebaseData["RazonSocialEmpresa"]) as? String
                                                        NombreCuentaVerificada = (firebaseData["nombreCuentaVerificada"] ?: firebaseData["NombreCuentaVerificada"]) as? String
                                                        RfcEmpresa = (firebaseData["rfcEmpresa"] ?: firebaseData["RfcEmpresa"]) as? String
                                                    }

                                                    Log.d("DALUsuario", "✅ Usuario PascalCase mapeado exitosamente")
                                                    Log.d("DALUsuario", "👤 Email: ${usuario.Email}")
                                                    Log.d("DALUsuario", "🟢 Activo: ${usuario.Activo}")
                                                    Log.d("DALUsuario", "✅ Verificado: ${usuario.CuentaVerificada}")

                                                    onFinish(usuario)
                                                    return
                                                }
                                            } catch (e: Exception) {
                                                Log.e("DALUsuario", "❌ Error mapeando usuario PascalCase: ${e.message}")
                                            }
                                        }
                                    }

                                    Log.w("DALUsuario", "❌ Usuario no encontrado para email: $email")
                                    onFinish(null)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("DALUsuario", "💥 Error Firebase PascalCase: ${error.message}")
                                    onFinish(null)
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Cancelar timeout
                        android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                        Log.e("DALUsuario", "💥 Error Firebase: ${error.message}")
                        Log.e("DALUsuario", "💥 Código error: ${error.code}")
                        Log.e("DALUsuario", "💥 Detalles: ${error.details}")
                        onFinish(null)
                    }
                })

        } catch (e: Exception) {
            Log.e("DALUsuario", "🔥 Excepción en getUsuarioByEmail: ${e.message}")
            onFinish(null)
        }
    }

    fun insert(usuario: UsuarioNube, onFinish: (String) -> Unit) {
        Log.d("DALUsuario", "💾 === INSERTANDO USUARIO ===")
        Log.d("DALUsuario", "👤 Email: ${usuario.Email}")
        Log.d("DALUsuario", "👤 Nombre: ${usuario.NombreCompleto}")

        try {
            val usuariosRef = database.child("NEGOCIOMX-FB").child("Usuario")

            // ✅ GENERAR ID NUMÉRICO ALEATORIO
            var userId = generateNumericId()

            // ✅ VERIFICAR SI EL ID EXISTE
            usuariosRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 🔄 SI EL ID EXISTE, GENERAR UNO NUEVO RECURSIVAMENTE
                        Log.w("DALUsuario", "⚠️ ID $userId ya existe, generando uno nuevo")
                        insert(usuario, onFinish) // Llamada recursiva
                    } else {
                        // ✅ SI EL ID NO EXISTE, PROCEDER CON LA INSERCIÓN
                        Log.d("DALUsuario", "🔑 ID generado: $userId")

                        // ✅ USAR camelCase para nuevos usuarios (consistencia)
                        val firebaseData = mapOf(
                            "id" to userId,
                            "idLocal" to usuario.IdLocal,
                            "nombreCompleto" to usuario.NombreCompleto,
                            "email" to usuario.Email,
                            "password" to usuario.Password,
                            "idRol" to usuario.IdRol,
                            "idEmpresa" to usuario.IdEmpresa,
                            "activo" to usuario.Activo,
                            "cuentaVerificada" to usuario.CuentaVerificada,
                            "razonSocialEmpresa" to usuario.RazonSocialEmpresa,
                            "nombreCuentaVerificada" to usuario.NombreCuentaVerificada,
                            "rfcEmpresa" to usuario.RfcEmpresa
                        )

                        // Timeout de 30 segundos
                        val timeoutRunnable = Runnable {
                            Log.e("DALUsuario", "⏰ TIMEOUT: Insert tardó más de 30 segundos")
                            onFinish("")
                        }

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(timeoutRunnable, 30000)

                        usuariosRef.child(userId).setValue(firebaseData)
                            .addOnSuccessListener {
                                // Cancelar timeout
                                android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                                Log.d("DALUsuario", "✅ Usuario insertado exitosamente")
                                Log.d("DALUsuario", "🔑 ID retornado: $userId")
                                onFinish(userId)
                            }
                            .addOnFailureListener { exception ->
                                // Cancelar timeout
                                android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                                Log.e("DALUsuario", "❌ Error insertando usuario: ${exception.message}")
                                onFinish("")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DALUsuario", "💥 Error Firebase al verificar ID: ${error.message}")
                    onFinish("")
                }
            })

        } catch (e: Exception) {
            Log.e("DALUsuario", "🔥 Excepción en insert: ${e.message}")
            onFinish("")
        }
    }

    fun getAllUsuarios(onFinish: (List<UsuarioNube>) -> Unit) {
        Log.d("DALUsuario", "📋 === OBTENIENDO TODOS LOS USUARIOS ===")

        try {
            val usuariosRef = database.child("NEGOCIOMX-FB").child("Usuario")

            // Timeout de 30 segundos
            val timeoutRunnable = Runnable {
                Log.e("DALUsuario", "⏰ TIMEOUT: getAllUsuarios tardó más de 30 segundos")
                onFinish(emptyList())
            }

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(timeoutRunnable, 30000)

            usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Cancelar timeout
                    android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                    Log.d("DALUsuario", "📊 Snapshot existe: ${snapshot.exists()}")
                    Log.d("DALUsuario", "📊 Número de usuarios: ${snapshot.childrenCount}")

                    val usuarios = mutableListOf<UsuarioNube>()

                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            try {
                                val firebaseData = userSnapshot.value as? Map<String, Any>

                                if (firebaseData != null) {
                                    val usuario = UsuarioNube().apply {
                                        // ✅ MANEJO DUAL: camelCase Y PascalCase
                                        Id = (firebaseData["id"] ?: firebaseData["Id"]) as? String
                                        IdLocal = (firebaseData["idLocal"] ?: firebaseData["IdLocal"]) as? String
                                        NombreCompleto = (firebaseData["nombreCompleto"] ?: firebaseData["NombreCompleto"]) as? String
                                        Email = (firebaseData["email"] ?: firebaseData["Email"]) as? String
                                        Password = (firebaseData["password"] ?: firebaseData["Password"]) as? String
                                        IdRol = (firebaseData["idRol"] ?: firebaseData["IdRol"]) as? String
                                        IdEmpresa = (firebaseData["idEmpresa"] ?: firebaseData["IdEmpresa"]) as? String
                                        Activo = (firebaseData["activo"] ?: firebaseData["Activo"]) as? Boolean
                                        CuentaVerificada = (firebaseData["cuentaVerificada"] ?: firebaseData["CuentaVerificada"]) as? Boolean
                                        RazonSocialEmpresa = (firebaseData["razonSocialEmpresa"] ?: firebaseData["RazonSocialEmpresa"]) as? String
                                        NombreCuentaVerificada = (firebaseData["nombreCuentaVerificada"] ?: firebaseData["NombreCuentaVerificada"]) as? String
                                        RfcEmpresa = (firebaseData["rfcEmpresa"] ?: firebaseData["RfcEmpresa"]) as? String
                                    }

                                    usuarios.add(usuario)
                                }
                            } catch (e: Exception) {
                                Log.e("DALUsuario", "❌ Error mapeando usuario: ${e.message}")
                            }
                        }
                    }

                    Log.d("DALUsuario", "✅ ${usuarios.size} usuarios mapeados exitosamente")
                    onFinish(usuarios)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Cancelar timeout
                    android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(timeoutRunnable)

                    Log.e("DALUsuario", "💥 Error Firebase: ${error.message}")
                    Log.e("DALUsuario", "💥 Código error: ${error.code}")
                    Log.e("DALUsuario", "💥 Detalles: ${error.details}")
                    onFinish(emptyList())
                }
            })

        } catch (e: Exception) {
            Log.e("DALUsuario", "🔥 Excepción en getAllUsuarios: ${e.message}")
            onFinish(emptyList())
        }
    }

    // ✅ FUNCIÓN PARA GENERAR ID NUMÉRICO ALEATORIO
    private fun generateNumericId(): String {
        return (100000..999999).random().toString()
    }
}
