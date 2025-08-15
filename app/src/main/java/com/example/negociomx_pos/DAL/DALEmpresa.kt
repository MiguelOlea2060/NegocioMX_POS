package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.CategoriaNube
import com.example.negociomx_pos.BE.EmpresaDispositivoAcceso
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

public class DALEmpresa {
    private lateinit var firebaseRef: DatabaseReference
    var NombreTabla="Empresa"

    fun insert(entidad: EmpresaNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val id= ThreadLocalRandom.current().nextInt(2000001)
        entidad.Id= id.toString()

        try{
            firebaseRef.child(id.toString()).setValue(entidad)
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

    fun update(entidad: EmpresaNube, onFinishUpdateListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val idEntidad=entidad.Rfc+"|"+entidad.RazonSocial
        entidad.Id= idEntidad

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

    public fun insertEDA(entidad: EmpresaDispositivoAcceso,onFinishEDAListener: (String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child("EmpresaDispositivoAcceso")

        val idEntidad=firebaseRef.push().key!!
        entidad.Id= idEntidad

        firebaseRef.child(idEntidad).setValue(entidad)
            .addOnCompleteListener{
                onFinishEDAListener(entidad.Id.toString())
            }
            .addOnFailureListener{
                onFinishEDAListener("")
            }
    }

    public fun getByFilters(idEmpresa:String?,  onClickListener: (List<EmpresaNube>?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        var orderRef=firebaseRef.child(NombreTabla)
        if(idEmpresa!=null)
            orderRef.orderByChild("Id").equalTo(idEmpresa)

        val eData = orderRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var lista:List<EmpresaNube>
                    lista= arrayListOf()
                    try {
                        lista = snapshot.children
                            .map {
                                    snapshot -> snapshot.getValue(EmpresaNube::class.java)!!
                            } as List<EmpresaNube>
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

    public fun getByRfcRazonSocial(rfcRazonSocial:String, onfinishReadListener: (EmpresaNube?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        val orderRef=firebaseRef.child(NombreTabla).orderByKey().equalTo(rfcRazonSocial)

        val listener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var item:EmpresaNube?=null
                if(snapshot.exists()) {
                    for (e in snapshot.children) {
                        item=e.getValue(EmpresaNube::class.java)
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