package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.CategoriaNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

class DALCategoria {
    private lateinit var firebaseRef: DatabaseReference
    var NombreTabla:String="Categoria"

    fun insert(entidad: CategoriaNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val key= ThreadLocalRandom.current().nextInt(2000001)
        entidad.Id= key.toString()

        try{
            firebaseRef.child(key.toString()).setValue(entidad)
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

    fun update(entidad: CategoriaNube, onFinishUpdateListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val idEntidad=entidad.Id

        try{
            firebaseRef.child(idEntidad.toString()).setValue(entidad)
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

    public fun getAll(idEmpresa:String?,activa:Boolean?, onClickListener: (List<CategoriaNube>?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        var orderRef=firebaseRef.child(NombreTabla)
        if(idEmpresa!=null)
            orderRef.orderByChild("IdEmpresaNube").equalTo(idEmpresa)

        val eData = orderRef.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        var lista:List<CategoriaNube>
                        lista= arrayListOf()
                        try {
                            lista = snapshot.children
                                .map {
                                        snapshot -> snapshot.getValue(CategoriaNube::class.java)!!
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
}