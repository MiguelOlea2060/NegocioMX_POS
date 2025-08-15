package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.CfgNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.room.entities.Admins.CfgNV
import com.example.negociomx_pos.room.entities.Admins.Config
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.ThreadLocalRandom

class DALCfg {
    private lateinit var firebaseRef: DatabaseReference
    var NombreTabla:String="Cfg"

    fun insert(entidad: CfgNube, onFinishListener:(String)->Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)

        val key= ThreadLocalRandom.current().nextInt(2000001)
        entidad.IdCfg= key.toString()

        try{
            firebaseRef.child(key.toString()).setValue(entidad)
                .addOnCompleteListener{
                    onFinishListener(entidad.IdCfg.toString())
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

    public fun getAll(idEmpresa:String?, onClickListener: (List<CfgNube>) -> Unit)
    {
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(NombreTabla)
        if(idEmpresa!=null)
            firebaseRef.orderByChild("IdEmpresa").equals(idEmpresa)

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lista: List<CfgNube>
                lista= arrayListOf()

                if(snapshot.exists())
                {
                    try {
                        for (d in snapshot.children) {
                            var item = d.getValue(CfgNube::class.java)
                            if (item != null) {
                                if (idEmpresa == null || item.IdEmpresa.equals(idEmpresa)) {
                                    lista.add(item)

                                    if (idEmpresa != null) break
                                }
                            }
                        }
                    }
                    catch (ex:Exception)
                    {
                        var cadena=ex.toString()
                    }
                }
                onClickListener(lista)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}