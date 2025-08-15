package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.ArticuloActNube
import com.example.negociomx_pos.BE.UnidadMedidaNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

class DALUnidadMedida {
    private lateinit var firebaseRef: DatabaseReference

    var NombreTabla="UnidadMedida"

    fun insert(entidad: UnidadMedidaNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val idEntidad=entidad.Id
        val key= ThreadLocalRandom.current().nextInt(2000001)
        entidad.Id=key.toString()

        try{
            firebaseRef.child(idEntidad.toString()).setValue(entidad)
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

    fun update(entidad: UnidadMedidaNube, onFinishUpdateListener:(String)->Unit)
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

    private fun getListUMActFromSnapshot(actualizado: Boolean?, idUsuario: String?, snapshot: DataSnapshot):List<ArticuloActNube>
    {
        var lista:List<ArticuloActNube>
        lista= arrayListOf()
        try {
            lista = snapshot.children
                .map {
                        snapshot -> snapshot.getValue(ArticuloActNube::class.java)!!
                }
                .filter { art -> (actualizado==null || actualizado == art.ActualizadoLocal
                        && (idUsuario==null || art.IdUsuario==idUsuario)) } as List<ArticuloActNube>
        }
        catch (ex:Exception)
        {
        }
        return lista
    }

    fun getAll(idEmpresa: String?, onClickListener: (List<UnidadMedidaNube>?) -> Unit)
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
                        var lista:List<UnidadMedidaNube>
                        lista= arrayListOf()
                        try {
                            lista = snapshot.children
                                .map {
                                        snapshot -> snapshot.getValue(UnidadMedidaNube::class.java)!!
                                } as List<UnidadMedidaNube>
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

    fun getAllByIdEmpresa(idEmpresa:String?, onfinishReadListener: (List<UnidadMedidaNube>) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        val orderRef=firebaseRef.child(NombreTabla)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lista:List<UnidadMedidaNube>
                lista= arrayListOf()

                try {
                    var item: UnidadMedidaNube? = null
                    if (snapshot.exists()) {
                        for (e in snapshot.children) {
                            item = e.getValue(UnidadMedidaNube::class.java)
                            if(item!=null && (idEmpresa==null || item.IdEmpresaNube.equals(idEmpresa)))
                                lista.add(item!!)
                        }
                    }
                }
                catch (ex:Exception)
                {
                    lista= arrayListOf()
                }
                onfinishReadListener(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        orderRef.addListenerForSingleValueEvent(listener)
    }
}