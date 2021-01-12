package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

data class Torcedor(val time:String)
class Onibus(val time:String,  val tocedores:MutableList<Torcedor>){

   suspend fun sair()= runBlocking {


        if(tocedores.size==5){
             print("onibus "+time+" precisa sair esta lotado!!")
             print(tocedores)
             this@runBlocking.cancel()
        }

        println("onibus "+time+" iniciou contagem para saida")
        partida()
    }

    suspend fun partida()= runBlocking {
        delay(1000)
        println("onibus "+time+" saiu")
        println(tocedores)
    }
}


fun main() = runBlocking{
    val channelTorcedor = Channel<Torcedor>()
    val onibus1channel = Channel<Torcedor>()
    val onibus2channel = Channel<Torcedor>()



    val oni1 = Onibus("fortaleza",mutableListOf<Torcedor>())
    val oni2 = Onibus("ceara", mutableListOf<Torcedor>())

        launch {
            println("iniciou a ouvir onibus1")
            for (tocedor in onibus1channel){
                if(oni1.tocedores.size==4){
                    runBlocking {
                        oni1.sair()
                    }
                }
                oni1.tocedores.add(tocedor)

            }
        }
        launch {
            println("iniciou a ouvir onibus2")
            for (tocedor in onibus2channel){
                if(oni2.tocedores.size==4){
                    runBlocking {
                        oni2.sair()
                    }

                }
                oni2.tocedores.add(tocedor)

            }
        }



   launch {
       // Criar e enviar tocedor para amc
       for (i in 1..10){
           if (i%2==0){
                channelTorcedor.send(Torcedor("fortaleza"))
           }else{
                channelTorcedor.send(Torcedor("ceara"))
           }
       }
   }

    fun amc(channelTorcedor: ReceiveChannel<Torcedor>, onibus1:Channel<Torcedor>, onibus2:Channel<Torcedor>) = produce<Torcedor> {
        for(torcedor in channelTorcedor){
            if(torcedor.time=="fortaleza"){
                println("1 torcedor do fortaleza")
                onibus1.send(torcedor)
            }else{
                println("1 torcedor do ceara")
                onibus2.send(torcedor)
            }
        }
    }

    val Amc = amc(channelTorcedor, onibus1 = onibus1channel, onibus2 = onibus2channel)


    // Criar AMC para ouvir pedidos
    // Criar dois onibusc que recebem pedidos das AMC
    // Criar torcedores que pedem passagem
    // AMC Envia um onibus pro torcedor
    // Onibus inicia tempo para saida

}

