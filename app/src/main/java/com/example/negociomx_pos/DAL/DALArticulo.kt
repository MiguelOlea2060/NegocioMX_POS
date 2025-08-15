package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.ArticuloActNube
import com.example.negociomx_pos.BE.ArticuloNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

class DALArticulo {
    private lateinit var firebaseRef: DatabaseReference
    var NombreTabla:String="Articulo"
    var NombreTablaAct:String="ArticuloAct"

    fun insert(entidad: ArticuloNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
            .child(NombreTabla)

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

    fun insertArtAct(entidad: ArticuloActNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTablaAct)

        val key= ThreadLocalRandom.current().nextInt(3000001)
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

    fun update(entidad: ArticuloNube, onFinishUpdateListener:(String)->Unit)
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

    fun updateArtAct(entidades: List<ArticuloActNube>, onFinishUpdateListener:(Boolean)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTablaAct)

        var lista:Map<String,ArticuloActNube>
        lista= hashMapOf()
        entidades.forEach{
            lista.put(it.Id.toString(),it);
        }
        try{
            firebaseRef.setValue(entidades)
                .addOnCompleteListener{
                    onFinishUpdateListener(true)
                }
                .addOnFailureListener{
                    onFinishUpdateListener(false)
                }
        }
        catch (ex:Exception)
        {
            onFinishUpdateListener(false)
        }
    }

    public fun getAll( onClickListener: (List<ArticuloNube>) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lista: List<ArticuloNube>
                lista= arrayListOf()

                if(snapshot.exists())
                {
                    for (d in snapshot.children)
                    {
                        var item=d.getValue(ArticuloNube::class.java)
                        if(item!=null) lista.add(item)
                    }
                }
                onClickListener(lista)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    public fun getByIdEmpresa(idEmpresa:String?, idsArticulo:List<String>?,
                              onClickListener: (List<ArticuloNube>?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        var orderRef=firebaseRef.child(NombreTabla)

        val eData = orderRef.orderByChild("IdEmpresa").equalTo(idEmpresa)
            .addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var lista= getListArtFromSnapshot(idsArticulo,snapshot)
                    onClickListener(lista)
                }
                else
                    onClickListener(null)
            }
        })
    }

    public fun getArticuloActByFilter(idEmpresa:String?,idUsuario:String?, actualizado:Boolean?,
                                      onClickListener: (List<ArticuloActNube>?) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD)
        var orderRef=firebaseRef.child(NombreTablaAct)

        val eData = orderRef.orderByChild("IdEmpresa").equalTo(idEmpresa)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                         var lista:List<ArticuloActNube>
                         lista=getListArtActFromSnapshot(actualizado,idUsuario, snapshot)
                        onClickListener(lista)
                    }
                    else
                        onClickListener(null)
                }
            })
    }

    public fun getListArtActFromSnapshot(actualizado: Boolean?, idUsuario: String?, snapshot: DataSnapshot):List<ArticuloActNube>
    {
        var lista:List<ArticuloActNube>
        lista= arrayListOf()
        try {
            lista = snapshot.children
                .map {
                    snapshot -> snapshot.getValue(ArticuloActNube::class.java)!!
                }
                .filter {
                    art -> (actualizado==null || actualizado == art.ActualizadoLocal
                            && (idUsuario==null || art.IdUsuario==idUsuario))
                }
        }
        catch (ex:Exception)
        {
        }
        return lista
    }

    public fun getListArtFromSnapshot(idsArticulo: List<String>?, snapshot: DataSnapshot):List<ArticuloNube>
    {
        var lista:List<ArticuloNube>
        lista= arrayListOf()
        try {
            lista = snapshot.children
                .map {
                        snapshot -> snapshot.getValue(ArticuloNube::class.java)!!
                }
                .filter {
                    art -> (idsArticulo==null || idsArticulo?.filter { b-> b.equals(art.Id) }?.count()!!>0) }
        }
        catch (ex:Exception)
        {
            var mensaje=ex.toString()
            if(mensaje.isEmpty()==false)
            {

            }
        }
        return lista
    }
}