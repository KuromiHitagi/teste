package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpHeaders;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    private static final RecadoDAO DAO = new RecadoDAO();

    static void main() throws Exception{
        testarConexao();

        //0.0.0.0 aceita conexoes de qualquer IP da rede (celular, outro PC)
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/api/recados", Main::atenderRecados);
        server.createContext("/", Main::abrirPagina);
        server.start();
        System.out.println("Mural aberto em http://localhost:8080");
        System.out.println("Celulares podem acessar pelo IP da rede local na porta 8080");
    }

    private static void testarConexao() throws SQLException {
        try(Connection ignored = Conexao.abrir()) {
            System.out.println("Banco de dados conectado!");
        }
    }

    private static void atenderRecados(HttpExchange troca) throws IOException{
        troca.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        troca.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        troca.getResponseHeaders().set("Access-Control-Allow-Headers", "Context-Type");

        try{
            if(troca.getRequestMethod().equals("OPTIONS")) {
                troca.sendResponseHeaders(204, -1);
                troca.close();
            } else if(troca.getRequestMethod().equals("GET")) {
                listar(troca);
            } else if(troca.getRequestMethod().equals("POST")) {
                cadastrar(troca);
            } else{
                troca.getResponseHeaders().set("Allow", "GET", "POST", "OPTIONS");
                responder(troca, 405, "{\"erro\":\"Método não permitido\"}");
            }
        } catch(SQLException erro) {
            erro.printStackTrace();
            responder(troca, 500, "{\"erro\":\"Erro ao acessar o banco\"}");
        }
    }

    private static void cadastrar(HttpExchange troca) throws IOException, SQLException {
        //Map: guarda informações no formato chave e valor
        Map<String, String> dados = lerFormulario(troca);
        String autor = dados.getOrDefault("autor", "").trim();
        String mensagem = dados.getOrDefault("mensagem", "").trim();

        if(autor.isEmpty() || mensagem.isEmpty()) {
            responder(troca, 400, "{\"erro\":\"Preencha todos os campos\"}");
            return;
        }
        DAO.cadastrar(new Recado(0, autor, mensagem));
        responder(troca, 201, "{\"mensagem\":\"Recado cadastrado\"}");
    }
}
