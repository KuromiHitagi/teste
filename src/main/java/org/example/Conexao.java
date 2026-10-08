package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mural_recados";
    private static final String USUARIO = "root";
    private static final String SENHA = "senaisp";

    public static Connection abrir() throws SQLException{
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}