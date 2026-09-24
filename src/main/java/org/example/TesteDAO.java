package org.example;

import java.sql.SQLException;

public class TesteDAO {
    public void main (String[] args) throws SQLException{
        RecadoDAO dao = new RecadoDAO();

        dao.cadastrar(new Recado(0, "Professor", "Teste feito com sucesso!"));
        for(Recado recado : dao.listar()) {
            System.out.println(recado.getAutor() + ": " + recado.getMensagem());
        }
    }
}
