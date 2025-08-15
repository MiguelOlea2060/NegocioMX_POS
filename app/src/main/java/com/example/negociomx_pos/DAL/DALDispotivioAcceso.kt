package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.DispositivoAcceso
import com.example.negociomx_pos.BE.DispositivoAccesoUnico
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.Intento
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

public class DALDispotivioAcceso {
    var NombreTabla="DispositivoAcceso"
    private lateinit var firebaseRef: DatabaseReference

    init {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)
    }

    public fun insert(entidad: DispositivoAcceso):String
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)
        var id:String=""

//        val idEntidad=firebaseRef.push().key!!
        val idEntidad=entidad.IdDispositivo.toString()

        firebaseRef.child(idEntidad).setValue(entidad)
            .addOnCompleteListener{
                id=entidad.IdDispositivo.toString()
            }
            .addOnFailureListener{
                id=""
            }

        return id
    }

    public fun update(entidad: DispositivoAcceso, onFinishUpdateListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val idEntidad=entidad.IdDispositivo.toString()
        val idDispositivo:String=entidad.IdDispositivo!!

        firebaseRef.child(idDispositivo).setValue(entidad)
            .addOnCompleteListener{
               onFinishUpdateListener( entidad.IdDispositivo.toString())
            }
            .addOnFailureListener{
                onFinishUpdateListener("")
            }
    }

    public fun insertIntento(entidad: Intento, onFinishIntentoListener :(String) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)
        var id:String=""

        val idDispositivo=entidad.IdDispositivo.toString()
        val idEntidad=firebaseRef.push().key!!
        entidad.Id=idEntidad

        firebaseRef.child(idDispositivo).child("Intento").child(idEntidad).setValue(entidad)
            .addOnCompleteListener{
                onFinishIntentoListener(entidad.Id.toString())
            }
            .addOnFailureListener{
                onFinishIntentoListener("")
            }
    }

    /*    var email: String = "email_address_1"
    var q_2: Query = ref.child("users").orderByChild("profile").child("email").equalTo(email)
    var q_3: Query = ref.child("users").orderByChild("profile").orderByChild("email").equalTo(email)*/
    public fun getAll( onClickListener: (List<DispositivoAccesoUnico>) -> Unit)
    {
        var listaDispositivos: List<DispositivoAccesoUnico>
        listaDispositivos= arrayListOf()

        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)

        try {
            val dispositivosRef = firebaseRef.child("DispositivoAcceso")

            val listenerNombreEmpresa = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lista: List<EmpresaNube>
                    lista = arrayListOf()

                    if (snapshot.exists()) {
                        for (d in snapshot.children) {
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    var cad: String
                    cad = error.toString()
                }
            }

            val listenerDispositivo = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (d in snapshot.children) {
                            var item = d.getValue(DispositivoAccesoUnico::class.java)
                            if (item != null) {
                                var find =
                                    listaDispositivos.find { it.IdDispositivo == item.IdDispositivo }
                                if (find != null)
                                    find.CantidadLogueos += 1
                                else {
                                    item.CantidadLogueos = 1
                                    listaDispositivos.add(item)
                                }
                            }
                        }
                    }
                    onClickListener(listaDispositivos)
                }

                override fun onCancelled(error: DatabaseError) {
                    var cad: String
                    cad = error.toString()
                }
            }
            dispositivosRef.addValueEventListener(listenerDispositivo)
        }
        catch (ex:Exception)
        {
            var cad=ex.toString()
        }
    }

    public fun getByIdDispositivo(idDispositivo:String, onfinishReadListener: (DispositivoAcceso?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        val orderRef=firebaseRef.child(NombreTabla).orderByKey().equalTo(idDispositivo)

        val listener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var item: DispositivoAcceso?=null
                if(snapshot.exists()) {
                    for (e in snapshot.children) {
                        item=e.getValue(DispositivoAcceso::class.java)
                    }
                }
                onfinishReadListener(item)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        orderRef.addListenerForSingleValueEvent(listener)
    }
}