package com.example.negociomx_pos.DAL

import com.example.negociomx_pos.BE.CfgNVNube
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DALCfgNV {
    private lateinit var firebaseRef: DatabaseReference
    var NombreTablaCfg:String="Cfg"
    var NombreTablaCfgNV:String="CfgNV"

    public fun getAll(idCfg:String, onClickListener: (CfgNVNube?) -> Unit)
    {
        var query:String=NombreTablaCfg+"/"+idCfg+"/"+NombreTablaCfgNV
        firebaseRef = FirebaseDatabase.getInstance().getReference(ParametrosSistema.NombreBD).child(query)

        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var res: CfgNVNube?=null
                if(snapshot.exists())
                {
                    for (d in snapshot.children)
                    {
                        var item = d.getValue(CfgNVNube::class.java)
                        if (item != null) {
                            res = item
                            break
                        }
                    }
                }
                onClickListener(res)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}