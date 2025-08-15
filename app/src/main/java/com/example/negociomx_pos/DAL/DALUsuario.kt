package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

class DALUsuario {
    var NombreTabla="Usuario"

    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference

    public fun insert(entidad: UsuarioNube, onFinishIntentoListener :(String) -> Unit)
    {
        Log.d("DALUsuario", "🚀 INICIANDO INSERT - REFERENCIA CORREGIDA")

        // ✅ REFERENCIA CORREGIDA: NEGOCIOMX-FB/Usuario
        firebaseRef = FirebaseDatabase.getInstance().getReference("NEGOCIOMX-FB").child(NombreTabla)

        var rnd=Random(1000001)
        firebaseAuth=FirebaseAuth.getInstance()
        val idEntidad:Int=ThreadLocalRandom.current().nextInt(3000001)
        entidad.Id=idEntidad.toString()

        try {
            Log.d("DALUsuario", "📝 Guardando usuario: ${entidad.Email} con ID: ${idEntidad}")
            Log.d("DALUsuario", "🔗 Referencia Firebase: ${firebaseRef.toString()}")
            Log.d("DALUsuario", "📍 Se guardará en: NEGOCIOMX-FB/Usuario/${idEntidad}")

            // CONFIGURAR FIREBASE PARA FUNCIONAR OFFLINE
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            } catch (e: Exception) {
                Log.w("DALUsuario", "Persistencia ya habilitada: ${e.message}")
            }

            firebaseRef.child(idEntidad.toString()).setValue(entidad)
                .addOnCompleteListener { task ->
                    Log.d("DALUsuario", "📊 Task completado. Exitoso: ${task.isSuccessful}")

                    if (task.isSuccessful) {
                        Log.d("DALUsuario", "✅ Usuario guardado en NEGOCIOMX-FB/Usuario")

                        // CREAR EN AUTH - MANTENER TU LÓGICA
                        firebaseAuth.createUserWithEmailAndPassword(entidad.Email!!, entidad.Password!!)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    Log.d("DALUsuario", "🎉 Usuario creado en Auth exitosamente")
                                    onFinishIntentoListener("SUCCESS_COMPLETE")
                                } else {
                                    Log.w("DALUsuario", "⚠️ Auth falló pero DB OK: ${authTask.exception?.message}")
                                    // ÉXITO PARCIAL - DB guardado
                                    onFinishIntentoListener("SUCCESS_DB_ONLY")
                                }
                            }
                            .addOnCanceledListener{
                                Log.w("DALUsuario", "🚫 Auth cancelado pero DB OK")
                                onFinishIntentoListener("SUCCESS_DB_ONLY")
                            }
                    } else {
                        Log.e("DALUsuario", "❌ Error guardando en DB: ${task.exception?.message}")
                        onFinishIntentoListener("")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DALUsuario", "💥 Fallo completo: ${exception.message}")
                    onFinishIntentoListener("")
                }
        }
        catch (ex:Exception)
        {
            Log.e("DALUsuario", "🔥 Excepción crítica: ${ex.message}")
            onFinishIntentoListener("")
        }
    }

    public fun update(entidad: UsuarioNube, onFinishIntentoListener :(String) -> Unit)
    {
        // ✅ REFERENCIA CORREGIDA: NEGOCIOMX-FB/Usuario
        firebaseRef = FirebaseDatabase.getInstance().getReference("NEGOCIOMX-FB").child(NombreTabla)
        firebaseAuth=FirebaseAuth.getInstance()

        try {
            firebaseRef.child(entidad.Id!!).setValue(entidad)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.createUserWithEmailAndPassword(entidad.Email!!, entidad.Password!!)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    onFinishIntentoListener(authTask.result.toString())
                                } else {
                                    onFinishIntentoListener("")
                                }
                            }
                            .addOnCanceledListener{
                                onFinishIntentoListener("")
                            }
                    } else {
                        onFinishIntentoListener("")
                    }
                }
                .addOnFailureListener { exception ->
                    onFinishIntentoListener("")
                }
        }
        catch (ex:Exception)
        {
            var cad=ex.toString()
            onFinishIntentoListener("")
        }
    }

    public fun getUsuarioByEmail(email:String, onfinishReadListener: (UsuarioNube?) -> Unit)
    {
        Log.d("DALUsuario", "🔍 BÚSQUEDA DE USUARIO - REFERENCIA CORREGIDA")
        Log.d("DALUsuario", "📧 Buscando email: '$email'")

        // ✅ REFERENCIA CORREGIDA: NEGOCIOMX-FB/Usuario
        firebaseRef = FirebaseDatabase.getInstance().getReference("NEGOCIOMX-FB").child(NombreTabla)

        Log.d("DALUsuario", "🔗 Referencia: ${firebaseRef.toString()}")
        Log.d("DALUsuario", "📍 Buscando en: NEGOCIOMX-FB/Usuario")

        firebaseRef.addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DALUsuario", "📊 onDataChange ejecutado")
                Log.d("DALUsuario", "📈 Snapshot existe: ${snapshot.exists()}")
                Log.d("DALUsuario", "👥 Número de usuarios: ${snapshot.childrenCount}")

                var item: UsuarioNube? = null
                var encontrado: Boolean = false

                if(snapshot.exists()) {
                    for (e in snapshot.children) {
                        try {
                            item = e.getValue(UsuarioNube::class.java)
                            Log.d("DALUsuario", "👤 Revisando usuario: ${item?.Email}")

                            if(item?.Email != null && item.Email.equals(email, ignoreCase = true)) {
                                Log.d("DALUsuario", "🎯 ¡USUARIO ENCONTRADO!")
                                encontrado = true
                                break
                            }
                        } catch (ex: Exception) {
                            Log.e("DALUsuario", "⚠️ Error procesando usuario: ${ex.message}")
                        }
                    }
                } else {
                    Log.d("DALUsuario", "📭 No hay datos en NEGOCIOMX-FB/Usuario")
                }

                Log.d("DALUsuario", "🏁 Resultado final - Encontrado: $encontrado")

                if(encontrado) {
                    onfinishReadListener(item)
                } else {
                    onfinishReadListener(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DALUsuario", "❌ Error en consulta: ${error.message}")
                Log.e("DALUsuario", "🔢 Código: ${error.code}")
                Log.e("DALUsuario", "📝 Detalles: ${error.details}")

                // EN CASO DE ERROR, ASUMIR QUE EMAIL ESTÁ DISPONIBLE
                onfinishReadListener(null)
            }
        })
    }

    public fun getAllUsuarios(idEmpresaNube:String?, activo:Boolean?, onfinishReadListener: (List<UsuarioNube>,String) -> Unit)
    {
        var mensajeError:String=""
        // ✅ REFERENCIA CORREGIDA: NEGOCIOMX-FB/Usuario
        firebaseRef = FirebaseDatabase.getInstance().getReference("NEGOCIOMX-FB").child(NombreTabla)

        firebaseRef.addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var item: UsuarioNube
                var listaUsuarios:MutableList<UsuarioNube> = mutableListOf()

                if(snapshot.exists()) {
                    for (e in snapshot.children) {
                        try {
                            item = e.getValue(UsuarioNube::class.java)!!
                            if(item != null && (activo == null || item.Activo == activo)
                                && (idEmpresaNube == null || item.IdEmpresa.equals(idEmpresaNube))) {
                                listaUsuarios.add(item)
                            }
                        } catch (ex: Exception) {
                            Log.e("DALUsuario", "Error procesando usuario en getAllUsuarios: ${ex.message}")
                        }
                    }
                }
                onfinishReadListener(listaUsuarios, mensajeError)
            }

            override fun onCancelled(error: DatabaseError) {
                onfinishReadListener(arrayListOf(), error.toString())
            }
        })
    }
}
