import com.example.negociomx_pos.DAL.model.VehiculoModel

/*object VehiculoDao {
/*    fun get(vin:String):List<VehiculoModel>
    {
        val lista= mutableListOf<VehiculoModel>()

        val ps= JtdsConexion.getConexion()?.prepareStatement(
            "select IDVEHICULO, vin, V.ANNIO, V.IdMarca, V.IdModelo, MO.NOMBRE NOMBREMODELO, M.Nombre NOMBREMARCA, MOTOR from dbo.Vehiculo V \n" +
                    "INNER JOIN DBO.MarcaAuto M ON V.IdMarca=M.IdMarcaAuto \n" +
                    "INNER JOIN DBO.Modelo MO ON V.IdModelo=MO.IdModelo\n" +
                    "where vin= ?"
        )

        ps?.setString(1,vin)
        val  result=ps?.executeQuery()
        while (result!=null && result.next())
        {
            lista.add(
                VehiculoModel().apply {
                    IdVehiculo=result.getInt("IDVEHICULO")
                    NombreMarca=result.getString("NOMBREMARCA")
                    NombreModelo=result.getString("NOMBREMODELO")
                    Annio=result.getShort("ANNIO")
                    IdMarca=result.getInt("IDMARCA")
                    IdModelo=result.getInt("IDMODELO")
                    Motor=result.getString("MOTOR")
                    Vin=result.getString("VIN")
                }
            )
        }

        result?.close()
        ps?.close()

        return lista
    }*/

    /*private fun agregar(model: VehiculoModel)
    {
        val ps=JtdsConexion.getConexion()?.prepareStatement(
            "INSERT INTO DBO.VEHICULO(VIN, MOTOR, IDMARCA, IDMODELO, ANNIO, IDTRANSMISION, IDDIRECCION, " +
                    "VERSION, FECHAMODIFICACION, IDCONFIGURACIONAUTOTRANSPORTE) (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )

        ps?.setString(1, model.Vin)
        ps?.setString(2, model.Motor)
        ps?.setInt(3, model.IdMarca)
        ps?.setInt(4, model.IdModelo)
        ps?.setShort(5, model.Annio)
        ps?.setInt(6, 0)
        ps?.setInt(7, 0,)
        ps?.setString(8, "")
        ps?.setDate(9, null)
        ps?.setShort(10,1)

        var result=ps?.executeUpdate()

        ps?.close()

        if(result!=null && result<1)
            throw Exception("Error desconocido: No se pudo realizar la Operación")

    }/

    /*private fun actualizar(model: VehiculoModel)
    {
        val ps=JtdsConexion.getConexion()?.prepareStatement(
            "UPDATE VEHICULO SET VIN=?, MOTOR=?, IDMARCA=?, IDMODELO=?, ANNIO=? WHERE IDVEHICULO = ?"
        )

        ps?.setString(1, model.Vin)
        ps?.setString(2, model.Motor)
        ps?.setInt(3, model.IdMarca)
        ps?.setInt(4, model.IdModelo)
        ps?.setShort(5, model.Annio)
        ps?.setInt(6,model.IdVehiculo)

        var result=ps?.executeUpdate()

        ps?.close()

        if(result!=null && result<1)
            throw Exception("Error desconocido: No se pudo realizar la Operación")

    }*/

    /*fun eliminar(model: VehiculoModel)
    {
        val ps=JtdsConexion.getConexion()?.prepareStatement(
            "DELETE FROM VEHICULO WHERE IDVEHICULO = ?"
        )
        ps?.setInt(1,model.IdVehiculo)

        var result=ps?.executeUpdate()

        ps?.close()

        if(result!=null && result<1)
            throw Exception("Error desconocido: No se pudo realizar la Operación")

    }*/

    /*fun add(model: VehiculoModel)
    {
        if(model.IdVehiculo==0)
            agregar(model)
        else
            actualizar(model)
    }*/
}*/