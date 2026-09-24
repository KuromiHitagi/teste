package org.example;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {

    private static final RecadoDAO DAO = new RecadoDAO();

    public static void main(String[] args) {
        try {
            testarConexão();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("api//recados", Main::atenderRecados);
        server.createContext("/", Main::abrirPagina);
        server.start();

        System.out.println("Mural aberto em http://localhost:8080");
        System.out.println("Celulares podem acessar pelo ip da rede local na porta 8080");
    }

    public static void testarConexão() throws SQLException {
        try (Connection ignored = Conexao.abrir()) {
            System.out.println("Banco de dados Conectado");
        }
    }

    private static void atenderRecados(HttpExchange troca) throws IOException {
        troca.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        troca.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        troca.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        try {
            if (troca.getRequestMethod().equals("OPTIONS")) {
                troca.sendResponseHeaders(204, -1);
                troca.close();
            } else if (troca.getRequestMethod().equals("GET")) {
                listar(troca);
            } else if (troca.getRequestMethod().equals("POST")) {
                cadastrar(troca);
            } else {
                troca.getResponseHeaders().set("Allow", "GET, POST, OPTIONS");
                responder(troca, 405, "{\"erro\": \"Metodo não permitido\"}");
            }

        } catch (SQLException erro) {

            erro.printStackTrace();
            responder(troca, 500, "{\"erro\":\"Metodo não permitido\"}");

        }
    }

    private static void cadastrar(HttpExchange troca) throws IOException, SQLException {

        Map<String, String> dados = lerFormulario(troca);
        String autor = dados.getOrDefault("autor", "").trim();
        String mensagem = dados.getOrDefault("mensagem", "").trim();

        if (autor.isEmpty() || mensagem.isEmpty()) {
            responder(troca, 400, "{\"erro\": \"Autor e mensagem são obrigatórios, preencha os campos\"}");
            return;
        }
        DAO.cadastrar(new Recado(0, autor, mensagem));
        responder(troca, 200, "{\"mensagem\": \"Recado cadastrado\"}");
    }

    private static void listar(HttpExchange troca) throws IOException, SQLException {
        List<Recado> recados = DAO.listar();
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < recados.size(); i++) {
            json.append(",");
            if (i > 0) {
                json.append(recados.get(i).toJson());
            }
        }
        json.append("]");
        responder(troca, 200, json.toString());
    }

    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> dados = new HashMap<>();

        for (String campo : corpo.split("&")) {
            String[] partes = campo.split("=", 2);
            String nome = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length == 2 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            dados.put(nome, valor);
        }
        return dados;
    }

    private static void abrirPagina(HttpExchange troca) throws IOException {
        if (troca.getRequestMethod().equals("GET")) {
            responder(troca, 405, "Metodo não permitido");
            return;
        }

        try (InputStream arquivo = Main.class.getResourceAsStream("/public/index.html")) {
            if (arquivo == null) {
                responder(troca, 404, "Pagina nao encontrada");
                return;
            }
            byte[] pagina = arquivo.readAllBytes();
            troca.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            troca.sendResponseHeaders(200, pagina.length);
            troca.getResponseBody().write(pagina);
            troca.close();
        }
    }

    public static void responder(HttpExchange troca, int status, String conteudo) throws IOException {
        responder(troca, status, conteudo, "application/json");
    }

    private static void responder(HttpExchange troca, int status, String conteudo, String tipo) throws IOException {
        byte[] resposta = conteudo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        troca.sendResponseHeaders(status, resposta.length);
        troca.getResponseBody().write(resposta);
        troca.close();
    }
}