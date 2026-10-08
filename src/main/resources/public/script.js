const formulario = document.querySelector("#form-recado")
const lista = document.querySelector("#lista-recados")
const aviso = document.querySelector("#aviso")

async function carregarRecados() {
    const resposta = await fetch("http://localhost:8080/api/recados")
    const recados = await resposta.json()

    lista.innerHTML = ""

    for (const recado of recados) {
        const cartao = document.createElement("article")
        const autor = document.createElement("strong")
        const mensagem = document.createElement("p")

        autor.textContent = recado.autor
        mensagem.textContent = recado.mensagem

        cartao.append(autor, mensagem)
        lista.appendChild(cartao)
    }
}

formulario.addEventListener("submit", async (e) => {
    e.preventDefault()
    aviso.textContent = "Salvando..."

    const dados = new URLSearchParams(new FormData(formulario))
    const resposta = await fetch("http://localhost:8080/api/recados", {
        method: "POST",
        body: dados
    })
    if (resposta.ok) {
        formulario.reset()
        aviso.textContent = "Recado cadastro com sucesso."
        await carregarRecados()
    } else {
        aviso.textContent = "Não foi possível cadastrar o recado."
    }
})