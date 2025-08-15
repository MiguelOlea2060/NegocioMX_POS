package com.example.negociomx_pos.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.negociomx_pos.room.entities.ItemSpinner

class SpinnerAdapter(private val context:Context, private val items: List<ItemSpinner>,
                     private val idLayout:Int, private val lblDisplay:Int)
    :BaseAdapter()
{
    override fun getCount()=items.size

    override fun getItem(position: Int):Any=items[position]

    override fun getItemId(position: Int):Long=position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater=LayoutInflater.from(context)
//        val view=inflater.inflate(R.layout.item_spinner_status,parent,false)
        val view=inflater.inflate(idLayout,parent,false)
        val item=getItem(position)as ItemSpinner

        val txt=view.findViewById<TextView>(lblDisplay)

        txt.text=item.Display

        return view
    }
}