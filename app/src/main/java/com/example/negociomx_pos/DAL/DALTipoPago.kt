package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.EmpresaDispositivoAcceso
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.TipoPagoNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

class DALTipoPago {
    private lateinit var firebaseRef: DatabaseReference

    var NombreTabla="TipoPago"

    fun insert(entidad: TipoPagoNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val idEntidad=entidad.Clave
        val key= ThreadLocalRandom.current().nextInt(2000001)
        entidad.Id=key.toString()

        try{
            firebaseRef.child(idEntidad).setValue(entidad)
                .addOnCompleteListener{
                    onFinishListener(entidad.Id.toString())
                }
                .addOnFailureListener{
                    onFinishListener("")
                }
        }
        catch (ex:Exception)
        {
            var cad=ex.toString()
            if(cad.isNotEmpty()==true)
            {

            }
        }
    }

    fun update(entidad: TipoPagoNube, onFinishUpdateListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(NombreTabla)

        val idEntidad=entidad.Clave
        try{
            firebaseRef.child(idEntidad).setValue(entidad)
                .addOnCompleteListener{
                    onFinishUpdateListener(entidad.Id.toString())
                }
                .addOnFailureListener{
                    onFinishUpdateListener("")
                }
        }
        catch (ex:Exception)
        {
            var cad=ex.toString()
            if(cad.isNotEmpty()==true)
            {

            }
        }
    }

    fun getAll(idEmpresa:String?, onClickListener: (List<TipoPagoNube>?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)

        var orderRef=firebaseRef.child(NombreTabla)
        if(idEmpresa!=null)
            orderRef.orderByChild("IdEmpresaNube").equalTo(idEmpresa)

        val eData = orderRef
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        var lista:List<TipoPagoNube>
                        lista= arrayListOf()
                        try {
                            lista = snapshot.children
                                .map {
                                        snapshot -> snapshot.getValue(TipoPagoNube::class.java)!!
                                }
                        }
                        catch (ex:Exception)
                        {
                        }
                        onClickListener(lista)
                    }
                    else
                        onClickListener(null)
                }
            })
    }

    fun getByClave(clave:String, onfinishReadListener: (TipoPagoNube?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        val orderRef=firebaseRef.child(NombreTabla).orderByKey().equalTo(clave)

        val listener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var item:TipoPagoNube?=null
                if(snapshot.exists()) {
                    for (e in snapshot.children) {
                        item=e.getValue(TipoPagoNube::class.java)
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